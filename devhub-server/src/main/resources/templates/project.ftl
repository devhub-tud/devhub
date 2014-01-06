[#import "macros.ftl" as macros]
[@macros.renderHeader "${group.project.code} ${group.project.name}" /]
[@macros.renderProjectMenu user group /]
		<div class="content">
			<ol class="breadcrumb breadcrumb-nav">
				<li>
					<a href="/projects"><i class="glyphicon glyphicon-home"></i></a>
				</li>
				<li>
					<a href="/projects/${group.project.code}/groups/${group.groupNumber}">${group.project.code} - ${group.project.name} (Group #${group.groupNumber})</a>
				</li>
[#if section?has_content]
				<li>
					<a href="/projects/${group.project.code}/groups/${group.groupNumber}/${section}">${section?cap_first}</a>
				</li>
	[#if subsection?has_content]
				<li>
					<a href="/projects/${group.project.code}/groups/${group.groupNumber}/${section}/${subsection}">${subsection}</a>
				</li>
	[/#if]
[/#if]
			</ol>
			<h1>${pageHeader}</h1>
		</div>
[@macros.renderScripts /]
[@macros.renderFooter /]