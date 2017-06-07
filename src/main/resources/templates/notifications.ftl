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
            [#list notifications as notificationWithUser]
                <tr>
                    <form action="${path}/markRead" method="post" class="pull-right">
                        <input type="hidden" name="notificationId" value="${notificationWithUser.getNotification().getId()}">
                        <button type="submit" class="btn btn-danger btn-sm" style="margin: 5px;">
                            <i class="glyphicon glyphicon-remove-sign"></i> MARKREAD
                        </button>
                    </form>
                    ${notificationWithUser.getNotification().getId()}
                </tr>
            [/#list]
        </table>
    </div>
</div>
[@macros.renderScripts /]
[@macros.renderFooter /]
