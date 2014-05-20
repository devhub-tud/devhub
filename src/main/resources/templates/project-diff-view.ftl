[#import "macros.ftl" as macros]
[@macros.renderHeader i18n.translate("section.projects") /]
[@macros.renderMenu i18n user /]
		<div class="container">
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
			<div class="commit">
				<span class="state glyphicon glyphicon-align-justify" title="Build queued..."></span>
	[/#if]
[#else]
			<div class="commit">
				<span class="state"></span>
[/#if]

				<span>
					<h2 class="header">${commit.getMessage()}</h2>
					<h5 class="subheader">${commit.getAuthor()}</h5>
				</span>
			</div>
	[#if diffs?has_content]
		[#list diffs as diff]
			<div class="diff box">
				<div class="header">
					<button class="pull-right btn btn-sm btn-default folder"><i class="glyphicon glyphicon-chevron-up"></i> Fold</button>
					<button class="pull-right btn btn-sm btn-default unfolder" style="display: none;"><i class="glyphicon glyphicon-chevron-down"></i> Unfold</button>
			[#if diff.isMoved()]
					<h5><span class="label label-warn">Moved</span> ${diff.diffModel.oldPath} -&gt; ${diff.diffModel.newPath}</h5>
			[#elseif diff.isCopied()]
					<h5><span class="label label-warn">Copied</span> ${diff.diffModel.oldPath} -&gt; ${diff.diffModel.newPath}</h5>
			[#elseif diff.isDeleted()]
					<h5><span class="label label-danger">Deleted</span> ${diff.diffModel.oldPath}</h5>
			[#elseif diff.isAdded()]
					<h5><span class="label label-success">Created</span> </i> ${diff.diffModel.newPath}</h5>
			[#elseif diff.isModified()]
					<h5><span class="label label-primary">Modified</span> ${diff.diffModel.newPath}</h5>
			[/#if]
				</div>
			[#if  diff.lines?has_content]
				<div class="scrollable">
					<table class="table diffs">
						<tbody>
				[#list diff.lines as line]
					[#if line.contents??]
							<tr>
						[#if line.isRemoved()]
								<td class="ln delete">${line.oldLineNumber}</td>
								<td class="ln delete">${line.newLineNumber}</td>
								<td class="code delete"><pre>${line.contents}</pre></td>
						[#elseif line.isAdded()]
								<td class="ln add">${line.oldLineNumber}</td>
								<td class="ln add">${line.newLineNumber}</td>
								<td class="code add"><pre>${line.contents}</pre></td>
						[#else]
								<td class="ln">${line.oldLineNumber}</td>
								<td class="ln">${line.newLineNumber}</td>
								<td class="code"><pre>${line.contents}</pre></td>
						[/#if]
							</tr>
					[/#if]
				[/#list]
						</tbody>
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
