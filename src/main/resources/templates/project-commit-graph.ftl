[#macro listTags repository commitId]
    [#list repository.getTags() as tag]
        [#if tag.getCommit().getCommit() == commitId]
        <span class="label label-primary">${tag.getSimpleName()}</span>
        [/#if]
    [/#list]
[/#macro]

[#import "macros.ftl" as macros]
[#import "components/project-frameset.ftl" as projectFrameset]
[#import "components/commit-row.ftl" as commitRow]

[@macros.renderHeader i18n.translate("section.projects") /]
[@macros.renderMenu i18n user /]
<div class="container">

[#if repositoryEntity?? && repositoryEntity?has_content]
    [@projectFrameset.renderBreadcrumb i18n group![] repositoryEntity/]
[/#if]

    <div class="row">
        <div class="col-md-10 col-md-offset-2">
        [#if courseEdition?? && !(user.isAdmin() || user.isAssisting(courseEdition))]
        [#else]
            <a href="contributors/edit" class="btn btn-default pull-right" >Edit</a>
        [/#if]
            <h4 style="line-height:34px; margin-top:0;">${i18n.translate("group.contributors")}</h4>
        </div>
    </div>

    <div class="row">
        <div class="col-md-2">
        [@projectFrameset.renderSidemenu "insights" i18n group![] repository/]
        </div>
        <div class="col-md-10">
            <table class="table table-bordered" id="table-commits">
                <thead>
                <tr>
                    <th>${i18n.translate("course.control.username")}</th>
                    <th>${i18n.translate("course.control.name")}</th>
                [#if group?? && group?has_content]
                    <th>Student Number</th>
                [/#if]
                    <th>${i18n.translate("course.control.email")}</th>
                </tr>
                </thead>
                <tbody>
                [#list repositoryEntity.getCollaborators() as member]
                <tr>
                    <td>${member.getNetId()}</td>
                    <td>${member.getName()}</td>
                    [#if group?? && group?has_content]
                        <td>${member.getStudentNumber()!"-"}</td>
                    [/#if]
                    <td><a href="mailto:${member.getEmail()}">${member.getEmail()}</a></td>
                </tr>
                [/#list]
                </tbody>
            </table>

        </div>
    </div>
</div>

[@macros.renderScripts /]
[@macros.renderFooter /]
