[#import "../../macros.ftl" as macros]
[@macros.renderHeader i18n.translate("form.build-server-setup.title") /]
[@macros.renderMenu i18n user /]
<div class="container">
<h2>Create assignment</h2>
<form class="form-horizontal" method="POST" target="_self" action="">

    [#if error?? && error?has_content]
        <div class="alert alert-danger">
        ${i18n.translate(error)}
        </div>
    [/#if]

    <div class="form-group">
        <label for="code" class="col-sm-2 control-label">${i18n.translate("course.control.course-code")}</label>
        <div class="col-sm-10">
            <input type="text" class="form-control" name="code" id="code" placeholder="${i18n.translate("course.control.course-code.example")}" value="${course.getCode()}" disabled>
        </div>
    </div>

    <div class="form-group">
        <label for="name" class="col-sm-2 control-label">Assignment number</label>
        <div class="col-sm-10">
        [#if assignment?exists]
            <input type="number" class="form-control" name="id" id="name" value="${assignment.getAssignmentId()}" disabled>
        [#else]
            <input type="number" class="form-control" name="id" id="name" value="1">
        [/#if]
        </div>
    </div>

    <div class="form-group">
        <label for="due-date" class="col-sm-2 control-label">Due date</label>
        <div class="col-sm-10">
        [#if assignment?exists]
            <input type="text" class="form-control" name="due-date" id="due-date" value="${assignment.getDueDate()}">
        [#else]
            <input type="text" class="form-control" name="due-date" id="due-date">
        [/#if]
        </div>
    </div>

    <div class="form-group">
        <label for="name" class="col-sm-2 control-label">Assignment name</label>
        <div class="col-sm-10">
        [#if assignment?exists]
            <input type="text" class="form-control" name="name" id="name" placeholder="Assignment 1" value="${assignment.getName()}">
        [#else]
            <input type="text" class="form-control" name="name" id="name" placeholder="Assignment 1">
        [/#if]
        </div>
    </div>

    <div class="form-group">
        <label for="summary" class="col-sm-2 control-label">Summary</label>
        <div class="col-sm-10">
        [#if assignment?exists]
            <textarea rows="8" class="form-control" name="summary" id="summary" placeholder="Summary">${assignment.getSummary()}</textarea>
        [#else]
            <textarea rows="8" class="form-control" name="summary" id="summary" placeholder="Summary"></textarea>
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
                <button type="submit" class="btn btn-primary">Create assignment</button>
            [/#if]
            </div>
        </div>
    </div>

</form>
</div>
[@macros.renderScripts /]
[@macros.renderFooter /]
