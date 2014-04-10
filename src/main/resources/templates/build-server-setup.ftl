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
					<input type="text" id="build-server-name" name="name" maxlength="32" class="form-control" autofocus="autofocus" placeholder="${i18n.translate("form.build-server-setup.build-server-name.label")}" data-placement="top" data-trigger="hover">
				</div>
				<div class="form-group">
					<label for="build-server-secret">${i18n.translate("form.build-server-setup.build-server-secret.label")}</label>
					<input type="text" id="build-server-secret" name="secret" maxlength="40" class="form-control" placeholder="${i18n.translate("form.build-server-setup.build-server-secret.label")}" data-placement="top" data-trigger="hover">
				</div>
				<div class="form-group">
					<label for="build-server-host">${i18n.translate("form.build-server-setup.build-server-host.label")}</label>
					<input type="text" id="build-server-host" name="host" maxlength="256" class="form-control" placeholder="${i18n.translate("form.build-server-setup.build-server-host.label")}" data-placement="top" data-trigger="hover">
				</div>
				<div class="form-group">
					<input type="submit" class="btn btn-xl btn-success pull-right" name="add-build-server" value="${i18n.translate("form.build-server-setup.buttons.add-build-server.caption")}" disabled="disabled">
				</div>
			</form>
		</div>
[@macros.renderScripts /]
		<script type="text/javascript">
			$(document).ready(function() {
				
				var nameField = $('input[name="name"]');
				var secretField = $('input[name="secret"]');
				var hostField = $('input[name="host"]');
				var submitBtn = $('input[name="add-build-server"]');
			
				var interval = setInterval(function() { validateForm(); }, 250);
				
				function validateForm() {
					var nameValid = validateField(nameField, /^[a-zA-Z0-9]+$/, '${i18n.translate("error.invalid-build-server-name")}');
					var secretValid = validateField(secretField, /^[a-zA-Z0-9]+$/, '${i18n.translate("error.invalid-build-server-secret")}');
					var hostValid = validateField(hostField, /^http:\/\/.+$/, '${i18n.translate("error.invalid-build-server-host")}');
					
					if (nameValid && secretValid && hostValid) {
						submitBtn.removeAttr('disabled');
					}
					else {
						submitBtn.attr('disabled', 'disabled');
					}
				}
				
				function validateField(field, regex, message) {
					var value = field.val();
					if (value.length > 0 && !value.match(regex)) {
						fieldError(field, message);
						return false;
					}
					fieldValid(field);
					return value.length > 0;
				}
				
				function fieldError(field, message) {
					if (!field.parent().hasClass('has-error')) {
						field.parent().addClass('has-error');
						field.popover({ html: true, content: '<font color="#a94442">' + message + '</font>' }).popover('show');
					}
				}
				
				function fieldValid(field) {
					if (field.parent().hasClass('has-error')) {
						field.parent().removeClass('has-error');
						field.popover('destroy');
					}
				}
			});
		</script>
[@macros.renderFooter /]
