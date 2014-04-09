[#import "macros.ftl" as macros]
[@macros.renderHeader i18n.translate("section.build-servers") /]
[@macros.renderMenu i18n user /]
		<div class="container">
[#if error?? && error?has_content]
			<div class="alert alert-danger">
				${i18n.translate(error)}
			</div>
[/#if]
			<h4>
				${i18n.translate("block.build-servers.title")}
				<a href="/build-servers/setup" class="btn btn-success btn-sm pull-right">
					<i class="glyphicon glyphicon-plus-sign"></i> ${i18n.translate("block.build-servers.buttons.setup-new-build-server.caption")}
				</a>
			</h4>
			<table class="table table-bordered">
				<tbody>
[#if servers?? && servers?has_content]
	[#list servers as server]
					<tr>
						<td>
							<form action="build-servers/delete" method="post" class="pull-right">
								<input type="hidden" name="id" value="${server.id}">
								<button type="submit" class="btn btn-danger btn-sm" style="margin: 5px;">
									<i class="glyphicon glyphicon-remove-sign"></i> ${i18n.translate("block.build-servers.buttons.delete-build-server.caption")}
								</button>
							</form>
							<div><b>${server.name}</b></div>
							<div class="truncate">${server.host}</div> 
						</td>
					</tr>
	[/#list]
[#else]
					<tr>
						<td class="muted">
							${i18n.translate("block.build-servers.empty-list")}
						</td>
					</tr>
[/#if]
				</tbody>
			</table>
		</div>
[@macros.renderScripts /]
[@macros.renderFooter /]
