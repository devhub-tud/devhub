[#import "macros.ftl" as macros]
[@macros.renderHeader i18n.translate("section.courses") /]
[@macros.renderMenu i18n user /]

[#macro renderBreadcrumb course]
    <ol class="breadcrumb hidden-xs">
        <li><a href="/courses">${ i18n.translate("section.courses") }</a></li>
        <li><a href="${course.getURI()}">${course.code} - ${course.name}</a></li>
    </ol>
[/#macro]

<div class="container">
[@renderBreadcrumb course /]
    <div class="row">
        <div class="col-md-8">
            <div class=" panel panel-default">
                <div class="panel-heading">
                    Course Editions
        [#if user.isAdmin()]
                    <a href="${course.getURI()}setup" title="Setup new course edition" class="btn btn-link btn-xs pull-right">
                        <span class="glyphicon glyphicon-plus" aria-hidden="true"></span>
                    </a>
        [/#if]
                </div>
                <table class="table">
                    <thead>
                        <tr>
                            <th>Code</th>
                            <th>Name</th>
                            <th>Min Group Size</th>
                            <th>Max Group Size</th>
                        </tr>
                    </thead>
                    <tbody>
        [#assign courseEditions=course.getEditions()]
        [#if courseEditions?has_content]
            [#list courseEditions as courseEdition]
                            <tr>
                                <td><a href="${courseEdition.getURI()}">${courseEdition.code}</a></td>
                                <td><a href="${courseEdition.getURI()}">${courseEdition.name}</a></td>
                                <td><a href="${courseEdition.getURI()}">${courseEdition.minGroupSize}</a></td>
                                <td><a href="${courseEdition.getURI()}">${courseEdition.maxGroupSize}</a></td>
                            </tr>
            [/#list]
        [#else]
                        <tr>
                            <td colspan="2">No editions for course</td>
                        </tr>
        [/#if]
                    </tbody>

                </table>
            </div>
        </div>
        <div class="col-md-4">
            <div class=" panel panel-default">
                <div class="panel-heading">
                Course Managers
        [#if user.isAdmin() ]
                    <a href="#" class="btn btn-link btn-xs pull-right" disabled="disabled">
                        <span class="glyphicon glyphicon-cog" aria-hidden="true"></span>
                    </a>
        [/#if]
                </div>
                <table class="table">
                    <thead>
                    <tr>
                        <th>${i18n.translate("course.control.username")}</th>
                        <th>${i18n.translate("course.control.name")}</th>
                    </tr>
                    </thead>
                    <tbody>
        [#assign managers=[]]
        [#if managers?has_content]
            [#list managers as manager]
                    <tr>
                        <td>${manager.getNetId()}</td>
                        <td>
                            ${manager.getName()}
                            <a href="mailto:${manager.getEmail()}" class="btn btn-default btn-xs pull-right">
                                <span class="glyphicon glyphicon-envelope" aria-hidden="true"></span>
                            </a>
                        </td>
                    </tr>
            [/#list]
        [#else]
                    <tr>
                        <td colspan="2">No course managers for course</td>
                    </tr>
        [/#if]
                    </tbody>

                </table>
            </div>

            <div class=" panel panel-default">
                <div class="panel-heading">
                    ${i18n.translate("course.control.details")}
    [#if user.isAdmin()]
                    <a href="${course.getURI()}edit" title="Edit course" class="btn btn-link btn-xs pull-right">
                        <span class="glyphicon glyphicon-edit" aria-hidden="true"></span>
                    </a>
    [/#if]
                </div>
                <table class="table">
                    <tbody>
                      <tr>
                        <th>Name</th>
                        <td>${course.name}</td>
                      </tr>
                    </tbody>
                </table>
            </div>
        </div>

    </div>

</div>
[@macros.renderScripts ]
<script src="/static/vendor/angular/angular.min.js"></script>
<script src="/static/vendor/angular-bootstrap/ui-bootstrap.min.js"></script>

<script type="text/javascript">
	angular.module('devhub', ['ui.bootstrap']);
</script>
[/@macros.renderScripts]
[@macros.renderFooter /]
