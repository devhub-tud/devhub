[#import "../../macros.ftl" as macros]
[#import "../../components/project-frameset.ftl" as projectFrameset]
[#import "../../components/commit-row.ftl" as commitRow]

[#macro deliveryStateButton user course delivery]
    <div class="pull-right">
    [#assign state = delivery.getState()]
    [#if user.isAdmin() || user.isAssisting(course)]
        <div class="btn-group">
            <button type="button" class="btn btn-${state.style}">${i18n.translate(state.translationKey)}</button>
            <button type="button" class="btn btn-${state.style} dropdown-toggle" data-toggle="dropdown" aria-expanded="false">
                <span class="caret"></span>
                <span class="sr-only">${i18n.translate("button.label.options")}</span>
            </button>
            <ul class="dropdown-menu" role="menu">
                <li><a href="deliveries/${delivery.getDeliveryId()}/review">${i18n.translate("button.label.review")}</a></li>
            </ul>
        </div>
    [#else]
        <span class="label label-${state.style}">${i18n.translate(state.translationKey)}</span>
    [/#if]
    </div>
[/#macro]

[@macros.renderHeader i18n.translate("section.projects") /]
[@macros.renderMenu i18n user /]
<div class="container">

    [@projectFrameset.renderBreadcrumb i18n group/]

    <div class="row">
        <div class="col-md-10 col-md-offset-2">
        [#if user.isAdmin() || user.isAssisting(course)]
            <a href="/courses/${course.code}/assignments/${assignment.assignmentId}" class="btn btn-default pull-right">
                <span class="glyphicon glyphicon-chevron-left"></span>
            ${i18n.translate("assignment.go-back-to-assignment")}
            </a>
        [/#if]
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
            [#if myDeliveries?? && myDeliveries?has_content]
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
                                        <dt>${i18n.translate("delivery.grade")}</dt>
                                        <dd>${review.getGrade()}</dd>
                                    [/#if]
                                    [#if review.getCommentary()??]
                                        <dt>${i18n.translate("delivery.remarks")}</dt>
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
                [#if user.isAdmin() || user.isAssisting(course)]
                    ${i18n.translate("assignment.no-submission.assistant")}
                [#else]
                    ${i18n.translate("assignment.no-submission.member")}
                [/#if]
                    </td>
                </tr>
            [/#if]
            </table>

            [#if canSubmit?? && canSubmit && group.getMembers()?seq_contains(user)]
            <div class="panel panel-default">
                <div class="panel-heading">${i18n.translate("assignment.submit.title")}</div>
                <div class="panel-body">
                    <form action="" method="post" target="_self" enctype="multipart/form-data">
                        <div class="form-group">
                            <label>${i18n.translate("course.control.assignment")}</label>
                            <input type="text" class="form-control" value="${assignment.getName()}" disabled>
                        </div>

                        <div class="form-group">
                            <label for="commit-id">${i18n.translate("assignment.commit")}</label>
                            <select class="form-control" name="commit-id" id="commit-id">
                    [#if recentCommits?? && recentCommits?has_content]
                        [#list recentCommits as commit]
                                <option value="${commit.getCommit()}">${commit.getMessage()} (${(commit.getTime() * 1000)?number_to_datetime?string["EEEE dd MMMM yyyy HH:mm"]})</option>
                        [/#list]
                    [#else]
                                <option value="">${i18n.translate("assignment.no-commit")}</option>
                    [/#if]
                            </select>
                        </div>

                        <div class="form-group">
                            <label for="notes">${i18n.translate("delivery.notes")}</label>
                            <textarea class="form-control" name="notes" id="notes" rows="3"></textarea>
                        </div>

                        <div class="form-group">
                            <label for="file-attachment">${i18n.translate("delivery.file-attachment")}</label>
                            <input type="file" id="file-attachment" name="file-attachment">
                        </div>

                        <button type="submit" class="btn btn-primary pull-right">${i18n.translate("button.label.submit")}</button>
                    </form>
                </div>
            </div>
            [/#if]
        </div>
    </div>

</div>
[@macros.renderScripts /]
[@macros.renderFooter /]
