[#import "macros.ftl" as macros]
[@macros.renderHeader i18n.translate("section.projects") /]
[@macros.renderMenu i18n user /]
		<div class="container">
[@macros.renderCommitHeader i18n group commit "List files" /]
	<div class="diff box">
		<div class="header">
			<button class="pull-right btn btn-sm btn-default folder"><i class="glyphicon glyphicon-chevron-up"></i> Fold</button>
			<button class="pull-right btn btn-sm btn-default unfolder" style="display: none;"><i class="glyphicon glyphicon-chevron-down"></i> Unfold</button>
			<h5>[@macros.renderTreeBreadcrumb group commit repository path /]</h5>
		</div>
		[#if entries?? && entries?has_content]
			<div class="scrollable">
				<table class="table files">
					<tbody>
			[#list entries?keys as entry]
			[#assign type=entries[entry]]
						<tr>
							<td>
				[#if type = "FOLDER"]
					[#if path?? && path?has_content]
							<i class="glyphicon glyphicon-folder-open"></i> <a href="/projects/${group.course.code}/groups/${group.groupId}/commits/${commit.commit}/tree/${path}/${entry}">${entry}</a>
					[#else]
							<i class="glyphicon glyphicon-folder-open"></i> <a href="/projects/${group.course.code}/groups/${group.groupId}/commits/${commit.commit}/tree/${entry}">${entry}</a>
					[/#if]
				[#else]
					[#if path?? && path?has_content]
							<i class="glyphicon glyphicon-file"></i> <a href="/projects/${group.course.code}/groups/${group.groupId}/commits/${commit.commit}/blob/${path}/${entry}">${entry}</a>
					[#else]
							<i class="glyphicon glyphicon-file"></i> <a href="/projects/${group.course.code}/groups/${group.groupId}/commits/${commit.commit}/blob/${entry}">${entry}</a>
					[/#if]
				[/#if]
							</td>
						</tr>
			[/#list]
					</tbody>
			</table>
			</div>
		[#else]
			<div>${i18n.translate("diff.changes.nothing")}</div>
		[/#if]
	</div>
[@macros.renderScripts /]
	<script>
		$(document).ready(function() {
			$(".diff").each(function() {
				var diffBody = $(this).find(".files");
				if (diffBody.length == 0) {
					var folder = $(this).find(".folder");
					folder.css("display", "none");
				}
			});
			
			$(".folder").click(function(e) {
				var body = $(this).parentsUntil(".box").parent();
				var unfolder = $(this).parent().find(".unfolder");
				
				body.addClass("folded");
				$(this).css("display", "none").blur();
				unfolder.css("display", "block"); 
			});
			
			$(".unfolder").click(function(e) {
				var body = $(this).parentsUntil(".box").parent();
				var folder = $(this).parent().find(".folder");

				body.removeClass("folded");
				$(this).css("display", "none").blur();
				folder.css("display", "block"); 
			});
		});
	</script>
[@macros.renderFooter /]
