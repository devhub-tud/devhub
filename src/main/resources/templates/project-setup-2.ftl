[#import "macros.ftl" as macros]
[@macros.renderHeader i18n.translate("form.project-setup.title") /]
[@macros.renderMenu i18n user /]
		<div class="container">
			<h4>${i18n.translate("form.project-setup.title")}</h4>
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
				<div class="form-group">
					<label for="member-${x}">${i18n.translate("form.project-setup.group-member.label")} #${x}</label>
		[#if members?? && members?size > x - 1]
					<input type="text" class="form-control" name="member-${x}" placeholder="${i18n.translate("form.project-setup.net-id.label")}" value="${members[x - 1].getNetId()}">
		[#elseif x == 1 && !user.isAdmin() && !user.isAssisting(course)]
					<input type="text" class="form-control" name="member-${x}" placeholder="${i18n.translate("form.project-setup.net-id.label")}" value="${user.getNetId()}" readonly>
		[#else]
					<input type="text" class="form-control" name="member-${x}" placeholder="${i18n.translate("form.project-setup.net-id.label")}">
		[/#if]
				</div>
	[/#list]
				<div class="form-group pull-right">
					<a class="btn btn-xl btn-default" href="/projects/setup?step=1">
						<span class="glyphicon glyphicon-chevron-left"></span> ${i18n.translate("form.project-setup.buttons.previous.caption")}
					</a>
					<button type="submit" class="btn btn-xl btn-success" name="next">
						${i18n.translate("form.project-setup.buttons.next.caption")} <span class="glyphicon glyphicon-chevron-right"></span>
					</button>
				</div>
			</form>
		</div>
[@macros.renderScripts /]
		<script type="text/javascript">
			$(document).ready(function() {
[#list 1..maxGroupSize as x]
	[#if x > minGroupSize]
				addNetIdValidationRule($('input[name="member-' + ${x} + '"]'), "${i18n.translate("error.invalid-net-id")}", true);
	[#else]
				addNetIdValidationRule($('input[name="member-' + ${x} + '"]'), "${i18n.translate("error.invalid-net-id")}");
	[/#if]
[/#list]
			});
		</script>
[@macros.renderFooter /]
