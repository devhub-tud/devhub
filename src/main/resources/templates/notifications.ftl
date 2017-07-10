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
    [#list notificationController.getLatestNotificationsFor(user) as notificationWithUser, isRead]
        <tr>
            [#if isRead]
            <td class="notification read">
            [#else]
            <td class="notification unread">
            [/#if]

            <a href="${notificationWithUser.URI}">
                [#if !isRead]
                <form action="${path}/markRead" method="post" class="pull-right">
                    <input type="hidden" name="notificationId" value="${notificationWithUser.getId()}">
                    <button type="submit" class="btn btn-danger btn-sm" style="margin: 5px;">
                        <i class="glyphicon glyphicon-remove-sign"></i> ${i18n.translate("block.my-notifications.mark-read")}
                    </button>
                </form>
                [#else]
                <span class="pull-right">
                    <i class="pull-right">${i18n.translate("block.my-notifications.is-read")}</i>
                </span>
                [/#if]
                <div class="notificationtitle">${notificationWithUser.getTitle(i18n)}</div>
                <div class="truncate">${notificationWithUser.getDescription(i18n)}</div>

            </a>
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
