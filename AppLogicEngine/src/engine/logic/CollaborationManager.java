package engine.logic;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class CollaborationManager {

    public static String Push(Path i_RemotePath, RepositoryManager i_LocalManager) throws IOException {
        String errorDescription = null;
        RepositoryManager remoteManager = new RepositoryManager(i_RemotePath, "", false, false, null);
        Path localPath = i_LocalManager.GetRepositoryPath();

        if (!remoteManager.IsUncommittedFilesInRepository(remoteManager.getRootFolder(), remoteManager.GetCurrentUserName())) {
            FilesManagement.CleanWC(remoteManager.GetRepositoryPath());
            Branch rb = getRBIfIsHeadRTB(i_LocalManager);
            if (rb != null) {
                String branchNameInRR = Paths.get(rb.GetBranchName()).toFile().getName();
                Branch rbBranchInRR = remoteManager.FindBranchByName(branchNameInRR);
                if (areCommitsAdjacent(rbBranchInRR.GetCurrentCommit(), rb.GetCurrentCommit())) {
                    rb.UpdateBranchCommit(i_LocalManager.GetHeadBranch().GetBranch().GetCurrentCommit());
                    fetchRemoteBranch(rbBranchInRR, remoteManager, i_LocalManager);
                } else {
                    errorDescription = "the branch in RR isnt synchronized with current RB";
                }
            } else {
                errorDescription = "Current head Branch isnt RTB";
            }

            remoteManager.HandleCheckout(remoteManager.GetHeadBranch().GetBranch().GetBranchName());
        } else {
            errorDescription = "Uncommitted files in remote directory";
        }


        return errorDescription;
    }

    public static void PushLocalBranch(Path i_RemotePath, RepositoryManager i_LocalManager) throws IOException {
        Branch localBranch = i_LocalManager.GetHeadBranch().GetBranch();
        Branch previousRemoteBranchInLR = i_LocalManager.GetPreviousRemoteBranch(localBranch);
        RepositoryManager remoteManager = new RepositoryManager(i_RemotePath, "", false, false, null);

        int indexOfSlash = previousRemoteBranchInLR.GetBranchName().lastIndexOf("\\");
        String previousRemoteBranchInRRName = previousRemoteBranchInLR.GetBranchName().substring(indexOfSlash + 1);

        Branch previousRemoteBranchInRR = remoteManager.FindBranchByName(previousRemoteBranchInRRName);
        Branch remoteBranchInRR = new Branch(localBranch.GetBranchName(), previousRemoteBranchInRR.GetCurrentCommit(), i_RemotePath, true,  null, false, null);
        String branchName = i_RemotePath.toFile().getName() + "\\" + remoteBranchInRR.GetBranchName();
        Branch localRemoteBranch = new Branch(branchName, previousRemoteBranchInLR.GetCurrentCommit(), i_LocalManager.GetRepositoryPath(), true, null, true, null);
        localBranch.SetTrackingAfter(localRemoteBranch.GetBranchName());
        i_LocalManager = new RepositoryManager(i_LocalManager.GetRepositoryPath(), i_LocalManager.GetCurrentUserName(), false, false, i_LocalManager.GetRemoteReference());
        Push(i_RemotePath, i_LocalManager);
    }

   public static void HandlePullRequest(){

   }

    public static String Pull(Path i_RemotePath, RepositoryManager i_LocalManager) throws IOException {
        Boolean isPushRequired = false;
        String errorDescription = null;
        RepositoryManager remoteManager = new RepositoryManager(i_RemotePath, "", false, false, null);
        Branch rb = getRBIfIsHeadRTB(i_LocalManager);
        Branch rtb = null;
        FilesManagement.CleanWC(i_LocalManager.GetRepositoryPath());
        if (rb != null) {
            rtb = i_LocalManager.GetHeadBranch().GetBranch();
            isPushRequired = isPushRequired(rb, rtb);
            if (!isPushRequired) {
                //Branch i_RemoteBranchInLR, RepositoryManager i_LocalRepository, RepositoryManager i_RemoteRepository
                fetchRemoteBranch(rb, i_LocalManager, remoteManager);
                i_LocalManager.HandleFFMerge(rb.GetBranchName());
            } else {
                errorDescription = "You must push the branch, before this action.";
            }
        } else {
            errorDescription = "Current head Branch is doesnt RTB";
        }

        i_LocalManager.HandleCheckout(i_LocalManager.GetHeadBranch().GetBranch().GetBranchName());

        return errorDescription;
    }

    private static Boolean isPushRequired(Branch i_RB, Branch i_RTB) {
        return !areCommitsAdjacent(i_RTB.GetCurrentCommit(), i_RB.GetCurrentCommit());
    }

    private static Branch getRBIfIsHeadRTB(RepositoryManager i_LocalManager) {
        Branch rbBranch = null;
        String rbName = i_LocalManager.GetHeadBranch().GetHeadBranch().GetTrackingAfter();
        if (rbName != null) {
            rbBranch = i_LocalManager.FindBranchByName(rbName);
        }
        return rbBranch;
    }

    public static void Fetch(Path i_RemotePath, RepositoryManager i_LocalManager) throws IOException {
        RepositoryManager remoteManager = new RepositoryManager(i_RemotePath, "", false, false, null);
        FilesManagement.CleanWC(i_LocalManager.GetRepositoryPath());
        Branch oldHeadBranch = i_LocalManager.GetHeadBranch().GetHeadBranch();
        List<Commit> remoteCommitList = remoteManager.GetSortedAccessibleCommitList();
        List<Commit> clonedCommits = cloneCommits(remoteCommitList, i_LocalManager.GetRepositoryPath());
        cloneBranches(remoteManager, clonedCommits, remoteManager.GetRepositoryPath(), i_LocalManager.GetRepositoryPath(), false);
        i_LocalManager.HandleCheckout(oldHeadBranch.GetBranchName());
        FilesManagement.CleanWC(i_LocalManager.GetRepositoryPath());
    }

    private static void fetchRemoteBranch(Branch i_RemoteBranchInLR, RepositoryManager i_LocalRepository, RepositoryManager i_RemoteRepository) throws FileNotFoundException, UnsupportedEncodingException {
        String remoteBranchNameInRR = Paths.get(i_RemoteBranchInLR.GetBranchName()).toFile().getName();
        Branch remoteBranchInRR = i_RemoteRepository.FindBranchByName(remoteBranchNameInRR);
        if (!areCommitsAdjacent(i_RemoteBranchInLR.GetCurrentCommit(), remoteBranchInRR.GetCurrentCommit())) {
            List<Commit> newerCommits = i_RemoteRepository.GetNewerCommitsInBranch(i_RemoteBranchInLR.GetCurrentCommit(), remoteBranchInRR);
            newerCommits.get(newerCommits.size() - 1).SetPrevCommitsList(null);
            List<Commit> clonedCommits = cloneCommits(newerCommits, i_LocalRepository.GetRepositoryPath());
            linkToLocalCommit(clonedCommits.get(0), i_RemoteBranchInLR.GetCurrentCommit(), i_LocalRepository.GetRepositoryPath());
            updateRB(i_RemoteBranchInLR, clonedCommits.get(clonedCommits.size() - 1), i_LocalRepository.GetRepositoryPath());
        }
    }

    private static void updateRB(Branch i_RemoteBranch, Commit i_Commit, Path i_RepositoryPath) {
        i_RemoteBranch.SetCurrentCommit(i_Commit);
        FilesManagement.CreateBranchFile(i_RemoteBranch.GetBranchName(), i_RemoteBranch.GetCurrentCommit(), i_RepositoryPath);
    }

    private static void linkToLocalCommit(Commit i_NewCommit, Commit i_PrevCommit, Path i_RepositoryPath) {
        List<Commit> prevCommits = new LinkedList<>();
        prevCommits.add(i_PrevCommit);
        i_NewCommit.SetPrevCommitsList(prevCommits);
        FilesManagement.CreateCommitDescriptionFile(i_NewCommit, i_RepositoryPath, true);
    }


    private static Boolean areCommitsAdjacent(Commit i_LocalCommit, Commit i_RemoteCommit) {
        return i_LocalCommit.GetCommitComment()
                .equals(i_RemoteCommit.GetCommitComment())
                && i_LocalCommit.GetCreationDate()
                .equals(i_RemoteCommit.GetCreationDate())
                && i_LocalCommit.GetCreatedBy()
                .equals(i_RemoteCommit.GetCreatedBy());
    }

    public static RepositoryManager CloneRepository(Path i_RemotePath, Path i_LocalPath, String i_CurrentUserName) throws IOException {
        RepositoryManager remoteRepositoryManager = new RepositoryManager(i_RemotePath, "Administrator", false, false, null);
        new RepositoryManager(i_LocalPath, "Administrator", true, true, null);
        handleClone(remoteRepositoryManager, i_RemotePath, i_LocalPath);
        RepositoryManager localRepository = new RepositoryManager(i_LocalPath, i_CurrentUserName, false, false, i_RemotePath);
        localRepository.HandleCheckout(localRepository.GetHeadBranch().GetBranch().GetBranchName());

        return localRepository;
    }

    private static void handleClone(RepositoryManager i_RepositoryManager, Path i_FromPath, Path i_LocalPath) throws IOException {
        List<Commit> remoteCommitList = i_RepositoryManager.GetSortedAccessibleCommitList();
        List<Commit> clonedCommits = cloneCommits(remoteCommitList, i_LocalPath);
        cloneBranches(i_RepositoryManager, clonedCommits, i_FromPath, i_LocalPath, true);
        FilesManagement.CreateRemoteReferenceFile(i_FromPath, i_LocalPath);
    }

    private static void cloneBranches(RepositoryManager i_RepositoryManager, List<Commit> i_ClonedCommitsList, Path i_FromPath, Path i_TargetPath, Boolean i_IsHandleHeadBranch) {
        List<Branch> branchesList = i_RepositoryManager.GetAllBranchesList();
        List<Commit> remoteCommitsList = i_RepositoryManager.GetSortedAccessibleCommitList();
        Collections.reverse(remoteCommitsList);

        for (Branch remoteBranch : branchesList) {
            Integer commitIndex = getCommitIndexByBranch(remoteCommitsList, remoteBranch);
            Commit clonedCommit = i_ClonedCommitsList.get(commitIndex);

            String branchName = i_FromPath.toFile().getName() + "\\" + remoteBranch.GetBranchName();
            Branch clonedBranch = new Branch(branchName, clonedCommit, i_TargetPath, true, null, true, null);

            if (i_IsHandleHeadBranch) {
                if (remoteBranch.equals(i_RepositoryManager.GetHeadBranch().GetBranch())) {
                    HeadBranch headBranch = new HeadBranch(clonedBranch, i_TargetPath, true, null);
                    Branch trackingBranch = new Branch(remoteBranch.GetBranchName(), clonedCommit, i_TargetPath, true, null, false, clonedBranch.GetBranchName());
                }
            }
        }
    }

    private static Integer getCommitIndexByBranch(List<Commit> i_CommitList, Branch branch) {
        int i = 0;
        for (i = 0; i < i_CommitList.size(); i++) {
            if (branch.GetCurrentCommit().GetCurrentCommitSHA1().equals(i_CommitList.get(i).GetCurrentCommitSHA1())) {
                break;
            }
        }

        return i;
    }

    private static List<Commit> cloneCommits(List<Commit> remoteCommitList, Path i_LocalPath) throws FileNotFoundException, UnsupportedEncodingException {
        HashMap<Integer, List<Integer>> commitMap = getCommitMap(remoteCommitList);
        List<Commit> clonedCommits = new LinkedList<>();

        for (Commit remoteCommit : remoteCommitList) {
            RootFolder clonedRootFolder = cloneRootFolder(remoteCommit.GetCommitRootFolder(), i_LocalPath);
            Commit clonedCommit = new Commit(clonedRootFolder, remoteCommit.GetCommitComment(), remoteCommit.GetCreatedBy(), null, null, remoteCommit.GetCreationDate());
            clonedCommits.add(clonedCommit);
            FilesManagement.CleanWC(i_LocalPath);
        }

        connectClonedCommits(clonedCommits, commitMap);
        createCommitObjects(clonedCommits, i_LocalPath);

        return clonedCommits;
    }

    private static void createCommitObjects(List<Commit> i_CommitList, Path i_TargetRootPath) {
        Collections.reverse(i_CommitList);
        i_CommitList.forEach(commit -> {
            String sha1 = FilesManagement.CreateCommitDescriptionFile(commit, i_TargetRootPath, true);
            commit.SetCurrentCommitSHA1(sha1);
        });
    }

    private static void connectClonedCommits(List<Commit> io_ClonedCommits, HashMap<Integer, List<Integer>> i_CommitMap) {
        for (int i = 0; i < io_ClonedCommits.size(); i++) {
            List<Integer> prevCommitIndexList = i_CommitMap.get(i);
            List<Commit> prevCommitsList = new LinkedList<>();
            for (Integer index : prevCommitIndexList) {
                prevCommitsList.add(io_ClonedCommits.get(index));
            }

            prevCommitsList = prevCommitsList.isEmpty() ? null : prevCommitsList;
            io_ClonedCommits.get(i).SetPrevCommitsList(prevCommitsList);
        }
    }

    private static HashMap<Integer, List<Integer>> getCommitMap(List<Commit> i_SortedCommitList) {
        HashMap<Integer, List<Integer>> commitMap = new HashMap<>();

        for (int i = 0; i < i_SortedCommitList.size(); i++) {
            Commit currentCommit = i_SortedCommitList.get(i);
            List<Commit> prevCommits = currentCommit.GetPrevCommitsList();
            List<Integer> prevCommitIndexList = new LinkedList<>();

            if (prevCommits != null) {
                for (Commit prevCommit : prevCommits) {
                    Integer index = i_SortedCommitList.indexOf(prevCommit);//!!!!!!!!!
                    if (index != -1) {
                        prevCommitIndexList.add(index);
                    }
                }
            }

            commitMap.put(i, prevCommitIndexList);

        }

        return commitMap;
    }

    private static RootFolder cloneRootFolder(RootFolder i_RootFolder, Path i_TargetPath) throws FileNotFoundException, UnsupportedEncodingException {
        BlobData clonedFolder = cloneFolder(i_RootFolder.GetBloBDataOfRootFolder(), true, i_TargetPath, i_TargetPath);
        return new RootFolder(clonedFolder, i_TargetPath);
    }

    private static BlobData cloneFolder(BlobData i_RemoteFolder, Boolean i_IsRootFolder, Path i_TargetRootPath, Path i_TargetPath) throws FileNotFoundException, UnsupportedEncodingException {
        String folderName = "";
        if (!i_IsRootFolder) {
            folderName = Paths.get(i_RemoteFolder.GetPath()).toFile().getName();
            FilesManagement.CreateFolder(i_TargetPath, folderName);
        }

        Folder folder = new Folder();
        Path folderPath = Paths.get(i_TargetPath + "\\" + folderName);
        BlobData clonedFolder = new BlobData(i_TargetRootPath, folderPath.toString(), i_RemoteFolder.GetLastChangedBY(), i_RemoteFolder.GetLastChangedTime(), true, "", folder);
        List<BlobData> containedItems = i_RemoteFolder.GetCurrentFolder().GetBlobList();
        for (BlobData blob : containedItems) {
            if (blob.GetIsFolder()) {
                BlobData containedFolder = cloneFolder(blob, false, i_TargetPath, Paths.get(clonedFolder.GetPath()));
                clonedFolder.GetCurrentFolder().AddBlobToList(containedFolder);
            } else {
                BlobData containedElement = cloneSimpleBlob(blob, i_TargetRootPath, Paths.get(clonedFolder.GetPath()));
                clonedFolder.GetCurrentFolder().AddBlobToList(containedElement);
            }
        }

        String sha1 = FilesManagement.CreateFolderDescriptionFile(clonedFolder, i_TargetRootPath, Paths.get(clonedFolder.GetPath()), clonedFolder.GetLastChangedBY(), "", true, null);
        clonedFolder.SetSHA1(sha1);

        return clonedFolder;
    }

    private static BlobData cloneSimpleBlob(BlobData i_Blob, Path i_TargetRootPath, Path i_TargetPath) throws FileNotFoundException, UnsupportedEncodingException {
        String fileName = Paths.get(i_Blob.GetPath()).toFile().getName();
        String lastUpdater = i_Blob.GetLastChangedBY();
        String lastUpdateDate = i_Blob.GetLastChangedTime();
        String content = i_Blob.GetFileContent();
        PrintWriter writer = new PrintWriter(i_TargetPath + "\\" + fileName, "UTF-8");
        writer.println(content);
        writer.close();

        BlobData blob = FilesManagement.CreateSimpleFileDescription(
                i_TargetRootPath,
                Paths.get(i_TargetPath + "\\" + fileName),
                lastUpdater, lastUpdateDate,
                "",
                null,
                true);
        return blob;
    }
}
