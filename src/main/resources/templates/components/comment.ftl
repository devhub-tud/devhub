[#macro renderComment comment]

<div class="panel panel-default panel-comment">
    <div class="panel-heading"><strong>${comment.user.name}</strong> on ${comment.time}</div>
    <div class="panel-body">
        <p>${comment.content}</p>
    </div>
</div>

[/#macro]