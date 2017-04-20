[#import "../../macros.ftl" as macros]
[@macros.renderHeader i18n.translate("assignments.title") /]
[@macros.renderMenu i18n user /]
<div class="container" ng-controller="rubricController">
<h2>${i18n.translate("assignment.create")}</h2>
<form class="form-horizontal" method="POST" target="_self" action="">

    [#if error?? && error?has_content]
        <div class="alert alert-danger">
        ${i18n.translate(error)}
        </div>
    [/#if]

    <div class="form-group">
        <label for="code" class="col-sm-2 control-label">${i18n.translate("course.control.course-code")}</label>
        <div class="col-sm-10">
            <input type="text" class="form-control" name="code" id="code" placeholder="${i18n.translate("course.control.course-code.example")}" value="${course.code}" disabled>
            <input type="hidden" name="code" value="${course.code}"/>
        </div>
    </div>

    [#if assignment??]
        [#assign assignmentNumber = assignment.assignmentId]
    [#else]
        [#assign assignmentNumber = course.getAssignments()?size + 1]
    [/#if]

        <input type="hidden" name="id" id="id" value="${assignmentNumber}"/>

    <div class="form-group">
        <label for="due-date" class="col-sm-2 control-label">${i18n.translate("course.control.due-date")}</label>
        <div class="col-sm-10">
            <input type="text" class="form-control" name="due-date" id="due-date"
               [#if assignment?? && assignment.getDueDate()??]
                   value="${assignment.getDueDate()?string["dd-MM-yyyy HH:mm"]}"
               [#else]
                   value="${.now?string["dd-MM-yyyy HH:mm"]}"
               [/#if]
                   placeholder="${i18n.translate("course.control.due-date.format")}">
        </div>
    </div>


    [#if !assignment??]

    <div class="form-group">
        <label for="existing-rubrics-from-course" class="col-sm-2 control-label">${i18n.translate("assignment.rubric.from-course-edition")}</label>
        <div class="col-sm-10">
            <select class="form-control" ng-model="courseEdition" name="courseEditionToCopyFromId" id="existing-rubrics-from-course"
                    ng-options="edition as edition.name for edition in courseEditions track by edition.courseEdition">
            </select>
        </div>
    </div>

    <div class="form-group">
        <label for="existing-rubrics" class="col-sm-2 control-label">${i18n.translate("assignment.rubric.from-assignment")}</label>
        <div class="col-sm-10">
            <select class="form-control" ng-model="selectedAssignment" name="assignmentToCopyFromId" id="existing-rubrics"
                    ng-options="assignment as assignment.name for assignment in courseEdition.assignments track by assignment.assignmentId">
            </select>
        </div>
    </div>
    [/#if]

    [#if assignment??]
    <div class="form-group">
        <label for="release" class="col-sm-2 control-label">${i18n.translate("assignment.release")}</label>
        <div class="col-sm-10">
            <input type="checkbox" class="form-control" name="release" id="release" [#if assignment.isGradesReleased()]checked [/#if]>
        </div>
    </div>
    [/#if]

    <div class="form-group">
        <label for="name" class="col-sm-2 control-label">${i18n.translate("course.control.assignment-name")}</label>
        <div class="col-sm-10">
        [#if assignment??]
            <input type="text" class="form-control" name="name" id="name" placeholder="${i18n.translate("course.control.assignment-name", assignmentNumber)}" value="${assignment.getName()}">
        [#else]
            <input type="text" class="form-control" name="name" id="name" placeholder="${i18n.translate("course.control.assignment-name", assignmentNumber)}" value="{{selectedAssignment.name}}">
        [/#if]
        </div>
    </div>

    <div class="form-group">
        <label for="summary" class="col-sm-2 control-label">${i18n.translate("course.control.summary")}</label>
        <div class="col-sm-10">
        [#if assignment??]
            <textarea rows="8" class="form-control" name="summary" id="summary" placeholder="${i18n.translate("course.control.summary")}">[#if assignment.getSummary()??]${assignment.getSummary()}[/#if]</textarea>
        [#else]
            <textarea rows="8" class="form-control" name="summary" id="summary" placeholder="${i18n.translate("course.control.summary")}">{{selectedAssignment.summary}}</textarea>
        [/#if]
        </div>
    </div>

    <div class="form-group">
        <div class="col-sm-offset-2 col-sm-10">
            <div class="pull-right">
            [#if assignment??]
                <a href="${course.getURI()}" class="btn btn-default">${i18n.translate("course.control.cancel")}</a>
                <button type="submit" class="btn btn-primary">${i18n.translate("course.control.save")}</button>
            [#else]
                <a href="${course.getURI()}" class="btn btn-default">${i18n.translate("course.control.cancel")}</a>
                <button type="submit" class="btn btn-primary">${i18n.translate("assignment.create")}</button>
            [/#if]
            </div>
        </div>
    </div>

</form>
</div>

[#if !assignment??]
<script src="/static/vendor/angular/angular.js"></script>
<script>
    var module = angular.module("devhub", []);
    module.controller("rubricController", function($scope, $http) {
        $scope.courseEditions = [];

        var assignmentData = $http.get("create/bliep").success(function(data) {
            $scope.courseEditions = data;
        });
    })

</script>
[/#if]

[@macros.renderScripts /]
[@macros.renderFooter /]
