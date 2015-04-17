[#import "../../macros.ftl" as macros]
[#import "../../components/project-frameset.ftl" as projectFrameset]
[@macros.renderHeader i18n.translate("section.projects") /]
[@macros.renderMenu i18n user /]
<div class="container">

    [@projectFrameset.renderBreadcrumb i18n group/]

    <div class="row">
        <div class="col-md-10 col-md-offset-2">
            <h4 style="line-height:34px; margin-top:0;">Assignments</h4>
        </div>
    </div>

    <div class="row">
        <div class="col-md-2">
            [@projectFrameset.renderSidemenu "assignments" i18n group repository/]
        </div>
        <div class="col-md-10">
        [#assign assignments = course.getAssignments()]
            <table class="table table-bordered">
                <colgroup>
                    <col span="1" width="5%"/>
                    <col span="1" width="65%"/>
                    <col span="1" width="10%"/>
                    <col span="1" width="10%"/>
                    <col span="1" width="10%"/>
                </colgroup>
            [#if assignments?? && assignments?has_content]
                <thead>
                <tr>
                    <th>#</th>
                    <th>Assignment</th>
                    <th>Due date</th>
                    <th>Grade</th>
                    <th>Status</th>
                </tr>
                </thead>
            [/#if]
                <tbody>
                [#if assignments?? && assignments?has_content]
                    [#list assignments as assignment]
                    [#assign delivery = deliveries.getLastDelivery(assignment, group)!]
                    <tr>
                        <td>
                            <a href="/courses/${course.getCode()}/groups/${group.getGroupNumber()}/assignments/${assignment.getAssignmentId()}">
                            ${assignment.getAssignmentId()!"-"}
                            </a>
                        </td>
                        <td>
                            <a href="/courses/${course.getCode()}/groups/${group.getGroupNumber()}/assignments/${assignment.getAssignmentId()}">
                            ${assignment.getName()!"-"}
                            </a>
                        </td>
                        <td>
                            [#if assignment.getDueDate()??]
                                <a href="/courses/${course.getCode()}/groups/${group.getGroupNumber()}/assignments/${assignment.getAssignmentId()}">
                                ${assignment.getDueDate()}
                                </a>
                            [/#if]
                        </td>
                        <td>
                            [#if delivery?has_content && delivery.getReview()??]
                            [#assign review = delivery.getReview()]
                            ${review.getGrade()!"-"}
                            [/#if]
                        </td>
                        <td>
                            [#if delivery?has_content]
                                [#if delivery.isSubmitted()]
                                    <a class="label label-info" href="/courses/${course.getCode()}/groups/${group.getGroupNumber()}/assignments/${assignment.getAssignmentId()}">
                                        Submitted
                                    </a>
                                [#elseif delivery.isApproved()]
                                    <a class="label label-success" href="/courses/${course.getCode()}/groups/${group.getGroupNumber()}/assignments/${assignment.getAssignmentId()}">
                                        Approved
                                    </a>
                                [#elseif delivery.isRejected()]
                                    <a class="label label-warning" href="/courses/${course.getCode()}/groups/${group.getGroupNumber()}/assignments/${assignment.getAssignmentId()}">
                                        Rejected
                                    </a>
                                [#elseif delivery.isDisapproved()]
                                    <a class="label label-danger" href="/courses/${course.getCode()}/groups/${group.getGroupNumber()}/assignments/${assignment.getAssignmentId()}">
                                        Disapproved
                                    </a>
                                [/#if]
                            [#else]
                                <a class="label label-default" href="/courses/${course.getCode()}/groups/${group.getGroupNumber()}/assignments/${assignment.getAssignmentId()}">
                                    Not submitted
                                </a>
                            [/#if]
                        </td>
                    </tr>
                    [/#list]
                [#else]
                <tr>
                    <td class="muted" colspan="5">
                        There are no assignments for this course.
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
