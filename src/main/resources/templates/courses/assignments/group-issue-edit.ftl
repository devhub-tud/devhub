[#import "../../macros.ftl" as macros]
[#import "../../components/project-frameset.ftl" as projectFrameset]
[#import "../../components/commit-row.ftl" as commitRow]

[@macros.renderHeader i18n.translate("section.projects") ]
<link rel="stylesheet" href="/static/vendor/multiselect/multiple-select.css">
[/@macros.renderHeader]
[@macros.renderMenu i18n user /]
<div class="container">

  [#if repositoryEntity?? && repositoryEntity?has_content]
      [@projectFrameset.renderBreadcrumb i18n group![] repositoryEntity/]
  [/#if]
  
    <div class="row">
        <div class="col-md-10 col-md-offset-2">
            <h4 style="line-height:34px; margin-top:0;">[#if issue?? && issue?has_content]${i18n.translate("issue.edit")}[#else]${i18n.translate("issue.create")}[/#if]</h4>
        </div>
    </div>

    <div class="row">
        <div class="col-md-2">
            [@projectFrameset.renderSidemenu "issues" i18n group![] repository/]
        </div>
        <div class="col-md-10">	
            <form id="create-issue-form" class="form-horizontal" method="POST" target="_self" action="">
				[#if error?? && error?has_content]
					<div class="alert alert-danger">
					${i18n.translate(error)}
					</div>
				[/#if]
				<div class="form-group">
					<label for="title" class="col-sm-2 control-label">${i18n.translate("label.title")}</label>
					<div class="col-sm-10">
						<input type="text" class="form-control" name="title" id="title" required 
							[#if issue??]
							value="${issue.title}">
							[#else]
							placeholder="${i18n.translate("label.title")}">
							[/#if]
						</input>
					</div>
				</div>
				<div class="form-group">
					<label for="description" class="col-sm-2 control-label">${i18n.translate("label.description")}</label>
					<div class="col-sm-10">
						<textarea rows="8" class="form-control" name="description" id="description"[#if issue??]>${issue.description}</textarea>[#else]placeholder="${i18n.translate("label.description")}"></textarea>[/#if]
					</div>
				</div>
				[#if issue?? && issue?has_content]				
                <div class="form-group">
					<label for="timestampOpened" class="col-sm-2 control-label">Opened on</label>
					<div class="col-sm-10">
						<input type="text" class="form-control" name="timestampOpened" id="timestampOpened" disabled value="${issue.timestamp?string["EEEE dd MMMM yyyy HH:mm"]}"></input>
					</div>
				</div>
				[/#if]
                <div class="form-group">
					<label for="assignee" class="col-sm-2 control-label">${i18n.translate("label.assignee")}</label>
					<div class="col-sm-10">
						<select form="create-issue-form" class="form-control" name="assignee" id="assignee">
							[#list repositoryEntity.getCollaborators() as collaborator]
							<option value="${collaborator.netId}" [#if issue?? && issue.assignee.netId == collaborator.netId]selected[/#if]>${collaborator.name}</option>
							[/#list]
						</select>
					</div>
				</div>
				[#if issue?? && issue?has_content]				
                <div class="form-group">
					<label for="status" class="col-sm-2 control-label">${i18n.translate("delivery.status")}</label>
					<div class="col-sm-10">
						<select form="create-issue-form" class="form-control" name="status" id="status">
							<option value="true" [#if issue?? && issue.open]selected[/#if]>${i18n.translate("label.open")}</option>
							<option value="false" [#if issue?? && !issue.open]selected[/#if]>${i18n.translate("label.closed")}</option>
						</select>
					</div>
				</div>
				[/#if]
				[#if issue?? && issue?has_content && !issue.open]					
                <div class="form-group">
					<label for="timestampClosed" class="col-sm-2 control-label">Closed on</label>
					<div class="col-sm-10">
						[#assign closedDate = issue.getClosed()]
						<input type="text" class="form-control" name="timestampClosed" id="timestampClosed" disabled value="${closedDate?string["EEEE dd MMMM yyyy HH:mm"]}"></input>
					</div>
				</div>
				[/#if]
				<div class="form-group">
					<label for="labels" class="col-sm-2 control-label">Labels</label>
					<div class="col-sm-10">
						[#if issue?? && issue?has_content]
							[#assign issuelabels = issue.getLabels()]
						[#else]
							[#assign issuelabels = []]
						[/#if]
						[#assign repoLabels = repositoryEntity.getLabels()]
						<select form="create-issue-form" class="form-control" name="labels" id="labels" multiple="multiple">
							[#list repoLabels as label]
							<option value="${label.labelId}" [#if issuelabels?seq_contains(label)]selected[/#if]>label.tag</option>
							[/#list]
						</select>
					</div>
				</div>
				<div class="form-group">
					<div class="col-sm-offset-2 col-sm-10">
						<div class="pull-right">
							<a href="[#if issue?? && issue?has_content]${issue.getURI()}[#else]${repositoryEntity.getURI()}issues[/#if]" class="btn btn-default">${i18n.translate("course.control.cancel")}</a>
							<button type="submit" id="submit" class="btn btn-primary">[#if issue??]${i18n.translate("course.control.save")}[#else]${i18n.translate("issue.create")}[/#if]</button>
						</div>
					</div>
				</div>
			</form>
    </div>
</div>
[@macros.renderScripts ]
	<script src="/static/vendor/multiselect/multiple-select.js"></script>
	<script type="text/javascript">
	$(function() {
		$('#labels').multipleSelect({
			selectAll: false,
			styler: function(value){
				[#list repositoryEntity.getLabels() as label]
				if(value == ${label.labelId}){
					return 'background-color: ${label.getColorAsHexString()};';
				}
				[/#list]
			}
		});
	});
	</script>
[/@macros.renderScripts ]
[@macros.renderFooter /]