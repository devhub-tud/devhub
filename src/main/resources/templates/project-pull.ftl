[#import "macros.ftl" as macros]
[#import "components/difftable.ftl" as difftable]
[#import "components/diffbox.ftl" as diffbox]
[#import "components/comment.ftl" as commentElement]

[@macros.renderHeader i18n.translate("section.projects") /]
[@macros.renderMenu i18n user /]
<div class="container">

    <ol class="breadcrumb">
        <li><a href="/courses">Projects</a></li>
        <li><a href="/courses/${group.course.code}/groups/${group.groupNumber}">${group.getGroupName()}</a></li>
        <li><a href="#">Pull Requests</a></li>
        <li class="active">Pull Request ${pullRequest.getIssueId()}</li>
    </ol>

[#if states.hasStarted(commit.getCommit())]
    [#if states.hasFinished(commit.getCommit())]
        [#if states.hasSucceeded(commit.getCommit())]
        <div class="commit succeeded">
            <span class="state glyphicon glyphicon-ok-circle" title="Build succeeded!"></span>
        [#else]
        <div class="commit failed">
            <span class="state glyphicon glyphicon-remove-circle" title="Build failed!"></span>
        [/#if]
    [#else]
    <div class="commit running">
        <span class="state glyphicon glyphicon-align-justify" title="Build queued..."></span>
    [/#if]
[#else]
<div class="commit ignored">
    <span class="state glyphicon glyphicon-unchecked"></span>
[/#if]

    <span class="view-picker">
        <div class="btn-group">
            <a href="/courses/${group.course.code}/groups/${group.groupNumber}/pull/${pullRequest.issueId}/diff" class="btn btn-default">View diff</a>
        </div>
    </span>

    <div class="headers" style="display: inline-block;">
        <h2 class="header">${commit.getMessage()}</h2>
        <h5 class="subheader">${commit.getAuthor()}</h5>
    [#if commit.getMessage()?has_content]
        <div class="description">${commit.getMessage()}</div>
    [/#if]
    </div>
</div>

[#if events?? && events?has_content]
    [#list events as event]
        [#if event.isCommitEvent()]
            [#-- <div>${event.date?datetime}</div> --]
            <ul class="list-unstyled">
                <li style="line-height:30px;">
                    <a href="/courses/${group.course.code}/groups/${group.groupNumber}/commits/${event.commit.commit}/diff">
                        <span class="octicon octicon-git-commit"></span>
                        <span class="label label-default">${event.commit.getCommit()?substring(0,7)?upper_case }</span>
                        <span>${event.commit.getMessage()}</span>
                    </a>
                </li>
            </ul>
        [#elseif event.isCommentContextEvent()]
            [#if event.comments?? && event.comments?has_content && event.diffBlameFile?? && event.diffBlameFile?has_content]
                [#-- [@difftable.diffTable event.diffBlameFile event.diffBlameFile 0 commit/] --]
                [@diffbox.diffbox event.diffBlameFile 0][/@diffbox.diffbox]

                [#list event.comments as comment]
                    [@commentElement.renderComment comment][/@commentElement.renderComment]
                [/#list]
            [/#if]

        [/#if]
    [/#list]
[/#if]

[#--
[#if diffViewModel.diffs?has_content]
    [#list diffViewModel.diffs as diffModel]
        [@diffbox.diffbox diffViewModel diffModel diffModel_index][/@diffbox.diffbox]
    [/#list]
[#else]
    <div>${i18n.translate("diff.changes.nothing")}</div>
[/#if]
--]

</div>

[@macros.renderScripts /]
[@diffbox.renderScripts/]
[@difftable.renderScripts/]
[@macros.renderFooter /]
