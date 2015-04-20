[#import "macros.ftl" as macros]
[#import "components/difftable.ftl" as difftable]
[#import "components/diffbox.ftl" as diffbox]
[#import "components/comment.ftl" as commentElement]
[#import "components/inline-comments.ftl" as inlineComments]

[@macros.renderHeader i18n.translate("section.projects") /]
[@macros.renderMenu i18n user /]
<div class="container">

    <ol class="breadcrumb">
        <li><a href="/courses">${i18n.translate("section.projects")}</a></li>
        <li><a href="/courses/${group.course.code}/groups/${group.groupNumber}">${group.getGroupName()}</a></li>
        <li><a href="/courses/${group.course.code}/groups/${group.groupNumber}/pulls">${i18n.translate("section.pull-requests")}</a></li>
        <li class="active">Pull Request ${pullRequest.getIssueId()}</li>
    </ol>

[#if states.hasStarted(commit.getCommit())]
    [#if states.hasFinished(commit.getCommit())]
        [#if states.hasSucceeded(commit.getCommit())]
        <div class="commit succeeded">
            <span class="state glyphicon glyphicon-ok-circle" title="${i18n.translate("build.state.succeeded")}"></span>
        [#else]
        <div class="commit failed">
            <span class="state glyphicon glyphicon-remove-circle" title="${i18n.translate("build.state.failed")}"></span>
        [/#if]
    [#else]
    <div class="commit running">
        <span class="state glyphicon glyphicon-align-justify" title="${i18n.translate("build.state.queued")}"></span>
    [/#if]
[#else]
<div class="commit ignored">
    <span class="state glyphicon glyphicon-unchecked"></span>
[/#if]

    <span class="view-picker">
        <div class="btn-group">
            <a href="/courses/${group.course.code}/groups/${group.groupNumber}/pull/${pullRequest.issueId}" class="btn btn-default">${i18n.translate("pull-request.overview")}</a>
            <a href="/courses/${group.course.code}/groups/${group.groupNumber}/pull/${pullRequest.issueId}/diff" class="btn btn-default active">${i18n.translate("pull-request.view-diff")}</a>
        </div>
    </span>

    <div class="headers" style="display: inline-block;">
        <h2 class="header">${commit.getTitle()}</h2>
        <h5 class="subheader">${commit.getAuthor()}</h5>
        <div>
            <ul class="list-unstyled">
            [#list diffViewModel.commits as commit]
                <li style="line-height:30px;">
                    <a href="/courses/${group.course.code}/groups/${group.groupNumber}/commits/${commit.commit}/diff">
                        <span class="octicon octicon-git-commit"></span>
                        <span class="label label-default">${commit.getCommit()?substring(0,7)?upper_case }</span>
                        <span>${commit.getMessage()}</span>
                    </a>
                </li>
            [/#list]
            </ul>
        </div>
    [#if commit.getMessage()?has_content]
        <div class="description">${commit.getMessage()}</div>
    [/#if]
    </div>
</div>

[#if diffViewModel.diffs?has_content]
    [#list diffViewModel.diffs as diffModel]
        [@diffbox.diffbox diffModel diffModel_index][/@diffbox.diffbox]
    [/#list]
[#else]
    <div>${i18n.translate("diff.changes.nothing")}</div>
[/#if]

    <div id="list-comments">
[#assign comments = pullRequest.getComments()]
[#if comments?has_content]
    [#list comments as comment]
        [@commentElement.renderComment comment][/@commentElement.renderComment]
    [/#list]
[/#if]
    </div>

    <div class="panel panel-default" style="position: relative">
        <div class="panel-heading">${i18n.translate("panel.label.add-comment")}</div>
        <div class="panel-body">
            <form class="form-horizontal" id="pull-comment-form" >
                <textarea rows="5" class="form-control" name="content" style="margin-bottom:10px;"></textarea>
                <button type="submit" class="btn btn-primary">${i18n.translate("button.label.submit")}</button>
                <button type="button" class="btn btn-default" id="btn-cancel">${i18n.translate("button.label.cancel")}</button>
            </form>
        </div>
    </div>

</div>

[@macros.renderScripts /]

    <script>
        $(function() {
            $('#pull-comment-form').submit(function(event) {
                $.post('/courses/${group.course.code}/groups/${group.groupNumber}/pull/${pullRequest.issueId}/comment',
                    $('[name="content"]', '#pull-comment-form').val()).done(function(res) {
                        // Add comment block
                        $('<div class="panel panel-default panel-comment">' +
                        '<div class="panel-heading"><strong>' + res.name + '</strong> on '+
                            '<a href="#comment-'+ res.commentId + '" id="comment-'+ + res.commentId + '">' + res.date + '</a></div>'+
                        '<div class="panel-body">'+
                        '<p>' + res.content + '</p>'+
                        '</div>'+
                        '</div>').appendTo('#list-comments');
                        // Clear input
                        $('[name="content"]', '#pull-comment-form').val('');
                    });
                event.preventDefault();
            });
        });
    </script>

[@diffbox.renderScripts/]
[@inlineComments.renderScripts group i18n commit/]
[@macros.renderFooter /]
