[#import "../../macros.ftl" as macros]
[#import "../../components/commit-row.ftl" as commitRow]

[@macros.renderHeader i18n.translate("form.build-server-setup.title") /]
[@macros.renderMenu i18n user /]

<div class="container">

    <ol class="breadcrumb">
        <li><a href="/courses">${ i18n.translate("section.courses") }</a></li>
        <li><a href="/courses/${course.getCode()}">${course.getCode()} - ${course.getName()}</a></li>
        <li>Assignment ${assignment.getAssignmentId()}: ${assignment.getName()}</li>
    </ol>

    [#if assignmentStats??]

        <div class="well well-sm">
            <!--
            <ul class="list-inline">
                <li><span class="label label-info pull-right">Submitted</span></li>
                <li><span class="label label-success pull-right">Approved</span></li>
                <li><span class="label label-warning pull-right">Disapproved</span></li>
                <li><span class="label label-danger pull-right">Rejected</span></li>
            </ul>
            -->
            <h5>Progress</h5>

            <div class="progress">
                <div class="progress-bar progress-bar-info" style="width: ${assignmentStats.getSubmittedPercentage()}%">
                    Submitted
                </div>
                <div class="progress-bar progress-bar-success" style="width: ${assignmentStats.getApprovedPercentage()}%">
                    Approved
                </div>
                <div class="progress-bar progress-bar-warning" style="width: ${assignmentStats.getDisapprovedPercentage()}%">
                    Disapproved
                </div>
                <div class="progress-bar progress-bar-danger" style="width: ${assignmentStats.getRejectedPercentage()}%">
                    Rejected
                </div>
            </div>


        </div>
    [/#if]

    [#if lastDeliveries?? && lastDeliveries?has_content]
    <table class="table table-bordered">
        [#list lastDeliveries as delivery]
            [#assign group = delivery.getGroup()]
            [@commitRow.render group states![] commitId![] "/courses/${course.getCode()}/groups/${group.getGroupNumber()}/assignments/${assignment.getAssignmentId()}"]
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
                <div class="comment"><strong>${delivery.getGroup().getGroupName()}</strong></div>
                <div class="committer">${delivery.createdUser.getName()} on ${delivery.getCreated()?string["EEEE dd MMMM yyyy HH:mm"]}</div>
            [/@commitRow.render]
        [/#list]
    </table>
    [/#if]

</div>
[@macros.renderScripts /]
[@macros.renderFooter /]
