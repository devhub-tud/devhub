[#import "macros.ftl" as macros]
[@macros.renderHeader "" /]
[#if user??]
	[@macros.renderMenu i18n user /]
[/#if]
		<div class="container">
			<div style="width: 760px; margin-left: auto; margin-right: auto; margin-top: 96px;">
				<img style="float: left;" src="/static/img/unauthorized.png">
				<div style="float: left; margin-top: 45px;">
					<h1>Hey there!</h1>
					<h2>You do not have access to this page...</h2>
					<h4>Event: ${error_id}</h4>
				</div>
			</div>
		</div>
[@macros.renderScripts /]
[@macros.renderFooter /]
