package engine;

import com.google.gson.JsonArray;
import engine.logic.*;
import engine.repositories.RepositoriesManager;
import engine.repositories.RepositoryData;
import engine.repositories.UserData;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class AppManager {
    private RepositoriesManager m_RepositoriesManager;

    public AppManager(String i_UserName) {
        m_RepositoriesManager = new RepositoriesManager();
        recoverRepositoriesManagerFromFiles();
    }

    private void recoverRepositoriesManagerFromFiles()
    {
        String userName;
        String repositoryName;
        Path repositoryPath;
        RepositoryManager repository=null;

        if(Constants.REPOSITORIES_FOLDER_PATH.toFile().exists()) {
            for (File file : Constants.REPOSITORIES_FOLDER_PATH.toFile().listFiles()) {
                userName = file.getName();
                for (File repositoryFile : getUserRepositoriesFolderPath(userName).toFile().listFiles()) {
                    repositoryName = repositoryFile.getName();
                    repositoryPath = Paths.get(getUserRepositoriesFolderPath(userName) + "\\" + repositoryName);

                    if (!repositoryName.equals("notifications")){
                        try {
                            repository = new RepositoryManager(repositoryPath, userName, false, false, null);
                            repository.HandleCheckout(repository.GetHeadBranch().GetBranch().GetBranchName());
                            RepositoryData repositoryData = new RepositoryData(repository, null);
                            m_RepositoriesManager.addRepositoryData(userName, repositoryData, repository);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }
        }
    }

   public void HandleClone(String i_OriginRepositoryUserName,String i_OriginRepositoryName, String i_UserName, String i_NewRepositoryName)
   {
       Path originPath = getRepositoryPath(i_OriginRepositoryUserName,i_OriginRepositoryName);
       Path localPath = getRepositoryPath(i_UserName,i_NewRepositoryName);
       createUserFolder(i_UserName);

       try {
           RepositoryManager localRepository= CollaborationManager.CloneRepository(originPath, localPath, i_UserName);
           RepositoryData repositoryData = new RepositoryData(localRepository,null);
           m_RepositoriesManager.addRepositoryData(i_UserName, repositoryData, localRepository);

       } catch (IOException e) {
           e.printStackTrace();
       }
   }

    public UserData GetUserData(String i_UserName) {
        return m_RepositoriesManager.GetUserData(i_UserName);
    }

    public Map<String, UserData> GetAllUserMap() {
        return m_RepositoriesManager.getUsersDataHashMap();
    }

    public RepositoryManager GetRepositoryByName(String i_UserName, String i_RepositoryName)
    {
        return m_RepositoriesManager.GetRepositoryByName(i_UserName, i_RepositoryName);
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
            RepositoryData repositoryData = new RepositoryData(repository,null);

            m_RepositoriesManager.addRepositoryData(i_UserName, repositoryData, repository);

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
           // FilesManagement.CreateFolder(userFolderPath,"notifications");
          //  FilesManagement.CreateNewFile(Paths.get(userFolderPath+"\\"+"notifications\\"+"version.txt").toString(),"-1,-1");
          //  FilesManagement.CreateNewFile(Paths.get(userFolderPath+"\\"+"notifications\\"+"notifications.txt").toString(),"");
        }
    }

    private Path getUserRepositoriesFolderPath(String i_UserName) {
        return Paths.get(Constants.REPOSITORIES_FOLDER_PATH + "\\" + i_UserName);
    }

    private Path getRepositoryPath(String i_UserName, String i_RepositoryName) {
        return Paths.get(getUserRepositoriesFolderPath(i_UserName)+"\\"+i_RepositoryName);
    }

    public void ChangeFiles(JsonArray i_OpenChangesArray, String i_RepositoryName, String i_UserName) {
        String action = null;
        String path = null;
        String content = null;
        Boolean isFolder;
        RepositoryManager repository = GetRepositoryByName(i_UserName, i_RepositoryName);
        for (int i = 0; i < i_OpenChangesArray.size(); i++) {
            action = i_OpenChangesArray.get(i).getAsJsonObject().get("action").getAsString();
            path = i_OpenChangesArray.get(i).getAsJsonObject().get("path").getAsString();
            Path currentPath = Paths.get(path);
            isFolder = i_OpenChangesArray.get(i).getAsJsonObject().get("isFolder").getAsBoolean();
            if (action.equals("create")) {
                if (isFolder) {
                    FilesManagement.CreateFolder(currentPath.getParent(), currentPath.getFileName().toString());
                } else {
                    content = i_OpenChangesArray.get(i).getAsJsonObject().get("content").getAsString();
                    FilesManagement.CreateNewFile(path, content);
                }
            } else if (action.equals("delete")) {
               if(Paths.get(path).toFile().exists()) {
                   if (isFolder) {
                       FilesManagement.DeleteFolder(path);
                   } else {
                       FilesManagement.RemoveFileByPath(currentPath);
                   }
               }
            } else if (action.equals("edit")) {
                content = i_OpenChangesArray.get(i).getAsJsonObject().get("content").getAsString();
                FilesManagement.RemoveFileByPath(currentPath);
                FilesManagement.CreateNewFile(path, content);
            } else {
                ///error
            }
        }
    }
}
