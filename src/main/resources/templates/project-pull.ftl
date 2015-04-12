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
        <li><a href="/courses/${group.course.code}/groups/${group.groupNumber}/pulls">Pull Requests</a></li>
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
            <a href="/courses/${group.course.code}/groups/${group.groupNumber}/pull/${pullRequest.issueId}" class="btn btn-default active">Overview</a>
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


    <div class="panel panel-default">

        <div class="panel-body">
[#if branch?? && branch.getAhead() > 0]
            <div class="pull-right">
                <button id="btn-merge" class="btn btn-primary"><i class="octicon octicon-git-merge"></i> <span>Merge pull request</span></button>
            </div>
            Hey! You can merge the pull request by clicking the button to the right.
[#else]
            It seems the branch is already merged into the master!
[/#if]
[#--        Or merge the pull request with the command line:
            <div>
                <code>git fetch origin/${pullRequest.branchName}</code><br/>
                <code>git checkout master</code><br/>
                <code>git merge origin/${pullRequest.branchName}</code><br/>
                <code>git push origin master</code>
            </div>
--]
        </div>

    </div>

</div>


[@macros.renderScripts /]
[@diffbox.renderScripts/]
[@difftable.renderScripts/]
<script type="text/javascript">
$(function() {
    $('#btn-merge').on('click', function mergeClickHandler() {
        var btn = $(this).attr('disabled', true);
        var label = $('span', btn).html('Merging...');

        $.post("/courses/${group.course.code}/groups/${group.groupNumber}/pull/${pullRequest.issueId}/merge")
            .done(function(res) {
                console.log(res);
                if(res.success) {
                    label.html('Merged');
                    btn.removeClass('btn-primary').addClass('btn-success');
                }
                else {
                    label.html('Failed to merge');
                    btn.removeClass('btn-primary').addClass('btn-danger');
                }
            })
            .fail(function() {
                label.html('Failed to merge');
                btn.removeClass('btn-primary').addClass('btn-danger');
            })
    })
})
</script>
[@macros.renderFooter /]

