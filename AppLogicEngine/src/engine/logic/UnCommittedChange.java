package engine.logic;

public class UnCommittedChange {
    BlobData m_File;
    String m_ChangeType;//deleted, updated, added

    public UnCommittedChange(BlobData i_File, String i_ChangeType) {
        m_File =i_File;
        m_ChangeType = i_ChangeType;
    }

    public BlobData getFile() {
        return m_File;
    }

    public String getChangeType() {
        return m_ChangeType;
    }
}
