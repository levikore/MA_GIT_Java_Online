const refreshNotificationsRate = 2000; //milli seconds
let isExistNotificationsInSessionStorage;
const NOTIFICATIONS_URL = buildUrlWithContextPath("notifications");
let lastVersionSeen;

function ajaxNotifications() {
    $.ajax({
        url: NOTIFICATIONS_URL,
        success: function (i_NotificationsData) {
            if (i_NotificationsData !== "") {
                if (!isExistNotificationsInSessionStorage) {
                    sessionStorage.setItem("notificationsData", JSON.stringify(i_NotificationsData));
                    isExistNotificationsInSessionStorage = true;
                }
                lastVersionSeen = JSON.parse(sessionStorage.getItem("notificationsData")).m_LastVersionSeen;
                //var userData= JSON.parse(i_UserData);
                if (i_NotificationsData.m_Version !== lastVersionSeen) {
                    appendUserNotifications(i_NotificationsData.m_Notifications);
                    lastVersionSeen = i_NotificationsData.m_Version;
                }
            }
        }
    });
    setTimeout(ajaxNotifications, refreshNotificationsRate);
}

function appendUserNotifications(i_Notifications) {
    const notificationElementId = $("#notifications-area");
    if (i_Notifications != null) {
        i_Notifications.forEach(notification => {
            notificationElementId.append(
                $('<ul><small>' + notification.m_DateAndTime + ':</small>' + notification.m_NotificationContent + '</ul>')
            );
        });
    }
}

$(function () {
    if (sessionStorage.getItem("notificationsData") !== null && JSON.parse(sessionStorage.getItem("notificationsData")) !== "") {
        isExistNotificationsInSessionStorage = true;
        const currentNotificationObject = JSON.parse(sessionStorage.getItem("notificationsData"));
        appendUserNotifications(currentNotificationObject.m_Notifications);
        lastVersionSeen = currentNotificationObject.m_LastVersionSeen;

    } else {
        isExistNotificationsInSessionStorage = false;
    }
    setTimeout(ajaxNotifications, refreshNotificationsRate);
});