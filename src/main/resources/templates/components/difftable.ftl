[#import "diffline.ftl" as diffline]
[#import "comment.ftl" as commentElement]

[#macro diffTable diffModel index commit]

<table class="table diffs">
    [#list diffModel.contexts as diffContext]
        <tbody>
            [#list diffContext.lines as line]
                [#if line.content??]
                    [@diffline.diffLine line index/]
                    [#if commentChecker?? && commentChecker?has_content]
                        [#assign commentsForThisLine = commentChecker.getCommentsForLine(line.sourceCommitId, line.sourceFilePath, line.sourceLineNumber)]
                        [#if commentsForThisLine?has_content ]
                        <tr>
                            <td colspan="3" class="comment-block">
                                [#list commentsForThisLine as comment]
                                    [@commentElement.renderComment comment][/@commentElement.renderComment]
                                [/#list]
                                    <button class="btn btn-default btn-add-line-comment">${i18n.translate("panel.button.add-inline-comment")}</button>
                            </td>
                        </tr>
                        [/#if]
                    [/#if]
                [/#if]
            [/#list]
        </tbody>
    [/#list]
</table>
[/#macro]
