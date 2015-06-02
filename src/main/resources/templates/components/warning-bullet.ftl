[#macro renderBullet i18n lineWarnings fromCommitId fromFilePath sourceLineNumber blockNumber lineNumber]
    [#assign warningsForLine = lineWarnings.retrieveWarnings(fromCommitId, fromFilePath, sourceLineNumber)]
    [#if warningsForLine?? && warningsForLine?has_content]
		<a class="warning"
		   data-container="body" data-toggle="popover" data-placement="right"
		   data-title="Warnings"
		   data-content-id="warnings-${blockNumber}R${lineNumber}">
			<i class="octicon octicon-primitive-dot"></i>
			<div class="hidden" id="warnings-${blockNumber}R${lineNumber}">
				<table class="table table-bordered">
            [#list warningsForLine as warning]
                    <tr><td>${warning.getMessage(i18n)}</td></tr>
            [/#list]
				</table>
			</div>
		</a>
    [/#if]
[/#macro]