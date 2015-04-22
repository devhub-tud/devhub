[#macro deadlineLabel delivery]
    [#if delivery.isLate()]
        <span class="label label-danger">${i18n.translate("assignment.handed-in-late")}</span>
    [/#if]
[/#macro]

[#macro buildLabel delivery group states]
    [#assign commitId = delivery.getCommitId()!]
    [#if commitId?? && commitId?has_content && states.hasStarted(commitId)]
        [#if states.hasFinished(commitId)]
            [#if states.hasSucceeded(commitId)]
            <span class="label label-success">${i18n.translate("build.state.succeeded")}</span>
            [#else]
            <span class="label label-success">${i18n.translate("build.state.failed")}</span>
            [/#if]
        [/#if]
    [/#if]
[/#macro]

[#macro deliveryStateLabel delivery]
    [#assign state = delivery.getState()]
    <span class="label label-${state.style}">${i18n.translate(state.translationKey)}</span>
[/#macro]

[#macro render delivery states]
    <div class="pull-right">
        [@deadlineLabel delivery/]

        [#if group?? && states?? && states?has_content]
            [@buildLabel delivery group states/]
        [/#if]

        [@deliveryStateLabel delivery/]
    </div>

    <dl>
        <dt>${i18n.translate("delivery.submitted-by")}</dt>
        <dd>${delivery.createdUser.getName()}</dd>
        <dt>${i18n.translate("delivery.date")}</dt>
        <dd>${delivery.getCreated()?string["EEEE dd MMMM yyyy HH:mm"]}</dd>
        [#if delivery.commitId?? && delivery.commitId?has_content]
            <dt>${i18n.translate("assignment.commit")}</dt>
            <dd><a href="/courses/${group.course.getCode()}/groups/${group.getGroupNumber()}/commits/${delivery.commitId}/diff">${delivery.commitId?substring(0, 8)}</a></dd>
        [/#if]
        [#if delivery.getNotes()?? && delivery.getNotes()?has_content]
            <dt>${i18n.translate("delivery.notes")}</dt>
            <dd>${delivery.getNotes()}</dd>
        [/#if]
    </dl>

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
[/#macro]