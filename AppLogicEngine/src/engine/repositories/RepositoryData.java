package engine.repositories;

import engine.logic.Branch;
import engine.logic.Commit;
import engine.logic.RepositoryManager;

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
}
