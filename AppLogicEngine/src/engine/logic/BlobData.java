package engine.logic;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class BlobData {
    private String m_Path;
    private String m_SHA1;
    private boolean m_IsFolder;
    private String m_Type;
    private Folder m_CurrentFolder;
    private String m_LastChangedBY;
    private String m_LastChangedTime;
    private Path m_RepositoryPath;


    public BlobData(
            Path i_RepositoryPath,
            String i_Path,
            String i_LastChangedBY,
            String i_LastChangedTime,
            Boolean i_IsFolder,
            String i_SHA1,
            Folder i_CurrentFolder
    ) {
        m_RepositoryPath = i_RepositoryPath;
        m_Path = i_Path;
        m_LastChangedBY = i_LastChangedBY;
        m_LastChangedTime = i_LastChangedTime;
        m_IsFolder = i_IsFolder;
        m_SHA1 = i_SHA1;
        m_Type = m_IsFolder ? "Folder" : "Blob";
        m_CurrentFolder = i_CurrentFolder;
    }

    public BlobData(
            Path i_RepositoryPath,
            String i_Path,
            Folder i_CurrentFolder,
            String i_UserName
    ) {
        m_RepositoryPath = i_RepositoryPath;
        m_Path = i_Path;
        m_CurrentFolder = i_CurrentFolder;
        m_IsFolder = true;
        m_Type = "Folder";
        m_LastChangedBY = i_UserName;
    }

    public Path GetRepositoryPath() {
        return m_RepositoryPath;
    }

    public Folder GetCurrentFolder() {
        return m_CurrentFolder;
    }

    public boolean GetIsFolder() {
        return m_IsFolder;
    }

    public String GetPath() {
        return m_Path;
    }

    public String GetSHA1() {
        return m_SHA1;
    }

    public void SetSHA1(String i_SHA1) {
        this.m_SHA1 = i_SHA1;
    }

    public String GetLastChangedBY() {
        return m_LastChangedBY;
    }

    public void SetLastChangedBY(String i_LastChangedBY) {
        this.m_LastChangedBY = i_LastChangedBY;
    }

    public String GetLastChangedTime() {
        return m_LastChangedTime;
    }

    public void SetLastChangedTime(String i_LastChangedTime) {
        this.m_LastChangedTime = i_LastChangedTime;
    }

    public void RecoverWCFromCurrentBlobData() {
        Path currentPath = Paths.get(m_Path);
        if (!m_IsFolder) {
            FilesManagement.ExtractZipFileToPath(Paths.get(m_RepositoryPath.toString() + "\\.magit\\objects\\" + m_SHA1 + ".zip"), currentPath.getParent());
        } else {
            FilesManagement.CreateFolder(currentPath.getParent(), currentPath.getFileName().toString());
            m_CurrentFolder.ScanBlobListIntoWc();
        }
    }

    public void AddBlobDataToList(List<BlobData> i_DataList) {
        Path currentPath = Paths.get(m_Path);
        i_DataList.add(this);
        if (m_IsFolder) {
            m_CurrentFolder.AddAllBlobsUnderCurrentFolderToList(i_DataList);
        }
    }

    public String GetFileContent()
    {
       String content="";

       if(Paths.get(m_RepositoryPath.toString() + "\\.magit\\objects\\" + m_SHA1 + ".zip").toFile().exists()) {
           content = FilesManagement.ReadZipIntoString(m_RepositoryPath.toString() + "\\.magit\\objects\\" + m_SHA1 + ".zip").stream().collect(Collectors.joining("\n"));
       }

        return content;
    }

    @Override
    public String toString() {
        return
                " Name='" + m_Path + '\'' +
                        ", Type='" + m_Type + '\'' +
                        ", SHA1='" + m_SHA1 + '\'' +
                        ", LastChangedBY='" + m_LastChangedBY + '\'' +
                        ", LastChangedTime=" + m_LastChangedTime +
                        '\n';
    }
}
