
function addNetIdValidationRule(field, message, allowEmpty, uniqueGroup) {
	allowEmpty = typeof allowEmpty !== 'undefined' ? allowEmpty : false;
	addValidationRule(field, /^[a-zA-Z0-9]+$/, message, function() { validateNetId(field, message); }, !allowEmpty, uniqueGroup);
}

function addValidationRule(field, regex, message, validator, validateOnEmpty, uniqueGroup) {
	validator = typeof validator !== 'undefined' ? validator : function() { setFieldStatus(field, true, message); };
	validateOnEmpty = typeof validateOnEmpty !== 'undefined' ? validateOnEmpty : true;
	
	var validated = undefined;
	setInterval(function() {
		if (field.val() == '' && !validateOnEmpty) {
			setFieldStatus(field, true, message);
			text = field.val();
		}
		else if (isValidField(field, regex, uniqueGroup)) {
			if (validated !== field.val()) {
				validator();
				validated = field.val();
			}
		}
		else {
			setFieldStatus(field, false, message);
			validated = undefined;
		}
	}, 100);
}

function isValidField(field, regex, uniqueGroup) {
	var value = field.val();
	if (value === null || !value.match(regex)) {
		return false;
	}
	
	if (typeof uniqueGroup !== 'undefined') {
		var form = field.parentsUntil('form').parent();
		var groups = form.find(uniqueGroup);
		for (var index = 0; index < groups.length; index++) {
			var group = $(groups[index]);
			var groupValue = group.val();
			var groupName = group.attr("name");
			var fieldName = field.attr("name");
			if (groupName !== fieldName && groupValue === value) {
				return false;
			}
		}
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

function validateNetId(field, message, illegalValue) {
	var netId = field.val();
	if (netId == illegalValue) {
		setFieldStatus(field, false, message);
		return;
	}
	
	$.ajax("/validation/netID?netID=" + netId, {
		success: function() {
			setFieldStatus(field, true, message);
		},
		error: function() {
			setFieldStatus(field, false, message);
		}
	});
}
