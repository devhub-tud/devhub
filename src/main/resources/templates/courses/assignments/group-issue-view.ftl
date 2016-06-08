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
						<p type="text" class="form-control" name="title" id="title">${issue.title}"</p>
					</div>
				</div>
				<div class="form-group">
					<label for="description" class="col-sm-2 control-label">${i18n.translate("label.description")}</label>
					<div class="col-sm-10">
						<textarea disabled style="background-color: #ffffff; cursor: auto" rows="8" class="form-control"[#if issue??]>${issue.description}</textarea>[#else]placeholder="${i18n.translate("label.description")}"></textarea>[/#if]
					</div>
				</div>				
                <div class="form-group">
					<label for="timestampOpened" class="col-sm-2 control-label">Opened on</label>
					<div class="col-sm-10">
						<p type="text" class="form-control" name="timestampOpened" id="timestampOpened">${issue.timestamp?string["EEEE dd MMMM yyyy HH:mm"]}</p>
					</div>
				</div>
                <div class="form-group">
					<label for="assignee" class="col-sm-2 control-label">${i18n.translate("label.assignee")}</label>
					<div class="col-sm-10">
						<p type="text" class="form-control">${issue.assignee.name}</p>
					</div>
				</div>				
                <div class="form-group">
					<label for="status" class="col-sm-2 control-label">${i18n.translate("delivery.status")}</label>
					<div class="col-sm-10">
						<p type="text" class="form-control">[#if issue?? && issue.open]Open[#else]Closed[/#if]</p>
					</div>
				</div>
				[#if !issue.open]
                <div class="form-group">
					<label for="timestampClosed" class="col-sm-2 control-label">Closed on</label>
					<div class="col-sm-10">
						[#assign closedDate = issue.getClosed()]
						<input type="text" class="form-control" name="timestampClosed" id="timestampClosed" readonly value="${closedDate?string["EEEE dd MMMM yyyy HH:mm"]}"></input>
					</div>
				</div>
				[/#if]
				<div class="form-group">
					<div class="col-sm-offset-2 col-sm-10">
						<div class="pull-right">
							<a href="${issue.getURI()}edit" class="btn btn-primary">${i18n.translate("label.edit")}</a>
						</div>
					</div>
				</div>

			</form>
    </div>
</div>
[@macros.renderScripts /]
[@macros.renderFooter /]
