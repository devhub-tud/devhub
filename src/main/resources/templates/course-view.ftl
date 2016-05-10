[#import "macros.ftl" as macros]
[@macros.renderHeader i18n.translate("section.courses") /]
[@macros.renderMenu i18n user /]

[#macro renderBreadcrumb course]
    <ol class="breadcrumb hidden-xs">
        <li><a href="/courses">${ i18n.translate("section.courses") }</a></li>
        <li><a href="${courseEdition.course.getURI()}">${courseEdition.course.code} - ${courseEdition.course.name}</a></li>
        <li>
            <span uib-dropdown dropdown-append-to-body="true">
              <a href id="simple-dropdown" uib-dropdown-toggle>
              ${course.timeSpan.start?string["yyyy"]}[#if course.timeSpan.end??] - ${course.timeSpan.end?string["yyyy"]}[/#if]
                <span class="caret"></span>
              </a>
              <ul uib-dropdown-menu>
                [#list course.course.getEditions() as a]
                  <li><a href="${a.getURI()}">${a.timeSpan.start?string["yyyy"]}[#if a.timeSpan.end??] - ${a.timeSpan.end?string["yyyy"]}[/#if]</a></li>
                [/#list]
              </ul>
            </span>
        </li>
        <li>
            <span uib-dropdown dropdown-append-to-body="true">
              <a href id="simple-dropdown" uib-dropdown-toggle>
                Overview
                <span class="caret"></span>
              </a>
              <ul uib-dropdown-menu>
                [#list course.course.getEditions() as a]
                  <li><a href="feed">Course Feed</a></li>
                [/#list]
              </ul>
            </span>
        </li>
    </ol>
[/#macro]

<div class="container">
[@renderBreadcrumb course /]
[#if user.isAdmin() || user.isAssisting(course) ]
    <div class="row">
        <div class="col-md-8">
            <div class=" panel panel-default">
                <div class="panel-heading">
                    ${i18n.translate("course.control.groups")}
                    <a href="${course.getURI()}enroll" class="btn btn-link btn-xs pull-right">
                        <span class="glyphicon glyphicon-plus" aria-hidden="true"></span>
                    </a>
                </div>
                <table class="table">
                    <thead>
                        <tr>
                            <th>${i18n.translate("course.control.group-name")}</th>
                        </tr>
                    </thead>
                    <tbody>
        [#--[#assign groups=course.getGroups()]--]
        [#if groups?has_content]
            [#list groups as group]
                            <tr>
                                <td><a href="${group.getURI()}">${group.getGroupName()}</a></td>
                            </tr>
            [/#list]
        [#else]
                        <tr>
                            <td colspan="2">${i18n.translate("course.control.no-groups")}</td>
                        </tr>
        [/#if]
                    </tbody>

                </table>
            </div>
        </div>
        <div class="col-md-4">
            <div class=" panel panel-default">
                <div class="panel-heading">
                    ${i18n.translate("course.control.assignments")}
                    <a href="${course.getURI()}assignments/create" class="btn btn-link btn-xs pull-right">
                        <span class="glyphicon glyphicon-plus" aria-hidden="true"></span>
                    </a>
                </div>
                <table class="table">
                    <thead>
                    <tr>
                        <th>No.</th>
                        <th>${i18n.translate("course.control.assignment")}</th>
                        <th>${i18n.translate("course.control.due-date")}</th>
                    </tr>
                    </thead>
                    <tbody>
        [#assign assignments = course.getAssignments()]
        [#if assignments?? && assignments?has_content]
        [#list assignments as assignment]
                    <tr>
                        <td>
                            <a href="${course.getURI()}assignments/${assignment.getAssignmentId()}">
                                ${assignment_index + 1}
                            </a>
                        </td>
                        <td>
                            <a href="${course.getURI()}assignments/${assignment.getAssignmentId()}">
                                ${assignment.getName()}
                            </a>
                        </td>
                        <td>
                    [#if assignment.getDueDate()??]
                            <a href="${course.getURI()}assignments/${assignment.getAssignmentId()}">
                                ${assignment.getDueDate()?string["EEE, d MMM yyyy HH:mm"]}
                            </a>
                    [#else]-
                    [/#if]
                            <a href="${course.getURI()}assignments/${assignment.getAssignmentId()}/edit" class="btn btn-default btn-xs pull-right">
                                <span class="glyphicon glyphicon-cog" aria-hidden="true"></span>
                            </a>
                        </td>
                    </tr>
        [/#list]
        [#else]
                    <tr>
                        <td colspan="3">${ i18n.translate("course.control.no-assignments")}</td>
                    </tr>
        [/#if]
                    </tbody>
                </table>
            </div>

            <div class=" panel panel-default">
                <div class="panel-heading">
                ${i18n.translate("course.control.assistants")}
        [#if user.isAdmin() ]
                    <a href="${course.getURI()}assistants" class="btn btn-link btn-xs pull-right">
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
        [#assign assistants=course.getAssistants()]
        [#if assistants?has_content]
            [#list assistants as assistant]
                    <tr>
                        <td>${assistant.getNetId()}</td>
                        <td>
                            ${assistant.getName()}
                            <a href="mailto:${assistant.getEmail()}" class="btn btn-default btn-xs pull-right">
                                <span class="glyphicon glyphicon-envelope" aria-hidden="true"></span>
                            </a>
                        </td>
                    </tr>
            [/#list]
        [#else]
                    <tr>
                        <td colspan="2">${i18n.translate("course.control.no-assistants")}</td>
                    </tr>
        [/#if]
                    </tbody>

                </table>
            </div>

            <div class=" panel panel-default">
                <div class="panel-heading">
                    ${i18n.translate("course.control.details")}
    [#if user.isAdmin()]
                    <a href="${course.getURI()}edit" class="btn btn-link btn-xs pull-right">
                        <span class="glyphicon glyphicon-edit" aria-hidden="true"></span>
                    </a>
    [/#if]
                </div>
                <table class="table">
                    <tbody>
	[#if course.buildInstruction?? && course.buildInstruction.buildTimeout??]
						<tr>
							<th>${i18n.translate("course.control.build-timeout")}</th>
							<td>${course.buildInstruction.buildTimeout} seconds</td>
						</tr>
	[/#if]
    [#if course.getMinGroupSize()?exists]
                        <tr>
                            <th>${i18n.translate("course.control.min-group-size")}</th>
                            <td>${course.getMinGroupSize()}</td>
                        </tr>
    [/#if]
    [#if course.getMaxGroupSize()?exists]
                        <tr>
                            <th>${i18n.translate("course.control.max-group-size")}</th>
                            <td>${course.getMaxGroupSize()}</td>
                        </tr>
    [/#if]
    [#if course.getTemplateRepositoryUrl()?exists]
                        <tr>
                            <th>${i18n.translate("course.control.template-repository-ul")}</th>
                            <td><code style="font-size:8px;">${course.getTemplateRepositoryUrl()}</code></td>
                        </tr>
    [/#if]
                    </tbody>
                </table>
            </div>
        </div>

    </div>

[#else]

[/#if]


</div>
[@macros.renderScripts ]
<script src="/static/vendor/angular/angular.min.js"></script>
<script src="/static/vendor/angular-bootstrap/ui-bootstrap.min.js"></script>

<script type="text/javascript">
	angular.module('devhub', ['ui.bootstrap']);
</script>
[/@macros.renderScripts]
[@macros.renderFooter /]
