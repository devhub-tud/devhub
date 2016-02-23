[#import "../../macros.ftl" as macros]
[#import "../../components/project-frameset.ftl" as projectFrameset]
[#import "../../components/commit-row.ftl" as commitRow]

[@macros.renderHeader i18n.translate("section.projects") /]
[@macros.renderMenu i18n user /]
<div class="container">

  [#if group?? && group?has_content]
      [@projectFrameset.renderBreadcrumb i18n group/]
  [/#if]

    <div class="row">
        <div class="col-md-10 col-md-offset-2">
            <h4 style="line-height:34px; margin-top:0;">${i18n.translate("pull-request.title")}</h4>
        </div>
    </div>

    <div class="row">
        <div class="col-md-2">
            [@projectFrameset.renderSidemenu "pull-requests" i18n group![] repository/]
        </div>
        <div class="col-md-10">
            <table class="table table-bordered">
                <tbody>
                [#if openPullRequests?? && openPullRequests?has_content]
                    [#list openPullRequests as pullRequest]
                        [#assign commit = pullRequest.destination]
                        [#assign buildResult = commit.getBuildResult()![]]
                        [@commitRow.render group![] buildResult![] pullRequest.destination "${pullRequest.getURI()}"]
                            <span class="pull-right">
                              <div>
                                <span class="text-success octicon octicon-arrow-up"></span>
                                <span class="text-muted">${pullRequest.ahead}</span>
                                <span class="text-danger octicon octicon-arrow-down"></span>
                                <span class="text-muted">${pullRequest.behind}</span>
                              </div>
                                [#assign numComments = pullRequest.comments?size]
                                [#if numComments > 0]
                                    <div class="pull-right"><i class="glyphicon glyphicon-comment"></i> ${numComments} </div>
                                [/#if]
                            </span>
                            <div class="comment">Pull Request #${pullRequest.issueId}: ${pullRequest.branchName}</div>
                            <div class="committer">${pullRequest.destination.author}</div>
                            <div class="timestamp" data-value="${pullRequest.destination.pushTime?date}">on ${pullRequest.destination.pushTime?string["EEEE dd MMMM yyyy HH:mm"]}</div>
                        [/@commitRow.render]
                    [/#list]
                [#else]
                <tr>
                    <td class="muted">
                        ${i18n.translate("pull-request.no-open-requests")}
                    </td>
                </tr>
                [/#if]
                </tbody>
            </table>

            <h4>Closed pull requests</h4>
            <table class="table table-bordered">
                <tbody>
                [#if closedPullRequests?? && closedPullRequests?has_content]
                    [#list closedPullRequests as pullRequest]
                        [#assign commit = pullRequest.destination]
                        [#assign buildResult = commit.getBuildResult()![]]
                        [@commitRow.render group![] buildResult![] commit "${pullRequest.getURI()}"]
                        <span class="pull-right">
                            [#if pullRequest.merged]
                                <span class="label label-success"><i class="octicon octicon-git-merge"></i> Merged</span>
                            [#else]
                                <span class="label label-danger"><i class="octicon octicon-issue-closed"></i> Closed</span>
                            [/#if]
                        </span>
                        <div class="comment">Pull Request #${pullRequest.issueId}: ${pullRequest.branchName}</div>
                        <div class="committer">${pullRequest.destination.author}</div>
                        <div class="timestamp" data-value="${pullRequest.destination.pushTime?date}">on ${pullRequest.destination.pushTime?string["EEEE dd MMMM yyyy HH:mm"]}</div>
                        [/@commitRow.render]
                    [/#list]
                [#else]
                <tr>
                    <td class="muted">
                        ${i18n.translate("pull-request.no-closed-requests")}
                    </td>
                </tr>
                [/#if]
                </tbody>
            </table>
        </div>
    </div>
</div>
[@macros.renderScripts /]
[@macros.renderFooter /]
