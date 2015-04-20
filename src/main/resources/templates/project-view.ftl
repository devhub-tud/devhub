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

[#if branch?? && branch.isAhead() ]
    <div class="alert alert-success" role="alert" style="clear:both; line-height: 34px;">
        [#if pullRequest??]
            <span>${i18n.translate("group.branch.pull-request-message")}</span>
            <a href="/courses/${group.course.code}/groups/${group.groupNumber}/pull/${pullRequest.getIssueId()}" class="btn btn-default pull-right">${i18n.translate("group.branch.go-to-pull-request")}</a>
        [#else]
            <form method="POST" action="/courses/${group.course.code}/groups/${group.groupNumber}/pull" target="_self">
                <span>${i18n.translate("group.branch.ahead-message")}</span>
                <input type="hidden" name="branchName" value="${branch.getName()}"/>
                <button type="submit" class="btn btn-default pull-right">${i18n.translate("group.branch.create-pull-request")}</button>
            </form>
        [/#if]
    </div>
[/#if]

    <div class="row">
        <div class="col-md-10 col-md-offset-2">
        [#if repository?? && repository?has_content && branch?? && branch?has_content]
            <div class="btn-group pull-right">
                <button type="button" class="btn btn-default">
                    <span class="octicon octicon-git-branch"></span>
                    <span class="text-muted">${i18n.translate("branch.current")}:</span>
                    ${branch.getSimpleName()}
                    [#if branch.isAhead() || branch.isBehind() ]
                        <span class="text-success octicon octicon-arrow-up"></span>
                        <span class="text-muted">${branch.getAhead()}</span>
                        <span class="text-danger octicon octicon-arrow-down"></span>
                        <span class="text-muted">${branch.getBehind()}</span>
                    [/#if]
                </button>
                <button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown">
                    <span class="caret"></span>
                    <span class="sr-only">${i18n.translate("branch.switch")}</span>
                </button>
                <ul class="dropdown-menu" role="menu">
                    [#list repository.getBranches() as b ]
                        <li><a href="/courses/${group.course.code}/groups/${group.groupNumber}/branch/${b.getSimpleName()}" style="text-align:right;">
                        ${b.getSimpleName()}
                            [#if b.behind?? && b.ahead?? && b.behind > 0 || b.ahead > 0 ]
                                <span class="text-success octicon octicon-arrow-up"></span>
                                <span class="text-muted">${b.getAhead()}</span>
                                <span class="text-danger octicon octicon-arrow-down"></span>
                                <span class="text-muted">${b.getBehind()}</span>
                            [/#if]
                        </a></li>
                    [/#list]
                </ul>
            </div>
        [/#if]

            <h4 style="line-height:34px; margin-top:0;">Recent commits</h4>
        </div>
    </div>

    <div class="row">
        <div class="col-md-2">
        [@projectFrameset.renderSidemenu "commits" i18n group repository/]
        </div>
        <div class="col-md-10">

            <table class="table table-bordered" id="table-commits">
                <tbody>
                [#if repository?? && repository?has_content]
                    [#if commits?? && commits?has_content]
                        [#list commits.commits as commit]
                            [@commitRow.render group states commit.commit "/courses/${group.course.code}/groups/${group.groupNumber}/commits/${commit.commit}/diff"]
                                [#if comments?? && comments?has_content]
                                    [#assign numComments = comments.amountOfCommits(commit.commit)]
                                    [#if numComments > 0]
                                        <span class="pull-right"><i class="glyphicon glyphicon-comment"></i> ${numComments}</span>
                                    [/#if]
                                [/#if]
                                <div class="comment">${commit.getMessage()} [@listTags repository commit.getCommit() /]</div>
                                <div class="committer">${commit.getAuthor()}</div>
                                <div class="timestamp" data-value="${(commit.getTime() * 1000)?c}">on ${(commit.getTime() * 1000)?number_to_datetime?string["EEEE dd MMMM yyyy HH:mm"]}</div>
                            [/@commitRow.render]
                        [/#list]
                    [#else]
                    <tr>
                        <td class="muted">
                            ${i18n.translate("group.no-commits")}
                        </td>
                    </tr>
                    [/#if]
                [#else]
                <tr>
                    <td class="muted">
                        ${i18n.translate("error.could-not-connect-git-server")}
                    </td>
                </tr>
                [/#if]
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
