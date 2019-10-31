package engine.repositories;

import com.google.gson.JsonArray;
import engine.Constants;
import engine.logic.FilesManagement;
import engine.logic.RepositoryManager;
import engine.users.ConnectedUsersManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class UserData {
    private String m_UserName;
    private List<RepositoryData> m_RepositoriesDataList;
    private List<Notification> m_NotificationsList;
    private int m_NotificationsVersion;
    private int m_LastNotificationsVersionSeen;
    private transient String m_UserFolderPath;


    public UserData(List<RepositoryData> i_RepositoriesDataList, String i_UserName) {
        m_RepositoriesDataList = i_RepositoriesDataList;
        m_UserName = i_UserName;
        m_UserFolderPath = Constants.REPOSITORIES_FOLDER_PATH + "\\" + m_UserName;
        m_NotificationsList = new LinkedList<>();
        recoverAllNotifications();
    }


    public List<Notification> GetAllNotificationsList() {
        return m_NotificationsList;
    }


    public int GetNotificationsVersion() {
        return m_NotificationsVersion;
    }

    public int GetLastNotificationsVersionSeen() {
        return m_LastNotificationsVersionSeen;
    }


    public List<Notification> GetNewNotifications() {
        List<Notification> notifications = new LinkedList<>();

        for (int i = m_LastNotificationsVersionSeen + 1; i < m_NotificationsList.size(); i++) {
            notifications.add(m_NotificationsList.get(i));
        }

        return notifications;
    }

    public synchronized void UpdateNotificationLastSeenVersion() {
        if (m_LastNotificationsVersionSeen < m_NotificationsList.size() - 1) {
            m_LastNotificationsVersionSeen = m_NotificationsList.size() - 1;
            FilesManagement.RemoveFileByPath(Paths.get(m_UserFolderPath + "\\" + Constants.USER_NOTIFICATIONS_VERSION));
            FilesManagement.CreateNewFile(m_UserFolderPath + "\\" + Constants.USER_NOTIFICATIONS_VERSION, m_NotificationsVersion + "," + m_LastNotificationsVersionSeen);
        }
    }

    private void recoverAllNotifications() {
        if (Paths.get(m_UserFolderPath + "\\" + Constants.USER_NOTIFICATIONS_VERSION).toFile().exists()) {
            List<String> versions = FilesManagement.ConvertCommaSeparatedStringToList(FilesManagement.ReadTextFileContent(m_UserFolderPath + "\\" + Constants.USER_NOTIFICATIONS_VERSION));
            m_NotificationsVersion = Integer.parseInt(versions.get(0));
            m_LastNotificationsVersionSeen = Integer.parseInt(versions.get(1));
            ///recover Notification from notifications.txt file
        }
    }

    public synchronized void AppendNewNotification(String i_Time, String i_Content) {
        Notification notification = new Notification(i_Content, i_Time);
        m_NotificationsList.add(notification);
        m_NotificationsVersion++;
        FilesManagement.RemoveFileByPath(Paths.get(m_UserFolderPath + "\\" + Constants.USER_NOTIFICATIONS_VERSION));
        FilesManagement.CreateNewFile(m_UserFolderPath + "\\" + Constants.USER_NOTIFICATIONS_VERSION, m_NotificationsVersion + "," + m_LastNotificationsVersionSeen);
        try {
            Files.write(Paths.get(m_UserFolderPath + "\\" + Constants.USER_NOTIFICATIONS_FILE), (i_Content + "\n").getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void UpdateSpecificRepositoryData(RepositoryManager i_RepositoryManager, JsonArray i_CurrentWCFilesList) {
        RepositoryData repositoryData = GetRepositoryDataByName(i_RepositoryManager.GetRepositoryName());
        Integer index = m_RepositoriesDataList.indexOf(repositoryData);
        m_RepositoriesDataList.set(index, new RepositoryData(i_RepositoryManager, i_CurrentWCFilesList));
    }

    public RepositoryData GetRepositoryDataByName(String i_RepositoryDataName) {
        RepositoryData repositoryDataPointer = null;

        for (RepositoryData repositoryData : m_RepositoriesDataList) {
            if (repositoryData.getRepositoryName().equals(i_RepositoryDataName)) {
                repositoryDataPointer = repositoryData;
                break;
            }
        }
        return repositoryDataPointer;
    }

    public void AddRepositoryData(RepositoryData i_RepositoryData) {
        m_RepositoriesDataList.add(i_RepositoryData);
    }

    public List<RepositoryData> getRepositoriesDataList() {
        return m_RepositoriesDataList;
    }

    public String getUserFolderPath() {
        return m_UserFolderPath;
    }

}
