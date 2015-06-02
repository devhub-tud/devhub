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

});