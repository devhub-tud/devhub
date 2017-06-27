[#macro listTags repository commitId]
    [#list repository.getTags() as tag]
        [#if tag.getCommit().getCommit() == commitId]
        <span class="label label-primary">${tag.getSimpleName()}</span>
        [/#if]
    [/#list]
[/#macro]

[#import "macros.ftl" as macros]
[#import "components/project-frameset.ftl" as projectFrameset]
[#import "components/commit-row.ftl" as commitRow]

[@macros.renderHeader i18n.translate("section.projects") /]
[@macros.renderMenu i18n user /]
<div class="container">

[#if repositoryEntity?? && repositoryEntity?has_content]
    [@projectFrameset.renderBreadcrumb i18n group![] repositoryEntity/]
[/#if]

    <div class="row">
        <div class="col-md-10 col-md-offset-2">
            <h4 style="line-height:34px; margin-top:0;">Nice graphs &nbsp; 😍</h4>
        </div>
    </div>

    <div class="row">
        <div class="col-md-2">
        [@projectFrameset.renderSidemenu "insights" i18n group![] repository/]
        </div>

        <!-- Graph for all commits -->
        <div class="col-md-10">
            <div id="allcommits_div" style="width: 100%; height: 500px; "></div>
        </div>
    </div>

    <!-- Graph for a person for a commit -->
    <div class="row" id="personcommit_divs">
    </div>

</div> <!-- closes div class="container" -->

[@macros.renderScripts /]
<script type="text/javascript" src="https://www.gstatic.com/charts/loader.js"></script>
<script type="text/javascript" src="http://code.stephenmorley.org/javascript/colour-handling-and-processing/Colour.js"></script>
<script type="text/javascript">

    function getRandColor(brightness){

        // Six levels of brightness from 0 to 5, 0 being the darkest
        var rgb = [Math.random() * 256, Math.random() * 256, Math.random() * 256];
        var mix = [brightness*51, brightness*51, brightness*51]; //51 => 255/5
        var mixedrgb = [rgb[0] + mix[0], rgb[1] + mix[1], rgb[2] + mix[2]].map(function(x){ return Math.round(x/2.0)})
        return "rgb(" + mixedrgb.join(",") + ")";
    }

    var randomcolor = getRandColor(5);
    google.charts.load('current', {'packages':['corechart']});
    google.charts.setOnLoadCallback(function() {
        $.get('http://localhost:50001/courses/ti1705/TI1705/groups/1/magical-chart-data')
            .then(function(res) {
                for(var i = 1, row, dateParts; row = res[i]; i++) {
                    dateParts = row[0].split('-').map(parseFloat)
                    row[0] = new Date(dateParts[0], dateParts[1], dateParts[2])
                }

                var data = google.visualization.arrayToDataTable(res);
                var options = {
                    title: 'Commits over time',
                    colors: [randomcolor],
                    backgroundColor: 'transparent',
                    hAxis: {title: 'Date',  titleTextStyle: {color: "#333"}, gridlines: {color : 'transparent'}},
                    vAxis: {minValue: 0, gridlines: {count : -1}}
                };

                var chart = new google.visualization.AreaChart(document.getElementById('allcommits_div'));
                chart.draw(data, options);
            });
    });


    google.charts.load('current', {'packages':['corechart']});
    google.charts.setOnLoadCallback(function() {
        $.get('http://localhost:50001/courses/ti1705/TI1705/groups/1/person-commit')
                .then(function(res) {
                    var minDate, maxDate;

                    for (var name in res) {
                        var personalChart = res[name];

                        for (var i = 1, row, dateParts; row = personalChart[i]; i++) {
                            dateParts = row[0].split('-').map(parseFloat)
                            row[0] = new Date(dateParts[0], dateParts[1], dateParts[2])
                            if (!minDate || minDate > row[0]) minDate = row[0];
                            if (!maxDate || maxDate < row[0]) maxDate = row[0];
                        }
                    }

                    for (var name in res) {
                        var personalChart = res[name];
                        var data = google.visualization.arrayToDataTable(personalChart);
                        randomcolor = getRandColor(5);
                        var options = {
                            title: name,
                            colors: [randomcolor],
                            backgroundColor: 'transparent',
                            hAxis: {minValue: minDate, maxValue: maxDate, title: 'Date',  titleTextStyle: {color: '#333'}, gridlines: {color : 'transparent'}},
                            vAxis: {minValue: 0, gridlines: {count : -1}}
                        };

                        var mijnMagischeElement = $('<div>')
                                .attr('height', '300px')
                                .attr('width', '40%')
                                .appendTo('#personcommit_divs');

                        mijnMagischeElement.wrap($('<div>').addClass("col-md-6"));
                        var chart = new google.visualization.AreaChart(mijnMagischeElement[0]);
                        chart.draw(data, options);
                    }

                });
    });
</script>
[@macros.renderFooter /]
