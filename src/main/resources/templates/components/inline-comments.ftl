[#macro renderScripts group i18n commit]
<script>
    $(document).ready(function() {

        function InlineCommentPlugin(line) {
           this.line = line;
        }

        InlineCommentPlugin.prototype = {
            getCommentForm: function() {
                if(this.$btnAdd) {
                    this.$btnAdd.remove();
                }
                return this.$form || this.createForm();
            },

            createForm: function() {
                this.$form =  $('<div class="panel panel-default panel-comment-form">' +
                    '<div class="panel-heading">${i18n.translate("panel.label.add-comment")}' +
                    '<span> - </span>' +
                    '<a href="https://github.com/vdurmont/emoji-java#available-emojis" target="_blank">' +
                    '${i18n.translate("panel.label.add-comment-emoji-link")}</a></div>' +
                    '<div class="panel-body">' +
                    '<form class="form-horizontal" action="${repositoryEntity.getURI()}comment" method="POST">' +
                    '<textarea rows="5" class="form-control" name="content"></textarea>' +
                    '<button type="submit" class="btn btn-primary">${i18n.translate("button.label.comment")}</button>' +
                    '<button type="button" class="btn btn-default" id="btn-cancel">${i18n.translate("button.label.cancel")}</button>' +
                    '</form>' +
                    '</div>' +
                    '</div>').appendTo(this.getCommentContainer());

                this.$form.find('textarea').focus();
                this.$form.submit((function(event) {
                    this.comment();
                    event.preventDefault();
                }).bind(this));

                this.$form.find('#btn-cancel').click(this.dismissForm.bind(this));
                var textArea = this.$form.find('textarea');
                textArea.bind(showEmojiHint(textArea));
            },

            comment: function() {
                var content = $('[name="content"]', this.$form).val();

                if(content) {
	                var lineData = this.line.data();
	                $.post('${repositoryEntity.getURI()}comment', {
		                "link-commit": lineData.linkCommit || "${commit.commit}",
		                "content": content,
		                "source-commit": lineData.sourceCommit,
		                "source-line-number": lineData.sourceLineNumber,
		                "source-file-name": lineData.sourceFileName,
		                "redirect": window.location.pathname
	                }).done((function(res) {
		                this.insertCommentElement(res);
		                this.dismissForm();
	                }).bind(this));
                }
            },

            insertCommentElement: function(res) {
                $('<div class="panel panel-default panel-comment">' +
                    '<div class="panel-heading"><strong>' + res.name + '</strong> on '+
                    '<a href="#comment-'+ res.commentId + '" id="comment-'+ + res.commentId + '">' + res.date + '</a></div>'+
                    '<div class="panel-body">'+
                    '<p>' + (twemoji.parse(res.formattedContent) ||'') + '</p>'+
                    '</div>'+
                    '</div>').appendTo(this.getCommentContainer());
            },

            dismissForm: function() {
                this.$form.remove();
                delete this.$form;

                if(this.hasComments()) {
                    this.addCommentButton();
                }
                else if(this.dismissContainerIfEmpty) {
                    this.dismissContainer();
                }
            },

            hasComments: function() {
                return this.getCommentContainer()
                    .find('.panel-comment')
                    .length > 0;
            },

            addCommentButton: function() {
                this.$btnAdd = $('<button class="btn btn-default btn-add-line-comment">' +
                    '${i18n.translate("panel.button.add-inline-comment")}</button>')
                    .appendTo(this.getCommentContainer())
                    .click(this.getCommentForm.bind(this));
            },

            dismissContainer : function() {
                if(this.commentContainer)
                    this.commentContainer.closest("tr").remove();
                delete this.commentContainer;
            },

            getCommentContainer: function() {
                if(this.commentContainer) {
                    return this.commentContainer;
                }
                this.commentContainer = this.lookForCommentsInNextRow(this.line);

                if(this.commentContainer.length) {
                    this.dismissContainerIfEmpty = true;
                }
                else {
                    this.commentContainer = this.lookForCommentsInSection(this.line);
                }

                if(!this.commentContainer.length) {
                    this.dismissContainerIfEmpty = true;
                    this.commentContainer = this.createCommentBlockInNewRow(this.line);
                }

                return this.commentContainer;
            },

            lookForCommentsInNextRow: function lookForCommentsInNextRow(row) {
                return row
                        .next("tr:has(.comment-block)")
                        .find(".comment-block");
            },

            lookForCommentsInSection: function lookForCommentsInSection(row) {
                return row.closest(".diff.box").next(".comment-block");
            },

            createCommentBlockInNewRow: function createCommentBlockInNewRow(row) {
                return $('<tr><td colspan="3" class="comment-block"></td></tr>')
                        .insertAfter(row)
                        .find('td');
            }

        }

        $(".btn-comment").click(function() {
            var currentRow = $(this).closest("tr");
            var a = currentRow.data('insertPlugin');
            if(!a) {
                a = new InlineCommentPlugin(currentRow);
                currentRow.data('insertPlugin', a);
            }
            a.getCommentForm();
        });

        $('.btn-add-line-comment').each(function(i,e) {
            var row = $(e).closest("tr").prev("tr");
            var a = row.data('insertPlugin');
            if(!a) {
                a = new InlineCommentPlugin(row);
                row.data('insertPlugin', a);
            }
            a.$btnAdd = e;
            $(e).click(a.getCommentForm.bind(a));
        });

    });
</script>
[/#macro]