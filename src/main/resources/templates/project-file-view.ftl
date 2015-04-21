[#import "macros.ftl" as macros]
[#import "components/comment.ftl" as commentElement]
[#import "components/inline-comments.ftl" as inlineComments]

[@macros.renderHeader i18n.translate("section.projects") /]
[@macros.renderMenu i18n user /]
		<div class="container">
[@macros.renderCommitHeader i18n group commit i18n.translate("commit.view-files") /]
			<div class="diff box">
				<div class="header">
					<a href="/courses/${group.course.code}/groups/${group.groupNumber}/commits/${commit.commit}/raw/${path?url('UTF8')}" class="pull-right btn btn-sm btn-default"><i class="glyphicon glyphicon-floppy-save"></i> Download</a>
					<h5>[@macros.renderTreeBreadcrumb group commit repository path /]</h5>
				</div>
			[#if contents?? && contents?has_content]
				<div class="scrollable">
					<table class="table diffs">
						<tbody>
				[#list contents as line]
                    [#assign blameBlock = blame.getBlameBlock(line_index + 1)]
                    [#assign sourceLineNumber = blameBlock.getFromLineNumber(line_index + 1)]
                    [#assign commentsForThisLine = comments.getCommentsForLine(blameBlock.fromCommitId, blameBlock.fromFilePath, sourceLineNumber)]

                            <tr data-source-commit="${blameBlock.fromCommitId}"
                                data-source-line-number="${sourceLineNumber}"
                                data-source-file-name="${blameBlock.fromFilePath}">
                                <td class="ln">${line_index + 1}</td>
                                <td class="code"> <a class="btn btn-xs btn-primary pull-left btn-comment"> <span class="octicon octicon-plus"></span></a><pre>${line}</pre></td>
                            </tr>

                    [#if commentsForThisLine?has_content ]
                            <tr>
                                <td colspan="3" class="comment-block">
                                    [#list commentsForThisLine as comment]
                                        [@commentElement.renderComment comment][/@commentElement.renderComment]
                                    [/#list]
                                    <button class="btn btn-default btn-add-line-comment">${i18n.translate("button.label.add-line-note")}</button>
                                </td>
                            </tr>
                    [/#if]
				[/#list]
						</tbody>
					</table>
				</div>
			[/#if]
			</div>
		</div>

[@macros.renderScripts /]

[#if highlight?? && highlight.isHighlight() ]
	<script src="/static/js/highlight.pack.js"></script>
[/#if]
	<script>
		$(document).ready(function() {
[#if highlight?? && highlight.isHighlight() ]
			hljs.configure({
				tabReplace: '	',
				useBR: true,
				languages : ["${highlight.getClassName()}"]
			});
			
			$('.code').each(function(i, e) {
				hljs.highlightBlock(e);
			});
[/#if]
		});
	</script>

[@inlineComments.renderScripts group i18n commit/]
[@macros.renderFooter /]
