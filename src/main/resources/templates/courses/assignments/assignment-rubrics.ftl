[#import "../../macros.ftl" as macros]
[#import "../../components/commit-row.ftl" as commitRow]

[@macros.renderHeader i18n.translate("assignments.title") ]
<style type="text/css">
body > .angular-bootstrap-contextmenu.dropdown {
	width: 300px !important;
}
</style>
[/@macros.renderHeader]
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
				<tr context-menu="contextMenuForAssignment()">
					<th>Name</th>
					<th>Weight</th>
					<th>Correlation</th>
					<th>Mastery</th>
					<th>Points</th>
					<th>Count</th>
				</tr>
            </thead>
            <tbody ng-repeat="task in assignment.tasks">
				<tr class="active"  context-menu="contextMenuForTask(task)">
					<td colspan="6">
						<a editable-text="task.description" ng-bind="task.description"></a>
					</td>
				</tr>
                <tr ng-repeat-start="characteristic in task.characteristics"  context-menu="contextMenuForCharacteristic(task, characteristic)">
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
<script src="/static/vendor/angular-bootstrap-contextmenu/contextMenu.js"></script>
<script src="/static/vendor/jstat/dist/jstat.min.js"></script>
<script src="/static/js/assignment-rubrics.js"></script>

[/@macros.renderScripts]
[@macros.renderFooter /]
