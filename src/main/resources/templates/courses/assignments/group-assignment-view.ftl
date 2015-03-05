[#import "../../macros.ftl" as macros]
[#import "../../components/project-frameset.ftl" as projectFrameset]
[@macros.renderHeader i18n.translate("section.projects") /]
[@macros.renderMenu i18n user /]
<div class="container">

    [@projectFrameset.renderBreadcrumb i18n group/]

    <div class="row">
        <div class="col-md-10 col-md-offset-2">
            <h4 style="line-height:34px; margin-top:0;">${assignment.getAssignmentId()}. ${assignment.getName()}</h4>
        </div>
    </div>

    <div class="row">
        <div class="col-md-2">
        [@projectFrameset.renderSidemenu "assignments" i18n group repository/]
        </div>
        <div class="col-md-10">
            [#if assignment.getSummary()??]
                <p>${assignment.getSummary()}</p>
            [/#if]

            [#if assignment.getDueDate()??]
                <p>
                    <strong>Due date</strong>
                    <span>${assignment.getDueDate()}</span>
                </p>
            [/#if]

            <table class="table table-bordered">
            [#assign myDeliveries = deliveries.getDeliveries(assignment, group)!]
            [#if myDeliveries?has_content]
                [#list myDeliveries as delivery]
                    <tr>
                        <td>
                        [#if delivery.isSubmitted()]
                            <span class="label label-info pull-right">Submitted</span>
                        [#elseif delivery.isApproved()]
                            [#assign approved = true]
                            <span class="label label-success pull-right">Approved</span>
                        [#elseif delivery.isDisapproved()]
                            <span class="label label-warning pull-right">Disapproved</span>
                        [#elseif delivery.isRejected()]
                            <span class="label label-danger pull-right">Rejected</span>
                        [/#if]

                            <span class="small">${delivery.getCreated()?string["EEEE dd MMMM yyyy HH:mm"]} by ${delivery.createdUser.getName()}</span>
                        [#if delivery.getNotes()??]
                            <p>${delivery.getNotes()}</p>
                        [/#if]

                        [#assign attachments = delivery.getAttachments()!]
                        [#if attachments?has_content]
                            [#list attachments as attachment]
                                <a class="btn btn-link btn-sm" target="_blank" href="/courses/${group.course.getCode()}/groups/${group.getGroupNumber()}/assignments/${assignment.getAssignmentId()}/attachment/${attachment.getPath()?url('ISO-8859-1')}">
                                    <span class="glyphicon glyphicon-file aria-hidden="true"></span>
                                    ${attachment.getFileName()}</a>
                            [/#list]
                        [/#if]


                    </tr>
                [/#list]
            [#else]
                <tr>
                    <td>
                        No documents have been uploaded yet.
                    </td>
                </tr>
            [/#if]
            </table>

            [#if approved??]
                <!-- APPROVED, hide submit panel -->
            [#else]
            <div class="panel panel-default">
                <div class="panel-heading">Submit assignment</div>
                <div class="panel-body">
                    <form action="" method="post" target="_self" enctype="multipart/form-data">
                        <div class="form-group">
                            <label>Assignment</label>
                            <input type="text" class="form-control" value="${assignment.getName()}" disabled>
                        </div>

                        <div class="form-group">
                            <label for="commit-id">Commit</label>
                            <select class="form-control" name="commit-id" id="commit-id">
                                <option value="">No commit</option>
                    [#if recentCommits?? && recentCommits?has_content]
                    [#list recentCommits as commit]
                                <option value="${commit.getCommit()}">${commit.getMessage()} (${(commit.getTime() * 1000)?number_to_datetime?string["EEEE dd MMMM yyyy HH:mm"]})</option>
                    [/#list]
                    [/#if]
                            </select>
                        </div>

                        <div class="form-group">
                            <label for="notes">Notes</label>
                            <textarea class="form-control" name="notes" id="notes" rows="3"></textarea>
                        </div>

                        <div class="form-group">
                            <label for="file-attachment">File attachment</label>
                            <input type="file" id="file-attachment" name="file-attachment">
                        </div>

                        <button type="submit" class="btn btn-primary pull-right">Submit</button>
                    </form>
                </div>
            </div>
            [/#if]
        </div>
    </div>

</div>
[@macros.renderScripts /]
[@macros.renderFooter /]
