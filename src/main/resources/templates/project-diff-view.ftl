[#import "macros.ftl" as macros]
[#import "components/difftable.ftl" as difftable]
[#import "components/diffbox.ftl" as diffbox]
[#import "components/comment.ftl" as commentElement]
[#import "components/inline-comments.ftl" as inlineComments]

[@macros.renderHeader i18n.translate("section.projects") /]
[@macros.renderMenu i18n user /]
        <div class="container">


    [@macros.renderCommitHeader i18n group![] repositoryEntity commit i18n.translate("commit.view-diff")/]

    [#if diffViewModel?? && diffViewModel?has_content]
        [#list diffViewModel.diffs as diffModel]
            [@diffbox.diffbox diffModel diffModel_index][/@diffbox.diffbox]
        [/#list]
    [#else]
        <div>${i18n.translate("diff.changes.nothing")}</div>
    [/#if]

        <div id="comment-list">
    [#if comments?? && comments?has_content]
        [#list comments as comment]
            [@commentElement.renderComment comment][/@commentElement.renderComment]
        [/#list]
    [/#if]
        </div>

        <div class="panel panel-default panel-comment-form">
            <div class="panel-heading">
                ${i18n.translate("panel.label.add-comment")}
                <span> - </span>
                <a href="https://github.com/vdurmont/emoji-java#available-emojis" target="_blank">
                    ${i18n.translate("panel.label.add-comment-emoji-link")}
                </a>
            </div>
            <div class="panel-body">
                <form class="form-horizontal" id="pull-comment-form" >
                    <textarea rows="5" class="form-control" name="content" style="margin-bottom:10px;" required></textarea>
                    <button type="submit" class="btn btn-primary">${i18n.translate("button.label.submit")}</button>
                    <button type="button" class="btn btn-default" id="btn-cancel">${i18n.translate("button.label.cancel")}</button>
                    <button type="button" class="btn btn-default" id="btn-preview">${i18n.translate("button.label.preview")}</button>
                </form>
            </div>
        </div>

		</div>

[@macros.renderScripts /]
[@inlineComments.renderScripts group![] i18n commit/]

<script>
    $(function() {
        $('#pull-comment-form').submit(function(event) {
            $.post('${repositoryEntity.getURI()}comment', {
                "link-commit": "${commit.commit}",
                "content": $('[name="content"]', '#pull-comment-form').val(),
                "redirect": window.location.pathname
            }).done(function(res) {
                // Add comment block
                $('<div class="panel panel-default panel-comment">' +
                '<div class="panel-heading"><strong>' + res.name + '</strong> on '+
                    '<a href="#comment-'+ res.commentId + '" id="comment-'+ + res.commentId + '">' + res.date + '</a></div>'+
                '<div class="panel-body">'+
                 twemoji.parse(res.formattedContent) +
               '</div>'+
                '</div>').appendTo('#comment-list');
                // Clear input
                $('[name="content"]', '#pull-comment-form').val('');
            });
            event.preventDefault();
        });
    });
    $(function () {
        $('#btn-preview').click(function (event) {
            $.get('/comment/preview', {
                "content": $('textarea.form-control').val()
            }).done(function (res) {
                var previewPanel = $('#preview-panel');
                if (previewPanel.length){
                    previewPanel.find('.panel-body:first').empty();
                    previewPanel.find('.panel-body').append(res);
                } else {
                    $('<hr style="border-color: #DDD;">'+
                        '<div class="panel panel-default" id ="preview-panel">'+
                        '<div class="panel-heading">Preview</div>'+
                        '<div class="panel-body">'+
                        res +
                        '</div>' +
                        '</div>').appendTo('.panel-comment-form .panel-body');
                }
                twemoji.parse(document.body);
                event.preventDefault();
            });
        });
    });
</script>

[@diffbox.renderScripts/]
[@macros.renderFooter /]
