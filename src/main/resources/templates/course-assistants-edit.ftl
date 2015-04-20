[#import "macros.ftl" as macros]
[@macros.renderHeader i18n.translate("section.courses") /]
[@macros.renderMenu i18n user /]
		<div class="container">
			<h2>${i18n.translate("course.control.assistants.title")}</h2>
[#if error?? && error?has_content]
			<div class="alert alert-danger">
				${i18n.translate(error)}
			</div>
[/#if]
			<form role="form" method="POST" action="">
				<label for="course">${i18n.translate("form.project-setup.course-selected.label")}</label>
				<div class="form-group">
					<div class="well well-sm" id="course">
						<code>${course.getCode()} - ${course.getName()}</code>
					</div>
				</div>
    [#if members??]
        [#list members as assistant]
				<div class="form-group member-field">
					<label class="control-label" for="member-${assistant_index + 1}">
                    ${i18n.translate("course.control.assistant")} #${assistant_index + 1}
                    </label>
 					<span class="glyphicon form-control-feedback"></span>
					<input type="text" class="form-control" name="member-${assistant_index + 1}" placeholder="${i18n.translate("form.project-setup.net-id.label")}" value="${assistant.getNetId()}">
					<span class="help-block"></span>
				</div>
        [/#list]
                <div class="form-group member-field">
                    <label class="control-label" for="member-${members?size + 1}">
                    ${i18n.translate("course.control.assistant")}  #${members?size + 1}
                    </label>
                    <span class="glyphicon form-control-feedback"></span>
                    <input type="text" class="form-control" name="member-${members?size + 1}" placeholder="${i18n.translate("form.project-setup.net-id.label")}" value="">
                    <span class="help-block"></span>
                </div>
    [#else]
                <div class="form-group member-field">
                    <label class="control-label" for="member-1">
                    ${i18n.translate("course.control.assistant")}  #1
                    </label>
                    <span class="glyphicon form-control-feedback"></span>
                    <input type="text" class="form-control" name="member-1" placeholder="${i18n.translate("form.project-setup.net-id.label")}" value="">
                    <span class="help-block"></span>
                </div>
    [/#if]

				<div class="form-group">
					<button type="button" class="btn btn-xl btn-default" name="Add member" onclick="addField()">
							<span class="glyphicon glyphicon-plus"></span> ${i18n.translate("form.project-setup.buttons.add-member.caption")} 
					</button>

					<div class="pull-right">
						<a class="btn btn-xl btn-default" href="/courses/${course.getCode()}">
							<span class="glyphicon glyphicon-chevron-left"></span> ${i18n.translate("form.project-setup.buttons.previous.caption")}
						</a>
						<button type="submit" class="btn btn-xl btn-success" name="next">
							${i18n.translate("form.project-setup.buttons.next.caption")} <span class="glyphicon glyphicon-chevron-right"></span>
						</button>
					</div>
				</div>
			</form>
		</div>
[@macros.renderScripts /]
		<script type="text/javascript">
            $(document).ready(function() {
    [#if members??]
        [#list members as assistant]
				addNetIdValidationRule($('input[name="member-' + ${assistant_index + 1} + '"]'), "${i18n.translate("error.invalid-net-id")}", true, 'input[name^="member-"]');
        [/#list]
                addNetIdValidationRule($('input[name="member-' + ${members?size + 1} + '"]'), "${i18n.translate("error.invalid-net-id")}", true, 'input[name^="member-"]');
    [#else]
                addNetIdValidationRule($('input[name="member-1"]'), "${i18n.translate("error.invalid-net-id")}", true, 'input[name^="member-"]');
    [/#if]
			});

			function addField() {
				var inputs = $(".member-field"),

					index = inputs.size() + 1,

					wrapper = $("<div>")
						.addClass("form-group member-field"),

					label = $("<label>")
						.addClass("control-label")
						.attr("for", "member-" + index)
						.html("${i18n.translate("course.control.assistant")}  #" + index)
						.appendTo(wrapper),

					icon = $("<span class=\"glyphicon form-control-feedback\"></span>").appendTo(wrapper),

					input = $("<input>")
						.attr({
							"type" : "text",
							"class" : "form-control",
							"name" : "member-" + index,
							"placeholder" : "${i18n.translate("form.project-setup.net-id.label")}"
						})
						.appendTo(wrapper);

				wrapper
					.append("<span class=\"help-block\"></span>")
					.insertAfter(inputs.last());

				addNetIdValidationRule(input, "${i18n.translate("error.invalid-net-id")}", true, 'input[name^="member-"]');
			}
		</script>
[@macros.renderFooter /]
