[#import "macros.ftl" as macros]
[#import "components/difftable.ftl" as difftable]
[#import "components/diffbox.ftl" as diffbox]

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
    <div class="headers" style="display: inline-block;">
        <h2 class="header">${commit.getMessage()}</h2>
        <h5 class="subheader">${commit.getAuthor()}</h5>
        <div>
            <ul class="list-unstyled">
            [#list diffViewModel.diffResponse.commits as commit]
                <li style="line-height:30px;">
                    <a href="">
                        <span class="octicon octicon-git-commit"></span>
                        <span class="label label-default">${commit.getCommit()?substring(0,7)?upper_case }</span>
                        <span>${commit.getMessage()}</span>
                    </a>
                </li>
            [/#list]
            </ul>
        </div>
    [#if commit.getMessageTail()?has_content]
        <div class="description">${commit.getMessageTail()}</div>
    [/#if]
    </div>
</div>

[#if diffViewModel.diffResponse?has_content]
    [#list diffViewModel.diffResponse.diffs as diffModel]
        [@diffbox.diffbox diffViewModel diffModel diffModel_index][/@diffbox.diffbox]
    [/#list]
[#else]
    <div>${i18n.translate("diff.changes.nothing")}</div>
[/#if]

</div>

[@macros.renderScripts /]
[@diffbox.renderScripts/]
[@difftable.renderScripts/]
[@macros.renderFooter /]
