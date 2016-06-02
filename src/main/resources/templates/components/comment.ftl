[#macro renderComment comment]
[#if comment?? && comment?has_content && comment.content?? && comment.content?has_content]
<div class="panel panel-default panel-comment">
    <div class="panel-heading"><strong>${comment.user.name}</strong>
    [#if comment.user.admin]
      <span class="label label-info">Admin</span>
    [#elseif comment.user.isAssisting(courseEdition)]
      <span class="label label-default">TA</span>
    [/#if]
      on <a href="#comment-${comment.commentId}" id="comment-${comment.commentId}">${comment.timestamp}</a></div>
    <div class="panel-body">
        [#--noinspection FtlWellformednessInspection--]
        [#noescape]${MarkDownParser.markdownToHtml(comment.content)}[/#noescape]
    </div>
</div>
[/#if]
[/#macro]