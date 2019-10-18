package engine.logic;

import java.nio.file.Path;

public class HeadBranch {
    private Branch m_HeadBranch;
    private String m_HeadBranchSha1;
    private Path m_RepositoryPath;

    public HeadBranch(Branch i_HeadBranch, Path i_RepositoryPath, boolean i_IsNewHead, String i_HeadBranchSha1) {
        m_RepositoryPath = i_RepositoryPath;
        m_HeadBranch = i_HeadBranch;
        if (i_IsNewHead)
            m_HeadBranchSha1 = FilesManagement.CreateHeadFile(i_HeadBranch, m_RepositoryPath);
        else
            m_HeadBranchSha1 = i_HeadBranchSha1;
    }

    public void UpdateCurrentBranch(Commit i_NewCommit) {
        m_HeadBranch.UpdateBranchCommit(i_NewCommit);
        m_HeadBranchSha1 = FilesManagement.UpdateHeadFile(this,m_HeadBranch, m_RepositoryPath);
    }

    public Branch GetBranch() {
        return m_HeadBranch;
    }

    public void Merge(Branch i_BranchToMerge) {

    }

    public void Checkout(Branch i_HeadBranch) {
        setHeadBranch(i_HeadBranch);
        FilesManagement.CleanWC(m_RepositoryPath);
        m_HeadBranch.GetCurrentCommit().GetCommitRootFolder().RecoverWCFromCurrentRootFolderObj();
    }

    private void setHeadBranch(Branch i_HeadBranch) {
        m_HeadBranch = i_HeadBranch;
        m_HeadBranchSha1 = FilesManagement.UpdateHeadFile(this,i_HeadBranch, m_RepositoryPath);
    }

    public Branch GetHeadBranch() {
        return m_HeadBranch;
    }

    public String GetHeadBranchSha1() {
        return m_HeadBranchSha1;
    }

    public Path GetRepositoryPath() {
        return m_RepositoryPath;
    }
}

