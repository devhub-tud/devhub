[#macro renderHeader title]
<!DOCTYPE html>
<html ng-app="devhub">
	<head>
[#if title?? && title != ""]
		<title>DevHub - ${title}</title>
[#else]
		<title>DevHub</title>
[/#if]
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
		<link rel="stylesheet" href="/static/css/devhub.css">
		[#nested/]
		<!--[if lt IE 9]>
			<script src="https://oss.maxcdn.com/libs/html5shiv/3.7.0/html5shiv.js"></script>
			<script src="https://oss.maxcdn.com/libs/respond.js/1.3.0/respond.min.js"></script>
		<![endif]-->
	</head>
	<body>
[/#macro]

[#macro renderMenu i18n user]
		<nav class="navbar navbar-default navbar-static-top menu">
			<div class="container">
				<div class="navbar-header">
					<button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#bs-example-navbar-collapse-1">
						<span class="sr-only">Toggle navigation</span>
						<span class="icon-bar"></span>
						<span class="icon-bar"></span>
						<span class="icon-bar"></span>
					</button>
					<a class="navbar-brand logo-text" href="/">
						<img class="logo-image" src="/static/img/logo.png"> DEVHUB
					</a>
				</div>
				<div class="collapse navbar-collapse nav-collapse" id="bs-example-navbar-collapse-1">
					<ul class="nav navbar-nav navbar-right">
                        <li><a href="/notifications">${i18n.translate("section.notifications")}
							[#if user?? && notificationController??]
								[#assign unreadNotifications = notificationController.getNumberOfUnreadNotificationsFor(user)]
								[#if unreadNotifications > 0]
                                	<span class="label label-danger">${unreadNotifications}</span>
								[/#if]
							[/#if]</a></li>
[#if user?? && user.isAdmin()]
				 		<li><a href="/build-servers">${i18n.translate("section.build-servers")}</a></li>
[/#if]
						<li><a href="/courses">${i18n.translate("section.courses")}</a></li>
						<li><a href="/projects">${i18n.translate("section.projects")}</a></li>
						<li><a href="/accounts">${i18n.translate("section.account")}</a></li>
						<li><a href="/logout">${i18n.translate("section.logout")}</a></li>
					</ul>
				</div>
			</div>
		</nav>
[/#macro]

[#macro renderFileTreeExplorer group commit repository path entries]
	<div class="diff box">
		<div class="header">
			<h5>[@macros.renderTreeBreadcrumb group![] commit repository path /]</h5>
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
							<i class="folder glyphicon glyphicon-folder-open"></i> <a href="${repositoryEntity.getURI()}commits/${commit.commit}/tree/${path}/${entry}">${entry}</a>
					[#else]
							<i class="folder glyphicon glyphicon-folder-open"></i> <a href="${repositoryEntity.getURI()}commits/${commit.commit}/tree/${entry}">${entry}</a>
					[/#if]
				[#elseif type = "TEXT"]
					[#if path?? && path?has_content]
							<i class="text glyphicon glyphicon-file"></i> <a href="${repositoryEntity.getURI()}commits/${commit.commit}/blob/${path}/${entry}">${entry}</a>
					[#else]
							<i class="text glyphicon glyphicon-file"></i> <a href="${repositoryEntity.getURI()}commits/${commit.commit}/blob/${entry}">${entry}</a>
					[/#if]
				[#else]
					[#if path?? && path?has_content]
							<i class="binary glyphicon glyphicon-save"></i> <a href="${repositoryEntity.getURI()}commits/${commit.commit}/raw/${path}/${entry}">${entry}</a>
					[#else]
							<i class="binary glyphicon glyphicon-save"></i> <a href="${repositoryEntity.getURI()}commits/${commit.commit}/raw/${entry}">${entry}</a>
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
	<a href="${repositoryEntity.getURI()}commits/${commit.commit}/tree">${repositoryEntity.getRepositoryName()}</a> /
	[#list pathParts as pathPart]
		[#if pathPart_has_next]
					<a href="${repositoryEntity.getURI()}commits/${commit.commit}/tree/[#list 0..pathPart_index as i]${pathParts[i]}[#if i_has_next]/[/#if][/#list]">${pathPart}</a> /
		[#elseif pathPart?has_content]
					${pathPart}
		[/#if]
	[/#list]
[/#macro]

[#macro renderCommitHeader i18n group repositoryEntity commit currentView]
	[#if repositoryEntity?? && repositoryEntity?has_content]
			<ol class="breadcrumb hidden-xs">
				[#if group?? && group?has_content]
					<li><a href="/courses">${ i18n.translate("section.courses") }</a></li>
					<li><a href="${group.course.course.getURI()}">${group.course.course.code} - ${group.course.course.name}</a></li>
					<li><a href="${group.course.getURI()}">${group.course.timeSpan.start?string["yyyy"]}[#if group.course.timeSpan.end??] - ${group.course.timeSpan.end?string["yyyy"]}[/#if]</a></li>
					<li><a href="${group.getURI()}">Group ${group.getGroupNumber()}</a></li>
				[#else]
					<li><a href="/projects">${ i18n.translate("section.projects") }</a></li>
					<li><a href="${repositoryEntity.getURI()}">${repositoryEntity.getTitle()}</a></li>
				[/#if]
	[#if commit.getMessage()?length > 30 ]		
				<li class="active">${commit.getMessage()?substring(0,30)}...</li>
	[#else]
				<li class="active">${commit.getMessage()}</li>
	[/#if]
			</ol>
	[/#if]

	[#if buildResult?? && buildResult?has_content]
		[#if buildResult.hasFinished()]
			[#if buildResult.hasSucceeded()]
			<div class="commit succeeded">
				<span class="state glyphicon glyphicon-ok-circle" title="${i18n.translate("build.state.succeeded")}"></span>
			[#else]
			<div class="commit failed">
				<span class="state glyphicon glyphicon-remove-circle" title="${i18n.translate("build.state.failed")}"></span>
			[/#if]
		[#else]
			<div class="commit running">
				<span class="state glyphicon glyphicon-align-justify" title="${i18n.translate("build.state.queued")}"></span>
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
							<li><a href="${repositoryEntity.getURI()}commits/${commit.commit}/diff">${i18n.translate("commit.view-diff")}</a></li>
							<li><a href="${repositoryEntity.getURI()}commits/${commit.commit}/tree">${i18n.translate("commit.view-files")}</a></li>
	[#if buildResult?? && buildResult?has_content]
		[#if buildResult.hasFinished()]
							<li><a href="${repositoryEntity.getURI()}commits/${commit.commit}/build">${i18n.translate("commit.view-build-log")}</a></li>
			[#if !buildResult.hasSucceeded()]
							<li><a href="${repositoryEntity.getURI()}commits/${commit.commit}/rebuild">${i18n.translate("commit.rebuild")}</a></li>
			[/#if]
		[/#if]
	[#else]
							<li><a href="${repositoryEntity.getURI()}commits/${commit.commit}/rebuild">${i18n.translate("commit.rebuild")}</a></li>
	[/#if]
						</ul>
					</div>
				</span>
				<span class="headers">
					<h2 class="header">${commit.getMessage()}</h2>
					<h5 class="subheader">${commit.getAuthor()}</h5>
				[#if diffViewModel?? && diffViewModel?has_content && diffViewModel.linesAdded?? && diffViewModel.linesAdded?has_content]
					<span class="addedlines">${"+" + diffViewModel.getLinesAdded()}</span>
					<span class="neutrallines">${"/"}</span>
					<span class="removedlines">${"-" + diffViewModel.getLinesRemoved()}</span>
				[/#if]
	[#if commit.getMessage()?has_content]
					<div class="description">${commit.getMessageTail()}</div>
	[/#if]
					<div>
  	[#if warnings?? && warnings?has_content]
						<ul class="list-unstyled">
  	    [#list warnings as warning]
							<li class="alert alert-warning alert-dismissible" role="alert">
								<button type="button" class="close" data-dismiss="alert" aria-label="Close"><span aria-hidden="true">&times;</span></button>
								<i class="glyphicon glyphicon-warning-sign"></i> ${warning.getMessage(i18n)}
							</li>
  	    [/#list]
						</ul>
  	[/#if]
					</div>
				</span>
			</div>
[/#macro]

[#macro renderLabel label]
	<div class="label label-default" style="background-color: ${label.getColorAsHexString()}">${label.tag}</div>
[/#macro]

[#macro renderScripts]
	<script src="/static/vendor/jquery/jquery.min.js"></script>
	<script src="/static/vendor/bootstrap/js/bootstrap.min.js"></script>
	<script src="/static/vendor/twemoji/twemoji.min.js"></script>
	<script src="/static/vendor/jquery-textcomplete/dist/jquery.textcomplete.js"></script>
	<script src="/static/js/validation.js"></script>
    <script src="/static/js/main.js"></script>
	[#nested/]
[/#macro]

[#macro renderFooter]
	</body>
</html>
[/#macro]