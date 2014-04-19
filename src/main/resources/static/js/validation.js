
function addNetIdValidationRule(field, message, allowEmpty, uniqueGroup) {
	allowEmpty = typeof allowEmpty !== 'undefined' ? allowEmpty : false;
	addValidationRule(field, /^[a-zA-Z0-9]+$/, message, function() { validateNetId(field, message); }, allowEmpty, uniqueGroup);
}

function addValidationRule(field, regex, message, validator, allowEmpty, uniqueGroup) {
	validator = typeof validator !== 'undefined' ? validator : function() { setFieldStatus(field, true, message); };
	allowEmpty = typeof allowEmpty !== 'undefined' ? allowEmpty : false;
	
	var validated = undefined;
	setInterval(function() {
		if (field.val() == '' && allowEmpty) {
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
	var parent = field.parent();
	var helpBlock = parent.find('.help-block');
	var stateIcon = parent.find('.form-control-feedback');
	
	if (valid) {
		parent.removeClass('has-error');
		stateIcon.removeClass('glyphicon-remove');
		
		if (field.val().length > 0) {
			parent.addClass('has-success');
			stateIcon.addClass('glyphicon-ok');
		}
		
		helpBlock.text('');
		helpBlock.css('display', 'none');
		
		validateForm(field.parentsUntil('form').parent());
	}
	else {
		parent.removeClass('has-success');
		parent.addClass('has-error');
		stateIcon.removeClass('glyphicon-ok');
		stateIcon.addClass('glyphicon-remove');
		
		if (typeof message !== 'undefined') {
			helpBlock.css('display', 'block');
			helpBlock.text(message);
		}
		
		validateForm(field.parentsUntil('form').parent());
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
