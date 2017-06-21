$(function() {
	"use strict";

	var currentFilter, emptyWarning;

	$('.delivery-filter').click(function() {
		var filterClass = $(this).data('filterClass');
		var deliveries = $('tr.delivery');
		var table = deliveries.closest("table");

		if(!filterClass) {
			return;
		}
        if(emptyWarning) {
            emptyWarning.remove();
            emptyWarning = undefined;
        }
		if(currentFilter == filterClass) {
			deliveries.show();
			currentFilter = undefined;
		}
		else {
			currentFilter = filterClass;
			var filteredDeliveries = deliveries.filter('.' + filterClass);
			filteredDeliveries.show();
			deliveries.filter(':not(.' + filterClass + ')').hide();

			if(filteredDeliveries.length == 0) {
				emptyWarning = $('<tr><td class="commit muted">' +
				'No deliveries for this filter!</td></tr>').appendTo(table);
			}
		}
	});
});