[#import "../../macros.ftl" as macros]
[#import "../../components/project-frameset.ftl" as projectFrameset]
[#import "../../components/commit-row.ftl" as commitRow]

[@macros.renderHeader i18n.translate("section.projects") /]
[@macros.renderMenu i18n user /]
<div class="container">

    [@projectFrameset.renderBreadcrumb i18n group/]

    <div class="row">
        <div class="col-md-10 col-md-offset-2">
            <h4 style="line-height:34px; margin-top:0;">Pull requests</h4>
        </div>
    </div>

    <div class="row">
        <div class="col-md-2">
            [@projectFrameset.renderSidemenu "pull-requests" i18n group repository/]
        </div>
        <div class="col-md-10">
            <table class="table table-bordered">
                <tbody>
                [#if pulls?? && pulls?has_content]
                    [#list pulls as pull]
                        [#assign pullRequest = pull.pullRequest]
                        [#assign branchModel = pull.branchModel]
                        [#assign commit = branchModel.commit]
                        [@commitRow.render group commitChecker commit.getCommit() "/courses/${group.course.code}/groups/${group.groupNumber}/pull/${pullRequest.issueId}"]
                            <span class="pull-right">
                                <span class="text-success octicon octicon-arrow-up"></span>
                                <span class="text-muted">${branchModel.getAhead()}</span>
                                <span class="text-danger octicon octicon-arrow-down"></span>
                                <span class="text-muted">${branchModel.getBehind()}</span>
                            </span>
                            <div class="comment">Pull Request #${pullRequest.issueId}: ${branchModel.getSimpleName()}</div>
                            <div class="committer">${commit.getMessage()}</div>
                            <div class="timestamp" data-value="${(commit.getTime() * 1000)?c}">on ${(commit.getTime() * 1000)?number_to_datetime?string["EEEE dd MMMM yyyy HH:mm"]}</div>
                        [/@commitRow.render]
                    [/#list]
                [#else]
                <tr>
                    <td class="muted">
                        No unmerged pull requests in this repository.
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
