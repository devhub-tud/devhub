[#import "macros.ftl" as macros]
[@macros.renderHeader "Profile" /]
[@macros.renderProjectMenu user "" /]
		<div class="content">
			<ol class="breadcrumb breadcrumb-nav">
				<li>
					<a href="/projects"><i class="glyphicon glyphicon-home"></i></a>
				</li>
				<li>
					<a href="/accounts/${user.studentNumber}">Profile of #${user.studentNumber?c}</a>
				</li>
			</ol>
			<h1>Profile of #${user.studentNumber?c}</h1>
		</div>
[@macros.renderScripts /]
[@macros.renderFooter /]