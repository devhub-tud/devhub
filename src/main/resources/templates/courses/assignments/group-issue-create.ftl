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
            <h4 style="line-height:34px; margin-top:0;">${i18n.translate("issue.create")}</h4>
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
						<input type="text" class="form-control" name="title" id="title" placeholder="${i18n.translate("label.title")}">
					</div>
				</div>
				<div class="form-group">
					<label for="description" class="col-sm-2 control-label">${i18n.translate("label.description")}</label>
					<div class="col-sm-10">
						<textarea rows="8" class="form-control" name="description" id="description" placeholder="${i18n.translate("label.description")}"></textarea>
					</div>
				</div>
                <div class="form-group">
					<label for="assignee" class="col-sm-2 control-label">${i18n.translate("label.assignee")}</label>
					<div class="col-sm-10">
						<select form="create-issue-form" class="form-control" name="assignee" id="assignee">
							[#list repositoryEntity.getCollaborators() as collaborator]
							<option value="${collaborator.netId}">${collaborator.name}</option>
							[/#list]
						</select>
					</div>
				</div>
				<div class="form-group">
					<div class="col-sm-offset-2 col-sm-10">
						<div class="pull-right">
							<a href="#" class="btn btn-default">${i18n.translate("course.control.cancel")}</a>
							<button type="submit" class="btn btn-primary">${i18n.translate("issue.create")}</button>
						</div>
					</div>
				</div>

			</form>
    </div>
</div>
[@macros.renderScripts /]
[@macros.renderFooter /]
