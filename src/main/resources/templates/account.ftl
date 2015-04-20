[#import "macros.ftl" as macros]
[@macros.renderHeader i18n.translate("section.account") /]

[@macros.renderMenu i18n user /]
		<div class="container">
			<h2>
				${i18n.translate("block.ssh-keys.title")}
				<div class="pull-right">
                    <a href="https://github.com/devhub-tud/devhub/wiki/Generating-SSH-keys" class="btn btn-primary btn-sm" target="_blank">Need some help?</a>
                    <a href="${path}/setup" class="btn btn-success btn-sm">
                        <i class="glyphicon glyphicon-plus-sign"></i> ${i18n.translate("block.ssh-keys.buttons.setup-new-ssh-key.caption")}
                    </a>
				</div>

			</h2>
[#if error?? && error?has_content]
			<div class="alert alert-danger">
				${i18n.translate(error)}
			</div>
[/#if]
			<table class="table table-bordered">
				<tbody>
[#if keys?? && keys?has_content]
        [#list keys as key]
					<tr>
						<td>
							<form action="${path}/delete" method="post" class="pull-right">
								<input type="hidden" name="name" value="${key.name}">
								<button type="submit" class="btn btn-danger btn-sm" style="margin: 5px;">
									<i class="glyphicon glyphicon-remove-sign"></i> ${i18n.translate("block.ssh-keys.buttons.delete-ssh-key.caption")}
								</button>
							</form>
							<div><b>${key.name}</b></div>
							<div class="truncate">${key.contents}</div> 
						</td>
					</tr>
	[/#list]
[#else]
					<tr>
						<td class="muted">
							${i18n.translate("block.ssh-keys.empty-list")}
						</td>
					</tr>
[/#if]
				</tbody>
			</table>
		</div>
[@macros.renderScripts /]
[@macros.renderFooter /]
