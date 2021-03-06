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
import java.util.*;

public class AppManager {
    private RepositoriesManager m_RepositoriesManager;

    public AppManager(String i_UserName) {
        m_RepositoriesManager = new RepositoriesManager();
        recoverRepositoriesManagerFromFiles();
    }

    private void recoverRepositoriesManagerFromFiles() {
        String userName;
        String repositoryName;
        Path repositoryPath;
        RepositoryManager repository = null;

        if (Constants.REPOSITORIES_FOLDER_PATH.toFile().exists()) {
            for (File file : Constants.REPOSITORIES_FOLDER_PATH.toFile().listFiles()) {
                userName = file.getName();
                for (File repositoryFile : getUserRepositoriesFolderPath(userName).toFile().listFiles()) {
                    repositoryName = repositoryFile.getName();
                    repositoryPath = Paths.get(getUserRepositoriesFolderPath(userName) + "\\" + repositoryName);

                    if (!repositoryName.equals("notifications")) {
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

    public void HandleClone(String i_OriginRepositoryUserName, String i_OriginRepositoryName, String i_UserName, String i_NewRepositoryName) {
        Path originPath = getRepositoryPath(i_OriginRepositoryUserName, i_OriginRepositoryName);
        Path localPath = getRepositoryPath(i_UserName, i_NewRepositoryName);
        createUserFolder(i_UserName);

        try {
            RepositoryManager localRepository = CollaborationManager.CloneRepository(originPath, localPath, i_UserName);
            RepositoryData repositoryData = new RepositoryData(localRepository, null);
            m_RepositoriesManager.addRepositoryData(i_UserName, repositoryData, localRepository);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String HandlePull(String i_LocalUserName, String i_LocalRepositoryName) {
        RepositoryManager localRepository = m_RepositoriesManager.GetRepositoryByName(i_LocalUserName, i_LocalRepositoryName);
        String response = "";
        try {
            response = CollaborationManager.Pull(localRepository.GetRemoteReference(), localRepository);
            RepositoryData repositoryData = new RepositoryData(localRepository, null);
            m_RepositoriesManager.UpdateRepositoryData(i_LocalUserName, repositoryData, localRepository);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return response;
    }

    public void HandlePushLocalBranch(String i_LocalUserName, String i_LocalRepositoryName) {
        RepositoryManager localRepository = m_RepositoriesManager.GetRepositoryByName(i_LocalUserName, i_LocalRepositoryName);
        List<Branch> branchesList = localRepository.GetAllBranchesList();
        Path remoteRepositoryReference = localRepository.GetRemoteReference();

        try {
            CollaborationManager.PushLocalBranch(remoteRepositoryReference, localRepository);
            String remoteUserName = getUserNameByUrl(remoteRepositoryReference.toString());
            RepositoryManager updatedRemoteRepository = new RepositoryManager(remoteRepositoryReference, remoteUserName, false, false, null);
            RepositoryData remoteRepositoryData = new RepositoryData(updatedRemoteRepository, null);
            m_RepositoriesManager.UpdateRepositoryData(remoteUserName, remoteRepositoryData, updatedRemoteRepository);//****

            RepositoryManager updatedLocalRepository = new RepositoryManager(localRepository.GetRepositoryPath(), localRepository.GetCurrentUserName(), false, false, localRepository.GetRemoteReference());

            recoverBranchIsModifiable(branchesList, updatedLocalRepository);

            updatedLocalRepository.GetHeadBranch().GetBranch().SetIsModifiable(true);

            RepositoryData localRepositoryData = new RepositoryData(updatedLocalRepository, null);
            //localRepositoryData.SetBranchDataIsModifiable(localRepository.GetHeadBranch().GetHeadBranch().GetBranchName(), true);
            m_RepositoriesManager.UpdateRepositoryData(i_LocalUserName, localRepositoryData, updatedLocalRepository);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void recoverBranchIsModifiable(List<Branch> i_BranchesList, RepositoryManager i_UpdatedLocalRepository) {
        for (int i = 0; i < i_BranchesList.size(); i++) {
            i_UpdatedLocalRepository.GetAllBranchesList().get(i).SetIsModifiable(i_BranchesList.get(i).GetIsModifiable());
        }
    }

    public void HandleAcceptPullRequest(String i_Index, String i_Time, String i_RepositoryName, String i_UserName, String i_AskingUserName, String i_TargetBranchName, String i_BaseBranchName) {
        RepositoryManager remoteRepository = m_RepositoriesManager.GetRepositoryByName(i_UserName, i_RepositoryName);
        remoteRepository.HandleFFMerge(i_BaseBranchName, i_TargetBranchName);
        RepositoryData remoteRepositoryData = new RepositoryData(remoteRepository, null);
        m_RepositoriesManager.UpdateRepositoryData(i_UserName, remoteRepositoryData, remoteRepository);

        UserData userData = GetUserData(i_UserName);
        UserData.PullRequest pullRequest = userData.GetPullRequest(Integer.parseInt(i_Index));
        pullRequest.Accept();

        UserData localUserData = GetUserData(i_AskingUserName);
        localUserData.AppendNewNotification(i_Time, i_UserName + " accepted your PR.  " +
                "\nRemote Repository: " + remoteRepository.GetRepositoryName() +
                "\nBase Branch: " + i_BaseBranchName +
                "\nTarget Branch: " + i_TargetBranchName +
                "\nTime of PR: " + pullRequest.GetTime() +
                "\n----------------");

    }

    public void HandleRejectPullRequest(String i_Index, String i_Time, String i_RepositoryName, String i_UserName, String i_AskingUserName, String i_TargetBranchName, String i_BaseBranchName, String i_RejectionComment) {
        UserData userData = GetUserData(i_UserName);
        UserData.PullRequest pullRequest = userData.GetPullRequest(Integer.parseInt(i_Index));
        pullRequest.Reject(i_RejectionComment);

        UserData localUserData = GetUserData(i_AskingUserName);
        localUserData.AppendNewNotification(i_Time, i_UserName + " rejected your PR.  " +
                "\nRemote Repository: " + i_RepositoryName +
                "\nBase Branch: " + i_BaseBranchName +
                "\nTarget Branch: " + i_TargetBranchName +
                "\nComment: " + i_RejectionComment +
                "\nTime of PR: " + pullRequest.GetTime() +
                "\n----------------");
    }

    public String HandlePullRequest(String i_LocalUserName, String i_LocalRepositoryName, String i_BaseBranchName, String i_TargetBranchName, String i_Message, String i_Time) {
        String errorList = "";

        try {
            RepositoryManager localRepository = m_RepositoriesManager.GetRepositoryByName(i_LocalUserName, i_LocalRepositoryName);
            Path remoteRepositoryReference = localRepository.GetRemoteReference();
            String remoteUserName = getUserNameByUrl(remoteRepositoryReference.toString());
            RepositoryManager remoteRepository = new RepositoryManager(remoteRepositoryReference, remoteUserName, false, false, null);
            if (remoteRepository.FindBranchByName(i_BaseBranchName) != null && remoteRepository.FindBranchByName(i_TargetBranchName) != null) {
                UserData.PullRequest newPullRequest = new UserData.PullRequest(i_Time, remoteRepository.GetRepositoryName(), i_LocalUserName, i_TargetBranchName, i_BaseBranchName, i_Message);
                newPullRequest.SetCommitsDeltaList(getCommitDataDeltaList(remoteRepository, i_BaseBranchName, i_TargetBranchName));
                UserData remoteUserData = GetUserData(remoteUserName);
                remoteUserData.AddPullRequest(newPullRequest);
                remoteUserData.AppendNewNotification(i_Time, i_LocalUserName + " sent pull request for repository " + remoteRepository.GetRepositoryName() +
                        " \nTarget Branch: " + i_TargetBranchName + " Base Branch: " + i_BaseBranchName + "\nMessage: " + i_Message);
            } else {
                errorList = "One of the branches doesnt exist in remote repository";
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return errorList;
    }

    private List<RepositoryData.CommitData> getCommitDataDeltaList(RepositoryManager i_RepositoryManager, String i_BaseBranchName, String i_TargetBranchName) {
        Branch baseBranch = i_RepositoryManager.FindBranchByName(i_BaseBranchName);
        Branch targetBranch = i_RepositoryManager.FindBranchByName(i_TargetBranchName);

        List<Commit> commitsDeltaList = i_RepositoryManager.GetNewerCommitsInBranch(baseBranch.GetCurrentCommit(), targetBranch);

        List<RepositoryData.CommitData> commitDataDeltaList = new LinkedList<>();

        for (Commit commit : commitsDeltaList) {
            Commit previousCommit = commit.GetPrevCommitsList().get(0);
            List<BlobData> addedFiles = new LinkedList<>();
            List<BlobData> updatedFiles = new LinkedList<>();
            List<BlobData> deletedFiles = new LinkedList<>();
            commit.BuildDeltaListsForOneCommit(previousCommit, addedFiles, updatedFiles, deletedFiles);
            List<RepositoryData.UnCommittedFile> fileChangeList = getFileChangeList(commit, addedFiles, updatedFiles, deletedFiles);
            RepositoryData.CommitData commitData = new RepositoryData.CommitData(i_RepositoryManager, commit);
            commitData.SetFilesDeltaList(fileChangeList);
            commitDataDeltaList.add(commitData);
        }

        return commitDataDeltaList;

    }

    public List<RepositoryData.UnCommittedFile> getFileChangeList(Commit i_Commit, List<BlobData> i_AddedFiles, List<BlobData> i_UpdatedFiles, List<BlobData> i_DeletedFiles) {
        List<RepositoryData.UnCommittedFile> changeList = getSpecificFileChangeList(i_AddedFiles, i_Commit, "create");
        changeList.addAll(getSpecificFileChangeList(i_UpdatedFiles, i_Commit, "edit"));
        changeList.addAll(getSpecificFileChangeList(i_DeletedFiles, i_Commit, "delete"));
        return changeList;
    }

    public List<RepositoryData.UnCommittedFile> getSpecificFileChangeList(List<BlobData> i_FileList, Commit i_Commit, String i_ChangeType) {
        List<RepositoryData.UnCommittedFile> changelist = new LinkedList<>();
        for (BlobData blobData : i_FileList) {
            RepositoryData.FileContent fileContent = new RepositoryData.FileContent(blobData.GetPath(),
                    blobData.GetFileContent(),
                    blobData.GetIsFolder());

            RepositoryData.UnCommittedFile change = new RepositoryData.UnCommittedFile(fileContent, i_ChangeType);
            changelist.add(change);

        }

        return changelist;
    }

    public String HandlePush(String i_LocalUserName, String i_LocalRepositoryName) {
        RepositoryManager localRepository = m_RepositoriesManager.GetRepositoryByName(i_LocalUserName, i_LocalRepositoryName);
        Path remoteRepositoryReference = localRepository.GetRemoteReference();
        String response = "";
        try {
            response = CollaborationManager.Push(remoteRepositoryReference, localRepository);
            String remoteUserName = getUserNameByUrl(remoteRepositoryReference.toString());
            //String remoteRepositoryName = remoteRepositoryReference.getFileName().toString();
            //RepositoryManager oldRemoteRepository = m_RepositoriesManager.GetRepositoryByName(remoteUserName, remoteRepositoryName);
            RepositoryManager updatedRemoteRepository = new RepositoryManager(remoteRepositoryReference, remoteUserName, false, false, null);
            RepositoryData remoteRepositoryData = new RepositoryData(updatedRemoteRepository, null);
            //remoteRepositoryData.SetBranchDataIsModifiable(localRepository.GetHeadBranch().GetBranch().GetBranchName(), true);
            m_RepositoriesManager.UpdateRepositoryData(remoteUserName, remoteRepositoryData, updatedRemoteRepository);//****

        } catch (IOException e) {
            e.printStackTrace();
        }

        return response;
    }

    private String getUserNameByUrl(String i_UrlString) {
        int indexOfRepositoryName = i_UrlString.lastIndexOf(Constants.REPOSITORIES_FOLDER_NAME) + Constants.REPOSITORIES_FOLDER_NAME.length() - 1;
        String temp = i_UrlString.substring(indexOfRepositoryName + 2, i_UrlString.length() - 1);
        int indexOfFirstSlashInTemp = temp.indexOf("\\");
        String userName = temp.substring(0, indexOfFirstSlashInTemp);
        return userName;
    }

    public UserData GetUserData(String i_UserName) {
        return m_RepositoriesManager.GetUserData(i_UserName);
    }

    public Map<String, UserData> GetAllUserMap() {
        return m_RepositoriesManager.getUsersDataHashMap();
    }

    public RepositoryManager GetRepositoryByName(String i_UserName, String i_RepositoryName) {
        return m_RepositoriesManager.GetRepositoryByName(i_UserName, i_RepositoryName);
    }

    public void CreateEmptyRepositoryFromXml(InputStream i_InputStreamOfXML, String i_UserName) {
        createUserFolder(i_UserName);
        String repositoryName = getRepositoryNameFromXml(i_InputStreamOfXML);
        Path repositoryPath = Paths.get(getUserRepositoriesFolderPath(i_UserName) + "\\" + repositoryName);
        try {

            RepositoryManager repository = new RepositoryManager(repositoryPath, i_UserName, true, false, null);
            RepositoryData repositoryData = new RepositoryData(repository, null);
            m_RepositoriesManager.addRepositoryData(i_UserName, repositoryData, repository);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            RepositoryData repositoryData = new RepositoryData(repository, null);

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
        return Paths.get(getUserRepositoriesFolderPath(i_UserName) + "\\" + i_RepositoryName);
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
                if (Paths.get(path).toFile().exists()) {
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
