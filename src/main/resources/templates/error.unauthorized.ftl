[#import "macros.ftl" as macros]
[@macros.renderHeader "" /]
[#if user??]
	[@macros.renderMenu i18n user /]
[/#if]
		<div class="container">
			<div style="width: 760px; margin-left: auto; margin-right: auto; margin-top: 96px;">
				<img style="float: left;" src="/static/img/unauthorized.png">
				<div style="float: left; margin-top: 45px;">
                    <h1>${i18n.translate("error.unauthorized.title")}</h1>
                    <h2>${i18n.translate("error.unauthorized.subtitle")}</h2>
                    <h4>${i18n.translate("error.well.description", error_id)}</h4>
				</div>
			</div>
		</div>
[@macros.renderScripts /]
[@macros.renderFooter /]
