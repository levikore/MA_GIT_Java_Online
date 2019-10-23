package engine.repositories;

import com.google.gson.JsonArray;
import engine.Constants;
import engine.logic.RepositoryManager;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

public class UserData {
    private String m_UserName;
    private List<RepositoryData> m_RepositoriesDataList;
    //the keys are the the time of any Notification
    private LinkedList<Notification> m_NotificationsList;
    private Notification m_LastNotificationRead;
    private boolean isUserConnected;
    private Path m_UserFolderPath;


    public UserData(List<RepositoryData> i_RepositoriesDataList, String i_UserName) {
        m_RepositoriesDataList = i_RepositoriesDataList;
        m_UserName = i_UserName;
        Path m_UserFolderPath = Paths.get(Constants.REPOSITORIES_FOLDER_PATH + "\\" + m_UserName);
    }

    public void UpdateSpecificRepositoryData(RepositoryManager i_RepositoryManager, JsonArray i_CurrentWCFilesList) {
        RepositoryData repositoryData= GetRepositoryDataByName(i_RepositoryManager.GetRepositoryName());
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

    public Path getUserFolderPath() {
        return m_UserFolderPath;
    }

}
