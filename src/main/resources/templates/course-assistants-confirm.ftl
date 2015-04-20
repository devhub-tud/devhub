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
			<form role="form" id="project-setup-form" method="POST" action="">
				<label for="course">${i18n.translate("form.project-setup.course-selected.label")}</label>
				<div class="form-group">
					<div class="well well-sm" id="course">
						<code>${course.getCode()} - ${course.getName()}</code>
					</div>
				</div>
				<div class="form-group">
					<label for="members">${i18n.translate("course.control.assistants")}</label>
					<table class="table table-bordered">
						<tbody>
    [#if members?? && members?has_content ]
        [#list members as member]
							<tr>
								<td>
									<div><b>${member.getName()}</b></div>
									<div class="truncate">${member.getNetId()}</div> 
								</td>
							</tr>
        [/#list]
    [#else]
                            <tr>
                                <td>
                                    <div>${i18n.translate("course.control.no-assistants")}</div>
                                </td>
                            </tr>
    [/#if]
				</tbody>
			</table>
				</div>
				<div class="form-group pull-right">
					<a class="btn btn-xl btn-default" name="back" href="/courses/${course.getCode()}/assistants?step=1">
						<span class="glyphicon glyphicon-chevron-left"></span> ${i18n.translate("form.project-setup.buttons.previous.caption")}
					</a>
					<button type="submit" class="btn btn-xl btn-success" name="finish">
						<span class="glyphicon glyphicon-ok"></span> ${i18n.translate("button.label.confirm")}
					</button>
				</div>
			</form>
		</div>
[@macros.renderScripts /]
		<script type="text/javascript">
			$(document).ready(function() {
				var submitted = false;
				$('#project-setup-form').submit(function(e) {
					if (!submitted) {
						submitted = true;
						setTimeout(function() {
							$(".logo-image").addClass("spinner");
							$('a[name="back"]').attr("disabled", "disabled");
							$('button[name="finish"]').attr("disabled", "disabled");
						}, 100);
					}
					else {
						e.preventDefault();
					}
				});
			});
		</script>
[@macros.renderFooter /]
