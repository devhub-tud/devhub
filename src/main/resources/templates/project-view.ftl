[#import "macros.ftl" as macros]
[@macros.renderHeader i18n.translate("section.projects") /]
[@macros.renderMenu i18n user /]
		<div class="container">
			<h4>Git clone URL</h4>
			<div class="well well-sm">
[#if repository?? && repository?has_content]
				<code>git clone ${repository.getUrl()} ${group.getCourse().getCode()?lower_case}</code>
[#else]
				<code>Could not connect to the Git server!</code>
[/#if]
			</div>
			<h4>Recent commits</h4>
			<table class="table table-bordered">
				<tbody>
[#if repository?? && repository?has_content]
	[#if repository.getRecentCommits()?has_content]
		[#list repository.getRecentCommits() as commit]
					<tr>
						<td>
			[#if states.hasFinished(commit.getCommit())]
				[#if states.hasSucceeded(commit.getCommit())]
							<i style="color: #00aa00;" class="glyphicon glyphicon-ok icon"></i>
				[#else]
							<i style="color: #aa0000;" class="glyphicon glyphicon-remove icon"></i>
				[/#if]
			[#else]
							<i class="icon"> </i>
			[/#if]
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
[#else]
						<tr>
							<td class="muted">
								Could not connect to the Git server!
							</td>
						</tr>
[/#if]
				</tbody>
			</table>
		</div>
[@macros.renderScripts /]
[@macros.renderFooter /]
