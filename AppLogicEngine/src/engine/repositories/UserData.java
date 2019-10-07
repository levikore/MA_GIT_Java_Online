package engine.repositories;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class UserData {
    private List<RepositoryData> m_RepositoriesDataList;
    //the keys are the the time of any Notification
    private LinkedList<Notification> m_NotificationsList;
    private Notification m_LastNotificationRead;
    private boolean isUserConnected;

    public UserData(List<RepositoryData> i_RepositoriesDataList)
    {
        m_RepositoriesDataList=i_RepositoriesDataList;
    }

    public void AddRepositoryData(RepositoryData i_RepositoryData)
    {
        m_RepositoriesDataList.add(i_RepositoryData);
    }

    public List<RepositoryData> getRepositoriesDataList() {
        return m_RepositoriesDataList;
    }
}
