[#macro render group buildResult commitId href=""]
<tr>
    [#if buildResult?? && commitId?? && commitId?has_content]
        [#if buildResult?has_content]
            [#if buildResult.hasFinished()]
                [#if buildResult.hasSucceeded()]
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
        <td class="commit" style="width:100%; min-height: 44px;">
        [#if href?? && href?has_content]<a href="${href}">[/#if]
    [/#if]
    [#nested /]
    [#if href?? && href?has_content]</a>[/#if]
    </td>
</tr>
[/#macro]