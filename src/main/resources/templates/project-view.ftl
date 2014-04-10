[#import "macros.ftl" as macros]
[@macros.renderHeader i18n.translate("section.projects") /]
[@macros.renderMenu i18n user /]
		<div class="container">
			<h4>Git clone URL</h4>
			<div class="well well-sm">
				<code>git clone ${repository.getUrl()} ${group.getCourse().getCode()?lower_case}</code>
			</div>
			<h4>Recent commits</h4>
			<table class="table table-bordered">
				<tbody>
[#if repository?? && repository?has_content]
	[#list repository.getRecentCommits() as commit]
					<tr>
						<td>
							${commit.getMessage()} 
							<span class="pull-right" style="color: #bbb !important;">${commit.getAuthor()}</span>
						</td>
					</tr>
	[/#list]
[#else]
					<tr>
						<td class="muted">
							There are no commits in this repository yet!
						</td>
					</tr>
[/#if]
				</tbody>
			</table>
		</div>
[@macros.renderScripts /]
[@macros.renderFooter /]
