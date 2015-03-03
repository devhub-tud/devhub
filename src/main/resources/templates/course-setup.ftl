[#import "macros.ftl" as macros]
[@macros.renderHeader "Create a new course" /]
[@macros.renderMenu i18n user /]
<div class="container">

[#if course?exists]
    <h2>Editing course: ${course.getName()}</h2>
    <form class="form-horizontal" method="POST" target="_self">
[#else]
    <h2>Create a new course</h2>
    <form class="form-horizontal" method="POST" target="_self">
[/#if]
        [#if error?? && error?has_content]
            <div class="alert alert-danger">
            ${i18n.translate(error)}
            </div>
        [/#if]

        [#if course?exists]
            <input type="hidden" name="id" value="${course.getId()}"/>
        [/#if]

        <div class="form-group">
            <label for="code" class="col-sm-2 control-label">Course code</label>
            <div class="col-sm-10">
        [#if course?exists]
                <input type="text" class="form-control" name="code" id="code" placeholder="TI1705, ..." value="${course.getCode()}" disabled>
        [#else]
                <input type="text" class="form-control" name="code" id="code" placeholder="TI1705, ...">
        [/#if]
            </div>
        </div>

        <div class="form-group">
            <label for="name" class="col-sm-2 control-label">Course name</label>
            <div class="col-sm-10">
        [#if course?exists]
                <input type="text" class="form-control" name="name" id="name" placeholder="Software Quality & Testing, ...." value="${course.getName()}">
        [#else]
                <input type="text" class="form-control" name="name" id="name" placeholder="Software Quality & Testing, ....">
        [/#if]
            </div>
        </div>

        <div class="form-group">
            <label for="template" class="col-sm-2 control-label">Template repository</label>
            <div class="col-sm-10">
        [#if course?exists]
                <input type="text" class="form-control" name="template" id="template" value="${course.getTemplateRepositoryUrl()}">
        [#else]
                <input type="text" class="form-control" name="template" id="template" value="https://github.com/octocat/Spoon-Knife.git">
        [/#if]
            </div>
        </div>

        <div class="form-group">
            <label for="min" class="col-sm-2 control-label">Min group size</label>
            <div class="col-sm-10">
        [#if course?exists && course.getMinGroupSize()?exists]
            <input type="number" class="form-control" name="min" id="min" value="${course.getMinGroupSize()}" min="1" max="100">
        [#else]
            <input type="number" class="form-control" name="min" id="min" value="1" min="1" max="100">
        [/#if]
            </div>
        </div>

        <div class="form-group">
            <label for="max" class="col-sm-2 control-label">Max group size</label>
            <div class="col-sm-10">
        [#if course?exists && course.getMaxGroupSize()?exists]
            <input type="number" class="form-control" name="max" id="max" value="${course.getMaxGroupSize()}" min="1" max="100">
        [#else]
            <input type="number" class="form-control" name="max" id="max" value="1" min="1" max="100">
        [/#if]
            </div>
        </div>

        <div class="form-group">
            <label for="timeout" class="col-sm-2 control-label">Build timeout (seconds)</label>
            <div class="col-sm-10">
        [#if course?exists && course.getBuildTimeout()?exists]
            <input type="number" class="form-control" name="timeout" id="timeout" value="${course.getBuildTimeout()}" min="1" max="3600">
        [#else]
            <input type="number" class="form-control" name="timeout" id="timeout" value="600" min="1" max="3600">
        [/#if]
            </div>
        </div>


        <div class="form-group">
            <div class="col-sm-offset-2 col-sm-10">
                <div class="pull-right">
[#if course?exists]
                    <a href="/courses/${course.getCode()}" class="btn btn-default">Cancel</a>
                    <button type="submit" class="btn btn-primary">Save changes</button>
[#else]
                    <a href="/courses" class="btn btn-default">Cancel</a>
                    <button type="submit" class="btn btn-primary">Create course</button>
[/#if]
                </div>
            </div>
        </div>

    </form>

    <script type="text/javascript">


    </script>

</div>
[@macros.renderScripts /]
[@macros.renderFooter /]
