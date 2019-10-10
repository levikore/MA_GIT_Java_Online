package engine.repositories;

import engine.logic.Commit;
import engine.logic.RepositoryManager;

public class RepositoryData {

    private String m_RepositoryName;
    private String m_ActiveBranchName;
    private int m_NumOfBranches;
    private String m_LastCommitComment;
    private String m_LastCommitDate;

    public RepositoryData(RepositoryManager i_RepositoryManager) {
        m_RepositoryName = i_RepositoryManager.getRepositoryName();
        m_ActiveBranchName=i_RepositoryManager.GetHeadBranch().GetBranch().GetBranchName();
        m_NumOfBranches=i_RepositoryManager.GetAllBranchesList().size();
        m_LastCommitComment=i_RepositoryManager.GetLastCommit().GetCommitComment();
        m_LastCommitDate=i_RepositoryManager.GetLastCommit().GetCreationDate();
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
