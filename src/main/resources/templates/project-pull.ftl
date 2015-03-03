[#import "macros.ftl" as macros]
[#import "components/comment.ftl" as commentComponent]

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
		[#list commits as commit]
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

	[#if diffs?has_content]
		[#list diffs as diffModel]
			<div class="diff box" data-old-path="${diffModel.oldPath}" data-new-path="${diffModel.newPath}">
				<div class="header">
					<button class="pull-right btn btn-sm btn-default folder"><i class="glyphicon glyphicon-chevron-up"></i> Fold</button>
					<button class="pull-right btn btn-sm btn-default unfolder" style="display: none;"><i class="glyphicon glyphicon-chevron-down"></i> Unfold</button>
			[#if diffModel.isDeleted()]
					<h5><span class="label label-danger">Deleted</span> ${diffModel.oldPath}</h5>
			[#else]
					<a href="/courses/${group.course.code}/groups/${group.groupNumber}/${commit.commit}/raw/${diffModel.newPath?url('UTF8')}" class="pull-right btn btn-sm btn-default" style="margin-right:5px;"><i class="glyphicon glyphicon-floppy-save"></i> Download</a>
				[#if diffModel.isMoved()]
					<h5><span class="label label-warn">Moved</span> ${diffModel.oldPath} -&gt; ${diffModel.newPath}</h5>
				[#elseif diffModel.isCopied()]
					<h5><span class="label label-warn">Copied</span> ${diffModel.oldPath} -&gt; ${diffModel.newPath}</h5>
				[#elseif diffModel.isAdded()]
					<h5><span class="label label-success">Created</span> </i> ${diffModel.newPath}</h5>
				[#elseif diffModel.isModified()]
					<h5><span class="label label-primary">Modified</span> ${diffModel.newPath}</h5>
				[/#if]
			[/#if]
				</div>
			[#if  diffModel.diffContexts?has_content]
				<div class="overflow-hidden">
					<table class="table diffs">
			[#list diffModel.diffContexts as diffContext]
						<tbody>
			[#assign oldLineNumber=diffContext.oldStart + 1]
			[#assign newLineNumber=diffContext.newStart + 1]
				[#list diffContext.diffLines as line]
					[#if line.content??]
							<tr>
						[#if line.isRemoved()]
								<td class="ln delete">
									<a href="#${diffModel_index}L${oldLineNumber}" id="${diffModel_index}L${oldLineNumber}">${oldLineNumber}</a>
								</td>
								<td class="ln delete"></td>
								<td class="code delete">
									<a class="btn btn-xs btn-primary pull-left btn-comment"><span class="octicon octicon-plus"></span></a>
									<pre>${line.content}</pre>
								</td>
						[#elseif line.isAdded()]
								<td class="ln add"></td>
								<td class="ln add">
									<a href="#${diffModel_index}R${newLineNumber}" id="${diffModel_index}R${newLineNumber}">${newLineNumber}</a>
								</td>
								<td class="code add">
									<a class="btn btn-xs btn-primary pull-left btn-comment"><span class="octicon octicon-plus"></span></a>
									<pre>${line.content}</pre>
								</td>
						[#else]
								<td class="ln">
									<a href="#${diffModel_index}L${oldLineNumber}" id="${diffModel_index}L${oldLineNumber}">${oldLineNumber}</a>
								</td>
								<td class="ln">
									<a href="#${diffModel_index}R${newLineNumber}" id="${diffModel_index}R${newLineNumber}">${newLineNumber}</a>
								</td>
								<td class="code">
									<a class="btn btn-xs btn-primary pull-left btn-comment"><span class="octicon octicon-plus"></span></a>
									<pre>${line.content}</pre>
								</td>
						[/#if]
							</tr>
							
						[#if line.isRemoved()]
								[#assign commentsForThisLine = comments.commentsForLine(diffModel.oldPath, oldLineNumber, null, null)]
						[#elseif line.isAdded()]
								[#assign commentsForThisLine = comments.commentsForLine(null, null, diffModel.newPath, newLineNumber)]
						[#else]
								[#assign commentsForThisLine = comments.commentsForLine(diffModel.oldPath, oldLineNumber, diffModel.newPath, newLineNumber)]
						[/#if]
						
						[#if commentsForThisLine?has_content ]
							<tr class="comment-block">
							<td colspan="3">
						[#list commentsForThisLine as comment]
							[@commentComponent.renderComment comment     /]
						[/#list]
							</td>
							</tr>
						[/#if]
						
						[#if line.isRemoved()]
								[#assign oldLineNumber=oldLineNumber + 1]
						[#elseif line.isAdded()]
								[#assign newLineNumber=newLineNumber + 1]
						[#else]
								[#assign oldLineNumber=oldLineNumber + 1]
								[#assign newLineNumber=newLineNumber + 1]
						[/#if]
					[/#if]
				[/#list]
						</tbody>
			[/#list]
					</table>
				</div>
			[/#if]
			</div>
		[/#list]
	[#else]
			<div>${i18n.translate("diff.changes.nothing")}</div>
	[/#if]
		</div>
[@macros.renderScripts /]
		<script>
			$(document).ready(function() {
				$(".diff").each(function() {
					var diffBody = $(this).find(".diffs");
					if (diffBody.length == 0) {
						var folder = $(this).find(".folder");
						folder.css("display", "none");
					}
				});
				
				$(".folder").click(function(e) {
					var body = $(this).parentsUntil(".box").parent();
					var unfolder = $(this).parent().find(".unfolder");
					
					body.addClass("folded");
					$(this).css("display", "none").blur();
					unfolder.css("display", "block"); 
				});
				$(".unfolder").click(function(e) {
					var body = $(this).parentsUntil(".box").parent();
					var folder = $(this).parent().find(".folder");

					body.removeClass("folded");
					$(this).css("display", "none").blur();
					folder.css("display", "block"); 
				});
				
function getCommentBlockWithInput(row) {
  var commentBlock = row.next(".comment-block");
  if(commentBlock.length === 0) {
    commentBlock = createCommentBlock(row);
  }
  if(!hasCommentForm(commentBlock)) {
  	var oldRowNumber, newRowNumber, oldPath, newPath, data;
  	
  	oldRowNumber = parseInt($("td", row).eq(0).text());
  	if(isNaN(oldRowNumber))
  		oldRowNumber = null;
  	
  	newRowNumber = parseInt($("td", row).eq(1).text());
  	if(isNaN(newRowNumber))
  		newRowNumber = null;
  	
  	data = row.closest(".diff").data();
    createCommentForm(commentBlock.find("td"), data.oldPath, oldRowNumber, data.newPath, newRowNumber);
  }
  return commentBlock;
}

function createCommentBlock(row) {
  return $(
    '<tr class="comment-block">' +
      '<td colspan="3"></td>' +
    '</tr>')
  .insertAfter(row);
}

function hasCommentForm(commentBlock) {
  return $("#comment-form", commentBlock).length > 0;
}

function createCommentForm(commentBlock, oldPath, oldLineNumber, newPath, newLineNumber) {
  $(
    '<div class="panel panel-default" id="comment-form">' +
      '<div class="panel-heading">Add a comment</div>' +
      '<div class="panel-body">' +
        '<form class="form-horizontal" action="diff/comment" method="POST">' +
          '<input type="hidden" name="commitId" value="${ commit.commit }"/>' +
          '<input type="hidden" name="oldFilePath" value="' + oldPath + '"/>' +
          (( oldLineNumber !== null ) ? '<input type="hidden" name="oldLineNumber" value="' + oldLineNumber + '"/>' : '') +
          '<input type="hidden" name="newFilePath" value="' + newPath + '"/>' +
          (( newLineNumber !== null ) ? '<input type="hidden" name="newLineNumber" value="' + newLineNumber + '"/>' : '') +
          '<textarea rows="5" class="form-control" name="content"></textarea>' + 
          '<button type="submit" class="btn btn-primary">Submit</button>' +
          '<button type="button" class="btn btn-default" id="btn-cancel">Cancel</button>' +
        '</form>' + 
      '</div>' +
    '</div>')
    .appendTo(commentBlock)
    .find("#btn-cancel").click(function() {
		var row = $(this).closest("tr");
		if(row.find(".panel-comment").length === 0) {
			row.remove();
		}
		else {
			row.find("#comment-form").remove();
		}
	});
}

function appendComment(commentBlock) {
    $(
    '<div class="panel panel-default panel-comment">' +
      '<div class="panel-heading"><strong>Jan-Willem Gmelig Meyling</strong> on Monday, 28 february</div>' +
      '<div class="panel-body">' +
        '<p>' +
          'I think it would be really great if we do this!' +
        '</p>' + 
      '</div>' +
    '</div>')
    .appendTo(commentBlock);
}

$(".btn-comment").click(function() {
  var currentRow = $(this).closest("tr");
  getCommentBlockWithInput(currentRow);
});

});
		</script>
[@macros.renderFooter /]
