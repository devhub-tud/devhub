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

[@projectFrameset.renderBreadcrumb i18n group/]

    <div class="row">
        <div class="col-md-10 col-md-offset-2">
            <h4 style="line-height:34px; margin-top:0;">${i18n.translate("group.contributors")}</h4>
        </div>
    </div>

    <div class="row">
        <div class="col-md-2">
        [@projectFrameset.renderSidemenu "contributors" i18n group repository/]
        </div>
        <div class="col-md-10">
            <table class="table table-bordered" id="table-commits">
                <thead>
                    <tr>
                        <th>${i18n.translate("course.control.username")}</th>
                        <th>${i18n.translate("course.control.name")}</th>
                        <th>${i18n.translate("course.control.email")}</th>
                    </tr>
                </thead>
                <tbody>
            [#list group.getMembers() as member]
                    <tr>
                        <td>${member.getNetId()}</td>
                        <td>${member.getName()}</td>
                        <td><a href="mailto:${member.getEmail()}">${member.getEmail()}</a></td>
                    </tr>
            [/#list]
                </tbody>
            </table>


        [#function max x y]
            [#if (x<y)][#return y][#else][#return x][/#if]
        [/#function]

        [#function min x y]
            [#if (x<y)][#return x][#else][#return y][/#if]
        [/#function]

        [#if branch?? && branch?has_content && pagination?? ]
            [#assign pageCount = max(pagination.getPageCount(), 1)]
            [#assign currentPage = pagination.getPage() ]
            <div class="text-center">
                <ul class="pagination pagination-lg">
                    [#list max(1, currentPage-4)..min(pageCount, currentPage+4) as pageNumber ]
                        [#if pageNumber == currentPage ]
                            <li class="active"><a href="/courses/${group.course.code}/groups/${group.groupNumber}/branch/${branch.getSimpleName()}?page=${pageNumber}">${pageNumber}</a></li>
                        [#else]
                            <li><a href="/courses/${group.course.code}/groups/${group.groupNumber}/branch/${branch.getSimpleName()}?page=${pageNumber}">${pageNumber}</a></li>
                        [/#if]
                    [/#list]
                </ul>
            </div>
        [/#if]

        </div>
    </div>
</div>

[@macros.renderScripts /]
[@macros.renderFooter /]
