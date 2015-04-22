[#macro renderComment comment]
[#if comment?? && comment?has_content && comment.content?? && comment.content?has_content]
<div class="panel panel-default panel-comment">
    <div class="panel-heading"><strong>${comment.user.name}</strong> on <a href="#comment-${comment.commentId}" id="comment-${comment.commentId}">${comment.time}</a></div>
    <div class="panel-body">
        <p>[#list comment.content?split("\n") as line]${line}[#if line_has_next]<br/>[/#if][/#list]</p>
    </div>
</div>
[/#if]
[/#macro]