[#import "../../macros.ftl" as macros]
[#import "../../components/commit-row.ftl" as commitRow]

[@macros.renderHeader i18n.translate("assignments.title") /]
[@macros.renderMenu i18n user /]

<div class="container" ng-controller="StatisticsControl">

    <ol class="breadcrumb hidden-xs">
        <li><a href="/courses">${ i18n.translate("section.courses") }</a></li>
        <li><a href="${course.course.getURI()}">${course.course.code} - ${course.course.name}</a></li>
        <li><a href="${course.getURI()}">${course.timeSpan.start?string["yyyy"]}[#if course.timeSpan.end??] - ${course.timeSpan.end?string["yyyy"]}[/#if]</a></li>
        <li><a href="${course.getURI()}">${ i18n.translate("assignments.title") }</a></li>
        <li>${assignment.getName()}</li>
    </ol>

	<div class="panel panel-default">
		<div class="panel-heading" ng-bind="assignment.name"></div>

        <table class="table table-bordered">
            <thead>
				<tr>
					<th>Name</th>
					<th>Weight</th>
					<th>Correlation</th>
					<th>Mastery</th>
					<th>Points</th>
					<th>Count</th>
				</tr>
            </thead>
            <tbody ng-repeat="task in assignment.tasks">
				<tr class="active">
					<td colspan="6">
						<a editable-text="task.description" ng-bind="task.description"></a>
					</td>
				</tr>
                <tr ng-repeat-start="characteristic in task.characteristics">
                    <td rowspan="{{ characteristic.levels.length }}"><a editable-textarea="characteristic.description" ng-bind="characteristic.description"></a></td>
                    <td rowspan="{{ characteristic.levels.length }}"><a editable-number="characteristic.weight" ng-bind="characteristic.weight"></a></td>
					<td rowspan="{{ characteristic.levels.length }}" ng-bind="characteristic.correlation"></td>
                    <td><a editable-textarea="characteristic.levels[0].description" ng-bind="characteristic.levels[0].description"></a></td>
                    <td><a editable-number="characteristic.levels[0].points" ng-bind="characteristic.levels[0].points"></a></td>
					<td ng-bind="characteristic.levels[0].count"></td>
                </tr>
				<tr ng-repeat-end ng-repeat="level in characteristic.levels" ng-hide="$first">
					<td><a editable-textarea="level.description" ng-bind="level.description"></a></td>
					<td><a editable-number="level.points" ng-bind="level.points"></a></td>
					<td ng-bind="level.count"></td>
				</tr>
            </tbody>
        </table>
	</div>

</div>
[@macros.renderScripts]
<script src="/static/vendor/angular/angular.min.js"></script>
<script src="/static/vendor/angular-bootstrap/ui-bootstrap.min.js"></script>
<script src="/static/vendor/angular-xeditable/dist/js/xeditable.min.js"></script>
<script src="/static/vendor/jstat/dist/jstat.min.js"></script>
<script type="text/javascript">
var module = angular.module('devhub', ['ui.bootstrap', 'xeditable']);

module.run(function(editableOptions) {
    editableOptions.theme = 'bs3'; // bootstrap3 theme. Can be also 'bs2', 'default'
});

module.controller('StatisticsControl', function($scope, $http, $q) {
    var levels = {};

	$q.all([
        $http.get('json').then(function(res) { return res.data; }),
        $http.get('last-deliveries/json').then(function(res) { return res.data; })
	]).then(function(res) {
		$scope.assignment = res[0];
		$scope.deliveries = res[1];
	});

	$scope.$watch('assignment', calculateCountAndCorrel, true);

	function calculateCountAndCorrel() {
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
            });
        });
	}
})
</script>
[/@macros.renderScripts]
[@macros.renderFooter /]
