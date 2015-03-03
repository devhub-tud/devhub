[#macro listTags repository commitId]
	[#list repository.getTags() as tag]
		[#if tag.getCommit().getCommit() == commitId]
<span class="label label-primary">${tag.getSimpleName()}</span>
		[/#if]
	[/#list]
[/#macro]

[#import "macros.ftl" as macros]
[@macros.renderHeader i18n.translate("section.projects") /]
[@macros.renderMenu i18n user /]
		<div class="container">
		
			<ol class="breadcrumb">
                <li><a href="/courses">${ i18n.translate("section.courses") }</a></li>
                <li><a href="/courses/${group.course.getCode()}">${group.course.getCode()} - ${group.course.getName()}</a></li>
				<li class="active">Group ${group.getGroupNumber()}</li>
			</ol>

			<h4>Git clone URL</h4>
			<div class="well well-sm">
[#if repository?? && repository?has_content]
				<code>git clone ${repository.getUrl()}</code>
[#else]
				<code>Could not connect to the Git server!</code>
[/#if]
			</div>

[#if repository?? && repository?has_content && branch?? && branch?has_content]
	<span class="pull-right" style="margin-bottom: 20px;">
		<div class="btn-group">
			<button type="button" class="btn btn-default">
				<span class="octicon octicon-git-branch"></span>
				<span class="text-muted">${i18n.translate("branch.current")}:</span>
				${branch.getSimpleName()}
[#if branch.behind?? && branch.ahead?? && branch.behind > 0 || branch.ahead > 0 ]
				<span class="text-success octicon octicon-arrow-up"></span>
				<span class="text-muted">${branch.getAhead()}</span>
				<span class="text-danger octicon octicon-arrow-down"></span>
				<span class="text-muted">${branch.getBehind()}</span>
[/#if]
			</button>
			<button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown">
				<span class="caret"></span>
				<span class="sr-only">${i18n.translate("branch.switch")}</span>
			</button>
			<ul class="dropdown-menu" role="menu">
	[#list repository.getBranches() as b ]
				<li><a href="/courses/${group.course.code}/groups/${group.groupNumber}/branch/${b.getSimpleName()}" style="text-align:right;">
				${b.getSimpleName()}
[#if b.behind?? && b.ahead?? && b.behind > 0 || b.ahead > 0 ]
				<span class="text-success octicon octicon-arrow-up"></span>
				<span class="text-muted">${b.getAhead()}</span>
				<span class="text-danger octicon octicon-arrow-down"></span>
				<span class="text-muted">${b.getBehind()}</span>
[/#if]
				</a></li>
	[/#list]
			</ul>
		</div>
	</span>
[/#if]

[#if branch?? && branch.ahead?? && branch.ahead > 0 ]
	<div class="alert alert-success" role="alert" style="clear:both; line-height: 34px;">
	[#if pullRequest??]
		<span>Hey! There is an open pull request for this branch. Want to go to the pull request?</span>
		<a href="/courses/${group.course.code}/groups/${group.groupNumber}/pull/${pullRequest.getIssueId()}" class="btn btn-default pull-right">Go to Pull Request</a>
	[#else]
		<form method="POST" action="/courses/${group.course.code}/groups/${group.groupNumber}/pull" target="_self">
			<span>Hey! It seems you're branch is ahead of the master. Want to merge?</span>
			<input type="hidden" name="branchName" value="${branch.getName()}"/>
			<button type="submit" class="btn btn-default pull-right">Create Pull Request</button>
		</form>
	[/#if]
	</div>
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
						<td class="commit succeeded" id="${commit.getCommit()}">
							<a href="/courses/${group.course.code}/groups/${group.groupNumber}/commits/${commit.getCommit()}/build">
								<span class="state glyphicon glyphicon-ok-circle" title="Build succeeded!"></span>
							</a>
					[#else]
						<td class="commit failed" id="${commit.getCommit()}">
							<a href="/courses/${group.course.code}/groups/${group.groupNumber}/commits/${commit.getCommit()}/build">
								<span class="state glyphicon glyphicon-remove-circle" title="Build failed!"></span>
							</a>
					[/#if]
				[#else]
						<td class="commit running" id="${commit.getCommit()}">
							<span class="state glyphicon glyphicon-align-justify" title="Build queued..."></span>
				[/#if]
			[#else]
						<td class="commit ignored" id="${commit.getCommit()}">
							<span class="state glyphicon glyphicon-unchecked"></span>
			[/#if]
							<a href="/courses/${group.course.code}/groups/${group.groupNumber}/commits/${commit.getCommit()}/diff">
								<div class="comment">${commit.getMessage()} [@listTags repository commit.getCommit() /]</div>
								<div class="committer">${commit.getAuthor()}</div>
								<div class="timestamp" data-value="${(commit.getTime() * 1000)?c}">on ${(commit.getTime() * 1000)?number_to_datetime?string["EEEE dd MMMM yyyy HH:mm"]}</div>
							</a>
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


[#function max x y]
    [#if (x<y)][#return y][#else][#return x][/#if]
[/#function]

[#function min x y]
    [#if (x<y)][#return x][#else][#return y][/#if]
[/#function]

[#if branch?? && branch?has_content && pagination?? ]
[#assign pageCount = pagination.getPageCount() ]
[#assign currentPage = pagination.getPage() ]
			<div class="text-center">
				<ul class="pagination pagination-lg">
	[#list max(1, currentPage-4)..min(pageCount, currentPage+4) as pageNumber ]
		[#if pageNumber == currentPage ]
					<li class="active"><a href="/courses/${group.course.code}/groups/${group.groupNumber}/branch/${branch.getSimpleName()}?page=${pageNumber}">${pageNumber}</a></li>
		[#else]
					<li><a href="/courses/${group.course.code}/groups/${group.groupNumber}/branch/${branch.getSimpleName()}?page=${pageNumber}">${pageNumber}</a></li>
		[/#if]
	[/#list]
				</ul>
			</div>
[/#if]

		</div>
[@macros.renderScripts /]
[@macros.renderFooter /]
