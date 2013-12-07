[#macro render i18n project group view viewIcon]
<!DOCTYPE html>
<html>
	<head>
		<title>DevHub - ${project} - Group #${group} - ${view}</title>
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
		<link rel="stylesheet" href="/static/css/devhub.css">
		<!--[if lt IE 9]>
			<script src="https://oss.maxcdn.com/libs/html5shiv/3.7.0/html5shiv.js"></script>
			<script src="https://oss.maxcdn.com/libs/respond.js/1.3.0/respond.min.js"></script>
		<![endif]-->
	</head>
	<body>
		<div class="header">
			
		</div>
		<div class="main">
			<div class="menu">
				<ul class="items">
					<li>
						<a href="/projects/${project}/group/${group}/activity">
							<img src="/static/img/activity.png" />
							<span>Activity</span>
						</a>
					</li>
					<li>
						<a href="/projects/${project}/group/${group}/commits">
							<img src="/static/img/commits.png" />
							<span>Commits</span>
						</a>
					</li>
					<li>
						<a href="/projects/${project}/group/${group}/builds">
							<img src="/static/img/builds.png" />
							<span>Builds</span>
						</a>
					</li>
					<li>
						<a href="/projects/${project}/group/${group}/issues">
							<img src="/static/img/issues.png" />
							<span>Issues</span>
						</a>
					</li>
					<li>
						<a href="/projects/${project}/group/${group}/pull-requests">
							<img src="/static/img/pull-requests.png" />
							<span>Pull requests</span>
						</a>
					</li>
					<li>
						<a href="/projects/${project}/group/${group}/releases">
							<img src="/static/img/releases.png" />
							<span>Releases</span>
						</a>
					</li>
					<li>
						<a href="/projects/${project}/group/${group}/team">
							<img src="/static/img/team.png" />
							<span>Team</span>
						</a>
					</li>
					<li>
						<a href="/projects/${project}/group/${group}/settings">
							<img src="/static/img/settings.png" />
							<span>Settings</span>
						</a>
					</li>
					<li>
						<a href="/logout">
							<img src="/static/img/logout.png" />
							<span>Logout</span>
						</a>
					</li>
				</div>
			</div>
			<div class="container">
				<img src="/static/img/${viewIcon}" />
				<h1 style="margin: 0; padding: 0; display: inline-block; vertical-align: 13px;">${view}</h1>
				[#nested]
			</div>
		</div>
		<script src="/static/js/jquery.min.js"></script>
	</body>
</html>
[/#macro]