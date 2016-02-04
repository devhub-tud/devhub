[#import "macros.ftl" as macros]
[@macros.renderHeader i18n.translate("section.projects") /]
[@macros.renderMenu i18n user /]
		<div class="container">
			<h2>
				${i18n.translate("block.my-projects.title")}
				<a class="btn btn-success btn-sm pull-right" data-toggle="modal" data-target="#setupModal">
					<i class="glyphicon glyphicon-plus-sign"></i> New Repository
				</a>
			</h2>
			<table class="table table-bordered">
				<tbody>
[#if repositories?has_content]
	[#list repositories as repo]
					<tr>
						<td>
							<a href="${repo.getURI()}">${repo.getTitle()}</a>
						</td>
					</tr>
	[/#list]
[#else]
					<tr>
						<td class="muted">
							${i18n.translate("block.my-projects.empty-list")}
						</td>
					</tr>
[/#if]
				</tbody>
			</table>
		</div>
[@macros.renderScripts /]
[@macros.renderFooter /]


<!-- Repository Create Modal -->
<div class="modal fade" id="setupModal" tabindex="-1" role="dialog" aria-labelledby="setupModalLabel">
	<div class="modal-dialog" role="document">
		<form class="form-horizontal modal-content" action="projects/setup" method="POST" role="form">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
				<h4 class="modal-title" id="setupModalLabel">Create a new repository</h4>
			</div>
			<div class="modal-body">
				<p>A repository contains all the files for your project, including the revision history.</p>
				<div class="form-group">
					<label for="repositoryName" class="col-sm-2 control-label">Name</label>
					<div class="col-sm-10">
						<input type="text" class="form-control" id="repositoryName" name="repositoryName" placeholder="Repository Name">
					</div>
				</div>
			</div>
			<div class="modal-footer">
				<button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
				<button type="submit" class="btn btn-primary">Create Repository</button>
			</div>
		</form>
	</div>
</div>