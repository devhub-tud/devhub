[#macro renderComment comment]

<div class="panel panel-default panel-comment">
    <div class="panel-heading"><strong>${comment.user.name}</strong> on <a href="#comment-${comment.commentId}" id="comment-${comment.commentId}">${comment.time}</a></div>
    <div class="panel-body">
        <p>${comment.content}</p>
    </div>
</div>

[/#macro]