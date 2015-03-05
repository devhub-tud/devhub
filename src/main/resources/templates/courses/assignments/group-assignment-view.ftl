[#import "../../macros.ftl" as macros]
[#import "../../components/project-frameset.ftl" as projectFrameset]
[#import "../../components/commit-row.ftl" as commitRow]

[#macro deliveryStateButton user course delivery]
    [#if user.isAdmin() || user.isAssisting(course)]
        <div class="btn-group pull-right">
        [#if delivery.isSubmitted()]
            <button type="button" class="btn btn-info">Submitted</button>
        <button type="button" class="btn btn-info dropdown-toggle" data-toggle="dropdown" aria-expanded="false">
        [#elseif delivery.isApproved()]
            [#assign approved = true]
            <button type="button" class="btn btn-success">Approved</button>
        <button type="button" class="btn btn-success dropdown-toggle" data-toggle="dropdown" aria-expanded="false">
        [#elseif delivery.isDisapproved()]
            <button type="button" class="btn btn-warning">Disapproved</button>
        <button type="button" class="btn btn-warning dropdown-toggle" data-toggle="dropdown" aria-expanded="false">
        [#elseif delivery.isRejected()]
            <button type="button" class="btn btn-danger">Rejected</button>
        <button type="button" class="btn btn-danger dropdown-toggle" data-toggle="dropdown" aria-expanded="false">
        [/#if]
                <span class="caret"></span>
                <span class="sr-only">Options</span>
            </button>
            <ul class="dropdown-menu" role="menu">
                <li><a href="deliveries/${delivery.getDeliveryId()}/review">Review</a></li>
            </ul>
        </div>
    [#else]
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
    [/#if]
[/#macro]

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
                    [#assign commitId = delivery.getCommitId()!]
                    [@commitRow.render group states commitId! ""]
                        [@deliveryStateButton user course delivery/]

                        <div class="committer">${delivery.createdUser.getName()} on ${delivery.getCreated()?string["EEEE dd MMMM yyyy HH:mm"]}</div>

                        [#if delivery.getNotes()??]
                            <p>${delivery.getNotes()}</p>
                        [/#if]

                        [#assign attachments = delivery.getAttachments()!]
                        [#if attachments?has_content]
                            <ul class="list-inline">
                            [#list attachments as attachment]
                                <li>
                                <a class="btn btn-link btn-sm" target="_blank" href="/courses/${group.course.getCode()}/groups/${group.getGroupNumber()}/assignments/${assignment.getAssignmentId()}/attachment/${attachment.getPath()?url('ISO-8859-1')}">
                                    <span class="glyphicon glyphicon-file aria-hidden="true"></span>
                                ${attachment.getFileName()}
                                </a>
                                </li>
                            [/#list]
                            </ul>
                        [/#if]

                        [#assign review = delivery.getReview()!]
                        [#if review?? && review?has_content && (review.getGrade()?? || review.getCommentary()??)]
                            <blockquote>
                                <dl>
                                    [#if review.getGrade()??]
                                        <dt>Grade</dt>
                                        <dd>${review.getGrade()}</dd>
                                    [/#if]
                                    [#if review.getCommentary()??]
                                        <dt>Remarks</dt>
                                        <dd>${review.getCommentary()}</dd>
                                    [/#if]
                                </dl>
                                <footer class="small">${review.reviewUser.getName()} on ${review.getReviewTime()?string["EEEE dd MMMM yyyy HH:mm"]}</footer>
                            </blockquote>
                        [/#if]
                    [/@commitRow.render]
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
            [#elseif group.getMembers()?seq_contains(user)]
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
