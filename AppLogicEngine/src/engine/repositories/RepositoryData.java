package engine.repositories;

import engine.logic.Commit;

public class RepositoryData {

    private String m_RepositoryName;
    private String m_ActiveBranchName;
    private int m_NumOfBranches;
    private Commit m_LastCommit;

    public RepositoryData(String i_RepositoryName) {
        m_RepositoryName = i_RepositoryName;
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

    public Commit getLastCommit() {
        return m_LastCommit;
    }

    public void setLastCommit(Commit i_LastCommit) {
        m_LastCommit = i_LastCommit;
    }
}
