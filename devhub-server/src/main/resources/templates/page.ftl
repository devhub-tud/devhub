[#macro render i18n title name]
<!DOCTYPE html>
<html>
	<head>
		<title>${title}</title>
		<meta name="viewport" content="width=device-width, initial-scale=1.0">
		<link href="/static/css/bootstrap.css" rel="stylesheet">
		<!--[if lt IE 9]>
			<script src="https://oss.maxcdn.com/libs/html5shiv/3.7.0/html5shiv.js"></script>
			<script src="https://oss.maxcdn.com/libs/respond.js/1.3.0/respond.min.js"></script>
		<![endif]-->
	</head>
	<body>
		<div class="navbar" role="navigation">
			<div class="container">
				<h1 class="pull-left">Devhub</h1>
				<ul class="items">
					<li class="item">
						<a href="#">
							<img src="/static/img/logout.png" />
						</a>
					</li>
					<!-- <li class="item">
						<a href="#">
							<img src="/static/img/logout.png" />
						</a>
					</li>
					<li class="item">
						<a href="#">
							<img src="/static/img/logout.png" />
						</a>
					</li> --!>
				</div>
			</div>
		</div>
		<div class="sub-navbar" role="navigation">
		</div>
		<div class="container">
			<h2>${i18n.translate("greeting", name)}</h2>
			[#nested]
		</div>
		<script src="static/js/jquery.min.js"></script>
	</body>
</html>
[/#macro]