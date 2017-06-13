/**
 * Created by Maaike Visser on 13-6-2017.
 */

$(function () {
    "use strict";

    var view = true;

    $('.toggle-view').click(function () {
        var userDeliveriesTable = $('.table#user');
        var lastDeliveriesTable = $('.table#last');
        var filteredDeliveriesTable = $('.table#filtered');

        var userProgressBar = $('.well#user');
        var lastProgressBar = $('.well#last');

        if (!view) {
            userDeliveriesTable.hide();
            filteredDeliveriesTable.hide();
            lastDeliveriesTable.show();

            userProgressBar.hide();
            lastProgressBar.show();


            view = !view;
        } else {
            userDeliveriesTable.show();
            filteredDeliveriesTable.show();
            lastDeliveriesTable.hide();

            userProgressBar.show();
            lastProgressBar.hide();

            view = !view;
        }
    })
});

$(document).ready(function () {
    var userDeliveriesTable = $('.table#user');
    var filteredDeliveriesTable = $('.table#filtered');
    var userProgressBar = $('.well#user');

    userDeliveriesTable.hide();
    filteredDeliveriesTable.hide();

    userProgressBar.hide();
});