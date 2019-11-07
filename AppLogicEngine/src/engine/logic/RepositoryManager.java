package engine.logic;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class RepositoryManager {
    private String m_CurrentUserName;


    private String m_RepositoryName;
    private Path m_RepositoryPath;
    private RootFolder m_RootFolder;
    private HeadBranch m_HeadBranch;
    private Commit m_CurrentCommit;
    private Boolean m_IsFirstCommit = true;
    private Path m_MagitPath;
    private List<Branch> m_AllBranchesList = new LinkedList<>();
    private Path m_RemoteReference = null;

    private final String c_GitFolderName = ".magit";
    private final String c_ObjectsFolderName = "objects";
    private final String c_BranchesFolderName = "branches";
    private final String c_TestFolderName = "test";

    public RepositoryManager(Path i_RepositoryPath, String i_CurrentUserName, boolean i_IsNewRepository, boolean i_IsEmptyFolders, Path i_RemoteReference) throws IOException {
        m_RepositoryPath = i_RepositoryPath;
        m_RepositoryName = m_RepositoryPath.toFile().getName();
        m_CurrentUserName = i_CurrentUserName;
        m_MagitPath = Paths.get(m_RepositoryPath.toString() + "\\" + c_GitFolderName);
        m_RemoteReference = i_RemoteReference;

        if (i_IsNewRepository) {
            initializeRepository(i_IsEmptyFolders);
        } else {
            m_IsFirstCommit = false;
            recoverRepositoryFromFiles();
        }
    }

    public boolean HandleMerge(String i_BranchName, List<Conflict> o_ConflictsList) {
        Branch branchToMerge = FindBranchByName(i_BranchName);
        boolean retVal = false;
        if (branchToMerge != null) {
            Commit ancestorCommit = getCommonCommit(branchToMerge.GetCurrentCommit(), m_CurrentCommit);
            createMergedWC(ancestorCommit, branchToMerge, o_ConflictsList);
            retVal = true;
        }
        return retVal;
    }

    public void HandleFFMerge(String i_BranchName) {
        Branch branch = FindBranchByName(i_BranchName);
        GetHeadBranch().GetHeadBranch().SetCurrentCommit(branch.GetCurrentCommit());
        GetHeadBranch().UpdateCurrentBranch(branch.GetCurrentCommit());
        HandleCheckout(GetHeadBranch().GetHeadBranch().GetBranchName());
    }

    public boolean IsFFMerge(String i_BranchName) {
        boolean isFFMerge = false;
        Branch branchToMerge = FindBranchByName(i_BranchName);
        if (branchToMerge != null) {
            Commit ancestorCommit = getCommonCommit(branchToMerge.GetCurrentCommit(), m_CurrentCommit);
            if (ancestorCommit.GetCurrentCommitSHA1().equals(m_CurrentCommit.GetCurrentCommitSHA1())) {
                isFFMerge = true;
            }
        }
        return isFFMerge;
    }


    private void createMergedWC(Commit i_AncestorCommit, Branch i_branchToMerge, List<Conflict> o_ConflictsList) {
        List<UnCommittedChange> theirsBranchChangesFromParent = new LinkedList<>();
        List<UnCommittedChange> ourBranchChangesFromParent = new LinkedList<>();
        List<BlobData> ancestorCommitFilesList = i_AncestorCommit.GetCommitRootFolder().GetFilesDataList();
        addUncommittedBlobsToListRecursively(
                i_AncestorCommit.GetCommitRootFolder().GetBloBDataOfRootFolder().GetCurrentFolder(),
                i_branchToMerge.GetCurrentCommit().GetCommitRootFolder().GetBloBDataOfRootFolder().GetCurrentFolder(),
                theirsBranchChangesFromParent
        );
        addUncommittedBlobsToListRecursively(
                i_AncestorCommit.GetCommitRootFolder().GetBloBDataOfRootFolder().GetCurrentFolder(),
                m_RootFolder.GetBloBDataOfRootFolder().GetCurrentFolder(),
                ourBranchChangesFromParent
        );

        updateWcAndConflictsList(ourBranchChangesFromParent, theirsBranchChangesFromParent, ancestorCommitFilesList, o_ConflictsList);

    }

    public String GetRepositoryName() {
        return m_RepositoryName;
    }

    public RootFolder getRootFolder() {
        return m_RootFolder;
    }

    public Path GetRemoteReference() {
        return m_RemoteReference;
    }

    private Commit getCommonCommit(Commit i_Commit1, Commit i_Commit2) {
        Commit commonCommit = null;

        if (i_Commit1.GetCurrentCommitSHA1().equals(i_Commit2.GetCurrentCommitSHA1())) {
            commonCommit = i_Commit1;
        } else {
            Commit newerCommit = getTheNewerCommit(i_Commit1, i_Commit2);
            Commit olderCommit = getTheOlderCommit(i_Commit1, i_Commit2);
            List<Commit> commonCommitsList = new LinkedList<>();
            for (Commit commit : Objects.requireNonNull(newerCommit.GetPrevCommitsList())) {
                commonCommitsList.add(getCommonCommit(commit, olderCommit));
            }

            if (commonCommitsList.size() == 1) {
                commonCommit = commonCommitsList.get(0);
            } else if (commonCommitsList.size() == 2) {
                commonCommit = getTheNewerCommit(commonCommitsList.get(0), commonCommitsList.get(1));
            }
        }

        return commonCommit;
    }

    private Commit getTheOlderCommit(Commit i_Commit1, Commit i_Commit2) {
        Commit newerCommit = getTheNewerCommit(i_Commit1, i_Commit2);
        Commit olderCommit = i_Commit1;
        if (newerCommit == i_Commit1) {
            olderCommit = i_Commit2;
        }
        return olderCommit;
    }

    private Commit getTheNewerCommit(Commit i_Commit1, Commit i_Commit2) {
        Commit newerCommit;
        long commit1DateInMs = convertStringDateToLong(i_Commit1.GetCreationDate());
        long commit2DateInMs = convertStringDateToLong(i_Commit2.GetCreationDate());

        if (commit1DateInMs >= commit2DateInMs) {
            newerCommit = i_Commit1;
        } else {
            newerCommit = i_Commit2;
        }
        return newerCommit;
    }

    private long convertStringDateToLong(String i_Date) {
        long milliseconds = 0;
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss:sss");
        try {
            Date d = dateFormat.parse(i_Date);
            milliseconds = d.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return milliseconds;
    }

    public Path GetRepositoryPath() {
        return m_RepositoryPath;
    }

    public String GetCurrentUserName() {
        return m_CurrentUserName;
    }

    public void SetCurrentUserName(String i_CurrentUserName) {
        this.m_CurrentUserName = i_CurrentUserName;
    }

    private void initializeRepository(boolean i_IsEmptyFolders) {
        m_RootFolder = getInitializedRootFolder(m_CurrentUserName);
        createSystemFolders();

        if (!i_IsEmptyFolders) {
            Branch branch = new Branch("master", m_CurrentCommit, m_RepositoryPath, true, "", false, null);
            m_AllBranchesList.add(branch);
            m_HeadBranch = new HeadBranch(branch, m_RepositoryPath, true, "");
        }
    }

    private void createSystemFolders() {
        FilesManagement.CreateFolder(m_RepositoryPath.getParent(), m_RepositoryName);
        FilesManagement.CreateFolder(m_RepositoryPath, c_GitFolderName);
        FilesManagement.CreateFolder(m_MagitPath, c_ObjectsFolderName);
        FilesManagement.CreateFolder(m_MagitPath, c_BranchesFolderName);
    }

    private void createNewCommit(String i_CommitComment, Commit i_CommitToMerge) {
        Commit newCommit;

        if (m_IsFirstCommit) {
            newCommit = new Commit(m_RootFolder, i_CommitComment, m_CurrentUserName, null, "", "");
            m_IsFirstCommit = false;
        } else {
            List<Commit> prevCommitsList = new LinkedList<>();
            prevCommitsList.add(m_CurrentCommit);
            if (i_CommitToMerge != null) {
                prevCommitsList.add(i_CommitToMerge);
            }
            newCommit = new Commit(m_RootFolder, i_CommitComment, m_CurrentUserName, prevCommitsList, "", "");
        }

        m_CurrentCommit = newCommit;
        String sha1 = FilesManagement.CreateCommitDescriptionFile(m_CurrentCommit, m_RepositoryPath, false);
        m_CurrentCommit.SetCurrentCommitSHA1(sha1);

        if (m_HeadBranch == null) {
            Branch branch = new Branch("master", m_CurrentCommit, m_RepositoryPath, true, "", false, null);
            removeBranchFromBranchesListByName("master");
            m_AllBranchesList.add(branch);
            m_HeadBranch = new HeadBranch(branch, m_RepositoryPath, true, "");
        } else {
            m_HeadBranch.UpdateCurrentBranch(m_CurrentCommit);
        }
    }

    private RootFolder getInitializedRootFolder(String i_UserName) {
        Folder rootFolder = new Folder();
        BlobData rootFolderBlobData = new BlobData(m_RepositoryPath, m_RepositoryPath.toFile().toString(), rootFolder, i_UserName);
        return new RootFolder(rootFolderBlobData, m_RepositoryPath);
    }

    public Boolean HandleCommit(String i_CommitComment, Commit i_CommitToMerge) throws IOException {
        Boolean isCommitNecessary;

        if (m_IsFirstCommit) {
            handleFirstCommit(i_CommitComment);
            isCommitNecessary = true;
        } else {
            isCommitNecessary = handleSecondCommit(i_CommitComment, i_CommitToMerge);
        }

        return isCommitNecessary;
    }

    public void HandleBranch(String i_BranchName, Commit i_Commit, String i_TrackingAfter) {
        Branch branch;
        if (i_Commit == null) {
            branch = new Branch(i_BranchName, m_HeadBranch.GetBranch(), m_RepositoryPath, true, "", null, false, i_TrackingAfter);
        } else {
            branch = new Branch(i_BranchName, m_HeadBranch.GetBranch(), m_RepositoryPath, true, "", i_Commit, false, i_TrackingAfter);
        }

        m_AllBranchesList.add(branch);
    }

    public Boolean IsBranchExist(String i_BranchName) {
        Branch fountBranch = m_AllBranchesList.stream()
                .filter(branch -> i_BranchName.equals(branch.GetBranchName()))
                .findAny()
                .orElse(null);

        return fountBranch != null;
    }

    private void removeBranchFromBranchesListByName(String i_BranchName) {
        Branch branchToRemove = FindBranchByName(i_BranchName);
        if (branchToRemove != null) {
            m_AllBranchesList.remove(branchToRemove);
        }
    }

    public boolean RemoveBranch(String i_BranchName) {
        boolean returnValue = true;
        Branch branchToRemove = FindBranchByName(i_BranchName);

        if (branchToRemove == m_HeadBranch.GetBranch()) {
            returnValue = false;
        } else if (branchToRemove == null) {
            returnValue = false;
        } else {
            FilesManagement.RemoveFileByPath(branchToRemove.GetBranchPath());
            FilesManagement.RemoveFileByPath(Paths.get(m_MagitPath.toString() + "\\" + c_ObjectsFolderName + "\\" + branchToRemove.GetBranchSha1() + ".zip"));
            m_AllBranchesList.remove(branchToRemove);
        }

        return returnValue;
    }

    private void handleFirstCommit(String i_CommitComment) throws IOException {
        m_RootFolder = getInitializedRootFolder(m_CurrentUserName);
        m_RootFolder.UpdateCurrentRootFolderSha1(m_CurrentUserName, "", null, false);
        createNewCommit(i_CommitComment, null);
    }

    public boolean HandleCheckout(String i_BranchName) {
        Branch branchToCheckout = FindBranchByName(i_BranchName);
        boolean retVal = false;

        if (branchToCheckout != null) {
            m_HeadBranch.Checkout(branchToCheckout);
            m_RootFolder = m_HeadBranch.GetBranch().GetCurrentCommit().GetCommitRootFolder();
            m_CurrentCommit = new Commit();
            m_CurrentCommit = m_HeadBranch.GetBranch().GetCurrentCommit();
            retVal = true;
        }

        return retVal;
    }

    public Branch FindBranchByName(String i_BranchName) {
        Branch branchToReturn = null;
        if (m_AllBranchesList != null) {
            for (Branch branch : m_AllBranchesList) {
                if (branch.GetBranchName().equals(i_BranchName)) {
                    branchToReturn = branch;
                }
            }
        }
        return branchToReturn;
    }

    public synchronized List<UnCommittedChange> GetListOfUnCommittedFiles(RootFolder i__RootFolder, String i_CurrentUserName) throws IOException {
        RootFolder testRootFolder = createFolderWithZipsOfUnCommittedFiles(i__RootFolder, i_CurrentUserName);
        String testFolderPath = m_MagitPath + "\\" + c_TestFolderName;
        List<UnCommittedChange> unCommittedFilesList = new LinkedList<>();

        if (!testRootFolder.GetSHA1().equals(m_RootFolder.GetSHA1())) {
            getAllUncommittedFiles(testRootFolder, unCommittedFilesList);
        }

        FileUtils.deleteDirectory((Paths.get(testFolderPath).toFile()));
        return unCommittedFilesList;
    }

    private void getAllUncommittedFiles(RootFolder io_TestRootFolder, List<UnCommittedChange> io_UnCommittedFilesList) {

        addUncommittedBlobsToListRecursively(
                m_RootFolder.GetBloBDataOfRootFolder().GetCurrentFolder(),
                io_TestRootFolder.GetBloBDataOfRootFolder().GetCurrentFolder(),
                io_UnCommittedFilesList);
    }

    private void addUncommittedBlobsToListRecursively(Folder i_Folder, Folder i_TestFolder, List<UnCommittedChange> io_UnCommittedFilesList) {

        List<BlobData> currentBlobList = i_Folder.GetBlobList();
        List<BlobData> testBlobList = i_TestFolder.GetBlobList();
        int savedJ = 0;
        int savedI = 0;
        int j = 0;

        for (int i = 0; i < currentBlobList.size(); i++) {
            BlobData blob = currentBlobList.get(i);
            if (savedJ > j) {
                j = savedJ;
            }
            while (j < testBlobList.size()) {
                BlobData testBlob = testBlobList.get(j);
                if (!blob.GetPath().equals(testBlob.GetPath()) && isPath1AfterPath2(blob.GetPath(), testBlob.GetPath())) {//add new File
                    handleUncommittedNewFile(testBlob, io_UnCommittedFilesList, "added");
                } else if (blob.GetPath().equals(testBlob.GetPath())) {//add updated file
                    if (!blob.GetSHA1().equals(testBlob.GetSHA1())) {
                        handleAddUncommittedUpdatedFile(testBlob, blob, io_UnCommittedFilesList, "updated");
                    }
                    j++;
                    savedJ = j;
                    break;
                }
                if (!isPath1AfterPath2(blob.GetPath(), testBlob.GetPath())) {
                    handleUncommittedNewFile(blob, io_UnCommittedFilesList, "deleted");
                    savedJ = j;
                    break;
                }
                j++;
            }
            savedI = i;
            if (savedJ == testBlobList.size()) {
                break;
            }
        }

        for (int i = savedI + 1; i < currentBlobList.size(); i++) {
            BlobData blob = currentBlobList.get(i);
            UnCommittedChange unCommittedRemovedBlob = new UnCommittedChange(blob, "deleted");
            io_UnCommittedFilesList.add(unCommittedRemovedBlob);
            if (blob.GetIsFolder()) {
                addUncommittedFolderToList(blob.GetCurrentFolder(), io_UnCommittedFilesList, "deleted");
            }
        }

        for (j = savedJ; j < testBlobList.size(); j++) {
            BlobData testBlob = testBlobList.get(j);
            UnCommittedChange unCommittedNewBlob = new UnCommittedChange(testBlob, "added");
            io_UnCommittedFilesList.add(unCommittedNewBlob);
            if (testBlob.GetIsFolder()) {
                addUncommittedFolderToList(testBlob.GetCurrentFolder(), io_UnCommittedFilesList, "added");
            }
        }

    }

    private BlobData findBlobDataByHisPathInList(String i_Path, List<BlobData> i_List) {
        BlobData blob = null;
        for (BlobData blobData : Objects.requireNonNull(i_List)) {
            if (blobData.GetPath().equals(i_Path)) {
                blob = blobData;
                break;
            }
        }
        return blob;
    }

    private void updateWCInMergeWithoutConflict(UnCommittedChange unCommittedChange) {
        if (unCommittedChange != null) {
            BlobData blob = unCommittedChange.getFile();
            switch (unCommittedChange.getChangeType()) {
                case "added":
                    FilesManagement.ExtractZipFileToPath(Paths.get(m_MagitPath + "\\" + c_ObjectsFolderName + "\\" + blob.GetSHA1() + ".zip"), Paths.get(blob.GetPath()).getParent());
                    break;
                case "deleted":
                    FilesManagement.RemoveFileByPath(Paths.get(blob.GetPath()));
                    break;
                case "updated":
                    FilesManagement.RemoveFileByPath(Paths.get(blob.GetPath()));
                    FilesManagement.ExtractZipFileToPath(Paths.get(m_MagitPath + "\\" + c_ObjectsFolderName + "\\" + blob.GetSHA1() + ".zip"), Paths.get(blob.GetPath()).getParent());
                    break;
            }
        }
    }

    private void updateWcAndConflictsList(List<UnCommittedChange> io_OurFiles, List<UnCommittedChange> io_TheirFiles, List<BlobData> io_AncestorFiles, List<Conflict> io_ConflictsFilesList) {

        int savedJ = 0;
        int savedI = 0;
        int j = 0;

        for (int i = 0; i < io_OurFiles.size(); i++) {
            BlobData ourBlob = io_OurFiles.get(i).m_File;

            if (savedJ > j) {
                j = savedJ;
            }
            while (j < io_TheirFiles.size()) {
                BlobData theirBlob = io_TheirFiles.get(j).m_File;
                if (!theirBlob.GetIsFolder() && !ourBlob.GetIsFolder()) {
                    if (!ourBlob.GetPath().equals(theirBlob.GetPath()) && isPath1AfterPath2(ourBlob.GetPath(), theirBlob.GetPath())) {//new File
                        updateWCInMergeWithoutConflict(io_TheirFiles.get(j));
                    } else if (ourBlob.GetPath().equals(theirBlob.GetPath())) {//updated file
                        Conflict conflict = new Conflict(io_OurFiles.get(i), io_TheirFiles.get(j), findBlobDataByHisPathInList(io_OurFiles.get(i).getFile().GetPath(), io_AncestorFiles));
                        io_ConflictsFilesList.add(conflict);
                        j++;
                        savedJ = j;
                        break;
                    }
                    if (!isPath1AfterPath2(ourBlob.GetPath(), theirBlob.GetPath())) {
                        updateWCInMergeWithoutConflict(io_OurFiles.get(i));
                        savedJ = j;
                        break;
                    }
                }
                j++;

            }
            savedI = i;
            if (savedJ == io_TheirFiles.size()) {
                break;
            }
        }

        for (int i = savedI + 1; i < io_OurFiles.size(); i++) {
            BlobData blob = io_OurFiles.get(i).m_File;
            //removed
            if (!blob.GetIsFolder()) {
                updateWCInMergeWithoutConflict(io_OurFiles.get(i));
            }
        }

        for (j = savedJ; j < io_TheirFiles.size(); j++) {
            BlobData testBlob = io_TheirFiles.get(j).m_File;

            if (!testBlob.GetIsFolder()) {
                updateWCInMergeWithoutConflict(io_TheirFiles.get(j));
            }
        }

    }

    private void addUncommittedFolderToList(Folder i_Folder, List<UnCommittedChange> i_List, String i_UncommittedType) {
        List<BlobData> blobDataList = i_Folder.GetBlobList();
        for (BlobData blob : blobDataList) {
            UnCommittedChange unCommittedBlob = new UnCommittedChange(blob, i_UncommittedType);
            i_List.add(unCommittedBlob);
            if (blob.GetIsFolder()) {
                addUncommittedFolderToList(blob.GetCurrentFolder(), i_List, i_UncommittedType);
            }
        }

    }

    private void handleAddUncommittedUpdatedFile(BlobData i_TestBlob, BlobData i_Blob, List<UnCommittedChange> io_UnCommittedFilesList, String i_UncommittedType) {
        if (i_TestBlob.GetIsFolder()) {
            addUncommittedBlobsToListRecursively(i_Blob.GetCurrentFolder(), i_TestBlob.GetCurrentFolder(), io_UnCommittedFilesList);
        } else {
            UnCommittedChange unCommittedBlob = new UnCommittedChange(i_TestBlob, i_UncommittedType);
            io_UnCommittedFilesList.add(unCommittedBlob);
        }
    }

    private void handleUncommittedNewFile(BlobData i_TestBlob, List<UnCommittedChange> io_UnCommittedFilesList, String i_UncommittedType) {
        UnCommittedChange unCommittedBlob = new UnCommittedChange(i_TestBlob, i_UncommittedType);
        unCommittedBlob.SetContent(i_TestBlob.GetFileContent());
        io_UnCommittedFilesList.add(unCommittedBlob);
        if (i_TestBlob.GetIsFolder()) {
            addUncommittedFolderToList(i_TestBlob.GetCurrentFolder(), io_UnCommittedFilesList, i_UncommittedType);
        }
    }

    private boolean isPath1AfterPath2(String path1, String path2) {
        boolean retVal = false;
        if (path1.compareTo(path2) > 0) {
            retVal = true;
        }
        return retVal;
    }

    private RootFolder createFolderWithZipsOfUnCommittedFiles(RootFolder i_RootFolder, String i_CurrentUserName) throws IOException {
        FilesManagement.CreateFolder(m_MagitPath, c_TestFolderName);
        RootFolder testRootFolder = getInitializedRootFolder(i_CurrentUserName);
        Folder currentRootFolder = new Folder(i_RootFolder.GetBloBDataOfRootFolder().GetCurrentFolder().GetFolderSha1());
        List<BlobData> allFilesFromCurrentRootFolder = new LinkedList<>();

        if (i_RootFolder != null) {
            BlobData rootFolderBlobDataTemp = new BlobData(m_RepositoryPath,
                    m_RepositoryPath.toFile().toString(),
                    i_RootFolder.GetBloBDataOfRootFolder().GetLastChangedBY(),
                    i_RootFolder.GetBloBDataOfRootFolder().GetLastChangedTime(),
                    true,
                    i_RootFolder.GetSHA1(),
                    currentRootFolder);
            recoverRootFolder(rootFolderBlobDataTemp, allFilesFromCurrentRootFolder);
        }

        testRootFolder.UpdateCurrentRootFolderSha1(i_CurrentUserName, c_TestFolderName, allFilesFromCurrentRootFolder, false);
        return testRootFolder;
    }

    public boolean IsUncommittedFilesInRepository(RootFolder i__RootFolder, String i_CurrentUserName) throws IOException {
        RootFolder testRootFolder = createFolderWithZipsOfUnCommittedFiles(i__RootFolder, i_CurrentUserName);
        boolean isCommitNecessary = !(testRootFolder.GetSHA1().equals(m_RootFolder.GetSHA1()));
        clearDirectory((Paths.get(m_MagitPath.toString() + "\\" + c_TestFolderName).toFile()));
        return isCommitNecessary;
    }

    private Boolean handleSecondCommit(String i_CommitComment, Commit i_CommitToMerge) throws IOException {
        boolean isCommitNecessary = false;
        RootFolder testRootFolder = createFolderWithZipsOfUnCommittedFiles(m_RootFolder, m_CurrentUserName);

        if (!testRootFolder.GetSHA1().equals(m_RootFolder.GetSHA1())) {
            copyFiles(m_MagitPath + "\\" + c_TestFolderName, m_MagitPath + "\\" + c_ObjectsFolderName);
            m_RootFolder = testRootFolder;
            createNewCommit(i_CommitComment, i_CommitToMerge);
            isCommitNecessary = true;
        }

        clearDirectory((Paths.get(m_MagitPath.toString() + "\\" + c_TestFolderName).toFile()));

        return isCommitNecessary;
    }

    private void clearDirectory(File i_directory) {
        File[] fileList = i_directory.listFiles();

        for (File file : Objects.requireNonNull(fileList)) {
            file.delete();
        }

        i_directory.delete();
    }

    private void copyFiles(String i_From, String i_To) {

        Path source = Paths.get(i_From);
        Path destination = Paths.get(i_To);
        File[] fileList = source.toFile().listFiles();

        for (File file : Objects.requireNonNull(fileList)) {
            if (file.renameTo(new File(destination + "\\" + file.getName()))) {
                file.delete();
            }
        }
    }

    public HeadBranch GetHeadBranch() {
        return m_HeadBranch;
    }

    public List<Branch> GetAllBranchesList() {
        return m_AllBranchesList;
    }

    public List<String> GetAllBranchesStringList() {
        List<String> branchesList = new LinkedList<>();
        if (m_AllBranchesList != null) {
            String headBranchName = GetHeadBranch().GetBranch().GetBranchName();
            m_AllBranchesList.forEach(branch -> {

                String currentCommitSha1 = branch.GetCurrentCommit() != null ? branch.GetCurrentCommit().GetCurrentCommitSHA1() : "";
                String currentCommitComment = branch.GetCurrentCommit() != null ? branch.GetCurrentCommit().GetCommitComment() : "";
                branchesList.add(
                        "Branch name: " + branch.GetBranchName() + (headBranchName.equals(branch.GetBranchName()) ? " IS HEAD" : "") + "\n"
                                + "Commit SHA1: " + currentCommitSha1 + "\n"
                                + "Tracking After: " + (branch.GetTrackingAfter() == null ? "none" : branch.GetTrackingAfter()) + "\n"
                                + "Is Remote: " + branch.GetIsRemote().toString() + "\n"
                                + "Commit comment: " + currentCommitComment);
            });
        }
        return branchesList;
    }

    private Commit recoverCommit(String i_CommitSha1) {
        Commit commit = new Commit();
        recoverCommitRecursively(commit, i_CommitSha1);
        return commit;
    }

    public Commit FindCommitInAllBranches(String i_CommitSha1) {
        Commit commitToReturn = null;
        for (Branch branch : m_AllBranchesList) {
            commitToReturn = findCommitInBranchBySha1(branch.GetCurrentCommit(), i_CommitSha1);
            if (commitToReturn != null) {
                break;
            }
        }
        return commitToReturn;
    }

    private Commit findCommitInBranchBySha1(Commit i_CurrentCommit, String i_CommitSha1) {
        Commit commitToReturn = null;
        if (i_CurrentCommit.GetCurrentCommitSHA1().equals(i_CommitSha1)) {
            commitToReturn = i_CurrentCommit;
        } else {
            if (i_CurrentCommit.GetPrevCommitsList() != null) {
                for (Commit commit : i_CurrentCommit.GetPrevCommitsList()) {
                    commitToReturn = findCommitInBranchBySha1(commit, i_CommitSha1);
                    if (commitToReturn != null) {
                        break;
                    }
                }
            }
        }
        return commitToReturn;
    }

    private void recoverCommitRecursively(Commit i_CurrentCommit, String i_CurrentCommitSha1) {
        List<String> commitLines = FilesManagement.GetCommitData(i_CurrentCommitSha1, m_RepositoryPath.toString());
        String rootFolderSha1 = Objects.requireNonNull(commitLines).get(0);
        List<String> prevCommitsSha1List = FilesManagement.ConvertCommaSeparatedStringToList(commitLines.get(1));
        String commitComment = commitLines.get(2);
        String time = commitLines.get(3);
        String userName = commitLines.get(4);

        int parentIndex = 0;
        List<Commit> prevCommitsList = null;
        if (!prevCommitsSha1List.get(0).equals("")) {
            prevCommitsList = new LinkedList<>();
            for (String prevCommitSha1 : prevCommitsSha1List) {
                Commit commit = new Commit();
                prevCommitsList.add(commit);
                recoverCommitRecursively(prevCommitsList.get(parentIndex), prevCommitSha1);
                parentIndex++;
            }
        }
        Folder currentRootFolder = new Folder(rootFolderSha1);
        BlobData rootFolderBlobData = new BlobData(m_RepositoryPath, m_RepositoryPath.toFile().toString(), userName, time, true, rootFolderSha1, currentRootFolder);
        recoverRootFolder(rootFolderBlobData, null);
        RootFolder rootFolder = new RootFolder(rootFolderBlobData, m_RepositoryPath);
        i_CurrentCommit.UpdateCommit(rootFolder, commitComment, userName, prevCommitsList, i_CurrentCommitSha1, time);
    }

    private void recoverRootFolder(BlobData i_Root, List<BlobData> i_FilesList) {
        List<String> lines = FilesManagement.GetDataFilesList(m_RepositoryPath.toString(), i_Root.GetSHA1());
        List<String> fileDataList;

        if (lines != null) {
            for (String fileData : lines) {
                if (!fileData.equals("")) {
                    fileDataList = FilesManagement.ConvertCommaSeparatedStringToList(fileData);
                    BlobData blob;
                    if (fileDataList.get(1).equals("file")) {
                        blob = new BlobData(m_RepositoryPath, i_Root.GetPath() + "\\" + fileDataList.get(0), fileDataList.get(3), fileDataList.get(4), false, fileDataList.get(2), null);
                        i_Root.GetCurrentFolder().AddBlobToList(blob);
                    } else {
                        Folder currentRootFolder = new Folder(fileDataList.get(2));
                        blob = new BlobData(m_RepositoryPath, i_Root.GetPath() + "\\" + fileDataList.get(0), fileDataList.get(3), fileDataList.get(4), true, fileDataList.get(2), currentRootFolder);
                        i_Root.GetCurrentFolder().AddBlobToList(blob);
                        recoverRootFolder(blob, i_FilesList);
                    }
                    if (i_FilesList != null) {
                        i_FilesList.add(blob);
                    }
                }
            }
        }
    }

    private void recoverRepositoryFromFiles() throws IOException {
        m_RemoteReference = FilesManagement.RecoverRemoteReferenceFromFile(m_RepositoryPath);
        List<String> branchesList = FilesManagement.GetBranchesList(m_RepositoryPath.toString());
        String headBranchContent = FilesManagement.GetHeadBranchSha1(m_RepositoryPath.toString());
        String BranchDataOfHeadBranch = FilesManagement.GetFileNameInZipFromObjects(headBranchContent, m_RepositoryPath.toString());
        if (branchesList != null) {
            for (String listNode : branchesList) {
                List<String> data = FilesManagement.ConvertCommaSeparatedStringToList(listNode);
                String nameBranch = data.get(0);
                String currentCommitSha1 = data.get(1);

                Branch branch;
                Commit commit = recoverCommit(currentCommitSha1);
                String branchSha1 = DigestUtils.sha1Hex(nameBranch);//Configure Branch Sha1
                String headSha1 = FilenameUtils.removeExtension(FilesManagement.FindFileByNameInZipFileInPath("HEAD.txt", Paths.get(m_RepositoryPath.toString() + "\\" + c_GitFolderName + "\\" + c_ObjectsFolderName)).getName());
                String trackingAfter = FilesManagement.GetRemoteBranchFileNameByTrackingBranchName(nameBranch, m_RepositoryPath);
                branch = new Branch(nameBranch, commit, m_RepositoryPath, false, branchSha1, isBranchRemote(nameBranch), trackingAfter);
                removeBranchFromBranchesListByName(nameBranch);
                m_AllBranchesList.add(branch);
                if (BranchDataOfHeadBranch.equals(nameBranch)) {
                    m_HeadBranch = new HeadBranch(branch, m_RepositoryPath, false, headSha1);
                    m_RootFolder = m_HeadBranch.GetHeadBranch().GetCurrentCommit().GetCommitRootFolder();
                    m_CurrentCommit = commit;
                }
            }
        }
    }

    private boolean isBranchRemote(String i_BranchName) {
        return i_BranchName.contains("\\") || i_BranchName.contains("/");
    }

    public List<Commit> GetSortedAccessibleCommitList() {
        List<Commit> commitList = getCommitListFromBranches();
        List<Commit> sortedCommitList = commitList.stream()
                .sorted(Comparator.comparing(Commit::GetCreationDateInMilliseconds).reversed())
                .collect(Collectors.toList());

        List<Commit> uniqueCommitsList = sortedCommitList.stream()
                .filter(distinctByKey(Commit::GetCurrentCommitSHA1))
                .collect(Collectors.toList());

        return uniqueCommitsList;
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    private List<Commit> getCommitListFromBranches() {
        List<Commit> commitList = new LinkedList<>();

        for (Branch branch : m_AllBranchesList) {
            buildBranchCommitList(branch.GetCurrentCommit(), commitList);
        }

        return commitList;
    }

    private void buildBranchCommitList(Commit i_CurrentCommit, List<Commit> io_CommitList) {
        io_CommitList.add(i_CurrentCommit);
        if (i_CurrentCommit.GetPrevCommitsList() != null) {
            for (Commit commit : i_CurrentCommit.GetPrevCommitsList()) {
                buildBranchCommitList(commit, io_CommitList);
            }
        }
    }

    public List<Commit> GetAccessibleCommitsFromBranch(Commit i_CurrentCommit) {
        List<Commit> commitList = new LinkedList<>();
        buildBranchCommitList(i_CurrentCommit, commitList);
        return commitList;
    }

    public void SortBranchesList() {
        List<Branch> localBranches = new LinkedList<>();
        List<Branch> trackingBranches = new LinkedList<>();
        List<Branch> remoteBranches = new LinkedList<>();
        List<Branch> sortedBranches = new LinkedList<>();
        for (Branch branch : m_AllBranchesList) {
            if (m_HeadBranch.GetHeadBranch().GetBranchSha1().equals(branch.GetBranchSha1())) {
                sortedBranches.add(branch);
            } else if (branch.GetTrackingAfter() != null) {
                trackingBranches.add(branch);
            } else if (branch.GetIsRemote()) {
                remoteBranches.add(branch);
            } else {
                localBranches.add(branch);
            }
        }

        sortedBranches.addAll(trackingBranches);
        sortedBranches.addAll(localBranches);
        sortedBranches.addAll(remoteBranches);

        m_AllBranchesList = sortedBranches;
    }

    public Integer GetBranchNumber(Branch i_Branch) {
        Integer index = i_Branch == null ? m_AllBranchesList.size() : m_AllBranchesList.indexOf(i_Branch);
        return index;
    }

    public List<Branch> GetPointingBranchestoCommit(Commit i_Commit) {
        List<Branch> branchList = new LinkedList<>();
        for (Branch branch : m_AllBranchesList) {
            if (branch.GetCurrentCommit().GetCurrentCommitSHA1().equals(i_Commit.GetCurrentCommitSHA1())) {
                branchList.add(branch);
            }
        }

        return branchList;
    }

    public List<String> GetPointingBranchesNamestoCommit(Commit i_Commit) {
        List<Branch> branchList = GetPointingBranchestoCommit(i_Commit);
        List<String> namesList = new LinkedList<>();
        branchList.forEach(branch -> namesList.add(branch.GetBranchName()));
        return namesList;
    }

    public Branch GetBranchByCommit(Commit i_Commit) {
        Branch foundBranch = null;
        for (Branch branch : m_AllBranchesList) {
            //if (!branch.GetIsRemote()) {
            if (isCommitInBranch(i_Commit, branch)) {
                foundBranch = branch;
                break;
                //}
            }
        }

        return foundBranch;
    }

    private boolean isCommitInBranch(Commit i_Commit, Branch i_Branch) {
        boolean result = false;
        Commit currentCommit = i_Branch.GetCurrentCommit();
        while (currentCommit != null && !isOutOfBranch(currentCommit, i_Branch)) {
            if (currentCommit.equals(i_Commit)) {
                result = true;
                break;
            }

            currentCommit = currentCommit.GetPrevCommitsList() == null ? null : currentCommit.GetPrevCommitsList().get(0);
        }

        return result;
    }


    public Branch GetPreviousRemoteBranch(Branch i_LocalBranch){
        Commit currentCommit = i_LocalBranch.GetCurrentCommit();
        Branch previousBranch = null;
        while (currentCommit != null ) {
            previousBranch = GetDifferentBranchByCommit(currentCommit, i_LocalBranch);
            if(previousBranch!=null && previousBranch.GetIsRemote()){
                break;
            }else {
                currentCommit = currentCommit.GetPrevCommitsList() == null ? null : currentCommit.GetPrevCommitsList().get(0);
            }
        }

        return previousBranch;
    }

    private Branch GetDifferentBranchByCommit(Commit i_Commit, Branch i_Branch) {
        Branch result = null;

        for (Branch currentBranch : m_AllBranchesList) {
            if (!currentBranch.GetBranchSha1().equals(i_Branch.GetBranchSha1())) {
                if (currentBranch.GetCurrentCommit().equals(i_Commit) && !currentBranch.GetCurrentCommit().equals(i_Branch.GetCurrentCommit())) {
                    result = currentBranch;
                    break;
                }
            }
        }

        return result;
    }

    public List<Commit> GetCommitColumnByBranch(Branch i_Branch) {
        List<Commit> commitColumn = new LinkedList<>();
        Commit currentCommit = i_Branch.GetCurrentCommit();
        while (currentCommit != null && !isOutOfBranch(currentCommit, i_Branch)) {
            commitColumn.add(currentCommit);
            currentCommit = currentCommit.GetPrevCommitsList() == null ? null : currentCommit.GetPrevCommitsList().get(0);
        }

        return commitColumn;
    }

    private boolean isOutOfBranch(Commit i_Commit, Branch i_Branch) {
        boolean result = false;

        for (Branch currentBranch : m_AllBranchesList) {
            if (!currentBranch.GetBranchSha1().equals(i_Branch.GetBranchSha1())) {
                if (currentBranch.GetCurrentCommit().equals(i_Commit) && !currentBranch.GetCurrentCommit().equals(i_Branch.GetCurrentCommit())) {
                    result = true;
                    break;
                }
            }
        }

        return result;
    }



    public List<Commit> GetHeadBranchCommitHistory(Branch i_Branch) {
        List<Commit> commitsList = new LinkedList<>();
        Commit currentCommit =i_Branch.GetCurrentCommit();
        setHeadBranchCommitHistoryRec(commitsList, currentCommit);
        return commitsList;
    }

    private void setHeadBranchCommitHistoryRec(List<Commit> i_CommitsList, Commit i_CurrentCommit) {
        i_CommitsList.add(i_CurrentCommit);
        if (i_CurrentCommit.GetPrevCommitsList()!= null&&i_CurrentCommit.GetPrevCommitsList().get(0) != null) {
            setHeadBranchCommitHistoryRec(i_CommitsList,i_CurrentCommit.GetPrevCommitsList().get(0));
        }
    }

    public List<Branch> GetRemoteBranchesListByCommit(String i_CommitSha1) {
        List<Branch> branchList = new LinkedList<>();
        for (Branch branch : m_AllBranchesList) {
            if (branch.GetIsRemote()) {
                if (branch.GetCurrentCommit().GetCurrentCommitSHA1().equals(i_CommitSha1)) {
                    branchList.add(branch);
                }
            }
        }

        return branchList;
    }

    public List<Commit> GetNewerCommitsInBranch(Commit i_OldCommit, Branch i_Branch) {
        List<Commit> commitsColumn = GetCommitColumnByBranch(i_Branch);
        commitsColumn = commitsColumn.stream()
                .sorted(Comparator.comparing(Commit::GetCreationDateInMilliseconds).reversed())
                .collect(Collectors.toList());
        List<Commit> filteredList = commitsColumn.stream()
                .filter(commit -> commit.GetCreationDateInMilliseconds() > i_OldCommit.GetCreationDateInMilliseconds())
                .collect(Collectors.toList());

        return filteredList;
    }

    public Commit GetLastCommit() {
        List<Branch> branchesList = GetAllBranchesList();
        List<Commit> commitsList = new LinkedList<>();
        for (Branch branch : branchesList) {
            commitsList.add(branch.GetCurrentCommit());
        }
        commitsList.sort(Comparator.comparing(Commit::GetCreationDateInMilliseconds).reversed());
        return commitsList.get(0);
    }
///////////////////////////////////////////////////////
//    public List<String> GetHeadBranchCommitHistory() {
//        List<String> commitStringList = new LinkedList<>();
//        Commit currentCommit = m_HeadBranch.getBranch().getCurrentCommit();
//        setHeadBranchCommitHistoryRec(commitStringList, currentCommit);
//        return commitStringList;
//    }
//
//    private void setHeadBranchCommitHistoryRec(List<String> i_CommitStringList, Commit i_CurrentCommit) {
//        i_CommitStringList.add(i_CurrentCommit.toString());
//        if (i_CurrentCommit.getPrevCommit() != null) {
//            setHeadBranchCommitHistoryRec(i_CommitStringList, i_CurrentCommit.getPrevCommit());
//        }
//    }
//////////////////////////////////////////////////////////
}
