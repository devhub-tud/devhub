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
[#if notificationController??]
    [#assign notificationMap = notificationController.getLatestNotificationsFor(user)]
    [#list notificationMap as notificationWithUser, isRead]
        <tr>
            <td class="notification [#if isRead]read[#else]unread[/#if]">
                <a href="/notifications/${notificationWithUser.id}">
                    <div class="notificationtitle">${notificationWithUser.getTitle(i18n)}</div>
                    <div class="truncate">${notificationWithUser.getDescription(i18n)}</div>
                    <div class="truncate">${notificationWithUser.timestamp}</div>
                </a>
            </td>
        </tr>
    [/#list]
    [#if !notificationMap?has_content]
        <tr>
            <td>
                <em>Nothing to show here.</em>
            </td>
        </tr>
    [/#if]
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
