[#import "../../macros.ftl" as macros]
[#import "../../components/project-frameset.ftl" as projectFrameset]
[#import "../../components/commit-row.ftl" as commitRow]

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
            <table class="table table-bordered">
            [#assign commitId = delivery.getCommitId()!]
            [@commitRow.render group states commitId!]
                [#if delivery.isSubmitted()]
                    <span class="label label-info pull-right">Submitted</span>
                [#elseif delivery.isApproved()]
                    [#assign approved = true]
                    <span class="label label-success pull-right">Approved</span>
                [#elseif delivery.isRejected()]
                    <span class="label label-warning pull-right">Rejected</span>
                [#elseif delivery.isDisapproved()]
                    <span class="label label-danger pull-right">Disapproved</span>
                [/#if]

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
            </table>

            [#if user.isAdmin() || user.isAssisting(course)]
            [#assign review = delivery.getReview()!]
                <div class="panel panel-default">
                    <div class="panel-heading">Review assignment</div>
                    <div class="panel-body">
                        <form action="" method="post" target="_self">
                            <div class="row">
                                <div class="form-group col-md-6">
                                    <label for="state">Status</label>
                                    <select class="form-control" name="state" id="state">
                                        [#if deliveryStates?? && deliveryStates?has_content]
                                            [#list deliveryStates as deliveryState]
                                                <option value="${deliveryState?string}" [#if review?? && review?has_content && review.getState() == deliveryState]selected[/#if]>
                                                    ${ i18n.translate(deliveryState.getTranslationKey())}
                                                </option>
                                            [/#list]
                                        [/#if]
                                    </select>
                                </div>

                                <div class="form-group col-md-6">
                                    <label for="grade">Grade</label>
                                    <input type="number" class="form-control" name="grade" id="grade" min="1" max="10" step="0.1" [#if review?? && review?has_content ]value="${review.getGrade()!}"[/#if]>
                                </div>
                            </div>

                            <div class="form-group">
                                <label for="commentary">Remarks</label>
                                <textarea class="form-control" name="commentary" id="commentary" rows="5">[#if review?? && review?has_content ]${review.getCommentary()!}[/#if]</textarea>
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
