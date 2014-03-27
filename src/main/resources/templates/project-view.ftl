[#import "macros.ftl" as macros]
[@macros.renderHeader "Project commits" /]
[@macros.renderMenu /]
		<div class="container">
			<h4>Git clone URL</h4>
			<div class="well well-sm"><code>git clone ${repository.getUrl()} ${group.getCourse().getCode()}</code></div>
			<h4>Branches</h4>
			<table class="table table-bordered">
				<tbody>
[#if repository?? && repository?has_content]
	[#list repository.getBranches() as branch]
					<tr>
						<td>
							${branch.getName()}
						</td>
					</tr>
	[/#list]
[#else]
					<tr>
						<td class="muted">
							There are no branches in this repository yet!
						</td>
					</tr>
[/#if]
				</tbody>
			</table>
			<h4>Recent commits</h4>
			<table class="table table-bordered">
				<tbody>
[#if repository?? && repository?has_content]
	[#list repository.getRecentCommits() as commit]
					<tr>
						<td>
							<i style="color: #00aa00;" class="glyphicon glyphicon-ok-sign"></i>
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
