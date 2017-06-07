[#import "macros.ftl" as macros]
[@macros.renderHeader i18n.translate("section.account") /]

[@macros.renderMenu i18n user /]
[#if error?? && error?has_content]
    <div class="alert alert-danger">
    ${i18n.translate(error)}
    </div>
[/#if]
    <div>
        <table>
            [#list notifications as notification]
                <td>${notification.getId()}</td>
            [/#list]
        </table>
    </div>
</div>
[@macros.renderScripts /]
[@macros.renderFooter /]
