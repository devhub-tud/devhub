[#macro diffLine line index]

<tr data-source-commit="${line.sourceCommitId}" data-source-line-number="${line.sourceLineNumber}" data-source-file-name="${line.sourceFilePath}">

    [#if line.isRemoved()]
        <td class="ln delete">
            <a href="#${index}L${line.oldLineNumber}" id="${index}L${line.oldLineNumber}">${line.oldLineNumber}</a>
        </td>
        <td class="ln delete"></td>
        <td class="code delete">
            <a class="btn btn-xs btn-primary pull-left btn-comment"> <span class="octicon octicon-plus"></span></a>
            <pre>${line.content}</pre>
        </td>
    [#elseif line.isAdded()]
        <td class="ln add"></td>
        <td class="ln add">
          [#if lineWarnings??]
              [#assign warningsForLine = lineWarnings.retrieveWarnings(line.sourceCommitId, line.sourceFilePath, line.sourceLineNumber)]
              [#if warningsForLine?? && warningsForLine?has_content]
                <a class="warning"
                   data-container="body" data-toggle="popover" data-placement="right"
                   data-title="Warnings"
                   data-content-id="warnings-${index}R${line.newLineNumber}">
                    <i class="octicon octicon-primitive-dot"></i>
                    <div class="hidden" id="warnings-${index}R${line.newLineNumber}">
                      <table class="table table-bordered">
                  [#list warningsForLine as warning]
                        <tr><td>${warning.getMessage(i18n)}</td></tr>
                  [/#list]
                      </table>
                    </div>
                </a>
              [/#if]
          [/#if]
            <a href="#${index}R${line.newLineNumber}" id="${index}R${line.newLineNumber}">${line.newLineNumber}</a>
        </td>
        <td class="code add">
            <a class="btn btn-xs btn-primary pull-left btn-comment"> <span class="octicon octicon-plus"></span></a>
            <pre>${line.content}</pre>
        </td>
    [#else]
        <td class="ln">
            <a href="#${index}L${line.oldLineNumber}" id="${index}L${line.oldLineNumber}">${line.oldLineNumber}</a>
        </td>
        <td class="ln">
            <a href="#${index}R${line.newLineNumber}" id="${index}R${line.newLineNumber}">${line.newLineNumber}</a>
        </td>
        <td class="code">
            <a class="btn btn-xs btn-primary pull-left btn-comment"> <span class="octicon octicon-plus"></span></a>
            <pre>${line.content}</pre>
        </td>
    [/#if]
</tr>
[/#macro]