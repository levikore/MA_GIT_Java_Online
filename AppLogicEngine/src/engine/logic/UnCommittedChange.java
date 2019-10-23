package engine.logic;

public class UnCommittedChange {
    BlobData m_File;
    String m_ChangeType;//deleted, updated, added
    String m_Content;

    public UnCommittedChange(BlobData i_File, String i_ChangeType) {
        m_File = i_File;
        m_ChangeType = i_ChangeType;
    }

    public BlobData getFile() {
        return m_File;
    }

    public String getChangeType() {
        return m_ChangeType;
    }

    public void SetContent(String i_Content) {
        m_Content = i_Content;
    }

    public String GetContent()
    {
        return m_Content;
    }
}
