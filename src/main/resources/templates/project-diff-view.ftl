[#import "macros.ftl" as macros]
[#import "components/difftable.ftl" as difftable]
[#import "components/diffbox.ftl" as diffbox]

[@macros.renderHeader i18n.translate("section.projects") /]
[@macros.renderMenu i18n user /]
		<div class="container">

    [@macros.renderCommitHeader i18n group commit "View diff" /]

    [#if diffViewModel.diffResponse?has_content]
        [#list diffViewModel.diffResponse.diffs as diffModel]
            [@diffbox.diffbox diffViewModel diffModel diffModel_index][/@diffbox.diffbox]
        [/#list]
    [#else]
        <div>${i18n.translate("diff.changes.nothing")}</div>
    [/#if]

		</div>

[@macros.renderScripts /]
[@diffbox.renderScripts/]
[@difftable.renderScripts/]
[@macros.renderFooter /]
