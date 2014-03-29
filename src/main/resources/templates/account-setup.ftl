[#import "macros.ftl" as macros]
[@macros.renderHeader i18n.translate("form.ssh-key-setup.title") /]
[@macros.renderMenu i18n /]
		<div class="container">
			<h4>${i18n.translate("form.ssh-key-setup.title")}</h4>
[#if error?? && error?has_content]
			<div class="alert alert-danger">
				${i18n.translate(error)}
			</div>
[/#if]
			<form role="form" method="POST" action="">
				<div class="form-group">
					<label for="key-name">${i18n.translate("form.ssh-key-setup.key-name.label")}</label>
					<input type="text" id="key-name" name="name" class="form-control" placeholder="${i18n.translate("form.ssh-key-setup.key-name.label")}" data-placement="top" data-trigger="hover">
				</div>
				<div class="form-group">
					<label for="key-contents">${i18n.translate("form.ssh-key-setup.key-contents.label")}</label>
					<textarea name="contents" id="key-contents" class="form-control" placeholder="${i18n.translate("form.ssh-key-setup.key-contents.label")}" rows="6" data-placement="top" data-trigger="hover"></textarea>
				</div>
				<div class="form-group">
					<input type="submit" class="btn btn-xl btn-success pull-right" name="add-key" value="${i18n.translate("form.ssh-key-setup.buttons.add-key.caption")}" disabled="disabled">
				</div>
			</form>
		</div>
[@macros.renderScripts /]
		<script type="text/javascript">
			$(document).ready(function() {
				
				var keyNameField = $('input[name="name"]');
				var keyContentsField = $('textarea[name="contents"]');
				var submitBtn = $('input[name="add-key"]');
			
				var interval = setInterval(function() { validateForm(); }, 250);
				
				function validateForm() {
					var keyNameValid = validateField(keyNameField, /^[a-zA-Z0-9\.]+$/, '${i18n.translate("error.invalid-key-name")}');
					var keyContentsValid = validateField(keyContentsField, /^ssh-rsa\s.+$/, '${i18n.translate("error.invalid-key-contents")}');
					
					if (keyNameValid && keyContentsValid) {
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
