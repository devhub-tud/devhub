[#macro deadlineLabel delivery]
    [#if delivery.isLate()]
        <span class="label label-danger">${i18n.translate("assignment.handed-in-late")}</span>
    [/#if]
[/#macro]

[#macro buildLabel delivery group builds]
    [#assign commitId = delivery.getCommit().getCommitId()!]
    [#if commitId?? && commitId?has_content]
    [#assign buildResult = builds[commitId]![]]
    <a href="${repositoryEntity.getURI()}commits/${commitId}/diff">
        [#if buildResult.hasFinished()]
            [#if buildResult.hasSucceeded()]
                <span class="label label-success">${i18n.translate("build.state.succeeded")}</span>
            [#else]
                <span class="label label-danger">${i18n.translate("build.state.failed")}</span>
            [/#if]
        [#else]
            <span class="label label-info">${commitId?substring(0, 6)}</span>
        [/#if]
    </a>
    [/#if]
[/#macro]

[#macro deliveryStateLabel delivery seesGrade]
    [#assign state = delivery.getState()]
    [#if !seesGrade]
        [#assign state = submittedState]
    [/#if]
    <span class="label label-${state.style}" data-toggle="tooltip" title="${i18n.translate(state.messageTranslationKey)}">
        ${i18n.translate(state.translationKey)}
    </span>
[/#macro]

[#macro render delivery states]
    <div class="pull-right">
        [@deadlineLabel delivery/]

        [#if group?? && states?? && states?has_content]
            [@buildLabel delivery group states/]
        [/#if]

        [@deliveryStateLabel delivery seeGrade/]
    </div>

    <dl>
        <dt>${i18n.translate("delivery.submitted-by")}</dt>
        <dd>${delivery.createdUser.getName()}</dd>
        <dt>${i18n.translate("delivery.date")}</dt>
        <dd>${delivery.getTimestamp()?string["EEEE dd MMMM yyyy HH:mm"]}</dd>
        [#if delivery.commit?? && delivery.commit?has_content]
            <dt>${i18n.translate("assignment.commit")}</dt>
            <dd><a href="${delivery.commit.getDiffURI()}">${delivery.commit.commitId?substring(0, 8)}</a></dd>
        [/#if]
        [#if delivery.getNotes()?? && delivery.getNotes()?has_content]
            <dt>${i18n.translate("delivery.notes")}</dt>
            <dd>[#list delivery.getNotes()?split("\n") as line]${line}[#if line_has_next]<br/>[/#if][/#list]</dd>
        [/#if]

        [#if user.isAdmin() || user.isAssisting(course)]
            [#assign assignedTA = assignment.getAssignedTA(delivery).orElse(null)![]]
            <dt>Assigned TA</dt>
            <dd>
            [#if assignedTA?? && assignedTA?has_content]
              <a href="#" id="group" data-mode="inline" data-type="select" data-pk="${delivery.deliveryId}" data-value="${assignedTA.id}" data-url="${delivery.URI}assign-ta" data-source="${delivery.assignment.courseEdition.URI}teaching-assistants.json" data-title="Select teaching assistant">${assignedTA.name}</a>
            [#else]
	            <a href="#" id="group" data-mode="inline" data-type="select" data-pk="${delivery.deliveryId}" data-url="${delivery.URI}assign-ta" data-source="${delivery.assignment.courseEdition.URI}teaching-assistants.json" data-title="Select teaching assistant">Not assigned</a>
            [/#if]
            </dd>
        [/#if]
    </dl>

    [#assign attachments = delivery.getAttachments()!]
    [#if attachments?has_content]
    <ul class="list-inline">
        [#list attachments as attachment]
            <li>
                <a class="btn btn-link btn-sm" target="_blank" href="${attachment.getURI()}">
                    <span class="glyphicon glyphicon-file aria-hidden="true"></span>
                    ${attachment.getFileName()}
                </a>
            </li>
        [/#list]
    </ul>
    [/#if]

    [#assign review = delivery.getReview()!]
    [#if review?? && seeGrade && review?has_content && (review.getGrade()?? || review.getCommentary()??)]
    <blockquote>
        <dl>
            [#if review.getGrade()??]
                <dt>${i18n.translate("delivery.grade")}</dt>
                <dd>${review.getGrade()?string["0.#"]}</dd>
            [/#if]
            [#if review.getCommentary()??]
                <dt>${i18n.translate("delivery.remarks")}</dt>
                <dd>[#list review.getCommentary()?split("\n") as line]${line}[#if line_has_next]<br/>[/#if][/#list]</dd>
            [/#if]
        </dl>
        [#assign assignedTA = assignment.getAssignedTA(delivery).orElse(review.reviewUser)![]]
        [#if assignedTA?? && assignedTA?has_content]
            <footer class="small">${assignedTA.getName()} on ${review.getReviewTime()?string["EEEE dd MMMM yyyy HH:mm"]}</footer>
        [/#if]
    </blockquote>
    [/#if]
[/#macro]
