[#import "macros.ftl" as macros]
[@macros.renderHeader i18n.translate("section.notifications") /]

[@macros.renderMenu i18n user /]
<div class="container">
    <h2>
    ${i18n.translate("block.my-notifications.title")}
        <div class="pull-right"></div>
    </h2>
[#if error?? && error?has_content]
    <div class="alert alert-danger">
    ${i18n.translate(error)}
    </div>
[/#if]
    <table class="table table-bordered">
        <tbody>
[#if notifications??]
    [#list notifications as notification]
        <tr>
            <td>
                <div>${notification.getSender().getName()}, ${notification.getEvent()} #${notification.getId()}</div>
                <div class="truncate">${notification.getMessage()}</div>
            </td>
        </tr>
    [/#list]
[#else]
    <tr>
        <td class="muted">
        ${i18n.translate("block.my-notifications.empty-List")}
        </td>
    </tr>
[/#if]
        </tbody>
    </table>
</div>
[@macros.renderScripts /]
[@macros.renderFooter /]
