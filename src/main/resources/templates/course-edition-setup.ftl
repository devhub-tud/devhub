[#import "macros.ftl" as macros]
[@macros.renderHeader i18n.translate("section.courses") /]
[@macros.renderMenu i18n user /]
<div class="container">

[#if courseEdition?exists]
    <h2>${i18n.translate("course-edition.edit.title", course.name, courseEdition.name)}</h2>
    <form class="form-horizontal" method="POST" target="_self">
[#else]
    <h2>${i18n.translate("course-edition.create.title", course.name)}</h2>
    <form class="form-horizontal" method="POST" target="_self">
[/#if]
        [#if error?? && error?has_content]
            <div class="alert alert-danger">
            ${i18n.translate(error)}
            </div>
        [/#if]

        [#if courseEdition?exists]
            <input type="hidden" name="id" value="${courseEdition.getId()}"/>
        [/#if]

        <div class="form-group">
            <label for="code" class="col-sm-2 control-label">${i18n.translate("course.control.course-code")}</label>
            <div class="col-sm-10">
        [#if courseEdition?exists]
                <input type="text" class="form-control" name="code" id="code" placeholder="${i18n.translate("course.control.course-edition-code.example")}" value="${courseEdition.getCode()}" disabled>
        [#else]
                <input type="text" class="form-control" name="code" id="code" placeholder="${i18n.translate("course.control.course-edition-code.example")}">
        [/#if]
            </div>
        </div>

        <div class="form-group">
            <label for="template" class="col-sm-2 control-label">${i18n.translate("course.control.template-repository-ul")}</label>
            <div class="col-sm-10">
        [#if courseEdition?exists]
                <input type="text" class="form-control" name="template" id="template" value="${courseEdition.getTemplateRepositoryUrl()}">
        [#else]
                <input type="text" class="form-control" name="template" id="template" value="https://github.com/octocat/Spoon-Knife.git">
        [/#if]
            </div>
        </div>

        <div class="form-group">
            <label for="min" class="col-sm-2 control-label">${i18n.translate("course.control.min-group-size")}</label>
            <div class="col-sm-10">
        [#if courseEdition?exists && courseEdition.getMinGroupSize()?exists]
            <input type="number" class="form-control" name="min" id="min" value="${courseEdition.getMinGroupSize()}" min="1" max="100">
        [#else]
            <input type="number" class="form-control" name="min" id="min" value="1" min="1" max="100">
        [/#if]
            </div>
        </div>

        <div class="form-group">
            <label for="max" class="col-sm-2 control-label">${i18n.translate("course.control.max-group-size")}</label>
            <div class="col-sm-10">
        [#if courseEdition?exists && courseEdition.getMaxGroupSize()?exists]
            <input type="number" class="form-control" name="max" id="max" value="${courseEdition.getMaxGroupSize()}" min="1" max="100">
        [#else]
            <input type="number" class="form-control" name="max" id="max" value="1" min="1" max="100">
        [/#if]
            </div>
        </div>

        <div class="form-group">
            <label for="timeout" class="col-sm-2 control-label">${i18n.translate("course.control.build-timeout")} (${i18n.translate("course.control.seconds")})</label>
            <div class="col-sm-10">
        [#if courseEdition?exists && courseEdition.buildInstruction.getBuildTimeout()?exists]
            <input type="number" class="form-control" name="timeout" id="timeout" value="${courseEdition.buildInstruction.getBuildTimeout()}" min="1" max="3600">
        [#else]
            <input type="number" class="form-control" name="timeout" id="timeout" value="600" min="1" max="3600">
        [/#if]
            </div>
        </div>


        <div class="form-group">
            <div class="col-sm-offset-2 col-sm-10">
                <div class="pull-right">
[#if courseEdition?exists]
                    <a href="/courses/${courseEdition.getCode()}" class="btn btn-default">${i18n.translate("course.control.cancel")}</a>
                    <button type="submit" class="btn btn-primary">${i18n.translate("course.control.save")}</button>
[#else]
                    <a href="/courses" class="btn btn-default">${i18n.translate("course.control.cancel")}</a>
                    <button type="submit" class="btn btn-primary">${i18n.translate("course-edition.control.create")}</button>
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
