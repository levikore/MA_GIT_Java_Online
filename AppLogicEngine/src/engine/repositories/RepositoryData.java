package engine.repositories;

import engine.logic.*;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class RepositoryData {

    private String m_RepositoryName;
    private String m_ActiveBranchName;
    private int m_NumOfBranches;
    private String m_LastCommitComment;
    private String m_LastCommitDate;
    List<String> m_BranchesList;
    List<String> m_BranchesNamesList;
    List<CommitData> m_HeadBranchCommitsList;
    List<FileContent> m_CurrentWCFilesList;
    List<UnCommittedFile> m_UncommittedFilesList;
    // private RepositoryManager m_RepositoryManager;


    public RepositoryData(RepositoryManager i_RepositoryManager) {
        m_RepositoryName = i_RepositoryManager.GetRepositoryName();
        m_ActiveBranchName = i_RepositoryManager.GetHeadBranch().GetBranch().GetBranchName();
        m_NumOfBranches = i_RepositoryManager.GetAllBranchesList().size();
        m_LastCommitComment = i_RepositoryManager.GetLastCommit().GetCommitComment();
        m_LastCommitDate = i_RepositoryManager.GetLastCommit().GetCreationDate();
        m_BranchesList = i_RepositoryManager.GetAllBranchesStringList();
        commitsListToString(i_RepositoryManager.GetCommitColumnByBranch(i_RepositoryManager.GetHeadBranch().GetHeadBranch()), i_RepositoryManager);
        m_BranchesNamesList = new LinkedList<>();
        for (Branch branch : i_RepositoryManager.GetAllBranchesList()) {
            m_BranchesNamesList.add(branch.GetBranchName());
        }
        setCurrentWCFilesList(i_RepositoryManager.GetHeadBranch().GetBranch().GetCurrentCommit().GetCommitRootFolder().GetFilesDataList());
        try {
            setUncommittedFilesList(i_RepositoryManager.GetListOfUnCommittedFiles(i_RepositoryManager.getRootFolder(),i_RepositoryManager.GetCurrentUserName()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setCurrentWCFilesList(List<BlobData> i_BlobDataList) {
        m_CurrentWCFilesList = new LinkedList<>();

        for (BlobData blobData : i_BlobDataList) {
            FileContent fileContent = new FileContent(blobData.GetPath(), blobData.GetFileContent());
            m_CurrentWCFilesList.add(fileContent);
        }
    }

    private void setUncommittedFilesList(List<UnCommittedChange> i_UnCommittedChangeList) {
        m_UncommittedFilesList = new LinkedList<>();

        for (UnCommittedChange unCommittedChangeindex : i_UnCommittedChangeList) {
            FileContent fileContent = new FileContent(unCommittedChangeindex.getFile().GetPath(), unCommittedChangeindex.getFile().GetFileContent());
            UnCommittedFile unCommittedFile=new UnCommittedFile(fileContent,unCommittedChangeindex.getChangeType() );
            m_UncommittedFilesList.add(unCommittedFile);
        }
    }

    private void commitsListToString(List<Commit> i_CommitsList, RepositoryManager i_RepositoryManager) {
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

    private class CommitData {
        String m_CommitDescription;
        List<String> m_PointedByList;
        List<String> m_FilesList;

        private CommitData(RepositoryManager i_RepositoryManager, Commit i_Commit) {
            m_CommitDescription = i_Commit.toString();
            m_PointedByList = i_RepositoryManager.GetPointingBranchesNamestoCommit(i_Commit);
            m_FilesList = new LinkedList<>();
            i_Commit.GetCommitRootFolder().GetFilesDataList().forEach(blobData -> m_FilesList.add(blobData.GetPath()));
        }
    }

    private class FileContent {
        private String m_Path;
        private String m_Content;

        private FileContent(String i_Path, String i_Content) {
            m_Path = i_Path;
            m_Content = i_Content;
        }
    }

    private class UnCommittedFile {
        FileContent m_fileContent;
        String m_ChangeType;

        private UnCommittedFile(FileContent i_FileContent, String i_ChangeType) {
            m_fileContent = i_FileContent;
            m_ChangeType = i_ChangeType;
        }
    }
}
