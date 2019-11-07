package engine.logic;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Branch {
    private Commit m_CurrentCommit;
    private String m_BranchName;
    private String m_BranchSha1;
    private Path m_RepositoryPath;
    private Path m_BranchPath;

    private Boolean m_IsRemote = false;
    private String m_TrackingAfter = null;



    private Boolean m_IsModifiable = false;

    public Branch(String i_BranchName, Commit i_Commit, Path i_RepositoryPath, boolean i_IsNewBranch, String i_BranchSha1, Boolean i_IsRemote, String i_TrackingAfter)//for the first Branch in the git.
    {
        m_RepositoryPath = i_RepositoryPath;
        m_BranchName = i_BranchName;
        m_CurrentCommit = i_Commit;
        m_BranchPath = Paths.get(m_RepositoryPath + "\\.magit\\branches\\" + i_BranchName + ".txt");

        m_IsRemote = i_IsRemote;
        m_TrackingAfter = i_TrackingAfter;


        if (i_IsNewBranch) {
            FilesManagement.HandleTrackingFolder(i_BranchName, i_IsRemote, i_TrackingAfter, i_RepositoryPath);
            m_BranchSha1 = FilesManagement.CreateBranchFile(i_BranchName, i_Commit, i_RepositoryPath);
        } else {
            m_BranchSha1 = i_BranchSha1;
        }
    }

    public void SetIsModifiable(Boolean i_IsModifiable) {
        this.m_IsModifiable = i_IsModifiable;
    }

    public boolean GetIsModifiable() {
       return m_IsModifiable;
    }

    public Branch(String i_BranchName, Branch i_ParentBranch, Path i_RepositoryPath, boolean i_IsNewBranch, String i_BranchSha1, Commit i_Commit, Boolean i_IsRemote, String i_TrackingAfter) {
        m_RepositoryPath = i_RepositoryPath;
        m_BranchName = i_BranchName;
        m_BranchPath = Paths.get(m_RepositoryPath + "\\.magit\\branches\\" + i_BranchName + ".txt");

        m_IsRemote = i_IsRemote;
        m_TrackingAfter = i_TrackingAfter;

        if (i_Commit == null) {
            m_CurrentCommit = i_ParentBranch.m_CurrentCommit;
        } else {
            m_CurrentCommit = i_Commit;
        }
        if (i_IsNewBranch) {
            FilesManagement.HandleTrackingFolder(i_BranchName, i_IsRemote, i_TrackingAfter, i_RepositoryPath);
            m_BranchSha1 = FilesManagement.CreateBranchFile(i_BranchName, m_CurrentCommit, i_RepositoryPath);
        } else {
            m_BranchSha1 = i_BranchSha1;
        }
    }

    public void UpdateBranchCommit(Commit i_NewCommit) {
        m_BranchSha1 = FilesManagement.UpdateBranchFile(this, i_NewCommit, m_RepositoryPath);
        m_CurrentCommit = i_NewCommit;
    }

    public void ChangeBranchToRemoteBranch(String remoteRepositoryName) {
        m_BranchName = remoteRepositoryName + "\\" + m_BranchName;
        m_BranchPath = Paths.get(m_RepositoryPath + "\\.magit\\branches\\" + remoteRepositoryName + "\\" + m_BranchName + ".txt");
        m_IsRemote = true;
        m_BranchSha1 = FilesManagement.UpdateBranchFile(this, m_CurrentCommit, m_RepositoryPath);
    }

    public String GetBranchName() {
        return m_BranchName;
    }

    public void SetCurrentCommit(Commit i_CurrentCommit) {
        m_CurrentCommit = i_CurrentCommit;
    }


    public String GetBranchSha1() {
        return m_BranchSha1;
    }

    public Commit GetCurrentCommit() {
        return m_CurrentCommit;
    }

    public Path GetBranchPath() {
        return m_BranchPath;
    }

    public void SetIsRemote(Boolean i_IsRemote) {
        m_IsRemote = i_IsRemote;
    }

    public Boolean GetIsRemote() {
        return m_IsRemote;
    }

    public void SetTrackingAfter(String i_RemoteBranchName) {
        FilesManagement.HandleTrackingFolder(m_BranchName, m_IsRemote, i_RemoteBranchName, m_RepositoryPath);
        m_TrackingAfter = i_RemoteBranchName;
    }

    public String GetTrackingAfter() {
        return m_TrackingAfter;
    }
}
