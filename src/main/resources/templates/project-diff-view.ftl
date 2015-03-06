[#import "macros.ftl" as macros]
[#import "components/difftable.ftl" as difftable]
[#import "components/diffbox.ftl" as diffbox]

[@macros.renderHeader i18n.translate("section.projects") /]
[@macros.renderMenu i18n user /]
		<div class="container">
[@macros.renderCommitHeader i18n group commit "View diff" /]
	[#if diffs?has_content]
		[#list diffs as diffModel]

            [#if diffModel.isAdded()]
                [#-- If the file was added in this commit, there are no possible changes in previous commits --]
                [#assign blame=[]]
            [#else]
                [#assign blame=gitbackend.blame(repository, oldCommit, diffModel.getOldPath())]
            [/#if]

            [@diffbox.diffbox diffModel blame diffModel_index commit comments][/@diffbox.diffbox]
		[/#list]
	[#else]
			<div>${i18n.translate("diff.changes.nothing")}</div>
	[/#if]
		</div>

[@macros.renderScripts /]
[@diffbox.renderScripts/]
[@difftable.renderScripts/]
[@macros.renderFooter /]
