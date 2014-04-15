[#import "macros.ftl" as macros]
[@macros.renderHeader i18n.translate("form.build-server-setup.title") /]
[@macros.renderMenu i18n user /]
		<div class="container">
[#if error?? && error?has_content]
			<div class="alert alert-danger">
				${i18n.translate(error)}
			</div>
[/#if]
			<h4>${i18n.translate("form.build-server-setup.title")}</h4>
			<form role="form" method="POST" action="">
				<div class="form-group">
					<label for="build-server-name">${i18n.translate("form.build-server-setup.build-server-name.label")}</label>
					<input type="text" id="build-server-name" name="name" maxlength="32" class="form-control" autofocus="autofocus" placeholder="${i18n.translate("form.build-server-setup.build-server-name.label")}">
				</div>
				<div class="form-group">
					<label for="build-server-secret">${i18n.translate("form.build-server-setup.build-server-secret.label")}</label>
					<input type="text" id="build-server-secret" name="secret" maxlength="40" class="form-control" placeholder="${i18n.translate("form.build-server-setup.build-server-secret.label")}">
				</div>
				<div class="form-group">
					<label for="build-server-host">${i18n.translate("form.build-server-setup.build-server-host.label")}</label>
					<input type="text" id="build-server-host" name="host" maxlength="256" class="form-control" placeholder="${i18n.translate("form.build-server-setup.build-server-host.label")}">
				</div>
				<div class="form-group">
					<input type="submit" class="btn btn-xl btn-success pull-right" name="add-build-server" value="${i18n.translate("form.build-server-setup.buttons.add-build-server.caption")}" disabled="disabled">
				</div>
			</form>
		</div>
[@macros.renderScripts /]
		<script type="text/javascript">
			$(document).ready(function() {
				addValidationRule($('input[name="name"]'), /^[a-zA-Z0-9]+$/, '${i18n.translate("error.invalid-build-server-name")}');
				addValidationRule($('input[name="secret"]'), /^[a-zA-Z0-9]+$/, '${i18n.translate("error.invalid-build-server-secret")}');
				addValidationRule($('input[name="host"]'), /^http:\/\/.+$/, '${i18n.translate("error.invalid-build-server-host")}');
			});
		</script>
[@macros.renderFooter /]
