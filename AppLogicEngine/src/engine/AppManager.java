package engine;

import engine.logic.FilesManagement;
import engine.logic.RepositoryManager;
import engine.logic.XMLManager;
import engine.repositories.RepositoriesManager;
import engine.repositories.RepositoryData;
import engine.repositories.UserData;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class AppManager {
    private RepositoriesManager m_RepositoriesManager;

    public AppManager(String i_UserName) {
        m_RepositoriesManager = new RepositoriesManager();
        //recoverRepositoriesManagerFromFiles();
    }

    public UserData GetUserData(String i_UserName) {
        return m_RepositoriesManager.GetUserData(i_UserName);
    }

    public Map<String, UserData> GetAllUserMap() {
        return m_RepositoriesManager.getUsersDataHashMap();
    }

    public void CreateRepositoryFromXml(InputStream i_InputStreamOfXML, String i_UserName) {
        try {
            createUserFolder(i_UserName);
            String repositoryName = getRepositoryNameFromXml(i_InputStreamOfXML);
            Path repositoryPath = Paths.get(getUserRepositoriesFolderPath(i_UserName) + "\\" + repositoryName);
            new RepositoryManager(repositoryPath, i_UserName, true, true, null);
            XMLManager.BuildRepositoryObjectsFromXML(i_InputStreamOfXML, repositoryPath);
            RepositoryManager repository = new RepositoryManager(repositoryPath, i_UserName, false, false, null);
            repository.HandleCheckout(repository.GetHeadBranch().GetBranch().GetBranchName());
            RepositoryData repositoryData = new RepositoryData(repository);

            m_RepositoriesManager.addRepositoryData(i_UserName, repositoryData);

        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getRepositoryNameFromXml(InputStream i_InputStreamOfXML) {
        String repositoryName = null;
        try {
            repositoryName = XMLManager.GetRepositoryNameFromXml(i_InputStreamOfXML);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        return repositoryName;
    }

    //
    private void recoverRepositoriesManagerFromFiles() {

    }

    private void createRepositoriesFolder() {
        Path repositoriesFolderPath = engine.Constants.REPOSITORIES_FOLDER_PATH;
        if (!(repositoriesFolderPath.toFile().exists())) {
            FilesManagement.CreateFolder(repositoriesFolderPath.getParent(), Constants.REPOSITORIES_FOLDER_NAME);
        }
    }

    private void createUserFolder(String i_UserName) {
        createRepositoriesFolder();
        Path userFolderPath = getUserRepositoriesFolderPath(i_UserName);
        if (!(userFolderPath.toFile().exists())) {
            FilesManagement.CreateFolder(userFolderPath.getParent(), i_UserName);
        }
    }

    private Path getUserRepositoriesFolderPath(String i_UserName) {
        return Paths.get(Constants.REPOSITORIES_FOLDER_PATH + "\\" + i_UserName);
    }


}
