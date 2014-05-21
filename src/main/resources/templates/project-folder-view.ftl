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
		[#if entries?? && entries?has_content]
			<div class="scrollable">
				<table class="table files">
					<tbody>
			[#list entries?keys as entry]
			[#assign type=entries[entry]]
						<tr>
							<td>
				[#if type = "FOLDER"]
					[#if path?? && path?has_content]
							<i class="glyphicon glyphicon-folder-open"></i> <a href="${uri}/tree/${path}/${entry}">${entry}</a>
					[#else]
							<i class="glyphicon glyphicon-folder-open"></i> <a href="${uri}/tree/${entry}">${entry}</a>
					[/#if]
				[#else]
					[#if path?? && path?has_content]
							<i class="glyphicon glyphicon-file"></i> <a href="${uri}/blob/${path}/${entry}">${entry}</a>
					[#else]
							<i class="glyphicon glyphicon-file"></i> <a href="${uri}/blob/${entry}">${entry}</a>
					[/#if]
				[/#if]
							</td>
						</tr>
			[/#list]
					</tbody>
			</table>
			</div>
		[#else]
			<div>${i18n.translate("diff.changes.nothing")}</div>
		[/#if]
	</div>
[@macros.renderScripts /]
[@macros.renderFooter /]
