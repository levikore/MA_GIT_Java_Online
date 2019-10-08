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

public class AppManager {
    private String m_UserName;
    private RepositoryManager m_CurrentRepository;
    private RepositoriesManager m_RepositoriesManager;
    private Path m_UserFolderPath;

    public AppManager(String i_UserName) {
        m_UserName = i_UserName;
        m_UserFolderPath = Paths.get(Constants.REPOSITORIES_FOLDER_PATH + "\\" + m_UserName);
        m_RepositoriesManager = new RepositoriesManager();
        //recoverRepositoriesManagerFromFiles();
    }

    public String GetUserName() {
        return m_UserName;
    }

    public void CreateRepositoryFromXml(InputStream i_InputStreamOfXML, String i_UserName) {
        try {
            createUserFolder();
            String repositoryName = getRepositoryNameFromXml(i_InputStreamOfXML);
            Path repositoryPath = Paths.get(m_UserFolderPath + "\\" + repositoryName);
            new RepositoryManager(repositoryPath, i_UserName, true, true, null);
            XMLManager.BuildRepositoryObjectsFromXML(i_InputStreamOfXML, repositoryPath);
            RepositoryManager repository = new RepositoryManager(repositoryPath, m_UserName, false, false, null);
            repository.HandleCheckout(repository.GetHeadBranch().GetBranch().GetBranchName());
            RepositoryData repositoryData = new RepositoryData(repositoryName);
            m_RepositoriesManager.addRepositoryData(m_UserName, repositoryData);

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

    private void createUserFolder() {
        createRepositoriesFolder();
        if (!(m_UserFolderPath.toFile().exists())) {
            FilesManagement.CreateFolder(m_UserFolderPath.getParent(), m_UserName);
        }
    }


}
