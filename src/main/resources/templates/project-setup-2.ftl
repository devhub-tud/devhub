[#import "macros.ftl" as macros]
[@macros.renderHeader i18n.translate("form.project-setup.title") /]
[@macros.renderMenu i18n user /]
		<div class="container">
			<h2>${i18n.translate("form.project-setup.title")}</h2>
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
	[#list 1..maxGroupSize as x]
				<div class="form-group member-field">
					<label class="control-label" for="member-${x}">${i18n.translate("form.project-setup.group-member.label")} #${x}</label>
 					<span class="glyphicon form-control-feedback"></span>
		[#if members?? && members?size > x - 1]
					<input type="text" class="form-control" name="member-${x}" placeholder="${i18n.translate("form.project-setup.net-id.label")}" value="${members[x - 1].getNetId()}">
		[#elseif x == 1 && !user.isAdmin() && !user.isAssisting(course)]
					<input type="text" class="form-control" name="member-${x}" placeholder="${i18n.translate("form.project-setup.net-id.label")}" value="${user.getNetId()}" readonly>
		[#else]
					<input type="text" class="form-control" name="member-${x}" placeholder="${i18n.translate("form.project-setup.net-id.label")}">
		[/#if]
					<span class="help-block"></span>
				</div>
	[/#list]
				<div class="form-group">
		[#if user.isAdmin() || user.isAssisting(course) ]
					<button type="button" class="btn btn-xl btn-default" name="Add member" onclick="addField()">
							<span class="glyphicon glyphicon-plus"></span> ${i18n.translate("form.project-setup.buttons.add-member.caption")} 
					</button>
		[/#if]
					<div class="pull-right">
						<a class="btn btn-xl btn-default" href="/courses">
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
[#list 1..maxGroupSize as x]
	[#if x > minGroupSize]
				addNetIdValidationRule($('input[name="member-' + ${x} + '"]'), "${i18n.translate("error.invalid-net-id")}", true, 'input[name^="member-"]');
	[#else]
				addNetIdValidationRule($('input[name="member-' + ${x} + '"]'), "${i18n.translate("error.invalid-net-id")}", false, 'input[name^="member-"]');
	[/#if]
[/#list]
			});

			function addField() {
				var inputs = $(".member-field"),

					index = inputs.size() + 1,

					wrapper = $("<div>")
						.addClass("form-group member-field"),

					label = $("<label>")
						.addClass("control-label")
						.attr("for", "member-" + index)
						.html("${i18n.translate("form.project-setup.group-member.label")} #" + index)
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
