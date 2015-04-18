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
                        <tr class="comment-block">
                            <td colspan="3">
                                [#list commentsForThisLine as comment]
                                    [@commentElement.renderComment comment][/@commentElement.renderComment]
                                [/#list]
                                    <button class="btn btn-default btn-add-line-comment">${i18n.translate("button.label.add-line-note")}</button>
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
            $('.btn-add-line-comment', commentBlock).remove();
            var $form = $('<div class="panel panel-default" id="comment-form">' +
            '<div class="panel-heading">${i18n.translate("panel.label.add-comment")}</div>' +
            '<div class="panel-body">' +
            '<form class="form-horizontal" action="/courses/${group.course.code}/groups/${group.groupNumber}/comment" method="POST">' +
            '<textarea rows="5" class="form-control" name="content"></textarea>' +
            '<button type="submit" class="btn btn-primary">${i18n.translate("button.label.comment")}</button>' +
            '<button type="button" class="btn btn-default" id="btn-cancel">${i18n.translate("button.label.cancel")}</button>' +
            '</form>' +
            '</div>' +
            '</div>').appendTo(commentBlock)

            $form.submit(function(event) {
                $.post('/courses/${group.course.code}/groups/${group.groupNumber}/comment', {
                    "link-commit": "${commit.commit}",
                    "content": $('[name="content"]', this).val(),
                    "source-commit": lineData.sourceCommit,
                    "source-line-number": lineData.sourceLineNumber,
                    "source-file-name": lineData.sourceFileName,
                    "redirect": window.location.pathname
                }).done(function(res) {
                    // Add comment block
                    $('<div class="panel panel-default panel-comment">' +
                        '<div class="panel-heading"><strong>' + res.name + '</strong> on '+
                            '<a href="#comment-'+ res.commentId + '" id="comment-'+ + res.commentId + '">' + res.date + '</a></div>'+
                        '<div class="panel-body">'+
                        '<p>' + res.content + '</p>'+
                        '</div>'+
                        '</div>').appendTo(commentBlock);

                    addBtnAddLineComment($form.closest("tr"));
                    $form.remove();
                });
                event.preventDefault();
            });

            $form.find("#btn-cancel").click(function() {
                var row = $(this).closest("tr");
                if(row.find(".panel-comment").length === 0) {
                    row.remove();
                }
                else {
                    row.find("#comment-form").remove();
                    addBtnAddLineComment(row);
                }
            });

        }

        $(".btn-comment").click(function() {
            var currentRow = $(this).closest("tr");
            getCommentBlockWithInput(currentRow);
        });

        function addBtnAddLineComment(commentBlock) {
            var td = commentBlock.find('td');
            $('<button class="btn btn-default btn-add-line-comment">Add comment</button>')
                .appendTo(td)
                .click(function() {
                    var $this = $(this);
                    var currentRow = $this.closest("tr").prev("tr");
                    getCommentBlockWithInput(currentRow);
                    $this.remove();
                });
        }

        $('.btn-add-line-comment').click(function() {
            var $this = $(this);
            var currentRow = $this.closest("tr").prev("tr");
            getCommentBlockWithInput(currentRow);
            $this.remove();
        });

    });
</script>
[/#macro]