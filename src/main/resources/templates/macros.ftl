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
		<link rel="stylesheet" href="/static/octicons/octicons.css">
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
                    <a href="/courses">${i18n.translate("section.courses")}</a>
					<a href="/accounts">${i18n.translate("section.account")}</a>
					<a href="/logout">${i18n.translate("section.logout")}</a>
				</div>
			</div>
		</div>	
[/#macro]

[#macro renderFileTreeExplorer group commit repository path entries]
	<div class="diff box">
		<div class="header">
			<h5>[@macros.renderTreeBreadcrumb group commit repository path /]</h5>
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
							<i class="folder glyphicon glyphicon-folder-open"></i> <a href="/courses/${group.course.code}/groups/${group.groupNumber}/commits/${commit.commit}/tree/${path}/${entry}">${entry}</a>
					[#else]
							<i class="folder glyphicon glyphicon-folder-open"></i> <a href="/courses/${group.course.code}/groups/${group.groupNumber}/commits/${commit.commit}/tree/${entry}">${entry}</a>
					[/#if]
				[#elseif type = "TEXT"]
					[#if path?? && path?has_content]
							<i class="text glyphicon glyphicon-file"></i> <a href="/courses/${group.course.code}/groups/${group.groupNumber}/commits/${commit.commit}/blob/${path}/${entry}">${entry}</a>
					[#else]
							<i class="text glyphicon glyphicon-file"></i> <a href="/courses/${group.course.code}/groups/${group.groupNumber}/commits/${commit.commit}/blob/${entry}">${entry}</a>
					[/#if]
				[#else]
					[#if path?? && path?has_content]
							<i class="binary glyphicon glyphicon-save"></i> <a href="/courses/${group.course.code}/groups/${group.groupNumber}/commits/${commit.commit}/blob/${path}/${entry}">${entry}</a>
					[#else]
							<i class="binary glyphicon glyphicon-save"></i> <a href="/courses/${group.course.code}/groups/${group.groupNumber}/commits/${commit.commit}/blob/${entry}">${entry}</a>
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
[/#macro]

[#macro renderTreeBreadcrumb group commit repository path]
	[#assign pathParts=path?split("/")]
	<a href="/courses/${group.course.code}/groups/${group.groupNumber}/commits/${commit.commit}/tree">group-${group.groupNumber}</a> /
	[#list pathParts as pathPart]
		[#if pathPart_has_next]
					<a href="/courses/${group.course.code}/groups/${group.groupNumber}/commits/${commit.commit}/tree/[#list 0..pathPart_index as i]${pathParts[i]}[#if i_has_next]/[/#if][/#list]">${pathPart}</a> /
		[#elseif pathPart?has_content]
					${pathPart}
		[/#if]
	[/#list]
[/#macro]

[#macro renderCommitHeader i18n group commit currentView]
			<ol class="breadcrumb">

                <li><a href="/courses">${ i18n.translate("section.courses") }</a></li>
                <li><a href="/courses/${group.course.getCode()}">${group.course.getCode()} - ${group.course.getName()}</a></li>
				<li><a href="/courses/${group.course.code}/groups/${group.groupNumber}">Group ${group.getGroupNumber()}</a></li>
	[#if commit.getMessage()?length > 30 ]		
				<li class="active">${commit.getMessage()?substring(0,30)}...</li>
	[#else]
				<li class="active">${commit.getMessage()}</li>
	[/#if]
			</ol>

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
							<li><a href="/courses/${group.course.code}/groups/${group.groupNumber}/commits/${commit.commit}/diff">${i18n.translate("commit.view-diff")}</a></li>
							<li><a href="/courses/${group.course.code}/groups/${group.groupNumber}/commits/${commit.commit}/tree">${i18n.translate("commit.view-files")}</a></li>
	[#if states.hasFinished(commit.getCommit())]
							<li><a href="/courses/${group.course.code}/groups/${group.groupNumber}/commits/${commit.commit}/build">${i18n.translate("commit.view-build-log")}</a></li>
		[#if !states.hasSucceeded(commit.getCommit()) ]
							<li><a href="/courses/${group.course.code}/groups/${group.groupNumber}/commits/${commit.commit}/rebuild">${i18n.translate("commit.rebuild")}</a></li>
		[/#if]
	[#elseif !states.hasStarted(commit.getCommit()) ]
							<li><a href="/courses/${group.course.code}/groups/${group.groupNumber}/commits/${commit.commit}/rebuild">${i18n.translate("commit.rebuild")}</a></li>
	[/#if]
						</ul>
					</div>
				</span>
				<span class="headers">
					<h2 class="header">${commit.getTitle()}</h2>
					<h5 class="subheader">${commit.getAuthor()}</h5>
	[#if commit.getMessage()?has_content]
					<div class="description">${commit.getMessage()}</div>
	[/#if]
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