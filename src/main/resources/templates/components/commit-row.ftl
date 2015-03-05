[#macro render states commit]
<tr>
    [#if commit?? && commit?has_content]
        [#if states.hasStarted(commit.getCommit())]
            [#if states.hasFinished(commit.getCommit())]
                [#if states.hasSucceeded(commit.getCommit())]
                <td class="commit succeeded" id="${commit.getCommit()}">
                    <a href="/courses/${group.course.code}/groups/${group.groupNumber}/commits/${commit.getCommit()}/build">
                        <span class="state glyphicon glyphicon-ok-circle" title="Build succeeded!"></span>
                    </a>
                [#else]
                <td class="commit failed" id="${commit.getCommit()}">
                    <a href="/courses/${group.course.code}/groups/${group.groupNumber}/commits/${commit.getCommit()}/build">
                        <span class="state glyphicon glyphicon-remove-circle" title="Build failed!"></span>
                    </a>
                [/#if]
            [#else]
            <td class="commit running" id="${commit.getCommit()}">
                <span class="state glyphicon glyphicon-align-justify" title="Build queued..."></span>
            [/#if]
        [#else]
        <td class="commit ignored" id="${commit.getCommit()}">
            <span class="state glyphicon glyphicon-unchecked"></span>
        [/#if]
    [#else]
        <td class="commit">
    [/#if]
    [#nested /]
    </td>
</tr>
[/#macro]