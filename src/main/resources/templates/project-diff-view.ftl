[#import "macros.ftl" as macros]
[@macros.renderHeader i18n.translate("section.projects") /]
[@macros.renderMenu i18n user /]
		<div class="container">
			<div class="commit">
				<span class="state"></span>
					<span>
					<h2 class="header">Difference for: ${oldCommit.getMessage()} - ${newCommit.getMessage()}</h2>
					<h5 class="subheader">${oldCommit.getAuthor()} - ${newCommit.getAuthor()}</h5>
				</span>
			</div>
			<table class="table table-bordered">
				<tbody>
	[#list diffs as diff]
					<tr>
						<td><strong>${diff.newPath} - ${diff.oldPath}</strong></td>
					</tr>
		[#list DiffLine.getLinesFor(diff) as line]
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
