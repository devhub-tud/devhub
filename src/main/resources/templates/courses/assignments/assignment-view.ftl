[#import "../../macros.ftl" as macros]
[#import "../../components/commit-row.ftl" as commitRow]

[@macros.renderHeader i18n.translate("assignments.title") /]
[@macros.renderMenu i18n user /]

<div class="container">

    <ol class="breadcrumb">
        <li><a href="/courses">${ i18n.translate("section.courses") }</a></li>
        <li><a href="/courses/${course.getCode()}">${course.getCode()} - ${course.getName()}</a></li>
        <li>${assignment.getName()}</li>
    </ol>

[#if assignmentStats??]
    <div class="well well-sm">
        <h5><strong>Progress</strong></h5>

        <div class="progress">
            [#list deliveryStates as state]
                <div class="progress-bar progress-bar-${state.style}" style="width: ${assignmentStats.getPercentageFor(state)}%">
                    ${i18n.translate(state.translationKey)}
                </div>
            [/#list]
        </div>

        <div class="row">
            [#list deliveryStates as state]
                <div class="col-md-2 progress-info">
                    <span class="text-${state.style} glyphicon glyphicon-stop"></span>
                    ${i18n.translate(state.translationKey)}: ${assignmentStats.getCountFor(state)} (${assignmentStats.getPercentageFor(state)}%)
                </div>
            [/#list]
            <div class="col-md-2 progress-info">${i18n.translate("assignment.submissions")}: ${assignmentStats.amountOfSubmissions()}</div>
            <div class="col-md-2 progress-info">${i18n.translate("assignment.groups")}: ${assignmentStats.amountOfGroups()}</div>
        </div>
    </div>
[/#if]

    <table class="table table-bordered">
    [#if lastDeliveries?? && lastDeliveries?has_content]
        [#list lastDeliveries as delivery]
            [#assign group = delivery.getGroup()]
            [@commitRow.render group states![] commitId![] "/courses/${course.getCode()}/groups/${group.getGroupNumber()}/assignments/${assignment.getAssignmentId()}"]
                <div class="pull-right">
                [#if delivery.isLate()]
                    <span class="label label-danger">${i18n.translate("assignment.handed-in-late")}</span>
                [/#if]

                [#assign review = delivery.getReview()![]]
                [#if review?has_content && review.grade?? && review.grade?has_content]
                    <span class="label label-default">${review.grade}</span>
                [/#if]

                [#assign state = delivery.getState()]
                    <span class="label label-${state.style}">${i18n.translate(state.translationKey)}</span>
                </div>
                <div class="comment"><strong>${delivery.getGroup().getGroupName()}</strong></div>
                <div class="committer">${delivery.createdUser.getName()} on ${delivery.getCreated()?string["EEEE dd MMMM yyyy HH:mm"]}</div>
            [/@commitRow.render]
        [/#list]
    [#else]
        <tr>
            <td class="muted">${i18n.translate("assignment.no-deliveries")}</td>
        </tr>
    [/#if]
    </table>
    
    <div>
        <a href="/courses/${course.code}/assignments/${assignment.getAssignmentId()}/deliveries/download" class="pull-right btn btn-sm btn-default" style="margin-right:5px;"><i class="glyphicon glyphicon-floppy-save"></i> Download grades</a>
    </div>

</div>
[@macros.renderScripts /]
[@macros.renderFooter /]
