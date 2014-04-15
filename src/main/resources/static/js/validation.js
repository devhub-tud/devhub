
function addNetIdValidationRule(field, message, allowEmpty) {
	allowEmpty = typeof allowEmpty !== 'undefined' ? allowEmpty : false;
	addValidationRule(field, /^[a-zA-Z0-9]+$/, message, function() { validateNetId(field, message); }, !allowEmpty);
}

function addValidationRule(field, regex, message, validator, validateOnEmpty) {
	validator = typeof validator !== 'undefined' ? validator : function() { setFieldStatus(field, true, message); };
	validateOnEmpty = typeof validateOnEmpty !== 'undefined' ? validateOnEmpty : true;
	
	var text = undefined;
	setInterval(function() {
		if (text !== field.val()) {
			text = field.val();
			if (text == '' && !validateOnEmpty) {
				setFieldStatus(field, true, message);
			}
			else if (isValidField(field, regex)) {
				validator();
			}
			else {
				setFieldStatus(field, false, message);
			}
		}
	}, 100);
}

function isValidField(field, regex) {
	var value = field.val();
	if (value === null || !value.match(regex)) {
		return false;
	}
	return value.length > 0;
}

function setFieldStatus(field, valid, message) {
	if (valid) {
		if (field.parent().hasClass('has-error')) {
			field.parent().removeClass('has-error');
			field.popover('destroy');
			
			validateForm(field.parentsUntil('form').parent());
		}
	}
	else {
		if (!field.parent().hasClass('has-error')) {
			field.parent().addClass('has-error');
			if (typeof message !== 'undefined') {
				field.popover({ html: true, trigger: 'hover', placement: 'top', content: '<font color="#a94442">' + message + '</font>' });
			}
			
			validateForm(field.parentsUntil('form').parent());
		}
	}
}

function validateForm(form) {
	var submitBtn = form.find('*[type="submit"]');
	if (form.find('.has-error').length == 0) {
		submitBtn.removeAttr('disabled');
	}
	else {
		submitBtn.attr('disabled', 'disabled');
	}
}

function validateNetId(field, message) {
	var netId = field.val();
	
	$.ajax("/validation/netID?netID=" + netId, {
		success: function() {
			setFieldStatus(field, true, message);
		},
		error: function() {
			setFieldStatus(field, false, message);
		}
	});
}
