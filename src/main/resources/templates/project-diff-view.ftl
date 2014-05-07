[#import "macros.ftl" as macros]
[@macros.renderHeader i18n.translate("section.projects") /]
[@macros.renderMenu i18n user /]
		<div class="container">
			<div class="commit">
				<span class="state"></span>
					<span>
					<h2 class="header">Difference between commits</h2>
					<h5 class="subheader"><strong>${oldCommit.message}</strong> - ${oldCommit.author}</h5>
					<h5 class="subheader"><strong>${newCommit.message}</strong> - ${newCommit.author}</h5>
				</span>
			</div>
			<table class="table table-bordered">
				<tbody>
	[#list diffs as diff]
					<tr>
		[#if diff.isDeleted()]
						<td><strong>${diff.diffModel.oldPath}</strong></td>
		[#else]
						<td><strong>${diff.diffModel.newPath}</strong></td>
		[/#if]
					</tr>
		[#list diff.lines as line]
					<tr>
					[#if line.isRemoved()]
						<td class="commit failed">
					[#elseif line.isAdded()]
						<td class="commit succeeded">
					[#else]
						<td>
					[/#if]
						${line.lineNumber}. ${line.contents}</td>	
					</tr>
		[/#list]
	[/#list]
				</tbody>
			</table>
		</div>
[@macros.renderScripts /]
[@macros.renderFooter /]
