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

[#if pullRequest.isOpen()]
    [#if !pullRequest.isMerged()]
            <div class="pull-right">
                <button id="btn-close" class="btn btn-default"><span>Close pull request</span></button>
        [#if branch?? && branch?has_content]
                <button id="btn-merge" class="btn btn-primary"><i class="octicon octicon-git-merge"></i> <span>Merge pull request</span></button>
        [/#if]
            </div>
            <span id="merge-message">Hey! You can merge the pull request by clicking the button to the right.</span>
    [#-- else case should not happen --]
    [/#if]
[#else]
            <div class="pull-right">
    [#if branch?? && branch?has_content]
                <button id="btn-remove-branch" class="btn btn-default"><i class="octicon octicon-trashcan"></i> <span>Remove branch</span></button>
    [/#if]
    [#if pullRequest.isMerged()]
                <button class="btn btn-success" disabled><i class="octicon octicon-git-merge"></i> <span>Merged</span></button>
            </div>
            <span id="merge-message">It seems the branch is already merged into the master!</span>
    [#else]
                <button class="btn btn-danger" disabled><i class="octicon octicon-issue-closed"></i> <span>Closed</span></button>
            </div>
            <span id="merge-message">The pull request is closed and not merged into the master.</span>
    [/#if]
[/#if]

        </div>
    </div>

</div>


[@macros.renderScripts /]
[@diffbox.renderScripts/]
[@difftable.renderScripts/]
<script type="text/javascript">
$(function() {
    $('#btn-merge').click(function() {
        var btn = $(this).attr('disabled', true);
        var label = $('span', btn).html('Merging...');

        $.post("/courses/${group.course.code}/groups/${group.groupNumber}/pull/${pullRequest.issueId}/merge")
            .done(function(res) {
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
    });

    $('#btn-close').click(function() {
        $.post('/courses/${group.course.code}/groups/${group.groupNumber}/pull/${pullRequest.issueId}/close')
            .done(function(res) {
                $('#btn-close')
                    .html('<i class="octicon octicon-issue-closed"></i> <span>Closed</span>')
                    .attr('disabled', true)
                    .removeClass('btn-default').addClass('btn-danger');
                $('#btn-merge').remove();
                $('#merge-message').html('The pull request is closed and not merged into the master.');
                $('<button id="btn-remove-branch" class="btn btn-default"><i class="octicon octicon-trashcan"></i> <span>Remove branch</span></button>')
                    .insertBefore('#btn-close');
                bindDeleteHandler();
            });
    });

    function bindDeleteHandler() {
        $('#btn-remove-branch').click(function() {
            $.post('/courses/${group.course.code}/groups/${group.groupNumber}/pull/${pullRequest.issueId}/delete-branch')
                .done(function(res) {
                    $('#btn-remove-branch').remove();
                });
        });
    }

    bindDeleteHandler();
})
</script>
[@macros.renderFooter /]

