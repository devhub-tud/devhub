[#import "../../macros.ftl" as macros]
[#import "../../components/commit-row.ftl" as commitRow]

[@macros.renderHeader i18n.translate("assignments.title") /]
[@macros.renderMenu i18n user /]

<div class="container" ng-controller="TestCtrl">

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
					<th>Mastery</th>
					<th>Points</th>
				</tr>
            </thead>
            <tbody ng-repeat="task in assignment.tasks">
				<tr class="active">
					<td colspan="4">
						<a editable-text="task.description" ng-bind="task.description"></a>
					</td>
				</tr>
                <tr ng-repeat-start="characteristic in task.characteristics">
                    <td rowspan="{{ characteristic.levels.length }}"><a editable-textarea="characteristic.description" ng-bind="characteristic.description"></a></td>
                    <td rowspan="{{ characteristic.levels.length }}"><a editable-number="characteristic.weight" ng-bind="characteristic.weight"></a></td>
                    <td><a editable-textarea="characteristic.levels[0].description" ng-bind="characteristic.levels[0].description"></a></td>
                    <td><a editable-number="characteristic.levels[0].points" ng-bind="characteristic.levels[0].points"></a></td>
                </tr>
				<tr ng-repeat-end ng-repeat="level in characteristic.levels" ng-hide="$first">
					<td><a editable-textarea="level.description" ng-bind="level.description"></a></td>
					<td><a editable-number="level.points" ng-bind="level.points"></a></td>
				</tr>
            </tbody>
        </table>
	</div>

</div>
[@macros.renderScripts]
<script src="/static/vendor/angular/angular.min.js"></script>
<script src="/static/vendor/angular-bootstrap/ui-bootstrap.min.js"></script>
<script src="/static/vendor/angular-xeditable/dist/js/xeditable.min.js"></script>
<script type="text/javascript">
var module = angular.module('devhub', ['ui.bootstrap', 'xeditable']);

module.run(function(editableOptions) {
    editableOptions.theme = 'bs3'; // bootstrap3 theme. Can be also 'bs2', 'default'
});

module.controller('TestCtrl', function($scope) {
	$scope.assignment = {
        "id": 1,
        "name": "Part 1. E2E & Boundary Testing",
        "summary": "",
        "dueDate": null,
        "tasks": [
            {
                "description": "Smoke Testing",
                "characteristics": [
                    {
                        "weight": 4,
                        "description": "Correct implementation of the Smoke Test",
                        "levels": [
                            {
                                "description": "No implementation of the Smoke Test.",
                                "points": 0
                            },
                            {
                                "description": "Constructing dependencies manually.",
                                "points": 1
                            },
                            {
                                "description": "Correctly launching the interface.",
                                "points": 3
                            }
                        ]
                    },
                    {
                        "weight": 4,
                        "description": "Correct implementation of the Smoke Test",
                        "levels": [
                            {
                                "description": "No implementation of the Smoke Test.",
                                "points": 0
                            },
                            {
                                "description": "Constructing dependencies manually.",
                                "points": 1
                            },
                            {
                                "description": "Correctly launching the interface.",
                                "points": 3
                            }
                        ]
                    }
                ]
            },
            {
                "description": "Feature Based Testing Testing",
                "characteristics": [
                    {
                        "weight": 6,
                        "description": "Correct implementation of the Smoke Test",
                        "levels": [
                            {
                                "description": "No implementation of the Smoke Test.",
                                "points": 0
                            },
                            {
                                "description": "Correct Gherkin scenarios.",
                                "points": 1
                            },
                            {
                                "description": "Reasonably well step-definition implementation.",
                                "points": 2
                            },
                            {
                                "description": "Step-definitions are properly reused across scenarios.",
                                "points": 3
                            }
                        ]
                    }
                ]
            },
            {
                "description": "Consecutive build failures",
                "characteristics": [
                    {
                        "description": "Consecutive build failures",
                        "weightAddsToTotalWeight": false,
                        "weight": 1,
                        "levels": [
                            {
                                "description": "No more than four consecutive build failures.",
                                "points": 0
                            },
                            {
                                "description": "More than four consecutive build failures.",
                                "points": -3
                            }
                        ]
                    }
                ]
            }
        ]
    };
})
</script>
[/@macros.renderScripts]
[@macros.renderFooter /]
