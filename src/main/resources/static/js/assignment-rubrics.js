
var module = angular.module('devhub', ['ui.bootstrap', 'xeditable', 'ui.bootstrap.contextMenu']);

module.run(function(editableOptions) {
    editableOptions.theme = 'bs3'; // bootstrap3 theme. Can be also 'bs2', 'default'
});


function createLevel(n) {
    return {
        description: "New level...",
        points: n || 0
    }
}

function createCharacteristic() {
    return {
        description: "New characteristic...",
        weight: 0,
        levels: [
            createLevel(0),
            createLevel(1),
            createLevel(2)
        ]
    };
}

function createTask() {
    return {
        description: "New task...",
        characteristics: [
            createCharacteristic()
        ]
    }
}

module.controller('StatisticsControl', function($scope, $http, $q) {
    var levels = {};

    $scope.addTask = function() {
        $scope.assignment.tasks.push(createTask())
    };

    $scope.contextMenuForAssignment = function() {
        return [
            ['Add Task', $scope.addTask]
        ]
    };

    $scope.contextMenuForTask = function(task) {
        return $scope.contextMenuForAssignment().concat([
            ['Remove Task', function() {
                $scope.assignment.tasks.splice($scope.assignment.tasks.indexOf(task), 1);
            }],
            null,
            ['Add Characteristic', function() {
                task.characteristics.push(createCharacteristic());
            }]
        ])
    };

    $scope.contextMenuForCharacteristic = function(task, characteristic) {
        return $scope.contextMenuForTask(task).concat([
            ['Remove Characteristic', function() {
                task.characteristics.splice(task.characteristics.indexOf(characteristic), 1);
            }],
            null,
            ['Add Level', function() {
                characteristic.levels.push(createLevel());
            }]
        ])
    };


    $q.all([
        $http.get('json').then(function(res) { return res.data; }),
        $http.get('last-deliveries/json').then(function(res) { return res.data; })
    ]).then(function(res) {
        $scope.assignment = res[0];
        $scope.deliveries = res[1];
    });

    $scope.submit = function submit(assignment) {
        if (assignment) {
            $http.put('json', assignment).then(function(res) {
                $scope.assignment = res.data;
            });
        }
    };

    $scope.$watch('assignment', calculateCountAndCorrel, true);
    $scope.$watch('assignment', $scope.submit, true);

    function calculateCountAndCorrel(assignment) {
        if (!assignment) return;

        $scope.assignment.tasks.forEach(function(task) {
            task.characteristics.forEach(function(characteristic) {
                characteristic.levels.forEach(function(level) {
                    level.count = 0;
                    levels[level.id] = level;

                    Object.defineProperty(level, 'characteristic', {
                        value: characteristic,
                        enumerable: false,
                        configurable: false
                    });
                })
            })
        });

        $scope.deliveries.forEach(function(delivery) {
            // Defaulting to null, so the value is null when a group is not graded (delivery.masteries.length == 0)
            delivery.achievedNumberOfPoints = null;
            delivery.masteries.forEach(function(mastery) {
                mastery = levels[mastery.id];
                mastery.count++;
                delivery.achievedNumberOfPoints += mastery.points * mastery.characteristic.weight;
            })
        });

        $scope.assignment.tasks.forEach(function(task) {
            task.characteristics.forEach(function (characteristic) {
                // Construct an array of all points for this particular characteristic
                // [ 0, 1, 3, 1, 1]
                var scoresForCharacteristic = $scope.deliveries.map(function(delivery) {
                    var mastery = delivery.masteries.find(function(mastery) {
                        return levels[mastery.id].characteristic === characteristic
                    });
                    return mastery ? mastery.points : null;
                });
                // Construct an array of all actual achieved points per delivery
                // [ 60, 72, 70, 60]
                var achievedNumberOfPoints = $scope.deliveries.map(function(delivery) {
                    return delivery.achievedNumberOfPoints;
                });
                // Compute correlation
                characteristic.correlation =
                    (jStat.corrcoeff(scoresForCharacteristic, achievedNumberOfPoints)).toFixed(2);
                if (isNaN(characteristic.correlation)) {
                    console.warn('Failed to compute correlation for %o and %o', scoresForCharacteristic, achievedNumberOfPoints)
                }
            });
        });
    }
});
