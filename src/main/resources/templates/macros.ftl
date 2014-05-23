[#macro renderHeader title]
<!DOCTYPE html>
<html>
	<head>
[#if title?? && title != ""]
		<title>DevHub - ${title}</title>
[#else]
		<title>DevHub</title>
[/#if]
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
		<link rel="stylesheet" href="/static/css/devhub.css">
		<!--[if lt IE 9]>
			<script src="https://oss.maxcdn.com/libs/html5shiv/3.7.0/html5shiv.js"></script>
			<script src="https://oss.maxcdn.com/libs/respond.js/1.3.0/respond.min.js"></script>
		<![endif]-->
	</head>
	<body>
[/#macro]

[#macro renderMenu i18n user]
		<div class="menu">
			<div class="container">
				<a href="/" class="logo-text"><img class="logo-image" src="/static/img/logo.png"> DEVHUB</a>
				<div class="pull-right">
[#if user?? && user.isAdmin()]
					<a href="/build-servers">${i18n.translate("section.build-servers")}</a>
[/#if]
					<a href="/projects">${i18n.translate("section.projects")}</a>
					<a href="/accounts">${i18n.translate("section.account")}</a>
					<a href="/logout">${i18n.translate("section.logout")}</a>
				</div>
			</div>
		</div>	
[/#macro]

[#macro renderTreeBreadcrumb group commit repository path]
	[#assign pathParts=path?split("/")]
	<a href="/projects/${group.course.code}/groups/${group.groupId}/commits/${commit.commit}/tree">${repository.getName()}</a>
	[#list pathParts as pathPart]
		[#if pathPart_has_next]
					/ <a href="/projects/${group.course.code}/groups/${group.groupId}/commits/${commit.commit}/tree/[#list 0..pathPart_index as i]${pathParts[i]}[#if i_has_next]/[/#if][/#list]">${pathPart}</a>
		[#elseif pathPart?has_content]
					/ ${pathPart}
		[/#if]
	[/#list]
[/#macro]

[#macro renderCommitHeader i18n group commit currentView]
	[#if states.hasStarted(commit.getCommit())]
		[#if states.hasFinished(commit.getCommit())]
			[#if states.hasSucceeded(commit.getCommit())]
			<div class="commit succeeded">
				<span class="state glyphicon glyphicon-ok-circle" title="Build succeeded!"></span>
			[#else]
			<div class="commit failed">
				<span class="state glyphicon glyphicon-remove-circle" title="Build failed!"></span>
			[/#if]
		[#else]
			<div class="commit running">
				<span class="state glyphicon glyphicon-align-justify" title="Build queued..."></span>
		[/#if]
	[#else]
			<div class="commit ignored">
				<span class="state glyphicon glyphicon-unchecked"></span>
	[/#if]
				<span class="view-picker">
					<div class="btn-group">
						<button type="button" class="btn btn-default">${currentView}</button>
						<button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown">
							<span class="caret"></span>
							<span class="sr-only">Toggle Dropdown</span>
						</button>
						<ul class="dropdown-menu" role="menu">
							<li><a href="/projects/${group.course.code}/groups/${group.groupId}/commits/${commit.commit}/diff">View diff</a></li>
							<li><a href="/projects/${group.course.code}/groups/${group.groupId}/commits/${commit.commit}/tree">List files</a></li>
	[#if states.hasFinished(commit.getCommit())]
							<li><a href="build">View build log</a></li>
	[/#if]
						</ul>
					</div>
				</span>
				<span class="headers">
	[#if commit.getMessage()?length > 50]
					<h2 class="header">${commit.getMessage()?substring(0, 50)}...</h2>
	[#else]
					<h2 class="header">${commit.getMessage()}</h2>
	[/#if]
					<h5 class="subheader">${commit.getAuthor()}</h5>
				</span>
			</div>
[/#macro]

[#macro renderScripts]
		<script src="/static/js/jquery.min.js"></script>
		<script src="/static/js/bootstrap.min.js"></script>
		<script src="/static/js/validation.js"></script>
[/#macro]

[#macro renderFooter]
	</body>
</html>
[/#macro]