package engine.logic;

public class Conflict {
    private UnCommittedChange m_OurFile;
    private UnCommittedChange m_TheirsFile;
    private BlobData m_Ancestor;

    public Conflict(UnCommittedChange i_OurFile, UnCommittedChange i_TheirsFile, BlobData i_Ancestor) {
        m_OurFile = i_OurFile;
        m_TheirsFile = i_TheirsFile;
        m_Ancestor = i_Ancestor;
    }

    public UnCommittedChange getOurFile() {
        return m_OurFile;
    }

    public UnCommittedChange getTheirsFile() {
        return m_TheirsFile;
    }

    public BlobData getAncestor() {
        return m_Ancestor;
    }

    public void setAncestor(BlobData i_Ancestor) {
        this.m_Ancestor = i_Ancestor;
    }

    @Override
    public String toString() {
        return
                "OurFile: " + m_OurFile.getFile().GetPath()+" "+m_OurFile.getChangeType()+
                "\nTheirsFile: " + m_TheirsFile.getFile().GetPath() +" "+m_TheirsFile.getChangeType();
    }
}
