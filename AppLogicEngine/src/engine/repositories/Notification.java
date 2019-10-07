package engine.repositories;

public class Notification {

    String m_NotificationContent;
    String m_DateAndTime;

    public Notification(String i_NotificationContent, String i_DateAndTime) {
        m_NotificationContent = i_NotificationContent;
        m_DateAndTime = i_DateAndTime;
    }

    public String getNotificationContent() {
        return m_NotificationContent;
    }

    public String getDateAndTime() {
        return m_DateAndTime;
    }

}
