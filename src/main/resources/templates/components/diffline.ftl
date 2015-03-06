[#macro diffLine diffModel blame commit line index oldLineNumber newLineNumber]
<tr
    [#if line.isAdded()]
            data-source-commit="${commit.getCommit()}"
            data-source-line-number="${newLineNumber - 1}"
            data-source-file-name="${diffModel.newPath}">
    [#else]
        [#assign block = blame.getBlameBlock(oldLineNumber -1)]
        data-source-commit="${block.getFromCommitId()}"
        data-source-line-number="${block.getFromLineNumber(oldLineNumber - 1)}"
        data-source-file-name="${block.getFromFilePath()}">
    [/#if]
    [#if line.isRemoved()]
        <td class="ln delete">
            <a href="#${index}L${oldLineNumber}" id="${index}L${oldLineNumber}">${oldLineNumber}</a>
        </td>
        <td class="ln delete"></td>
        <td class="code delete">
            <a class="btn btn-xs btn-primary pull-left btn-comment"> <span class="octicon octicon-plus"></span></a>
            <pre>${line.content}</pre>
        </td>
    [#elseif line.isAdded()]
        <td class="ln add"></td>
        <td class="ln add">
            <a href="#${index}R${newLineNumber}" id="${index}R${newLineNumber}">${newLineNumber}</a>
        </td>
        <td class="code add">
            <a class="btn btn-xs btn-primary pull-left btn-comment"> <span class="octicon octicon-plus"></span></a>
            <pre>${line.content}</pre>
        </td>
    [#else]
        <td class="ln">
            <a href="#${index}L${oldLineNumber}" id="${index}L${oldLineNumber}">${oldLineNumber}</a>
        </td>
        <td class="ln">
            <a href="#${index}R${newLineNumber}" id="${index}R${newLineNumber}">${newLineNumber}</a>
        </td>
        <td class="code">
            <a class="btn btn-xs btn-primary pull-left btn-comment"> <span class="octicon octicon-plus"></span></a>
            <pre>${line.content}</pre>
        </td>
    [/#if]
</tr>
[/#macro]