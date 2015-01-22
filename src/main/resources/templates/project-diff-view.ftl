[#import "macros.ftl" as macros]
[@macros.renderHeader i18n.translate("section.projects") /]
[@macros.renderMenu i18n user /]
		<div class="container">
[@macros.renderCommitHeader i18n group commit "View diff" /]
	[#if diffs?has_content]
		[#list diffs as diffModel]
			<div class="diff box">
				<div class="header">
					<button class="pull-right btn btn-sm btn-default folder"><i class="glyphicon glyphicon-chevron-up"></i> Fold</button>
					<button class="pull-right btn btn-sm btn-default unfolder" style="display: none;"><i class="glyphicon glyphicon-chevron-down"></i> Unfold</button>
			[#if diffModel.isMoved()]
					<h5><span class="label label-warn">Moved</span> ${diffModel.oldPath} -&gt; ${diffModel.newPath}</h5>
			[#elseif diffModel.isCopied()]
					<h5><span class="label label-warn">Copied</span> ${diffModel.oldPath} -&gt; ${diffModel.newPath}</h5>
			[#elseif diffModel.isDeleted()]
					<h5><span class="label label-danger">Deleted</span> ${diffModel.oldPath}</h5>
			[#elseif diffModel.isAdded()]
					<h5><span class="label label-success">Created</span> </i> ${diffModel.newPath}</h5>
			[#elseif diffModel.isModified()]
					<h5><span class="label label-primary">Modified</span> ${diffModel.newPath}</h5>
			[/#if]
				</div>
			[#if  diffModel.diffContexts?has_content]
				<div class="scrollable">
					<table class="table diffs">
			[#list diffModel.diffContexts as diffContext]
						<tbody>
			[#assign oldLineNumber=diffContext.oldStart]
			[#assign newLineNumber=diffContext.newStart]
				[#list diffContext.diffLines as line]
					[#if line.content??]
							<tr>
						[#if line.isRemoved()]
								<td class="ln delete">${oldLineNumber}</td>
								<td class="ln delete"></td>
								<td class="code delete"><pre>${line.content}</pre></td>
								[#assign oldLineNumber=oldLineNumber + 1]
						[#elseif line.isAdded()]
								<td class="ln add"></td>
								<td class="ln add">${newLineNumber}</td>
								<td class="code add"><pre>${line.content}</pre></td>
								[#assign newLineNumber=newLineNumber + 1]
						[#else]
								<td class="ln">${oldLineNumber}</td>
								<td class="ln">${newLineNumber}</td>
								<td class="code"><pre>${line.content}</pre></td>
								[#assign oldLineNumber=oldLineNumber + 1]
								[#assign newLineNumber=newLineNumber + 1]
						[/#if]
							</tr>
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
			});
		</script>
[@macros.renderFooter /]
