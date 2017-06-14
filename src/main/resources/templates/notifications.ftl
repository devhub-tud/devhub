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
    [#list notifications as notificationWithUser]
        <tr>
            <td>
                <a href="${notificationWithUser.getNotification().getLink()}">
                    [#if !notificationWithUser.isRead()]
                    <form action="${path}/markRead" method="post" class="pull-right">
                        <input type="hidden" name="notificationId" value="${notificationWithUser.getNotification().getId()}">
                        <button type="submit" class="btn btn-danger btn-sm" style="margin: 5px;">
                            <i class="glyphicon glyphicon-remove-sign"></i> MARKREAD
                        </button>
                    </form>
                    [/#if]
                    <div>${notificationWithUser.getNotification().getTitle()}</div>
                    <div class="truncate">${notificationWithUser.getNotification().getMessage()}</div>
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
