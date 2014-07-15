[#macro listTags repository commitId]
	[#list repository.getTags() as tag]
		[#if tag.getCommit() == commitId]
			[#if tag.getName()?starts_with("refs/tags/")]
<span class="label label-primary">${tag.getName()?substring("refs/tags/"?length)}</span>
			[#else]
<span class="label label-primary">${tag.getName()}</span>
			[/#if]
		[/#if]
	[/#list]
[/#macro]

[#import "macros.ftl" as macros]
[@macros.renderHeader i18n.translate("section.projects") /]
[@macros.renderMenu i18n user /]
		<div class="container">
			<h2>${group.getGroupName()}</h2>
			<h4>Git clone URL</h4>
			<div class="well well-sm">
[#if repository?? && repository?has_content]
				<code>${repository.getUrl()}</code>
[#else]
				<code>Could not connect to the Git server!</code>
[/#if]
			</div>


[#if repository?? && repository?has_content && branch?? && branch?has_content]
	<span class="pull-right">
		<div class="btn-group">
			<button type="button" class="btn btn-default">
				<span class="octicon octicon-git-branch"></span>
				<span class="text-muted">${i18n.translate("branch.current")}:</span>
				${branch.getName()}
			</button>
			<button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown">
				<span class="caret"></span>
				<span class="sr-only">${i18n.translate("branch.switch")}</span>
			</button>
			<ul class="dropdown-menu" role="menu">
	[#list repository.getBranches() as b ]
				<li><a href="/projects/${group.course.code}/groups/${group.groupNumber}/branch/${b.getSimpleName()}">${b.getSimpleName()}</a></li>
	[/#list]
			</ul>
		</div>
	</span>
[/#if]
		
			<h4>Recent commits</h4>
			
			<table class="table table-bordered">
				<tbody>
[#if repository?? && repository?has_content]
	[#if branch?? && branch?has_content && branch.getCommits()?has_content]
		[#list branch.getCommits() as commit]
					<tr>
			[#if states.hasStarted(commit.getCommit())]
				[#if states.hasFinished(commit.getCommit())]
					[#if states.hasSucceeded(commit.getCommit())]
						<td class="commit succeeded">
							<a href="/projects/${group.course.code}/groups/${group.groupNumber}/commits/${commit.getCommit()}/build">
								<span class="state glyphicon glyphicon-ok-circle" title="Build succeeded!"></span>
							</a>
							<a href="/projects/${group.course.code}/groups/${group.groupNumber}/commits/${commit.getCommit()}/diff">
								<div class="comment">${commit.getMessage()} [@listTags repository commit.getCommit() /]</div>
								<div class="committer">${commit.getAuthor()}</div>
							</a>
						</td>
					[#else]
						<td class="commit failed">
							<a href="/projects/${group.course.code}/groups/${group.groupNumber}/commits/${commit.getCommit()}/build">
								<span class="state glyphicon glyphicon-remove-circle" title="Build failed!"></span>
							</a>
							<a href="/projects/${group.course.code}/groups/${group.groupNumber}/commits/${commit.getCommit()}/diff">
								<div class="comment">${commit.getMessage()} [@listTags repository commit.getCommit() /]</div>
								<div class="committer">${commit.getAuthor()}</div>
							</a>
						</td>
					[/#if]
				[#else]
						<td class="commit running">
							<span class="state glyphicon glyphicon-align-justify" title="Build queued..."></span>
							<a href="/projects/${group.course.code}/groups/${group.groupNumber}/commits/${commit.getCommit()}/diff">
								<div class="comment">${commit.getMessage()} [@listTags repository commit.getCommit() /]</div>
								<div class="committer">${commit.getAuthor()}</div>
							</a>
						</td>
				[/#if]
			[#else]
						<td class="commit ignored">
							<span class="state glyphicon glyphicon-unchecked"></span>
							<a href="/projects/${group.course.code}/groups/${group.groupNumber}/commits/${commit.getCommit()}/diff">
								<div class="comment">${commit.getMessage()} [@listTags repository commit.getCommit() /]</div>
								<div class="committer">${commit.getAuthor()}</div>
							</a>
						</td>
			[/#if]
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


[#if branch?? && branch?has_content]
[#assign pageCount = branch.getPagination().getPageCount() ]
[#assign currentPage = branch.getPagination().getPageIndex() ]
			<div class="text-center">
				<ul class="pagination pagination-lg">
	[#list 1..pageCount as pageNumber ]
		[#if pageNumber_index == currentPage ]
					<li class="active"><a href="/projects/${group.course.code}/groups/${group.groupNumber}/branch/${branch.getSimpleName()}?page=${pageNumber}">${pageNumber}</a></li>
		[#else]
					<li><a href="/projects/${group.course.code}/groups/${group.groupNumber}/branch/${branch.getSimpleName()}?page=${pageNumber}">${pageNumber}</a></li>
		[/#if]
	[/#list]
				</ul>
			</div>
[/#if]

		</div>
[@macros.renderScripts /]
[@macros.renderFooter /]
