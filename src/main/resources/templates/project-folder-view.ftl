[#import "macros.ftl" as macros]
[@macros.renderHeader i18n.translate("section.projects") /]
[@macros.renderMenu i18n user /]
		<div class="container">
[@macros.renderCommitHeader i18n group commit i18n.translate("commit.view-files")/]
[@macros.renderFileTreeExplorer group commit repository path entries /]
[@macros.renderScripts /]
[@macros.renderFooter /]
