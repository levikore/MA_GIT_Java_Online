package engine.repositories;

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
    List<String> m_HeadBranchCommitsList;
    // private RepositoryManager m_RepositoryManager;


    public RepositoryData(RepositoryManager i_RepositoryManager) {
        m_RepositoryName = i_RepositoryManager.getRepositoryName();
        m_ActiveBranchName = i_RepositoryManager.GetHeadBranch().GetBranch().GetBranchName();
        m_NumOfBranches = i_RepositoryManager.GetAllBranchesList().size();
        m_LastCommitComment = i_RepositoryManager.GetLastCommit().GetCommitComment();
        m_LastCommitDate = i_RepositoryManager.GetLastCommit().GetCreationDate();
        m_BranchesList = i_RepositoryManager.GetAllBranchesStringList();
        commitsListToString(i_RepositoryManager.GetCommitColumnByBranch(i_RepositoryManager.GetHeadBranch().GetHeadBranch()));
    }

    private void commitsListToString(List<Commit> i_CommitsList) {
        m_HeadBranchCommitsList = new LinkedList<>();
        i_CommitsList.forEach(commit -> m_HeadBranchCommitsList.add(commit.toString()));
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

}
