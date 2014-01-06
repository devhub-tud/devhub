[#macro renderHeader title]
<!DOCTYPE html>
<html>
	<head>
		<title>DevHub - ${title}</title>
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
		<link rel="stylesheet" href="/static/css/devhub.css">
		<!--[if lt IE 9]>
			<script src="https://oss.maxcdn.com/libs/html5shiv/3.7.0/html5shiv.js"></script>
			<script src="https://oss.maxcdn.com/libs/respond.js/1.3.0/respond.min.js"></script>
		<![endif]-->
	</head>
	<body>
[/#macro]

[#macro renderProjectMenu user group]
		<div class="menu">
			<ul class="menu-list">
				<li class="menu-item">
					<a href="/projects/${group.project.code}/groups/${group.groupNumber}/dashboard" data-toggle="tooltip" data-placement="right" title data-original-title="Dashboard">
						<i class="glyphicon glyphicon-dashboard"></i>
					</a>
				</li>
				<li class="menu-item">
					<a href="/projects/${group.project.code}/groups/${group.groupNumber}/issues" data-toggle="tooltip" data-placement="right" title data-original-title="Issues">
						<i class="glyphicon glyphicon-check"></i>
					</a>
				</li>
				<li class="menu-item">
					<a href="/projects/${group.project.code}/groups/${group.groupNumber}/pull-requests" data-toggle="tooltip" data-placement="right" title data-original-title="Pull-requests">
						<i class="glyphicon glyphicon-random"></i>
					</a>
				</li>
				<li class="menu-item">
					<a href="/projects/${group.project.code}/groups/${group.groupNumber}/deliverables" data-toggle="tooltip" data-placement="right" title data-original-title="Deliverables">
						<i class="glyphicon glyphicon-compressed"></i>
					</a>
				</li>
			</ul>
			<ul class="menu-list bottom">
				<li class="menu-item">
					<a href="/accounts/${user.studentNumber}" data-toggle="tooltip" data-placement="right" title data-original-title="Profile">
						<i class="glyphicon glyphicon-user"></i>
					</a>
				</li>
				<li class="menu-item">
					<a href="/accounts/logout" data-toggle="tooltip" data-placement="right" title data-original-title="Logout">
						<i class="glyphicon glyphicon-log-out"></i>
					</a>
				</li>
			</ul>
		</div>
[/#macro]

[#macro renderScripts]
		<script src="/static/js/jquery.min.js"></script>
		<script src="/static/js/bootstrap.min.js"></script>
		<script src="/static/js/menu.js"></script>
[/#macro]

[#macro renderFooter]
	</body>
</html>
[/#macro]