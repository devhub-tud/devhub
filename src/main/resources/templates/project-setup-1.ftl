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

[#if courses?? && courses?has_content]
			<form role="form" method="POST" action="">
				<div class="form-group">
					<label for="course">${i18n.translate("form.project-setup.course-id.label")}</label>
					<select class="form-control" id="course" name="course">
	[#if !course??]
						<option value="" disabled selected>${i18n.translate("form.project-setup.course-id.label")}</option>
	[/#if]
	[#list courses as c]
		[#if course?? && course.getId() == c.getId()]
						<option value="${c.getCode()}" selected>${c.getCode()} - ${c.getName()}</option>
		[#else]
						<option value="${c.getCode()}">${c.getCode()} - ${c.getName()}</option>
		[/#if]
	[/#list]
					</select>
				</div>
				<div class="form-group pull-right">
					<button type="submit" class="btn btn-xl btn-success" name="next">
						${i18n.translate("form.project-setup.buttons.next.caption")} <span class="glyphicon glyphicon-chevron-right"></span>
					</button>
				</div>
			</form>
[#else]
			<div class="alert alert-info">
				${i18n.translate("form.project-setup.no-courses")}
			</div>
[/#if]
		</div>
[@macros.renderScripts /]
		<script type="text/javascript">
			$(document).ready(function() {
				addValidationRule($('select[name="course"]'), /^[a-zA-Z0-9\-]+$/);
			});
		</script>
[@macros.renderFooter /]
