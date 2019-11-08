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
                const notificationsDataInSessionStorageObject = JSON.parse(sessionStorage.getItem("notificationsData"));
                lastVersionSeen = notificationsDataInSessionStorageObject.m_LastVersionSeen;
                //var userData= JSON.parse(i_UserData);
                if (i_NotificationsData.m_Version !== lastVersionSeen) {
                    appendUserNotifications(i_NotificationsData.m_Notifications);
                    notificationsDataInSessionStorageObject.m_LastVersionSeen = i_NotificationsData.m_Version;
                    notificationsDataInSessionStorageObject.m_Notifications=notificationsDataInSessionStorageObject.m_Notifications.concat(i_NotificationsData.m_Notifications);

                    notificationsDataInSessionStorageObject.m_Notifications = notificationsDataInSessionStorageObject.m_Notifications.filter((notification, index) => {
                        const _notification = JSON.stringify(notification);
                        return index === notificationsDataInSessionStorageObject.m_Notifications.findIndex(obj => {
                            return JSON.stringify(obj) === _notification;
                        });
                    });


                    sessionStorage.setItem("notificationsData", JSON.stringify(notificationsDataInSessionStorageObject));
                    lastVersionSeen = i_NotificationsData.m_Version;
                }
            }
        }
    });
    setTimeout(ajaxNotifications, refreshNotificationsRate);
}

function uniq(a) {

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