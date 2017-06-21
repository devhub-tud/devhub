/**
 * Created by Maaike Visser on 13-6-2017.
 */

$(function () {
    "use strict";

    var view = true;

	function toggleView() {
		var userDeliveriesTable = $('.table#user');
		var lastDeliveriesTable = $('.table#last');
		var filteredDeliveriesTable = $('.table#filtered');

		var userProgressBar = $('.well#user');
		var lastProgressBar = $('.well#last');

		userDeliveriesTable.toggle(view);
		filteredDeliveriesTable.toggle(view);
		lastDeliveriesTable.toggle(!view);

		userProgressBar.toggle(view);
		lastProgressBar.toggle(!view);
		view  = !view;
	};

    $('.toggle-view').click(toggleView);
	  toggleView();
});
