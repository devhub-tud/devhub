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
    <div class="row">
        <div class="col-md-10">
            <div id="personcommit_div" style="width: 100%; height: 500px; "></div>
        </div>
    </div>

</div> <!-- closes div class="container" -->

[@macros.renderScripts /]
<script type="text/javascript" src="https://www.gstatic.com/charts/loader.js"></script>
<script type="text/javascript">


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
                    backgroundColor: 'transparent',
                    hAxis: {title: 'Date',  titleTextStyle: {color: '#333'}},
                    vAxis: {minValue: 0}
                };

                var chart = new google.visualization.AreaChart(document.getElementById('allcommits_div'));
                chart.draw(data, options);
            });
    });


    google.charts.load('current', {'packages':['corechart']});
    google.charts.setOnLoadCallback(function() {
        $.get('http://localhost:50001/courses/ti1705/TI1705/groups/1/person-commit')
                .then(function(res) {
                    for(var i = 1, row, dateParts; row = res[i]; i++) {
                        dateParts = row[0].split('-').map(parseFloat)
                        row[0] = new Date(dateParts[0], dateParts[1], dateParts[2])
                    }

                    var data = google.visualization.arrayToDataTable(res);
                    var options = {
                        title: 'Commits for person',
                        backgroundColor: 'white',
                        hAxis: {title: 'Date',  titleTextStyle: {color: '#333'}},
                        vAxis: {minValue: 0}
                    };

                    var chart = new google.visualization.AreaChart(document.getElementById('personcommits_div'));
                    chart.draw(data, options);
                });
    });
</script>
[@macros.renderFooter /]
