[#import "macros.ftl" as macros]
[@macros.renderHeader i18n.translate("section.projects") /]
[@macros.renderMenu i18n /]
		<div class="container">
			<h4>
				${i18n.translate("block.current-projects.title")}
				<a href="/projects/setup" class="btn btn-success btn-sm pull-right">
					<i class="glyphicon glyphicon-plus-sign"></i> ${i18n.translate("block.current-projects.buttons.setup-new-project.caption")}
				</a>
			</h4>
			<table class="table table-bordered">
				<tbody>
[#if groups?? && groups?has_content]
	[#list groups as group]
					<tr>
						<td>
							<a href="/projects/${group.course.code}/groups/${group.groupNumber?c}">${group.course.code} - ${group.course.name} (Group #${group.groupNumber?c})</a>
						</td>
					</tr>
	[/#list]
[#else]
					<tr>
						<td class="muted">
							${i18n.translate("block.current-projects.empty-list")}
						</td>
					</tr>
[/#if]
				</tbody>
			</table>
		</div>
[@macros.renderScripts /]
[@macros.renderFooter /]
