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

});