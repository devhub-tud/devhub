[#import "macros.ftl" as macros]
[@macros.renderHeader i18n.translate("section.projects") /]
[@macros.renderMenu i18n user /]
		<div class="container">
			<div class="commit">
				<span class="state"></span>
					<span>
	[#if oldCommit??]
					<h2 class="header">${i18n.translate("diff.changes.commits")}</h2>
					<h5 class="subheader"><strong>${oldCommit.message}</strong> - ${oldCommit.author}</h5>
	[#else]
					<h2 class="header">${i18n.translate("diff.changes.commit")}</h2>
	[/#if]
					<h5 class="subheader"><strong>${newCommit.message}</strong> - ${newCommit.author}</h5>
				</span>
			</div>
			
	
	[#if diffs?has_content]
		[#list diffs as diff]
			[#if diff.isDeleted()]
							<h3>${diff.diffModel.oldPath}</h3>
			[#else]
							<h3>${diff.diffModel.newPath}</h3>
			[/#if]
			
				<table class="table table-bordered">
					<tbody>
			[#if  diff.lines ?has_content]
			[#list diff.lines as line]
						<tr>
						<td class="diff ln">${line.newLineNumber}</td>
						<td class="diff ln">${line.oldLineNumber}</td>
						
						[#if line.isRemoved()]
							<td class="diff add">${line.contents}</td>
						[#elseif line.isAdded()]
							<td class="diff delete">${line.contents}</td>
						[#else]
							<td class="diff">${line.contents}</td>
						[/#if]
						</tr>
			[/#list]
			[#else]
						<tr>
							<td>${i18n.translate("diff.changes.nothing")}</td>
						</tr>
			[/#if]
					</tbody>
				</table>
		[/#list]
	[#else]
			<table class="table table-bordered">
				<tbody>
					<tr>
						<td>${i18n.translate("diff.changes.nothing")}</td>
					</tr>
				</tbody>
			</table>
	[/#if]
		</div>
[@macros.renderScripts /]
[@macros.renderFooter /]
