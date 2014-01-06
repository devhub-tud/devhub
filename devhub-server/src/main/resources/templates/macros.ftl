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
[#if displayMenu?has_content && displayMenu?matches("project")]
			<ul class="menu-list">
				<li class="menu-item">
					<a href="/projects/${group.project.code}/groups/${group.groupNumber?c}/dashboard" data-toggle="tooltip" data-placement="right" title data-original-title="Dashboard">
						<i class="glyphicon glyphicon-dashboard"></i>
					</a>
				</li>
				<li class="menu-item">
					<a href="/projects/${group.project.code}/groups/${group.groupNumber?c}/issues" data-toggle="tooltip" data-placement="right" title data-original-title="Issues">
						<i class="glyphicon glyphicon-check"></i>
					</a>
				</li>
				<li class="menu-item">
					<a href="/projects/${group.project.code}/groups/${group.groupNumber?c}/pull-requests" data-toggle="tooltip" data-placement="right" title data-original-title="Pull-requests">
						<i class="glyphicon glyphicon-random"></i>
					</a>
				</li>
				<li class="menu-item">
					<a href="/projects/${group.project.code}/groups/${group.groupNumber?c}/deliverables" data-toggle="tooltip" data-placement="right" title data-original-title="Deliverables">
						<i class="glyphicon glyphicon-compressed"></i>
					</a>
				</li>
			</ul>
[#elseif displayMenu?has_content && displayMenu?matches("account")]
			<ul class="menu-list">
				<li class="menu-item">
					<a href="/accounts/${user.studentNumber?c}" data-toggle="tooltip" data-placement="right" title data-original-title="Overview">
						<i class="glyphicon glyphicon-dashboard"></i>
					</a>
				</li>
				<li class="menu-item">
					<a href="/accounts/${user.studentNumber?c}/ssh-keys" data-toggle="tooltip" data-placement="right" title data-original-title="SSH Keys">
						<i class="glyphicon glyphicon-qrcode"></i>
					</a>
				</li>
			</ul>
[/#if]
			<ul class="menu-list bottom">
				<li class="menu-item">
					<a href="/" data-toggle="tooltip" data-placement="right" title data-original-title="Home">
						<i class="glyphicon glyphicon-home"></i>
					</a>
				</li>
				<li class="menu-item">
					<a href="/accounts/${user.studentNumber?c}" data-toggle="tooltip" data-placement="right" title data-original-title="Profile">
						<i class="glyphicon glyphicon-user"></i>
					</a>
				</li>
				<li class="menu-item">
					<a href="/accounts/logout" data-toggle="tooltip" data-placement="right" title data-original-title="Logout">
						<i class="glyphicon glyphicon-off"></i>
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