[#import "macros.ftl" as macros]
[@macros.renderHeader i18n.translate("section.projects") /]
[@macros.renderMenu i18n user /]
		<div class="container">
			<h2>
				${i18n.translate("block.my-projects.title")}
				<a href="/projects/setup" class="btn btn-success btn-sm pull-right">
					<i class="glyphicon glyphicon-plus-sign"></i> ${i18n.translate("block.my-projects.buttons.setup-new-project.caption")}
				</a>
			</h2>
			<table class="table table-bordered">
				<tbody>
[#assign groups=user.listGroups()]
[#if groups?has_content]
	[#list groups as group]
					<tr>
						<td>
							<a href="/projects/${group.course.code}/groups/${group.groupNumber?c}">${group.getGroupName()}</a>
						</td>
					</tr>
	[/#list]
[#else]
					<tr>
						<td class="muted">
							${i18n.translate("block.my-projects.empty-list")}
						</td>
					</tr>
[/#if]
				</tbody>
			</table>
[#assign groups=user.listAssistedGroups()]
[#if groups?has_content]
			<h2>
				${i18n.translate("block.assisting-projects.title")}
			</h2>
			<table class="table table-bordered">
				<tbody>
	[#list groups as group]
					<tr>
						<td>
							<a href="/projects/${group.course.code}/groups/${group.groupNumber?c}">${group.getGroupName()}</a>
						</td>
					</tr>
	[/#list]
				</tbody>
			</table>
[/#if]
		</div>
[@macros.renderScripts /]
[@macros.renderFooter /]
