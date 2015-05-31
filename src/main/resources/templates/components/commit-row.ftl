[#macro render group states commitId href=""]
<tr>
    [#if group?? && states?? && states?has_content && commitId??]
        [#if states.hasStarted(commitId)]
            [#if states.hasFinished(commitId)]
                [#if states.hasSucceeded(commitId)]
                <td class="commit succeeded" id="${commitId}">
                    <a href="/courses/${group.course.code}/groups/${group.groupNumber}/commits/${commitId}/build">
                        <span class="state glyphicon glyphicon-ok-circle" title="${i18n.translate("build.state.succeeded")}"></span>
                    </a>
                    [#if href?? && href?has_content]<a href="${href}">[/#if]
                [#else]
                <td class="commit failed" id="${commitId}">
                    <a href="/courses/${group.course.code}/groups/${group.groupNumber}/commits/${commitId}/build">
                        <span class="state glyphicon glyphicon-remove-circle" title="${i18n.translate("build.state.failed")}"></span>
                    </a>
                    [#if href?? && href?has_content]<a href="${href}">[/#if]
                [/#if]
            [#else]
            <td class="commit running" id="${commitId}">
                [#if href?? && href?has_content]<a href="${href}">[/#if]
                <span class="state glyphicon glyphicon-align-justify" title="${i18n.translate("build.state.queued")}"></span>
            [/#if]
        [#else]
        <td class="commit ignored" id="${commitId}">
            [#if href?? && href?has_content]<a href="${href}">[/#if]
            <span class="state glyphicon glyphicon-unchecked"></span>
        [/#if]
    [#else]
        <td class="commit">
        [#if href?? && href?has_content]<a href="${href}">[/#if]
    [/#if]
    [#nested /]
    [#if href?? && href?has_content]</a>[/#if]
    </td>
</tr>
[/#macro]