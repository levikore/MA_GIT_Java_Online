package engine.logic;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class Folder {
    private List<BlobData> m_BlobList = new LinkedList<>();
    private String m_FolderSha1;

    public Folder() {

    }

    public Folder(String i_FolderSha1) {
        m_FolderSha1 = i_FolderSha1;
    }

    public void AddBlobToList(BlobData blobData) {
        m_BlobList.add(blobData);
        m_BlobList.sort(Comparator.comparing(BlobData::GetPath));
    }

    public List<BlobData> GetBlobList() {
        return m_BlobList;
    }

    public String GetFolderSha1() {
        return m_FolderSha1;
    }

    public void SetFolderSha1(String i_FolderSha1) {
        m_FolderSha1 = i_FolderSha1;
    }

    public void ScanBlobListIntoWc() {
        m_BlobList.forEach(BlobData::RecoverWCFromCurrentBlobData);
    }

    public void AddAllBlobsUnderCurrentFolderToList(List<BlobData> i_DataList) {
        for (BlobData blobData : m_BlobList) {
            blobData.AddBlobDataToList(i_DataList);
        }
    }
}
