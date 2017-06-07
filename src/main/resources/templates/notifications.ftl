[#import "macros.ftl" as macros]
[@macros.renderHeader i18n.translate("section.notifications") /]

[@macros.renderMenu i18n user /]
[#if error?? && error?has_content]
    <div class="alert alert-danger">
    ${i18n.translate(error)}
    </div>
[/#if]
    <div class="notifications">
        <table>
            [#list notifications as notification]
                <p>${notification.getSender().getName()}, ${notification.getEvent()} #${notification.getId()}</p>
                <p>${notification.getMessage()}</p>
            [/#list]
        </table>
    </div>
</div>
[@macros.renderScripts /]
[@macros.renderFooter /]
