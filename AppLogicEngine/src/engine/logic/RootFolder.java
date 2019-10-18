package engine.logic;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class RootFolder {
    private BlobData m_RootFolder;
    private Path m_RootFolderPath;

    public RootFolder(BlobData i_Folder, Path i_RootFolderPath) {
        m_RootFolder = i_Folder;
        m_RootFolderPath = i_RootFolderPath;
    }

    public void UpdateCurrentRootFolderSha1(String i_UserName, String i_TestFolderName, List<BlobData> io_AllFilesFromCurrentRootFolder, boolean isGeneratedFromXml) throws IOException {
        List<File> emptyFilesList = new LinkedList<>();
        updateRootTreeSHA1Recursively(m_RootFolder, m_RootFolderPath, i_UserName, emptyFilesList, i_TestFolderName, io_AllFilesFromCurrentRootFolder, isGeneratedFromXml);
    }

    private void enterRootTreeBranchAndUpdate(BlobData i_BlobDataOfCurrentFolder, Path i_RootFolderPath, String i_UserName, List<File> emptyFilesList, String i_TestFolderName, List<BlobData> i_AllFilesFromCurrentRootFolder, boolean isGeneretedFromXml) throws IOException {
        if (i_RootFolderPath.toFile() != null && i_RootFolderPath.toFile().listFiles() != null) {
            for (File file : Objects.requireNonNull(i_RootFolderPath.toFile().listFiles())) {
                if (!file.getAbsolutePath().equals(m_RootFolderPath + "\\.magit")) {
                    if (!file.isDirectory() && !FilesManagement.IsFileEmpty(file)) {
                        BlobData simpleBlob = FilesManagement.CreateSimpleFileDescription(m_RootFolderPath, Paths.get(file.getAbsolutePath()), i_UserName, null, i_TestFolderName, i_AllFilesFromCurrentRootFolder, isGeneretedFromXml);
                        i_BlobDataOfCurrentFolder.GetCurrentFolder().AddBlobToList(simpleBlob);
                    } else if (file.isDirectory() && !FilesManagement.IsDirectoryEmpty(file)) {
                        Folder folder = new Folder();
                        BlobData blob = new BlobData(m_RootFolderPath, file.toString(), folder, i_UserName);
                        i_BlobDataOfCurrentFolder.GetCurrentFolder().AddBlobToList(blob);
                        updateRootTreeSHA1Recursively(blob, Paths.get(file.getAbsolutePath()), i_UserName, emptyFilesList, i_TestFolderName, i_AllFilesFromCurrentRootFolder, isGeneretedFromXml);
                    } else {
                        emptyFilesList.add(file);
                    }
                }
            }
        }

        deleteEmptyFiles(emptyFilesList);
    }

    private void exitRootTreeBranchAndUpdate(BlobData i_BlobDataOfCurrentFolder, Path i_RootFolderPath, String i_UserName, List<File> emptyFilesList, String i_TestFolderName, List<BlobData> i_AllFilesFromCurrentRootFolder) {
        if (i_RootFolderPath.toFile().isDirectory() && !FilesManagement.IsDirectoryEmpty(i_RootFolderPath.toFile())) {
            String sha1 = FilesManagement.CreateFolderDescriptionFile(
                    i_BlobDataOfCurrentFolder,
                    m_RootFolderPath,
                    Paths.get(i_RootFolderPath.toAbsolutePath().toString()),
                    i_UserName,
                    i_TestFolderName,
                    false, i_AllFilesFromCurrentRootFolder);
            i_BlobDataOfCurrentFolder.SetSHA1(sha1);
            i_BlobDataOfCurrentFolder.GetCurrentFolder().SetFolderSha1(sha1);
            if (i_BlobDataOfCurrentFolder.GetLastChangedTime() == null) {
                i_BlobDataOfCurrentFolder.SetLastChangedTime(FilesManagement.ConvertLongToSimpleDateTime(i_RootFolderPath.toFile().lastModified()));
            }
        } else if (i_RootFolderPath.toFile().isDirectory()) {
            i_RootFolderPath.toFile().delete();
        }

        deleteEmptyFiles(emptyFilesList);
    }

    private void updateRootTreeSHA1Recursively(BlobData i_BlobDataOfCurrentFolder, Path i_RootFolderPath, String i_UserName, List<File> emptyFilesList, String i_TestFolderName, List<BlobData> i_AllFilesFromCurrentRootFolder, boolean isGeneretedFromXml) throws IOException {
        enterRootTreeBranchAndUpdate(i_BlobDataOfCurrentFolder, i_RootFolderPath, i_UserName, emptyFilesList, i_TestFolderName, i_AllFilesFromCurrentRootFolder, isGeneretedFromXml);
        exitRootTreeBranchAndUpdate(i_BlobDataOfCurrentFolder, i_RootFolderPath, i_UserName, emptyFilesList, i_TestFolderName, i_AllFilesFromCurrentRootFolder);
    }

    public String GetSHA1() {
        return m_RootFolder.GetSHA1();
    }

    private void deleteEmptyFiles(List<File> io_EmptyFilesList) {
        io_EmptyFilesList.forEach(File::delete);
    }

    public Path GetRootFolderPath() {
        return m_RootFolderPath;
    }

    public void RecoverWCFromCurrentRootFolderObj() {
        m_RootFolder.RecoverWCFromCurrentBlobData();
    }

    public List<BlobData> GetFilesDataList() {
        List<BlobData> list = new LinkedList<>();
        m_RootFolder.AddBlobDataToList(list);
        return list;
    }

    public BlobData GetBloBDataOfRootFolder() {
        return m_RootFolder;
    }
}