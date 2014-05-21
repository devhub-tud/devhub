[#import "macros.ftl" as macros]
[@macros.renderHeader i18n.translate("section.projects") /]
[@macros.renderMenu i18n user /]
		<div class="container">
[@macros.renderCommitHeader i18n commit "List files" /]
			<div class="diff box">
				<div class="header">
					<button class="pull-right btn btn-sm btn-default folder"><i class="glyphicon glyphicon-chevron-up"></i> Fold</button>
					<button class="pull-right btn btn-sm btn-default unfolder" style="display: none;"><i class="glyphicon glyphicon-chevron-down"></i> Unfold</button>
					<h5>
						[#assign pathParts=path?split("/")]
						<a href="${uri}/tree">${repository.getName()}</a>
						[#list pathParts as pathPart]
							[#if pathPart_has_next]
										/ <a href="${uri}/tree/[#list 0..pathPart_index as i]${pathParts[i]}[#if i_has_next]/[/#if][/#list]">${pathPart}</a>
							[#elseif pathPart?has_content]
										/ ${pathPart}
							[/#if]
						[/#list]
					</h5>
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
	<script>
		$(document).ready(function() {
			hljs.configure({
				tabReplace: '&nbsp;&nbsp;&nbsp;&nbsp;',
				useBR: true
			});
			
			$('.code').each(function(i, e) {
				hljs.highlightBlock(e);
			});
		});
	</script>
[/#if]
[@macros.renderFooter /]
