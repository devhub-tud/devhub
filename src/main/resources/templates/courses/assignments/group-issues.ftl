[#import "../../macros.ftl" as macros]
[#import "../../components/project-frameset.ftl" as projectFrameset]
[#import "../../components/commit-row.ftl" as commitRow]

[@macros.renderHeader i18n.translate("section.projects") /]
[@macros.renderMenu i18n user /]
<div class="container">

  [#if repositoryEntity?? && repositoryEntity?has_content]
      [@projectFrameset.renderBreadcrumb i18n group![] repositoryEntity/]
  [/#if]
  
    <div class="row">
        <div class="col-md-10 col-md-offset-2">
            <h4 style="line-height:34px; margin-top:0;">${i18n.translate("issue.title")}</h4>
        </div>
    </div>

	<div class="row">
		<div class="col-md-2">
			[@projectFrameset.renderSidemenu "issues" i18n group![] repository/]
		</div>
		<div class="col-md-8">	
			<div class="panel panel-default">
				<div class="panel-body">
					<a id="btn-add-issue" class="btn btn-primary pull-right" href="${repositoryEntity.getURI()}issues/create">${i18n.translate("issue.create")}</a>
				</div>
			</div>
			<h4>${i18n.translate("issue.open-issues")}</h4>
			<table class="table table-bordered">
				<tbody>
				[#if openIssues?? && openIssues?has_content]
					[#list openIssues as issue]
					<tr>
						<td class="commit ignored" id="${issue.title}">
							<a href="${issue.getURI()}">
								<span class="state glyphicon glyphicon-unchecked" title="${i18n.translate("issue.title")}"></span>
								<div class="comment">Issue #${issue.issueId}: ${issue.title}</div>
								<div class="timestamp" data-value="${issue.timestamp?date}">Opened on ${issue.timestamp?string["EEEE dd MMMM yyyy HH:mm"]}</div>
							</a>
						</td>
					</tr>
					[/#list]
				[#else]
				<tr>
					<td class="muted">
						${i18n.translate("issue.no-open-issues")}
					</td>
				</tr>
				[/#if]
				</tbody>
			</table>
			<h4>${i18n.translate("issue.closed-issues")}</h4>
			<table class="table table-bordered">
				<tbody>
				[#if closedIssues?? && closedIssues?has_content]
					[#list closedIssues as issue]
					<tr>
						<td class="commit ignored" id="${issue.title}">
							<a href="${issue.getURI()}">
								<span class="state glyphicon glyphicon-unchecked" title="${i18n.translate("issue.title")}"></span>
								<div class="comment">Issue #${issue.issueId}: ${issue.title}</div>
								<div class="timestamp" data-value="${issue.timestamp?date}">Opened on ${issue.timestamp?string["EEEE dd MMMM yyyy HH:mm"]}</div>
							</a>
						</td>
					</tr>
					[/#list]
				[#else]
				<tr>
					<td class="muted">
						${i18n.translate("issue.no-closed-issues")}
					</td>
				</tr>
				[/#if]
				</tbody>
			</table>
		</div>
		<div class="col-md-2 panel panel-default">
			<table class="table">
				<thead>
					<tr>
						<th>Labels
							<a class="btn-sm pull-right" data-toggle="modal" data-target="#addLabelModal">
								<i class="glyphicon glyphicon-plus-sign"></i>
							</a>
						</th>
					</tr>
				</thead>
				<tbody>
					<tr>
						<td>
							<p style="background-color:#ff0000; color="#ffffff">
								Fatal
							</p>
						</td>
					</tr>
				</tbody>
			</table>
		</div>
	</div>
</div>
<!-- Add label Modal -->
<div class="modal fade" id="addLabelModal" tabindex="-1" role="dialog" aria-labelledby="addLabelModalLabel">
	<div class="modal-dialog" role="document">
		<form class="form-horizontal modal-content" action="addlabel" target="_none" method="POST" role="form">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
				<h4 class="modal-title" id="addLabelModalLabel">Add Label</h4>
			</div>
			<div class="modal-body">
				<div class="form-group">
					<label for="repositoryName" class="col-sm-2 control-label">Tag</label>
					<div class="col-sm-10">
						<input type="text" class="form-control" id="tag" name="tag" placeholder="Tag">
					</div>
				</div>
				<div class="form-group">
					<label for="repositoryName" class="col-sm-2 control-label">Color</label>
					<div class="col-sm-10">
						<input type="color" class="form-control jscolor" id="color" name="color" placeholder="Color">
					</div>
				</div>
			</div>
			<div class="modal-footer">
				<button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
				<button type="submit" class="btn btn-primary">Add Label</button>
			</div>
		</form>
	</div>
</div>
[@macros.renderScripts /]
[@macros.renderFooter /]
