[#import "macros.ftl" as macros]
[#import "components/difftable.ftl" as difftable]
[#import "components/diffbox.ftl" as diffbox]
[#import "components/comment.ftl" as commentElement]
[#import "components/inline-comments.ftl" as inlineComments]

[@macros.renderHeader i18n.translate("section.projects") /]
[@macros.renderMenu i18n user /]
<div class="container">


[#if repositoryEntity?? && repositoryEntity?has_content]
	<ol class="breadcrumb">
		[#if group?? && group?has_content]
			<li><a href="/courses">${i18n.translate("section.projects")}</a></li>
			[#-- <li><a href="/courses/${group.course.code}/groups/${group.groupNumber}">${group.getGroupName()}</a></li> --]
			<li><a href="${group.course.course.getURI()}">${group.course.course.code} - ${group.course.course.name}</a></li>
			<li><a href="${group.course.getURI()}">${group.course.timeSpan.start?string["yyyy"]}[#if group.course.timeSpan.end??] - ${group.course.timeSpan.end?string["yyyy"]}[/#if]</a></li>
			<li><a href="${group.getURI()}">Group ${group.getGroupNumber()}</a></li>
			<li><a href="${group.getURI()}pulls">${i18n.translate("section.pull-requests")}</a></li>
		[#else]	
			<li><a href="/projects">${ i18n.translate("section.projects") }</a></li>
			<li><a href="${repositoryEntity.getURI()}">${repositoryEntity.getTitle()}</a></li>
		[/#if]
		<li class="active">Pull Request ${pullRequest.getIssueId()}</li>
	</ol>
[/#if]

[#assign buildResult = builds[pullRequest.destination.commitId]![]]
[#if buildResult?? && buildResult?has_content]
    [#if buildResult.hasFinished()]
        [#if buildResult.hasSucceeded()]
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
            <a href="${pullRequest.getURI()}" class="btn btn-default active">${i18n.translate("pull-request.overview")}</a>
            <a href="${pullRequest.getURI()}diff" class="btn btn-default">${i18n.translate("pull-request.view-diff")}</a>
        </div>
    </span>

    <div class="headers" style="display: inline-block;">
        <h2 class="header">${commit.getMessage()}</h2>
        <h5 class="subheader">${commit.getAuthor()}</h5>
    [#if commit.getMessage()?has_content]
        <div class="description">${commit.getMessageTail()}</div>
    [/#if]
    </div>
</div>

    <div class="pull-feed">
        <div class="timeline"></div>
[#if events?? && events?has_content]
    [#list events as event]
        [#if event.isCommitEvent()]
            <ul class="list-unstyled">
                <li style="line-height:30px;">
                    <a href="${repositoryEntity.getURI()}commits/${event.commit.commit}/diff">
                        <span class="octicon octicon-git-commit"></span>
                        <span class="label label-default">${event.commit.getCommit()?substring(0,7)?upper_case }</span>
                        <span>${event.commit.getMessage()}</span>
                    </a>
                    [#assign buildResult = builds[event.commit.commit]![]]
                    [#if buildResult?? && buildResult?has_content && buildResult.hasFinished()]
                        [#if buildResult.hasSucceeded()]
                            <a href="${repositoryEntity.getURI()}commits/${event.commit.commit}/build">
                                <span class="octicon octicon-check text-success"></span>
                            </a>
                        [#else]
                            <a href="${repositoryEntity.getURI()}commits/${event.commit.commit}/build">
                                <span class="octicon octicon-x text-danger"></span>
                            </a>
                        [/#if]
                    [/#if]
                </li>
            </ul>
        [#elseif event.isCommentContextEvent()]
            <div class="indent">
            [#if event.comments?? && event.comments?has_content && event.diffBlameFile?? && event.diffBlameFile?has_content]
                [@diffbox.diffbox event.diffBlameFile 0][/@diffbox.diffbox]
                <div class="comment-block">
                [#list event.comments as comment]
                    [#assign a = comment]
                    [@commentElement.renderComment comment][/@commentElement.renderComment]
                [/#list]
                </div>
            [/#if]
            </div>
        [#elseif event.isCommentEvent()]
            [@commentElement.renderComment event.comment][/@commentElement.renderComment]
        [/#if]
    [/#list]
[/#if]

    </div>

    <div class="panel panel-default panel-comment-form" style="position: relative">
        <div class="panel-heading">${i18n.translate("panel.label.add-comment")}</div>
        <div class="panel-body">
            <form class="form-horizontal" id="pull-comment-form" >
                <textarea rows="5" class="form-control" name="content" style="margin-bottom:10px;"></textarea>
                <button type="submit" class="btn btn-primary">${i18n.translate("button.label.submit")}</button>
            </form>
        </div>
    </div>

    <div class="panel panel-default">
        <div class="panel-body">

[#if pullRequest.isOpen()]
    [#if !pullRequest.isMerged()]
            <div class="pull-right">
                <button id="btn-close" class="btn btn-default"><span>${i18n.translate("pull-request.close")}</span></button>
        [#if branch?? && branch?has_content]
                <button id="btn-merge" class="btn btn-primary"><i class="octicon octicon-git-merge"></i> <span>${i18n.translate("pull-request.merge")}</span></button>
        [/#if]
            </div>
            <span class="octicon octicon-git-pull-request pull-request-badge"></span>
            <span id="merge-message">${i18n.translate("pull-request.open.message")}</span>
    [#-- else case should not happen --]
    [/#if]
[#else]
            <div class="pull-right">
    [#if branch?? && branch?has_content]
                <button id="btn-remove-branch" class="btn btn-default"><i class="octicon octicon-trashcan"></i> <span>${i18n.translate("pull-request.remove-branch")}</span></button>
    [/#if]
    [#if pullRequest.isMerged()]
                <button class="btn btn-success" disabled><i class="octicon octicon-git-merge"></i> <span>${i18n.translate("pull-request.merged")}</span></button>
            </div>
            <span id="merge-message">${i18n.translate("pull-request.merged.message")}</span>
    [#else]
                <button class="btn btn-danger" disabled><i class="octicon octicon-issue-closed"></i> <span>${i18n.translate("pull-request.closed")}</span></button>
            </div>
            <span id="merge-message">${i18n.translate("pull-request.closed.message")}</span>
    [/#if]
[/#if]

        </div>
    </div>

</div>


[@macros.renderScripts /]
[@diffbox.renderScripts/]
[@inlineComments.renderScripts group![] i18n commit/]

<script>
    $(function() {
        $('#pull-comment-form').submit(function(event) {
            $.post('${pullRequest.getURI()}comment',
            $('[name="content"]', '#pull-comment-form').val()).done(function(res) {
                // Add comment block
                $('<div class="panel panel-default panel-comment">' +
                '<div class="panel-heading"><strong>' + res.name + '</strong> on '+
                '<a href="#comment-'+ res.commentId + '" id="comment-'+ + res.commentId + '">' + res.date + '</a></div>'+
                    '<div class="panel-body">'+
                        '<p>' + res.content.replace(/\n/g, '<br/>') + '</p>'+
                    '</div>'+
                '</div>').appendTo('.pull-feed');
                // Clear input
                $('[name="content"]', '#pull-comment-form').val('');
            });
            event.preventDefault();
        });
    });
</script>

<script type="text/javascript">
$(function() {
    $('#btn-merge').click(function() {
        var btn = $(this).attr('disabled', true);
        var label = $('span', btn).html('${i18n.translate("pull-request.merging")}');
		var message = $('#merge-message');

        $.post("${pullRequest.getURI()}merge")
            .done(function(res) {
                if(res.success) {
                    label.html('${i18n.translate("pull-request.merged")}');
                    btn.removeClass('btn-primary').addClass('btn-success');
                    message.html('${i18n.translate("pull-request.merged.message")}');
                }
                else {
                    label.html('${i18n.translate("pull-request.failed-to-merge")}');
                    btn.removeClass('btn-primary').addClass('btn-danger');
                    message.html("${i18n.translate("pull-request.failed-to-merge.message")}");
                    $('.pull-request-badge').addClass('failed');
                }

                $('#btn-close').remove();
                $('<button id="btn-remove-branch" class="btn btn-default">' +
                    '<i class="octicon octicon-trashcan"></i> '+
                    '<span>${i18n.translate("pull-request.remove-branch")}</span></button>')
                        .insertBefore('#btn-merge');
                bindDeleteHandler();
            })
            .fail(function() {
                label.html('${i18n.translate("pull-request.failed-to-merge")}');
                btn.removeClass('btn-primary').addClass('btn-danger');
            })
    });

    $('#btn-close').click(function() {
        $.post('${pullRequest.getURI()}close')
            .done(function(res) {
                $('#btn-close')
                    .html('<i class="octicon octicon-issue-closed"></i> <span>${i18n.translate("pull-request.closed")}</span>')
                    .attr('disabled', true)
                    .removeClass('btn-default').addClass('btn-danger');
                $('#merge-message').html('${i18n.translate("pull-request.closed.message")}');
                $('#btn-merge').remove();
                $('<button id="btn-remove-branch" class="btn btn-default">' +
                    '<i class="octicon octicon-trashcan"></i> '+
                        '<span>${i18n.translate("pull-request.remove-branch")}</span></button>')
                    .insertBefore('#btn-close');
                bindDeleteHandler();
            });
    });

    function bindDeleteHandler() {
        $('#btn-remove-branch').click(function() {
            $.post('${pullRequest.getURI()}delete-branch')
                .done(function(res) {
                    $('#btn-remove-branch').remove();
                });
        });
    }

    bindDeleteHandler();
})
</script>
[@macros.renderFooter /]

