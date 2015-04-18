[#import "../../macros.ftl" as macros]
[#import "../../components/commit-row.ftl" as commitRow]

[@macros.renderHeader i18n.translate("assignments.title") /]
[@macros.renderMenu i18n user /]

<div class="container">

    <ol class="breadcrumb">
        <li><a href="/courses">${ i18n.translate("section.courses") }</a></li>
        <li><a href="/courses/${course.getCode()}">${course.getCode()} - ${course.getName()}</a></li>
        <li>Assignment ${assignment.getAssignmentId()}: ${assignment.getName()}</li>
    </ol>

    [#if assignmentStats??]

        <div class="well well-sm">
            <h5><strong>Progress</strong></h5>

            <div class="progress">
                <div class="progress-bar progress-bar-info" style="width: ${assignmentStats.getSubmittedPercentage()}%">
                    Submitted
                </div>
                <div class="progress-bar progress-bar-success" style="width: ${assignmentStats.getApprovedPercentage()}%">
                    Approved
                </div>
                <div class="progress-bar progress-bar-warning" style="width: ${assignmentStats.getRejectedPercentage()}%">
                    Rejected
                </div>
                <div class="progress-bar progress-bar-danger" style="width: ${assignmentStats.getDisapprovedPercentage()}%">
                    Disapproved
                </div>
            </div>

            <div class="row">
                <div class="col-md-2 progress-info">
                    Submitted: ${assignmentStats.getSubmittedCount()} (${assignmentStats.getSubmittedPercentage()}%)
                </div>
                <div class="col-md-2 progress-info">
                    Approved: ${assignmentStats.getApprovedCount()} (${assignmentStats.getApprovedPercentage()}%)
                </div>
                <div class="col-md-2 progress-info">
                    Rejected: ${assignmentStats.getRejectedCount()} (${assignmentStats.getRejectedPercentage()}%)
                </div>
                <div class="col-md-2 progress-info">
                    Disapproved: ${assignmentStats.getDisapprovedCount()} (${assignmentStats.getDisapprovedPercentage()}%)
                </div>
                <div class="col-md-2 progress-info">Submissions: ${assignmentStats.amountOfSubmissions()}</div>
                <div class="col-md-2 progress-info">Groups: ${assignmentStats.amountOfGroups()}</div>
            </div>

        </div>
    [/#if]

    <table class="table table-bordered">
    [#if lastDeliveries?? && lastDeliveries?has_content]
        [#list lastDeliveries as delivery]
            [#assign group = delivery.getGroup()]
            [@commitRow.render group states![] commitId![] "/courses/${course.getCode()}/groups/${group.getGroupNumber()}/assignments/${assignment.getAssignmentId()}"]
                <div class="pull-right">

                [#assign review = delivery.getReview()![]]
                [#if review?has_content && review.grade?? && review.grade?has_content]
                    <span class="label label-default">${review.grade}</span>
                [/#if]

                [#if delivery.isSubmitted()]
                    <span class="label label-info">Submitted</span>
                [#elseif delivery.isApproved()]
                    [#assign approved = true]
                    <span class="label label-success">Approved</span>
                [#elseif delivery.isRejected()]
                    <span class="label label-warning">Rejected</span>
                [#elseif delivery.isDisapproved()]
                    <span class="label label-danger">Disapproved</span>
                [/#if]
                </div>
                <div class="comment"><strong>${delivery.getGroup().getGroupName()}</strong></div>
                <div class="committer">${delivery.createdUser.getName()} on ${delivery.getCreated()?string["EEEE dd MMMM yyyy HH:mm"]}</div>
            [/@commitRow.render]
        [/#list]
    [#else]
        <tr>
            <td class="muted">No deliveries for assignment yet</td>
        </tr>
    [/#if]
    </table>
    
    <div>
        <a href="/courses/${group.course.code}/assignments/${assignment.getAssignmentId()}/deliveries/download" class="pull-right btn btn-sm btn-default" style="margin-right:5px;"><i class="glyphicon glyphicon-floppy-save"></i> Download grades</a>
    </div>

</div>
[@macros.renderScripts /]
[@macros.renderFooter /]
