// Document onload
$(function () {

	// Enable all tooltips on the page
	$("[data-toggle='tooltip']").tooltip();

	// Enable all popovers
	$("[data-toggle=popover]").each(function(i,e) {
		$(e).popover({
			html: true,
			content: $(e).find('#' + $(e).data('content-id')).html()
		});
	});

	// Size can be set to 16x16, 36x36, or 72x72
	twemoji.size = '16x16';

	// This parses all unicode emojis to image spans on page load (using size defined above)
	twemoji.parse(document.body);

	// This shows the emoji hint dropdown when typing a comment
	$('.panel-comment-form textarea.form-control').each(function() {
		showEmojiHint(this);
	});

});

var showEmojiHint = function(textarea) {
	// Defined by the emojies.json file.
	$.getJSON("/static/js/emojies.json", function(json) {
		emojies = json;
	});
	$(textarea).textcomplete([
		{ // emoji strategy
			match: /(^|\s):(\w*)$/,
			search: function (term, callback) {
				term = term.toLowerCase();
				var regexp = new RegExp('^' + term);
				callback($.grep(emojies, function (emoji) {
					var len = emoji.aliases.length,
						i = 0;
					for (; i < len; i++) {
						if (regexp.test(emoji.aliases[i]))
							return true;
					}
					return false;
				}));
			},
			template: function (value, term) {
				var emojiString = '';
				value.aliases.forEach(function(alias) {
					emojiString += ':' + alias + ':, ';
				});
				emojiString = emojiString.substring(0, emojiString.length - 2);
				emojiString += " - " + value.description;
				return emojiString;
			},
			replace: function (value) {
				return '$1:' + value.aliases[0] + ': ';
			}
		}
	], { maxCount: 8, debounce: 0 });
};