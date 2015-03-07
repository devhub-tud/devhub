[#import "macros.ftl" as macros]
[@macros.renderHeader i18n.translate("section.projects") /]
[@macros.renderMenu i18n user /]
		<div class="container">
[@macros.renderCommitHeader i18n group commit "List files" /]
			<div class="diff box">
				<div class="header">
					<a href="/courses/${group.course.code}/groups/${group.groupNumber}/${commit.commit}/raw/${path?url('UTF8')}" class="pull-right btn btn-sm btn-default"><i class="glyphicon glyphicon-floppy-save"></i> Download</a>
					<h5>[@macros.renderTreeBreadcrumb group commit repository path /]</h5>
				</div>
			[#if contents?? && contents?has_content]
				<div class="scrollable">
					<table class="table diffs">
						<tbody>
				[#list contents as line]
                            <tr data-source-line-number="${line_index}">
								<td class="ln">${line_index + 1}</td>
								<td class="code"> <a class="btn btn-xs btn-primary pull-left btn-comment"> <span class="octicon octicon-plus"></span></a><pre>${line}</pre></td>
							</tr>

                    [#assign commentsForThisLine = comments.getCommentsForLine(commit.getCommit(), path, line_index)]
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
				[/#list]
						</tbody>
					</table>
				</div>
			[/#if]
			</div>
		</div>
[@macros.renderScripts /]
[#if highlight?? && highlight.isHighlight() ]
	<script src="/static/js/highlight.pack.js"></script>
[/#if]
	<script>
		$(document).ready(function() {
[#if highlight?? && highlight.isHighlight() ]
			hljs.configure({
				tabReplace: '	',
				useBR: true,
				languages : ["${highlight.getClassName()}"]
			});
			
			$('.code').each(function(i, e) {
				hljs.highlightBlock(e);
			});
[/#if]
		});
	</script>
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
            '<form class="form-horizontal" action="/courses/${group.course.getCode()}/groups/${group.getGroupId()}/comment" method="POST">' +
            '<input type="hidden" name="link-commit" value="${ commit.commit }"/>' +
            '<input type="hidden" name="source-commit" value="${commit.commit }"/>' +
            '<input type="hidden" name="source-line-number" value="' + lineData.sourceLineNumber + '"/>' +
            '<input type="hidden" name="source-file-name" value="${path}"/>' +
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

        $(".btn-comment").click(function() {
            var currentRow = $(this).closest("tr");
            getCommentBlockWithInput(currentRow);
        });

    });
</script>
[@macros.renderFooter /]
