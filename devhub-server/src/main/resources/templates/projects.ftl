[#import "macros.ftl" as macros]
[@macros.renderHeader "Projects" /]
		<div class="panel panel-default notification">
			<div class="panel-heading">
				<h3 class="panel-title">Your current projects</h3>
			</div>
			<div class="panel-body">
[#if groups?? && groups?has_content]
				<ul class="list-group no-margin">
	[#list groups as group]
					<li class="list-group-item">
						<a href="/projects/${group.project.code}/groups/${group.groupNumber}">${group.project.code} - ${group.project.name} (Group #${group.groupNumber})</a>
					</li>
	[/#list]
				</ul>
[#else]
				<div class="alert alert-info no-margin">You do not appear to be participating in any projects. Contact your teacher to resolve this.</div>
[/#if]
			</div>
		</div>
[@macros.renderScripts /]
[@macros.renderFooter /]
