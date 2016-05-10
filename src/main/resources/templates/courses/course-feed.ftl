[#import "../macros.ftl" as macros]
[#import "../components/commit-row.ftl" as commitRow]

[@macros.renderHeader i18n.translate("assignments.title") ]
<link rel="stylesheet" href="/static/css/timeline.css">
[/@macros.renderHeader]
[@macros.renderMenu i18n user /]

<div class="container">

	<ol class="breadcrumb">
		<li><a href="/courses">${ i18n.translate("section.courses") }</a></li>
		<li><a href="${course.course.getURI()}">${course.course.code} - ${course.course.name}</a></li>
		<li>
          <span uib-dropdown dropdown-append-to-body="true">
            <a href id="simple-dropdown" uib-dropdown-toggle>
            ${course.timeSpan.start?string["yyyy"]}[#if course.timeSpan.end??] - ${course.timeSpan.end?string["yyyy"]}[/#if]
	            <span class="caret"></span>
            </a>
            <ul uib-dropdown-menu>
            [#list course.course.getEditions() as a]
	            <li><a href="${a.getURI()}">${a.timeSpan.start?string["yyyy"]}[#if a.timeSpan.end??] - ${a.timeSpan.end?string["yyyy"]}[/#if]</a></li>
            [/#list]
            </ul>
          </span>
		</li>
		<li>
          <span uib-dropdown dropdown-append-to-body="true">
            <a href id="simple-dropdown" uib-dropdown-toggle>
              Course Feed
	            <span class="caret"></span>
            </a>
            <ul uib-dropdown-menu>
            [#list course.course.getEditions() as a]
	            <li><a href="../">Overview</a></li>
            [/#list]
            </ul>
          </span>
		</li>
	</ol>


	  <ul class="timeline">
      [#list events as event]
		  <li [#if event_index % 2 == 1]class="timeline-inverted"[/#if]>
			  <div class="timeline-badge">
				  <a><i class="fa fa-circle [#if event_index % 2 == 1]invert[/#if]" id=""></i></a>
			  </div>
			  <div class="timeline-panel">
				  <div class="timeline-body">
              [#switch event.class.simpleName]
                [#case "Delivery"]
                  ${event.createdUser.name}
								  delivered [#if event.commit?? && event.commit?has_content]${event.commit.commitId} for[/#if]
								  <a href="${event.getURI()}../../">
                  ${event.assignment.name}
								  </a>
                  [#break]
                [#case "Commit"]
                  <span>
                  ${event.author?substring(0, event.author?index_of("<"))} pushed
                  <a href="${event.getURI()}diff">
                    <span class="label label-default">${event.commitId?substring(0,7)?upper_case }</span>
                  </a>
                  to <a href="${event.getURI()}diff">${event.repository.repositoryName}</a></span>
                  [#assign buildResult = event.buildResult![]]
                  [#if buildResult?? && buildResult?has_content && buildResult.hasFinished()]
                    [#if buildResult.hasSucceeded()]
										  <a href="${event.getURI()}build">
											  <span class="octicon octicon-check text-success"></span>
										  </a>
                    [#else]
										  <a href="${event.getURI()}build">
											  <span class="octicon octicon-x text-danger"></span>
										  </a>
                    [/#if]
                  [/#if]
                  [#break]
                [#case "CommitComment"]
								  <blockquote style="display:inline-block;" class="commit-comment">
								  <p style="font-size: 0.8em;">${event.content}</p>
								  <footer>
                  ${event.user.name} at
									  <a href="${event.commit.getURI()}diff"><span class="label label-default">${event.commit.commitId?substring(0,7)?upper_case }</span></a>
								  </footer>
								  </blockquote>
                  [#break]
                [#case "PullRequest"]
								  Pull request opened for branch <a href="${event.getURI()}">${event.branchName}</a>
                  [#break]
                [#case "PullRequestComment"]
								  <blockquote style="display:inline-block;">
									  <p style="font-size: 0.8em;">${event.content}</p>
									  <footer>
                    ${event.user.name} at
										  <a href="${event.pullRequest.getURI()}diff"><span class="label label-default">${event.pullRequest.branchName}</span></a>
									  </footer>
								  </blockquote>
                  [#break]
                [#default]
								  <span>${event.class.simpleName}</span>
              [/#switch]
				  </div>
				  <div class="timeline-footer">
					  <p class="text-right">${event.getTimestamp()}</p>
				  </div>
			  </div>
		  </li>
      [/#list]
    </ul>
</div>
[@macros.renderScripts]
<script src="/static/vendor/angular/angular.min.js"></script>
<script src="/static/vendor/angular-bootstrap/ui-bootstrap.min.js"></script>

<script type="text/javascript">
	angular.module('devhub', ['ui.bootstrap']);
</script>
[/@macros.renderScripts]
[@macros.renderFooter /]
