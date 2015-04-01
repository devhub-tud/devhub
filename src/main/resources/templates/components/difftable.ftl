[#import "diffline.ftl" as diffline]

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
                        <tr class="comment-block">
                            <td colspan="3">
                                [#list commentsForThisLine as comment]
                                    <div class="panel panel-default panel-comment">
                                        <div class="panel-heading"><strong>${comment.user.name}</strong> on ${comment.time}</div>
                                        <div class="panel-body">
                                            <p>${comment.content}</p>
                                        </div>
                                    </div>
                                [/#list]
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

[#macro renderScripts]
<script>
    $(document).ready(function() {

        function getCommentBlockWithInput(row) {
            var commentBlock = row.next(".comment-block");
            if(commentBlock.length === 0) {
                commentBlock = createCommentBlock(row);
            }
            if(!hasCommentForm(commentBlock)) {
                var oldRowNumber, newRowNumber, diffData, lineData;

                oldRowNumber = parseInt($("td", row).eq(0).text());
                if(isNaN(oldRowNumber))
                    oldRowNumber = null;

                newRowNumber = parseInt($("td", row).eq(1).text());
                if(isNaN(newRowNumber))
                    newRowNumber = null;

                diffData = row.closest(".diff").data();
                lineData = row.data();
                createCommentForm(commentBlock.find("td"), diffData, lineData);
            }
            return commentBlock;
        }

        function createCommentBlock(row) {
            return $(
                    '<tr class="comment-block">' +
                    '<td colspan="3"></td>' +
                    '</tr>')
                    .insertAfter(row);
        }

        function hasCommentForm(commentBlock) {
            return $("#comment-form", commentBlock).length > 0;
        }

        function createCommentForm(commentBlock, diffData, lineData) {
            $('<div class="panel panel-default" id="comment-form">' +
            '<div class="panel-heading">Add a comment</div>' +
            '<div class="panel-body">' +
            '<form class="form-horizontal" action="/courses/${group.course.code}/groups/${group.groupNumber}/comment" method="POST">' +
            '<input type="hidden" name="link-commit" value="${ commit.commit }"/>' +
            '<input type="hidden" name="source-commit" value="' + lineData.sourceCommit + '"/>' +
            '<input type="hidden" name="source-line-number" value="' + lineData.sourceLineNumber + '"/>' +
            '<input type="hidden" name="source-file-name" value="' + lineData.sourceFileName + '"/>' +
            '<input type="hidden" name="redirect" value="' + location.pathname + '"/>' +
            '<textarea rows="5" class="form-control" name="content"></textarea>' +
            '<button type="submit" class="btn btn-primary">Submit</button>' +
            '<button type="button" class="btn btn-default" id="btn-cancel">Cancel</button>' +
            '</form>' +
            '</div>' +
            '</div>')
                    .appendTo(commentBlock)
                    .find("#btn-cancel").click(function() {
                        var row = $(this).closest("tr");
                        if(row.find(".panel-comment").length === 0) {
                            row.remove();
                        }
                        else {
                            row.find("#comment-form").remove();
                        }
                    });
        }

        function appendComment(commentBlock) {
            $(
                    '<div class="panel panel-default panel-comment">' +
                    '<div class="panel-heading"><strong>Jan-Willem Gmelig Meyling</strong> on Monday, 28 february</div>' +
                    '<div class="panel-body">' +
                    '<p>' +
                    'I think it would be really great if we do this!' +
                    '</p>' +
                    '</div>' +
                    '</div>')
                    .appendTo(commentBlock);
        }

        $(".btn-comment").click(function() {
            var currentRow = $(this).closest("tr");
            getCommentBlockWithInput(currentRow);
        });

    });
</script>
[/#macro]