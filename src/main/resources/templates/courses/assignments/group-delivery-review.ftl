[#import "../../macros.ftl" as macros]
[#import "../../components/project-frameset.ftl" as projectFrameset]
[#import "../../components/commit-row.ftl" as commitRow]

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
            <h4 style="line-height:34px; margin-top:0;">${i18n.translate("assignment.assignment-title", assignment.getAssignmentId(), assignment.getName())}</h4>
        </div>
    </div>

    <div class="row">
        <div class="col-md-2">
        [@projectFrameset.renderSidemenu "assignments" i18n group repository/]
        </div>
        <div class="col-md-10">
            <table class="table table-bordered">
            [#assign commitId = delivery.getCommitId()!]
            [@commitRow.render group states commitId!]
                <div class="pull-right">
                    [#assign state = delivery.getState()]
                    <span class="label label-${state.style}">${i18n.translate(state.translationKey)}</span>
                </div>

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
            </table>

            [#if user.isAdmin() || user.isAssisting(course)]
            [#assign review = delivery.getReview()!]
                <div class="panel panel-default">
                    <div class="panel-heading">${i18n.translate("assignment.review.title")}</div>
                    <div class="panel-body">
                        <form action="" method="post" target="_self">
                            <div class="row">
                                <div class="form-group col-md-6">
                                    <label for="state">${i18n.translate("delivery.status")}</label>
                                    <select class="form-control" name="state" id="state">
                                        [#if deliveryStates?? && deliveryStates?has_content]
                                            [#list deliveryStates as deliveryState]
                                                <option value="${deliveryState?string}" [#if review?? && review?has_content && review.getState() == deliveryState]selected[/#if]>
                                                    ${i18n.translate(deliveryState.getTranslationKey())}
                                                </option>
                                            [/#list]
                                        [/#if]
                                    </select>
                                </div>

                                <div class="form-group col-md-6">
                                    <label for="grade">${i18n.translate("delivery.grade")}</label>
                                    <input type="number" class="form-control" name="grade" id="grade" min="1" max="10" step="0.1" [#if review?? && review?has_content ]value="${review.getGrade()!}"[/#if]>
                                </div>
                            </div>

                            <div class="form-group">
                                <label for="commentary">${i18n.translate("delivery.remarks")}</label>
                                <textarea class="form-control" name="commentary" id="commentary" rows="5">[#if review?? && review?has_content ]${review.getCommentary()!}[/#if]</textarea>
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
