[#import "macros.ftl" as macros]
[@macros.renderHeader i18n.translate("section.projects") /]
[@macros.renderMenu i18n user /]
		<div class="container">
[@macros.renderCommitHeader i18n group commit i18n.translate("commit.view-build-log")/]
[#if states.hasFinished(commit.getCommit())]
			<h4>${i18n.translate("commit.build-log.title")}</h4>
			<div class="well">
	[#assign log=states.getLog(commit.getCommit())!""]
	[#if log?has_content]
		[#assign lines=log?split("\n")]

		[#foreach line in lines]
			[#if line?starts_with("[TRACE]") || line?starts_with("[DEBUG]")]
				<code class="log debug">${line}</code>
			[#elseif line?starts_with("[WARN]")]
				<code class="log warn">${line}</code>
			[#elseif line?starts_with("[ERROR]") || line?starts_with("[FATAL]")]
				<code class="log error">${line}</code>
			[#else]
				<code class="log">${line}</code>
			[/#if]
		[/#foreach]
	[#else]
				<i>${i18n.translate("commit.build-log.not-found")}</i>
	[/#if]
			</div>
[/#if]
		</div>
[@macros.renderScripts /]
[@macros.renderFooter /]
