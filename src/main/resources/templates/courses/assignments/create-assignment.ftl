[#import "../../macros.ftl" as macros]
[@macros.renderHeader i18n.translate("assignments.title") /]
[@macros.renderMenu i18n user /]
<div class="container">
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

    [#if assignment?exists]
        [#assign assignmentNumber = assignment.assignmentId]
    [#else]
        [#assign assignmentNumber = course.getAssignments()?size + 1]
    [/#if]

    <div class="form-group">
        <label for="name" class="col-sm-2 control-label">${i18n.translate("course.control.assignment-number")}</label>
        <div class="col-sm-10">
            <input type="number" class="form-control" name="id" id="id" value="${assignmentNumber}" [#if assignment?exists]disabled[/#if]>
        </div>
    </div>

    <div class="form-group">
        <label for="due-date" class="col-sm-2 control-label">${i18n.translate("course.control.due-date")}</label>
        <div class="col-sm-10">
        [#if assignment?exists]
            <input type="text" class="form-control" name="due-date" id="due-date" placeholder="${i18n.translate("course.control.due-date")}" [#if assignment.getDueDate()??]value="${assignment.getDueDate()}"[/#if]>
        [#else]
            <input type="text" class="form-control" name="due-date" id="due-date" placeholder="${i18n.translate("course.control.due-date")}">
        [/#if]
        </div>
    </div>

    <div class="form-group">
        <label for="name" class="col-sm-2 control-label">${i18n.translate("course.control.assignment-name")}</label>
        <div class="col-sm-10">
        [#if assignment?exists]
            <input type="text" class="form-control" name="name" id="name" placeholder="${i18n.translate("course.control.assignment-number", assignmentNumber)}" value="${assignment.getName()}">
        [#else]
            <input type="text" class="form-control" name="name" id="name" placeholder="${i18n.translate("course.control.assignment-number", assignmentNumber)}">
        [/#if]
        </div>
    </div>

    <div class="form-group">
        <label for="summary" class="col-sm-2 control-label">${i18n.translate("course.control.summary")}</label>
        <div class="col-sm-10">
        [#if assignment?exists]
            <textarea rows="8" class="form-control" name="summary" id="summary" placeholder="${i18n.translate("course.control.summary")}">[#if assignment.getSummary()??]${assignment.getSummary()}[/#if]</textarea>
        [#else]
            <textarea rows="8" class="form-control" name="summary" id="summary" placeholder="${i18n.translate("course.control.summary")}"></textarea>
        [/#if]
        </div>
    </div>

    <div class="form-group">
        <div class="col-sm-offset-2 col-sm-10">
            <div class="pull-right">
            [#if assignment?exists]
                <a href="/courses/${course.getCode()}" class="btn btn-default">${i18n.translate("course.control.cancel")}</a>
                <button type="submit" class="btn btn-primary">${i18n.translate("course.control.save")}</button>
            [#else]
                <a href="/courses" class="btn btn-default">${i18n.translate("course.control.cancel")}</a>
                <button type="submit" class="btn btn-primary">${i18n.translate("assignment.create")}</button>
            [/#if]
            </div>
        </div>
    </div>

</form>
</div>
[@macros.renderScripts /]
[@macros.renderFooter /]
