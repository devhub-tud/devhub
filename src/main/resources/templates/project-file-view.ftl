[#import "macros.ftl" as macros]
[@macros.renderHeader i18n.translate("section.projects") /]
[@macros.renderMenu i18n user /]
		<div class="container">
			<div class="commit">
				<span class="state"></span>
					<span>
					<h2 class="header">
<a href="${uri}/tree/${commit}">${repository.getName()}</a> / 
[#assign pathParts=path?split("/")]
[#list pathParts as pathPart]
	[#if pathPart_has_next]
				<a href="${uri}/tree/${commit}/[#list 0..pathPart_index as i]${pathParts[i]}[#if i_has_next]/[/#if][/#list]">${pathPart}</a> /
	[#else]
				${pathPart}
	[/#if]
[/#list]
					</h2>
					<h5 class="subheader">${commit}</h5>
				</span>
			</div>
			
	
	[#if contents??]
			<div class="table-code">
				<div class="line-numbers pull-left">
		[#list contents as line]
					${line_index + 1}<br/>
		[/#list]
				</div>
				<div class="code hljs">
		[#list contents as line]
			[#if line_has_next]
${line}<br/>
			[#else]
${line}
			[/#if]
		[/#list]
				</div>
			</div>
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

[#if highlight]
	<script src="/static/js/highlight.pack.js"></script>
	<script>
		$(document).ready(function() {
			hljs.configure({
				tabReplace: '&nbsp;&nbsp;&nbsp;&nbsp;',
				useBR: true
			});
			
			$('.code').each(function(i, e) {
				hljs.highlightBlock(e)
			});
		});
	</script>
[/#if]

[@macros.renderFooter /]
