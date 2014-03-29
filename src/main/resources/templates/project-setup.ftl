[#import "macros.ftl" as macros]
[@macros.renderHeader i18n.translate("form.project-setup.title") /]
[@macros.renderMenu i18n /]
		<div class="container">
			<h4>${i18n.translate("form.project-setup.title")}</h4>
[#if error?? && error?has_content]
			<div class="alert alert-danger">
				${i18n.translate(error)}
			</div>
[/#if]

[#if courses?? && courses?has_content]
			<form role="form" method="POST" action="">
				<div class="form-group">
					<label for="course-id">${i18n.translate("form.project-setup.course-id.label")}</label>
					<select class="form-control" id="course-id" name="course-id">
	[#list courses as course]
						<option value="${course.getId()}">${course.getCode()} - ${course.getName()}</option>
	[/#list]
					</select>
				</div>
				<div class="form-group">
					<input type="submit" class="btn btn-xl btn-success pull-right" name="provision-project" value="${i18n.translate("form.project-setup.buttons.provision-project.caption")}">
				</div>
[#else]
			<div class="alert alert-info">
				${i18n.translate("form.project-setup.no-courses")}
[/#if]
			</form>
		</div>
[@macros.renderScripts /]
[@macros.renderFooter /]
