[#import "macros.ftl" as macros]
[#import "components/comment.ftl" as commentElement]
[#import "components/inline-comments.ftl" as inlineComments]
[#import "components/warning-bullet.ftl" as warningBullet]

[@macros.renderHeader i18n.translate("section.projects") /]
[@macros.renderMenu i18n user /]
		<div class="container">
[@macros.renderCommitHeader i18n group![] repositoryEntity commit i18n.translate("commit.view-files") /]
			<div class="diff box">
				<div class="header">
				  	<span class="pull-right hidden-xs buttons">
						<a href="${repositoryEntity.getURI()}commits/${commit.commit}/raw/${path?url('UTF8')}" class="btn btn-sm btn-default"><i class="glyphicon glyphicon-floppy-save"></i> Download</a>
					</span>
					<h5>[@macros.renderTreeBreadcrumb group![] commit repository path /]</h5>
				</div>
			[#if contents?? && contents?has_content]
				<div class="scrollable">
					[#if path?ends_with(".md")]
                        <div class="panel-body" style="background-color: white;">
                            [#--noinspection FtlWellformednessInspection--]
                            [#noescape]${MarkDownParser.markdownToHtml(contents)}[/#noescape]
                        </div>
                    [#else]
    					<table class="table diffs">
    						<tbody>
    				            [#list contents?split("\\r?\\n","r") as line]
    				            	[#assign line_number = line_index + 1]
                                    [#assign blameBlock = blame.getBlameBlock(line_number)]
                                    [#assign sourceLineNumber = blameBlock.getFromLineNumber(line_number)]
                                    [#assign commentsForThisLine = comments.getCommentsForLine(blameBlock.fromCommitId, blameBlock.fromFilePath, sourceLineNumber)]
                                        <tr data-source-commit="${blameBlock.fromCommitId}"
                                            data-source-line-number="${sourceLineNumber}"
                                            data-source-file-name="${blameBlock.fromFilePath}"
                                            data-link-commit="${blameBlock.fromCommitId}">
                                            <td class="ln">
                                                [#if lineWarnings??]
                                                    [@warningBullet.renderBullet i18n lineWarnings blameBlock.fromCommitId blameBlock.fromFilePath sourceLineNumber 0 sourceLineNumber/]
                                                [/#if]
                                                <a href="#R${line_number}" id="R${line_number}">${line_number}</a>
                                            </td>
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
                    [/#if]
				</div>
			[/#if]
			</div>
		</div>

[@macros.renderScripts /]

[#if highlight?? && highlight.isHighlight() ]
	<script src="/static/vendor/highlightjs/highlight.pack.js"></script>
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

[@inlineComments.renderScripts group![] i18n commit/]
[@macros.renderFooter /]
