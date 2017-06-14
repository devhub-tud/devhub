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

        <div class="col-md-10">
            <div id="chart_div" style="width: 100%; height: 500px; "></div>
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
                var data = new google.visualization.DataTable(res);
                var options = {
                    title: 'Company Performance',
                    backgroundColor: 'transparent',
                    hAxis: {title: 'Year',  titleTextStyle: {color: '#333'}},
                    vAxis: {minValue: 0}
                };

                var chart = new google.visualization.AreaChart(document.getElementById('chart_div'));
                chart.draw(data, options);
            });
    });
</script>
[@macros.renderFooter /]
