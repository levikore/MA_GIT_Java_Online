package engine.repositories;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class UserData {
   private  String m_UserName;
    private List<RepositoryData> m_RepositoriesDataList;
    //the keys are the the time of any Notification
    private LinkedList<Notification> m_NotificationsList;
    private Notification m_LastNotificationRead;
    private boolean isUserConnected;

    public UserData(List<RepositoryData> i_RepositoriesDataList, String i_UserName)
    {
        m_RepositoriesDataList=i_RepositoriesDataList;
        m_UserName=i_UserName;
    }

    public void AddRepositoryData(RepositoryData i_RepositoryData)
    {
        m_RepositoriesDataList.add(i_RepositoryData);
    }

    public List<RepositoryData> getRepositoriesDataList() {
        return m_RepositoriesDataList;
    }
}
