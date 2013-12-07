[#macro render i18n project group view viewIcon]
<!DOCTYPE html>
<html>
	<head>
		<title>DevHub - ${project} - Group #${group} - ${view}</title>
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
		<!--[if lt IE 9]>
			<script src="https://oss.maxcdn.com/libs/html5shiv/3.7.0/html5shiv.js"></script>
			<script src="https://oss.maxcdn.com/libs/respond.js/1.3.0/respond.min.js"></script>
		<![endif]-->
		<style>
		
			@font-face {
				font-family: 'Signika Negative';
				font-style: normal;
				font-weight: 300;
				src: local('Signika Negative Light'), local('SignikaNegative-Light'), url(http://themes.googleusercontent.com/static/fonts/signikanegative/v2/q5TOjIw4CenPw6C-TW06FrSCqyzKJANNqhjQyS0Fx0E.woff) format('woff');
			}
			@font-face {
				font-family: 'Signika Negative';
				font-style: normal;
				font-weight: 400;
				src: local('Signika Negative'), local('SignikaNegative-Regular'), url(http://themes.googleusercontent.com/static/fonts/signikanegative/v2/Z-Q1hzbY8uAo3TpTyPFMXZDOGCuI1pQIwqDbhXqhZhw.woff) format('woff');
			}
			@font-face {
				font-family: 'Signika Negative';
				font-style: normal;
				font-weight: 600;
				src: local('Signika Negative Semibold'), local('SignikaNegative-Semibold'), url(http://themes.googleusercontent.com/static/fonts/signikanegative/v2/q5TOjIw4CenPw6C-TW06FgvNGj7b0r5EVBw0pndrkW8.woff) format('woff');
			}
			@font-face {
				font-family: 'Signika Negative';
				font-style: normal;
				font-weight: 700;
				src: local('Signika Negative Bold'), local('SignikaNegative-Bold'), url(http://themes.googleusercontent.com/static/fonts/signikanegative/v2/q5TOjIw4CenPw6C-TW06FvRHEDGP1JGo_0-JTT3olAw.woff) format('woff');
			}
			
			body, html {
				margin: 0;
				height: 100%;
				background-color: #eeede9;
				font-weight: 100;
				font-family: 'Signika Negative';
				min-height: 720px;
			}
			
			h2 {
				margin: 0;
				padding: 0;
				line-height: 20px;
				font-family: 'Signika Negative';
			}
			
			a {
				text-decoration: none;
				font-family: 'Signika Negative';
			}
			
			.main {
				float: left;
				height: 100%;
				overflow: hidden;
				white-space: nowrap;
			}
		
			.menu {
				background-color: #222;
				color: white;
				height: 100%;
				margin-right: 32px;
			}
			
			.menu a {
				color: #666;
			}
			
			.menu a:hover {
				color: #f0f0f0;
			}
			
			.menu > ul {
				list-style-type: none;
				width: 48px;
				margin: 0;
				padding: 0;
				transition: width 0.3s ease-in-out;
				border-top: 1px solid #111;
				border-bottom: 1px solid #333;
			}
			
			.menu > ul:hover {
				width: 200px;
			}
			
			.menu > ul > li {
				height: 48px;
				width: 100%;
				border-bottom: 1px solid #111;
				border-top: 1px solid #333;
			}
			
			.menu > ul > li > a {
				display: inline-block;
				width: 100%;
				height: 100%;
			}
			
			.menu > ul > li > a > img {
				opacity: 0.5;
				transition: opacity 0.2s ease-in;
			}
			
			.menu > ul > li:hover > a > img {
				opacity: 1.0;
			}
			
			.menu > ul > li > a > span {
				opacity: 0.00;
				vertical-align: 18px;
				transition: opacity 0.2s ease-in;
			}
			
			.menu > ul:hover > li > a > span {
				opacity: 1.00;
			}
			
			.container {
				padding: 22px;
				min-width: 640px;
			}
			
		</style>
	</head>
	<body>
		<div class="main">
			<div class="menu">
				<ul class="items">
					<li style="height: 96px;">
						<a href="/">
							<img class="logo" style="float: right;" src="/static/img/logo.png" />
							<span style="font-size: 28px; font-weight: 100; color: white; position: absolute; top: 30px; left: 10px;">DevHub</span>
						</a>
					</li>
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