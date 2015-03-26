[#macro diffLine diffModel commit line index]

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