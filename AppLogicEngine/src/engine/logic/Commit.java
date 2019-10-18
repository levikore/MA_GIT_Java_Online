package engine.logic;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class  Commit {
    private String m_CurrentCommitSHA1;
    private List<Commit> m_PrevCommitsList;
    private String m_CommitComment;
    private String m_CreationDate;
    private String m_CreatedBy;
    private RootFolder m_RootFolder;

    public Commit() {

    }

    public Commit(RootFolder i_RootFolder, String i_CommitComment, String i_CreatedBy, List<Commit> i_PrevCommitsList, String i_Sha1, String i_CreationDate) {//commit 2
        m_RootFolder = i_RootFolder;
        m_PrevCommitsList = i_PrevCommitsList;
        m_CommitComment = i_CommitComment;
        m_CreatedBy = i_CreatedBy;
        m_CreationDate = i_CreationDate;
        m_CurrentCommitSHA1 = i_Sha1;
    }

    public void UpdateCommit(RootFolder i_RootFolder, String i_CommitComment, String i_CreatedBy, List<Commit> i_PrevCommitsList, String i_Sha1, String i_CreationDate) {
        m_RootFolder = i_RootFolder;
        m_PrevCommitsList = i_PrevCommitsList;
        m_CommitComment = i_CommitComment;
        m_CreatedBy = i_CreatedBy;
        m_CreationDate = i_CreationDate;
        m_CurrentCommitSHA1 = i_Sha1;
    }

    public String GetRootSHA1() {
        return m_RootFolder.GetSHA1();
    }

    public String GetCurrentCommitSHA1() {
        return this.m_CurrentCommitSHA1;
    }

    public void SetCurrentCommitSHA1(String i_CurrentCommitSHA1) {
        this.m_CurrentCommitSHA1 = i_CurrentCommitSHA1;
    }

    public void SetCreationDate(String i_CreationDate) {
        this.m_CreationDate = i_CreationDate;
    }

    public String GetCommitComment() {
        return m_CommitComment;
    }

    public String GetCreationDate() {
        return m_CreationDate;
    }

    public String GetCreatedBy() {
        return m_CreatedBy;
    }

    public String GetRootFolderSha1() {
        return m_RootFolder.GetSHA1();
    }

    public List<Commit> GetPrevCommitsList() {
        return m_PrevCommitsList;
    }

    public void SetPrevCommitsList(List<Commit> i_PrevCommitsList) {
        this.m_PrevCommitsList = i_PrevCommitsList;
    }

    public String GetPreviousCommitsSHA1String() {
        String previousCommitsSHA1String = "";

        if (m_PrevCommitsList != null) {
            for (Commit commit : m_PrevCommitsList) {
                previousCommitsSHA1String = previousCommitsSHA1String.concat(commit.GetCurrentCommitSHA1() + "," );
            }
        }

        previousCommitsSHA1String = previousCommitsSHA1String.length() != 0 ? previousCommitsSHA1String.substring(0, previousCommitsSHA1String.length() - 1) : ""; //remove last comma from string

        return previousCommitsSHA1String;
    }

    public RootFolder GetCommitRootFolder() {
        return m_RootFolder;
    }

    public long GetCreationDateInMilliseconds() {
        long milliseconds = 0;
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss:SSS" );
        try {
            Date d = dateFormat.parse(m_CreationDate);
            milliseconds = d.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return milliseconds;
    }

    public String GetDeltaString() {
        String deltaString = "";

        if (m_PrevCommitsList != null) {
            for (Commit previousCommit : m_PrevCommitsList) {
                List<String> addedFiles = new LinkedList<>();
                List<String> updatedFiles = new LinkedList<>();
                List<String> deletedFiles = new LinkedList<>();
                buildDeltaListsForOneCommit(previousCommit, addedFiles, updatedFiles, deletedFiles);
                deltaString = deltaString.concat(
                        "\n-Previous Commit SHA1: " + previousCommit.GetCurrentCommitSHA1() + "\n" +
                                (addedFiles.isEmpty() ? "" : "-added files: \n" + String.join(", \n", addedFiles) + "\n\n" ) +
                                (updatedFiles.isEmpty() ? "" : "-updated files: \n" + String.join(", \n", updatedFiles) + "\n\n" ) +
                                (deletedFiles.isEmpty() ? "" : "-deleted files: \n" + String.join(", \n", deletedFiles) + "\n" ) +
                                "----------------------------------------------------------\n"
                );
            }
        }

        return deltaString;
    }

    private void buildDeltaListsForOneCommit(Commit i_PreviousCommit, List<String> io_AddedFiles, List<String> io_UpdatedFiles, List<String> io_DeletedFiles) {
        List<BlobData> commitContentList = m_RootFolder.GetFilesDataList();
        List<BlobData> previousCommitBlobList = i_PreviousCommit.GetCommitRootFolder().GetFilesDataList();

        buildUpdatedAndAddedFilesLists(commitContentList, previousCommitBlobList, io_AddedFiles, io_UpdatedFiles);
        buildDeletedFilesList(previousCommitBlobList, commitContentList, io_DeletedFiles);
    }

    private void buildUpdatedAndAddedFilesLists(List<BlobData> i_CommitContentList, List<BlobData> i_PreviousCommitBlobList, List<String> io_AddedFiles, List<String> io_UpdatedFiles){
        boolean isFound;
        for (BlobData blobData : i_CommitContentList) {
            isFound = false;
            for (BlobData fatherBlobData : i_PreviousCommitBlobList) {
                if (blobData.GetPath().equals(fatherBlobData.GetPath())) {
                    isFound = true;
                    if (!blobData.GetFileContent().equals(fatherBlobData.GetFileContent())) {
                        io_UpdatedFiles.add(blobData.GetPath());
                    }

                    break;
                }
            }

            if (!isFound) {
                io_AddedFiles.add(blobData.GetPath());
            }
        }
    }

    private void buildDeletedFilesList(List<BlobData> i_PreviousCommitBlobList, List<BlobData> i_CommitContentList, List<String> io_DeletedFiles){
        boolean isFound;
        for (BlobData fatherBlobData : i_PreviousCommitBlobList) {
            isFound = false;
            for (BlobData blobData : i_CommitContentList) {
                if (blobData.GetPath().equals(fatherBlobData.GetPath())) {
                    isFound = true;
                    break;
                }
            }

            if (!isFound) {
                io_DeletedFiles.add(fatherBlobData.GetPath());
            }
        }
    }


    @Override
    public String toString() {
        return
                "SHA1: " + m_CurrentCommitSHA1 + '\n' +
                        "Commit Comment: " + m_CommitComment + '\n' +
                        "Date Created: " + m_CreationDate + '\n' +
                        "Created by: " + m_CreatedBy + '\n' +
                        '\n';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Commit) {
            return ((Commit) obj).GetCurrentCommitSHA1().equals(m_CurrentCommitSHA1);
        }

        return false;
    }
}
