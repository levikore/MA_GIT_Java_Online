package engine.repositories;

import com.google.gson.JsonArray;
import engine.logic.*;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

public class RepositoryData {

    private String m_Owner;
    private String m_RepositoryName;
    private String m_RepositoryPath;
    private String m_ActiveBranchName;
    private int m_NumOfBranches;
    private String m_LastCommitComment = "";
    private String m_LastCommitDate = "";
    private List<BranchData> m_BranchesList;
    private List<String> m_BranchesNamesList;
    private List<CommitData> m_HeadBranchCommitsList;
    private List<FileContent> m_CurrentWCFilesList = new LinkedList<>();

    private List<UnCommittedFile> m_UncommittedFilesList= new LinkedList<>();
    private String m_RemoteReference;
    // private RepositoryManager m_RepositoryManager;

    public RepositoryData(RepositoryManager i_RepositoryManager, JsonArray i_CurrentWCFilesList) {
        m_Owner = i_RepositoryManager.GetCurrentUserName();
        m_RepositoryPath = i_RepositoryManager.GetRepositoryPath().toString();
        m_RepositoryName = i_RepositoryManager.GetRepositoryName();
        m_ActiveBranchName = i_RepositoryManager.GetHeadBranch().GetBranch().GetBranchName();
        m_NumOfBranches = i_RepositoryManager.GetAllBranchesList().size();
        if (i_RepositoryManager.GetLastCommit() != null) {
            m_LastCommitComment = i_RepositoryManager.GetLastCommit().GetCommitComment();
            m_LastCommitDate = i_RepositoryManager.GetLastCommit().GetCreationDate();
        }

        m_RemoteReference = i_RepositoryManager.GetRemoteReference() != null ? i_RepositoryManager.GetRemoteReference().toString() : "";
        m_BranchesList = new LinkedList<>();
        if(i_RepositoryManager.GetAllBranchesList()!=null) {
            for (Branch branch : i_RepositoryManager.GetAllBranchesList()) {
                m_BranchesList.add(new BranchData(branch, isActiveBranch(branch, i_RepositoryManager.GetHeadBranch()), branch.GetIsModifiable()));
            }
        }
        List<Commit> commitStringList = i_RepositoryManager.GetHeadBranchCommitHistory(i_RepositoryManager.GetHeadBranch().GetBranch());
        setCommitsListToString(commitStringList, i_RepositoryManager);

        m_BranchesNamesList = new LinkedList<>();
        if(i_RepositoryManager.GetAllBranchesList()!=null) {

            for (Branch branch : i_RepositoryManager.GetAllBranchesList()) {
                m_BranchesNamesList.add(branch.GetBranchName());
            }
        }
        try {
            List<UnCommittedChange> uncommitedChangesList = i_RepositoryManager.GetListOfUnCommittedFiles(i_RepositoryManager.getRootFolder(), i_RepositoryManager.GetCurrentUserName());
            setUncommittedFilesList(uncommitedChangesList);
            List<FileContent> folderList = null;
            if (i_CurrentWCFilesList != null) {
                folderList = getWCFoldersListFromJson(i_CurrentWCFilesList);
            }
            if (i_RepositoryManager.GetHeadBranch().GetBranch().GetCurrentCommit() != null) {
                setCurrentWCFilesList(i_RepositoryManager.GetHeadBranch().GetBranch().GetCurrentCommit().GetCommitRootFolder().GetFilesDataList(), uncommitedChangesList, folderList);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean GetBranchDataIsModifiable(String i_BranchName) {
        boolean isModifiable = false;
        for (BranchData branchData : m_BranchesList) {
            if (branchData.m_BranchName.equals(i_BranchName)) {
                isModifiable = branchData.getisIsModifiable();
                break;
            }
        }

        return isModifiable;
    }

    public void SetBranchDataIsModifiable(String i_BranchName, boolean i_IsModifiable) {
        for (BranchData branchData : m_BranchesList) {
            if (branchData.m_BranchName.equals(i_BranchName)) {
                branchData.setIsModifiable(i_IsModifiable);
                break;
            }
        }
    }

    private List<FileContent> getWCFoldersListFromJson(JsonArray i_CurrentWCFilesList) {
        String content;
        String path;
        boolean isFolder;
        List<FileContent> folderList = new LinkedList<>();

        for (int i = 0; i < i_CurrentWCFilesList.size(); i++) {
            content = i_CurrentWCFilesList.get(i).getAsJsonObject().get("m_Content").getAsString();
            path = i_CurrentWCFilesList.get(i).getAsJsonObject().get("m_Path").getAsString();
            isFolder = i_CurrentWCFilesList.get(i).getAsJsonObject().get("m_IsFolder").getAsBoolean();
            if (isFolder) {
                folderList.add(new FileContent(path, content, true));
            }
        }
        return folderList;
    }

    private UnCommittedChange isBlobDataUncommited(BlobData blobData, List<UnCommittedChange> i_UncommitedChangesList) {
        UnCommittedChange unCommittedChangeToReturn = null;

        for (UnCommittedChange unCommittedChange : i_UncommitedChangesList) {
            if (blobData.GetPath().equals(unCommittedChange.getFile().GetPath()))
                unCommittedChangeToReturn = unCommittedChange;
            break;
        }
        return unCommittedChangeToReturn;
    }

    //types: deleted, updated, added
    private List<UnCommittedChange> getUncomittedFilesListByType(List<UnCommittedChange> i_allUnCommittedFilesList, String i_Type) {
        List<UnCommittedChange> unCommittedFilesList = new LinkedList<>();
        i_allUnCommittedFilesList
                .stream()
                .filter(unCommittedChange -> unCommittedChange.getChangeType().equals(i_Type))
                .forEach(unCommittedChange -> unCommittedFilesList.add(unCommittedChange));

        return unCommittedFilesList;
    }

    private String getContentByPath(List<FileContent> i_FolderList, String i_Path) {
        String content = null;
        for (FileContent fileContent : i_FolderList) {
            if (fileContent.m_Path.equals(i_Path)) {
                content = fileContent.m_Content;
            }
        }
        return content;
    }

    private void setCurrentWCFilesList(List<BlobData> i_BlobDataList, List<UnCommittedChange> i_UncommitedChangesList, List<FileContent> i_FolderList) {
        FileContent fileContent = null;
        for (BlobData blobData : i_BlobDataList) {

            if (Paths.get(blobData.GetPath()).toFile().exists()) {
                if (blobData.GetIsFolder()) {
                    if (i_FolderList != null) {
                        fileContent = new FileContent(blobData.GetPath(), getContentByPath(i_FolderList, blobData.GetPath()), blobData.GetIsFolder());
                    } else {
                        fileContent = new FileContent(blobData.GetPath(), blobData.GetFileContent(), blobData.GetIsFolder());
                    }
                } else {
                    fileContent = new FileContent(blobData.GetPath(), FilesManagement.ReadTextFileContent(blobData.GetPath()), blobData.GetIsFolder());
                }
                m_CurrentWCFilesList.add(fileContent);
            }
        }

        List<UnCommittedChange> unCommittedNewFiles = getUncomittedFilesListByType(i_UncommitedChangesList, "added");
        List<UnCommittedChange> unCommittedDeletedFiles = getUncomittedFilesListByType(i_UncommitedChangesList, "deleted");


        for (UnCommittedChange unCommittedChange : unCommittedNewFiles) {
            if (!unCommittedChange.getFile().GetIsFolder()) {
                fileContent = new FileContent(unCommittedChange.getFile().GetPath(), FilesManagement.ReadTextFileContent(unCommittedChange.getFile().GetPath()), unCommittedChange.getFile().GetIsFolder());
            } else {
                fileContent = new FileContent(unCommittedChange.getFile().GetPath(), getContentByPath(i_FolderList, unCommittedChange.getFile().GetPath()), unCommittedChange.getFile().GetIsFolder());
            }
            m_CurrentWCFilesList.add(fileContent);
        }

        for (UnCommittedChange unCommittedChange : unCommittedDeletedFiles) {
            if (!unCommittedChange.getFile().GetIsFolder()) {
                if (Paths.get(unCommittedChange.getFile().GetPath()).toFile().exists()) {
                    fileContent = new FileContent(unCommittedChange.getFile().GetPath(), FilesManagement.ReadTextFileContent(unCommittedChange.getFile().GetPath()), unCommittedChange.getFile().GetIsFolder());
                    m_CurrentWCFilesList.remove(fileContent);
                }
            } else {
                for (int i = 0; i < m_CurrentWCFilesList.size(); i++) {
                    if (m_CurrentWCFilesList.get(i).m_Path.equals(unCommittedChange.getFile().GetPath())) {
                        m_CurrentWCFilesList.remove(i);
                    }
                }
            }

        }
    }

    private void setUncommittedFilesList(List<UnCommittedChange> i_UnCommittedChangeList) {
        m_UncommittedFilesList = new LinkedList<>();
        for (UnCommittedChange unCommittedChangeindex : i_UnCommittedChangeList) {
            BlobData uncommittedFile = unCommittedChangeindex.getFile();
            FileContent fileContent = new FileContent(uncommittedFile.GetPath(), unCommittedChangeindex.GetContent(), uncommittedFile.GetIsFolder());
            UnCommittedFile unCommittedFile = new UnCommittedFile(fileContent, unCommittedChangeindex.getChangeType());
            m_UncommittedFilesList.add(unCommittedFile);
        }
    }

    private void setCommitsListToString(List<Commit> i_CommitsList, RepositoryManager i_RepositoryManager) {
        m_HeadBranchCommitsList = new LinkedList<>();
        for (Commit commit : i_CommitsList) {
            CommitData commitData = new CommitData(i_RepositoryManager, commit);
            m_HeadBranchCommitsList.add(commitData);
        }
    }

    public String getRepositoryName() {
        return m_RepositoryName;
    }

    public void setRepositoryName(String i_RepositoryName) {
        m_RepositoryName = i_RepositoryName;
    }

    public String getActiveBranchName() {
        return m_ActiveBranchName;
    }

    public void setActiveBranchName(String i_ActiveBranchName) {
        m_ActiveBranchName = i_ActiveBranchName;
    }

    public int getNumOfBranches() {
        return m_NumOfBranches;
    }

    public void setNumOfBranches(int i_NumOfBranches) {
        m_NumOfBranches = i_NumOfBranches;
    }

    private boolean isActiveBranch(Branch i_Branch, HeadBranch i_HeadBranch) {
        return i_Branch.equals(i_HeadBranch.GetBranch());
    }

    private class BranchData {
        private String m_BranchName;
        private String m_CommitSHA1;
        private String m_TrackingAfter;
        private boolean m_IsRemote;
        private String m_CommitComment;
        private boolean m_IsActiveBranch;

        private boolean getisIsModifiable() {
            return m_IsModifiable;
        }

        private boolean m_IsModifiable;

        private BranchData(Branch i_Branch, boolean i_IsActiveBranch, boolean i_IsModifiable) {
            m_BranchName = i_Branch.GetBranchName();
            m_CommitSHA1 = i_Branch.GetCurrentCommit()!=null?i_Branch.GetCurrentCommit().GetCurrentCommitSHA1():"none";
            m_CommitComment = i_Branch.GetCurrentCommit()!=null?i_Branch.GetCurrentCommit().GetCommitComment():"";
            m_IsRemote = i_Branch.GetIsRemote();
            m_TrackingAfter = i_Branch.GetTrackingAfter() != null ? i_Branch.GetTrackingAfter() : "none";
            m_IsActiveBranch = i_IsActiveBranch;
            m_IsModifiable = i_IsModifiable;
        }

        private void setIsModifiable(boolean i_IsModifiable) {
            m_IsModifiable = i_IsModifiable;
        }
    }

    public static class CommitData {
        private String m_CommitDescription;
        private List<String> m_PointedByList;
        private List<String> m_FilesList;
        private List<UnCommittedFile> m_FilesDeltaList = new LinkedList<>();

        private String m_CommitSha1;
        private String m_CommitComment;
        private String m_DataCreated;
        private String m_CreatedBy;

        public CommitData(RepositoryManager i_RepositoryManager, Commit i_Commit) {
            m_CommitDescription = i_Commit.toString();
            m_PointedByList = i_RepositoryManager.GetPointingBranchesNamestoCommit(i_Commit);
            m_FilesList = new LinkedList<>();
            i_Commit.GetCommitRootFolder().GetFilesDataList().forEach(blobData -> m_FilesList.add(blobData.GetPath()));

            m_CommitSha1 = i_Commit.GetCurrentCommitSHA1();
            m_CommitComment = i_Commit.GetCommitComment();
            m_DataCreated = i_Commit.GetCreationDate();
            m_CreatedBy = i_Commit.GetCreatedBy();
        }

        public void SetFilesDeltaList(List<UnCommittedFile> m_FilesDeltaList) {
            this.m_FilesDeltaList = m_FilesDeltaList;
        }
    }

    public static class FileContent {
        private String m_Path;
        private String m_Content;
        private boolean m_IsFolder;

        public FileContent(String i_Path, String i_Content, boolean i_IsFolder) {
            m_Path = i_Path;
            m_Content = i_Content;
            m_IsFolder = i_IsFolder;
        }
    }

    public static class UnCommittedFile {
        private FileContent m_fileContent;
        private String m_ChangeType;

        public UnCommittedFile(FileContent i_FileContent, String i_ChangeType) {
            m_fileContent = i_FileContent;
            m_ChangeType = i_ChangeType;
        }
    }
}
