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
            <h4 style="line-height:34px; margin-top:0;">Issues</h4>
        </div>
    </div>

    <div class="row">
        <div class="col-md-2">
            [@projectFrameset.renderSidemenu "issues" i18n group![] repository/]
        </div>
        <div class="col-md-10">	
            <div class="panel panel-default">
                <div class="panel-body">
                    <a id="btn-add-issue" class="btn btn-primary pull-right" href="${repositoryEntity.getURI()}issues/create">Add Issue</a>
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
    </div>
</div>
[@macros.renderScripts /]
[@macros.renderFooter /]
