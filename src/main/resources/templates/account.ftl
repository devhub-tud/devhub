[#import "macros.ftl" as macros]
[@macros.renderHeader "Account" /]
[@macros.renderMenu /]
		<div class="container">
			<h4>
				SSH keys
				<a href="/account/setup" class="btn btn-success btn-sm pull-right">
					<i class="glyphicon glyphicon-plus-sign"></i> Add new SSH key
				</a>
			</h4>
			<table class="table table-bordered">
				<tbody>
[#if keys?? && keys?has_content]
	[#list keys as key]
					<tr>
						<td>
							<a href="" class="btn btn-danger btn-sm pull-right" style="margin: 5px;"><i class="glyphicon glyphicon-remove-sign"></i> Delete</a>
							<div><b>${key.name}</b></div>
							<div class="truncate">${key.contents}...</div> 
						</td>
					</tr>
	[/#list]
[#else]
					<tr>
						<td class="muted">
							You don not appear to have any SSH keys registered yet!
						</td>
					</tr>
[/#if]
				</tbody>
			</table>
		</div>
[@macros.renderScripts /]
[@macros.renderFooter /]
