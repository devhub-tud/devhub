[#import "../../macros.ftl" as macros]
[#import "../../components/project-frameset.ftl" as projectFrameset]
[#import "../../components/delivery.ftl" as deliveryElement]

[@macros.renderHeader i18n.translate("section.projects") /]
[@macros.renderMenu i18n user /]
<div class="container">

    [@projectFrameset.renderBreadcrumb i18n group/]

    <div class="row">
        <div class="col-md-10 col-md-offset-2">
        [#if user.isAdmin() || user.isAssisting(course)]
            <a href="${course.getURI()}assignments/${assignment.assignmentId}" class="btn btn-default pull-right">
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
                <tr>
                    <td>[@deliveryElement.render delivery builds/]</td>
                </tr>
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
