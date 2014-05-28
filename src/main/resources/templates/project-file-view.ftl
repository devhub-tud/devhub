[#import "macros.ftl" as macros]
[@macros.renderHeader i18n.translate("section.projects") /]
[@macros.renderMenu i18n user /]
		<div class="container">
[@macros.renderCommitHeader i18n group commit "List files" /]
			<div class="diff box">
				<div class="header">
					<h5>[@macros.renderTreeBreadcrumb group commit repository path /]</h5>
				</div>
			[#if contents?? && contents?has_content]
				<div class="scrollable">
					<table class="table diffs">
						<tbody>
				[#list contents as line]
							<tr>
								<td class="ln">${line_index + 1}</td>
								<td class="code"><pre>${line}</pre></td>
							</tr>
				[/#list]
						</tbody>
					</table>
				</div>
			[/#if]
			</div>
		</div>
[@macros.renderScripts /]
[#if highlight]
	<script src="/static/js/highlight.pack.js"></script>
[/#if]
	<script>
		$(document).ready(function() {
[#if highlight]
			hljs.configure({
				tabReplace: '	',
				useBR: true
			});
			
			$('.code').each(function(i, e) {
				hljs.highlightBlock(e);
			});
[/#if]
		});
	</script>
[@macros.renderFooter /]
