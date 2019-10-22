package engine.logic;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;


public class FilesManagement {
    private final static String s_ObjectsFolderDirectoryString = "\\.magit\\objects\\";
    private final static String s_BranchesFolderDirectoryString = "\\.magit\\branches\\";
    private final static String s_TrackingFolderName = "tracking";
    private final static String s_GitDirectory = "\\.magit\\";
    private final static String s_XmlBuildFolderName = "XML Build";
    private final static String s_RemoteReferenceFileName = "Remote_Reference";

    public static boolean IsRepositoryExistInPath(String i_Path) {
        return Paths.get(i_Path + "\\.magit").toFile().exists();
    }

    public static void CleanWC(Path i_PathToClean) {
        File[] listFiles;

        if (i_PathToClean.toFile() != null) {
            listFiles = i_PathToClean.toFile().listFiles(pathname -> (!pathname.getAbsolutePath().contains(".magit")));
            Arrays.stream(Objects.requireNonNull(listFiles)).forEach(file -> {
                if (file.isDirectory()) {
                    try {
                        FileUtils.cleanDirectory(file);
                    } catch (IOException e) {
                        System.out.println("clear wc failed");
                    }
                }
                file.delete();
            });
        }
    }

    public static void CreateNewFile(String i_Path, String i_Content) {
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(i_Path), "utf-8"))) {
            writer.write(i_Content);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void RemoveFileByPath(Path i_PathToRemove) {
        i_PathToRemove.toFile().delete();
    }

    public static void CreateFolder(Path i_Path, String i_Name) {
        Path newDirectoryPath = Paths.get(i_Path.toString() + "/" + i_Name);
        File directory = new File(newDirectoryPath.toString());

        if (!directory.exists()) {
            directory.mkdir();
        }
    }

    public static String UpdateBranchFile(Branch i_Branch, Commit i_Commit, Path i_RepositoryPath) {
        RemoveFileByPath(Paths.get(i_RepositoryPath.toString() + s_BranchesFolderDirectoryString + i_Branch.GetBranchName() + ".txt"));
        RemoveFileByPath(Paths.get(i_RepositoryPath.toString() + s_ObjectsFolderDirectoryString + i_Branch.GetBranchSha1() + ".zip"));
        return CreateBranchFile(i_Branch.GetBranchName(), i_Commit, i_RepositoryPath);
    }

    public static String UpdateHeadFile(HeadBranch i_HeadBranch, Branch i_Branch, Path i_RepositoryPath) {
        RemoveFileByPath(Paths.get(i_RepositoryPath.toString() + s_BranchesFolderDirectoryString + "HEAD.txt"));
        RemoveFileByPath(Paths.get(i_RepositoryPath.toString() + s_ObjectsFolderDirectoryString + i_HeadBranch.GetHeadBranchSha1() + ".zip"));
        return CreateHeadFile(i_Branch, i_RepositoryPath);
    }

    public static String CreateBranchFile(String i_BranchName, Commit i_Commit, Path i_RepositoryPath) {
        FileWriter outputFile;
        boolean isRemoteBranch = handleRemoteBranchName(i_RepositoryPath, i_BranchName);
        Path branchPath = Paths.get(i_RepositoryPath.toString() + s_BranchesFolderDirectoryString + i_BranchName + ".txt");
        BufferedWriter bf = null;
        String sha1 = "";
        try {
            outputFile = new FileWriter(branchPath.toString());
            bf = new BufferedWriter(outputFile);
            if (i_Commit != null) {
                bf.write(i_Commit.GetCurrentCommitSHA1());

            }
            sha1 = DigestUtils.sha1Hex(i_BranchName);
        } catch (IOException ex) {
            System.out.println("create branch failed");
        } finally {
            try {
                bf.close();
            } catch (IOException e) {
                System.out.println("Action failed");
            }
        }

        createZipFileIntoObjectsFolder(i_RepositoryPath, branchPath, sha1, "");

        return sha1;
    }

    public static void HandleTrackingFolder(String i_BranchName, Boolean i_IsRemote, String i_TrackingAfter, Path i_RepositoryPath) {

        Path directory = createTrackingFolder(i_RepositoryPath);
        if (i_TrackingAfter != null && !i_TrackingAfter.isEmpty()) {
            HandleTrackingFileOfTrackingBranch(i_BranchName, i_TrackingAfter, directory);
        } else if (i_IsRemote) {
            handleTrackingFileOfRemoteBranch(i_BranchName, directory);
        }
    }

    public static Path RecoverRemoteReferenceFromFile(Path i_RepositoryPath) {
        File remoteReferenceFile = Paths.get(i_RepositoryPath.toString() + s_GitDirectory + s_RemoteReferenceFileName + ".txt").toFile();
        Path returnPath = remoteReferenceFile.exists()
                && !(ReadTextFileContent(remoteReferenceFile.getAbsolutePath()).isEmpty()) ?
                Paths.get(ReadContentWithoutNewLines(remoteReferenceFile.getAbsolutePath())) :
                null;
        return returnPath;
    }

    public static void CreateRemoteReferenceFile(Path m_RemoteReference, Path i_RepositoryPath) throws IOException {
        if (m_RemoteReference != null && !m_RemoteReference.toString().isEmpty()) {
            Path referenceFilePath = Paths.get(i_RepositoryPath + s_GitDirectory + s_RemoteReferenceFileName + ".txt");
            AppendToTextFile(referenceFilePath, m_RemoteReference != null ? m_RemoteReference.toString() : "");
        }
    }

    private static void handleTrackingFileOfRemoteBranch(String i_BranchName, Path i_Directory) {
        String remoteBranchName = Paths.get(i_BranchName).toFile().getName();
        File emptyTrackingFile = new File(i_Directory.toString() + "\\" + remoteBranchName + ".txt");
        if (!emptyTrackingFile.exists()) {
            try {
                emptyTrackingFile.createNewFile(); // if file already exists will do nothing
            } catch (IOException e) {
                System.out.println(e.toString());
            }
        }
    }

    public static void HandleTrackingFileOfTrackingBranch(String i_BranchName, String i_TrackingAfter, Path i_Directory) {
        String remoteBranchName = Paths.get(i_TrackingAfter).toFile().getName();
        try {
            AppendToTextFile(Paths.get(i_Directory.toString() + "\\" + remoteBranchName + ".txt"), i_BranchName);
        } catch (IOException ex) {
            System.out.println("write to tracking file failed");
        }
    }

    private static Path createTrackingFolder(Path i_RepositoryPath) {
        Path directory = Paths.get(i_RepositoryPath.toString() + s_GitDirectory);
        CreateFolder(directory, s_TrackingFolderName);
        return Paths.get(i_RepositoryPath.toString() + s_GitDirectory + s_TrackingFolderName);
    }

    public static void AppendToTextFile(Path i_FilePath, String i_TextToAppend) throws IOException {
        FileWriter fileWriter = new FileWriter(i_FilePath.toString(), true); //Set true for append mode
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printWriter.println(i_TextToAppend);
        printWriter.close();
    }

    private static boolean handleRemoteBranchName(Path i_RepositoryPath, String i_BranchName) {
        boolean isRemoteBranch = false;
        if (i_BranchName.contains("\\") || i_BranchName.contains("/")) {
            String parent = Paths.get(i_BranchName).toFile().getParent();
            //String segments[] = i_BranchName.split("\\s*//\\s*");
            CreateFolder(Paths.get(i_RepositoryPath.toString() + s_BranchesFolderDirectoryString), parent);
            isRemoteBranch = true;
        }

        return isRemoteBranch;
    }

    public static String CreateHeadFile(Branch i_HeadBranch, Path i_RepositoryPath) {
        FileWriter outputFile;
        BufferedWriter bf = null;
        Path headPath = Paths.get(i_RepositoryPath.toString() + s_BranchesFolderDirectoryString + "HEAD.txt");
        String sha1 = "";
        try {
            outputFile = new FileWriter(headPath.toString());
            bf = new BufferedWriter(outputFile);
            bf.write(i_HeadBranch.GetBranchSha1());
            sha1 = DigestUtils.sha1Hex(i_HeadBranch.GetBranchSha1());
        } catch (IOException ex) {
            System.out.println("Action failed");

        } finally {
            try {
                bf.close();
            } catch (IOException e) {
                System.out.println("Action failed");
            }
        }

        createZipFileIntoObjectsFolder(i_RepositoryPath, headPath, sha1, "");
        return sha1;
    }

    public static String ConvertLongToSimpleDateTime(long i_Time) {
        Date date = new Date(i_Time);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy-HH:mm:ss:sss");
        String dateText = dateFormat.format(date);

        return dateText;
    }

    public static BlobData CreateSimpleFileDescription(Path i_RepositoryPath, Path i_FilePathOrigin, String i_UserName, String i_DateCreated, String i_TestFolderName, List<BlobData> io_AllFilesFromCurrentRootFolder, boolean isGeneratedFromXml) {
        return createTemporaryFileDescription(i_RepositoryPath, i_FilePathOrigin, i_UserName, i_DateCreated, i_TestFolderName, io_AllFilesFromCurrentRootFolder, isGeneratedFromXml);
    }

    private static BlobData findBlobByPathInRootFolder(List<BlobData> io_AllFilesFromCurrentRootFolder, Path i_Path) {
        BlobData blobToReturn = null;
        if (io_AllFilesFromCurrentRootFolder != null) {
            for (BlobData blob : io_AllFilesFromCurrentRootFolder) {
                if (blob.GetPath().equals(i_Path.toString())) {
                    blobToReturn = blob;
                    break;
                }
            }
        }
        return blobToReturn;
    }

    private static BlobData createTemporaryFileDescription(Path i_RepositoryPath, Path i_FilePath, String i_UserName, String i_DateCreated, String i_TestFolderName, List<BlobData> io_AllFilesFromCurrentRootFolder, boolean isGeneretedFromXml) {
        BufferedWriter bf = null;
        File file = i_FilePath.toFile();
        String fileDescriptionFilePathString = i_RepositoryPath.toString() + s_ObjectsFolderDirectoryString + file.getName(); //+ ".txt";
        String descriptionStringForGenerateSha1;
        String description;
        FileWriter outputFile;
        String sha1 = "";
        String type = file.isFile() ? "file" : "folder";
        BlobData simpleBlob = null;

        try {
            outputFile = new FileWriter(fileDescriptionFilePathString);
            bf = new BufferedWriter(outputFile);
            description = ReadTextFileContent(i_FilePath.toString());
            descriptionStringForGenerateSha1 = String.format("%s,%s,%s", file.getAbsolutePath(), type, description);
            bf.write(String.format("%s", description));
            sha1 = DigestUtils.sha1Hex(descriptionStringForGenerateSha1);
            File testFile = Paths.get(i_RepositoryPath.toString() + s_ObjectsFolderDirectoryString + "\\" + sha1 + ".zip").toFile();
            BlobData blobDataOfTestFile = findBlobByPathInRootFolder(io_AllFilesFromCurrentRootFolder, Paths.get(file.getAbsolutePath()));
            if (!isGeneretedFromXml && (blobDataOfTestFile != null) && testFile.exists() && io_AllFilesFromCurrentRootFolder != null && !file.getAbsolutePath().equals(i_RepositoryPath.toString())) {
                simpleBlob = new BlobData(i_RepositoryPath, file.getAbsolutePath(), blobDataOfTestFile.GetLastChangedBY(), blobDataOfTestFile.GetLastChangedTime(), false, sha1, null);
            } else if (isGeneretedFromXml) {
                simpleBlob = new BlobData(i_RepositoryPath, file.getAbsolutePath(), i_UserName, i_DateCreated, false, sha1, null);
            } else {
                simpleBlob = new BlobData(i_RepositoryPath, file.getAbsolutePath(), i_UserName, getUpdateDate(i_DateCreated, file), false, sha1, null);
            }
        } catch (IOException e) {
            System.out.println("Action failed");

        } finally {
            try {
                bf.close();
            } catch (IOException e) {
                System.out.println("Action failed");
            }
        }

        createZipFileIntoObjectsFolder(i_RepositoryPath, Paths.get(fileDescriptionFilePathString), sha1, i_TestFolderName);
        Paths.get(fileDescriptionFilePathString).toFile().delete();
        return simpleBlob;
    }

    private static String getUpdateDate(String i_Date, File i_File) {
        String result = i_Date;
        if (i_Date == null || i_Date.equals("")) {
            result = ConvertLongToSimpleDateTime(i_File.lastModified());
        }

        return result;
    }

    public static void ExtractZipFileToPath(Path i_ZipFilePath, Path i_DestinationPath) {
        ZipInputStream zis = null;
        String fileZip = i_ZipFilePath.toString();
        File destDir = new File(i_DestinationPath.toString());
        if (!destDir.exists()) {
            try {
                Files.createDirectories(i_DestinationPath);
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
        byte[] buffer = new byte[1024];
        try {
            zis = new ZipInputStream(new FileInputStream(fileZip));
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {

                File newFile = newFile(destDir, zipEntry);
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();

        } catch (IOException ex) {
            System.out.println("Action failed");

        } finally {
            try {
                zis.close();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public static List<String> ConvertCommaSeparatedStringToList(String i_CommaSeparatedStr) {
        String[] commaSeparatedArr = i_CommaSeparatedStr.split("\\s*,\\s*");
        List<String> result = Arrays.stream(commaSeparatedArr).collect(Collectors.toList());
        return result;
    }

    public static List<String> ConvertNewLineSeparatedStringToList(String i_CommaSeparatedStr) {
        String[] commaSeparatedArr = i_CommaSeparatedStr.split("\\s*\n\\s*");
        List<String> result = Arrays.stream(commaSeparatedArr).collect(Collectors.toList());
        return result;
    }

    private static String getRootFolderSha1ByCommitFile(String i_CommitSha1, String i_RepositoryPath) {
        return ReadZipIntoString(i_RepositoryPath + "\\.magit\\objects\\" + i_CommitSha1 + ".zip").get(0);
    }

    public static List<String> ReadZipIntoString(String i_ZipPath) {
        ZipFile zipFile = null;
        InputStream stream = null;
        List<String> lines = null;
        try {
            zipFile = new ZipFile(i_ZipPath);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            stream = zipFile.getInputStream(entries.nextElement());
            lines = IOUtils.readLines(stream, "utf-8");
        } catch (IOException e) {
            System.out.println("Action failed");
        } finally {
            try {
                zipFile.close();
                stream.close();
            } catch (IOException ex) {
                System.out.println("Action failed");
            }
        }
        return lines;
    }

    private static File newFile(File i_DestinationDir, ZipEntry i_ZipEntry) throws IOException {
        File destFile = new File(i_DestinationDir, i_ZipEntry.getName());
        String destDirPath = i_DestinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + i_ZipEntry.getName());
        }

        return destFile;
    }

    private static void createZipFileIntoObjectsFolder(Path i_RepositoryPath, Path i_FilePath, String i_Sha1, String i_TestFolderName) {
        ZipOutputStream zos = null;
        FileOutputStream fos;
        try {
            File file = i_FilePath.toFile();
            String zipFileName = i_Sha1.concat(".zip");
            fos = new FileOutputStream(i_RepositoryPath.toString() + getZipSaveFolderName(i_TestFolderName) + "\\" + zipFileName);
            zos = new ZipOutputStream(fos);
            zos.putNextEntry(new ZipEntry(file.getName()));
            byte[] bytes = Files.readAllBytes(i_FilePath);
            zos.write(bytes, 0, bytes.length);
            zos.closeEntry();

        } catch (FileNotFoundException ex) {
            System.err.format("createZipFile: The file %s does not exist", i_FilePath.toString());
        } catch (IOException ex) {
            System.err.println("createZipFile: I/O error: " + ex);
        } finally {
            try {
                zos.close();
            } catch (IOException e) {
                System.out.println("Action failed");
            }
        }
    }

    private static String getZipSaveFolderName(String i_TestFolderName) {
        return !i_TestFolderName.equals("") ? s_GitDirectory + i_TestFolderName : s_ObjectsFolderDirectoryString;
    }

    public static String CreateCommitDescriptionFile(Commit i_Commit, Path i_RepositoryPath, Boolean i_IsGeneratedFromXML) {//clean thin function!!!!!!
        BufferedWriter bf = null;
        String commitDescriptionFilePathString = i_RepositoryPath.toString() + s_ObjectsFolderDirectoryString + i_Commit.GetCommitComment() + ".txt";
        FileWriter outputFile;
        String commitInformationString = "";
        String commitDataForGenerateSha1 = "";
        String sha1String = "";
        String fileCreationDateString;

        try {
            outputFile = new FileWriter(commitDescriptionFilePathString);
            bf = new BufferedWriter(outputFile);
            fileCreationDateString = i_IsGeneratedFromXML ? i_Commit.GetCreationDate() : getFileCreationDateByPath(Paths.get(commitDescriptionFilePathString));
            i_Commit.SetCreationDate(fileCreationDateString);
            commitDataForGenerateSha1 = commitDataForGenerateSha1.concat(i_Commit.GetRootSHA1() + "," + i_Commit.GetCommitComment());

            commitInformationString = commitInformationString.concat(
                    i_Commit.GetRootSHA1() + '\n' +
                            i_Commit.GetPreviousCommitsSHA1String() + '\n' +
                            i_Commit.GetCommitComment() + '\n' +
                            i_Commit.GetCreationDate() + '\n' +
                            i_Commit.GetCreatedBy());

            sha1String = DigestUtils.sha1Hex(commitDataForGenerateSha1);
            bf.write(commitInformationString);

        } catch (IOException e) {
            System.out.println("Action failed");
        } finally {
            try {
                bf.close();
            } catch (IOException e) {
                System.out.println("Action failed");
            }
        }
        createZipFileIntoObjectsFolder(i_RepositoryPath, Paths.get(commitDescriptionFilePathString), sha1String, "");
        Paths.get(commitDescriptionFilePathString).toFile().delete();
        return sha1String;
    }

    private static String getFileCreationDateByPath(Path i_Path) throws IOException {
        return ConvertLongToSimpleDateTime(Files.readAttributes(i_Path, BasicFileAttributes.class).creationTime().toMillis());
    }

    private static Boolean isFileOrDirectoryEmpty(File i_File) {
        return !i_File.isDirectory() && IsFileEmpty(i_File) || i_File.isDirectory() && IsDirectoryEmpty(i_File);
    }

    public static Boolean IsFileEmpty(File i_File) {
        boolean isEmpty = false;
        if (!i_File.isDirectory()) {
            if (i_File.length() == 0) {
                isEmpty = true;
            }
        }
        return isEmpty;
    }

    public static Boolean IsDirectoryEmpty(File i_File) {
        boolean isEmpty = false;
        if (i_File.isDirectory()) {
            if (Objects.requireNonNull(i_File.list()).length == 0) {
                isEmpty = true;
            }
        }
        return isEmpty;
    }

    private static BlobData getBlobByFile(Folder i_CurrentFolder, File i_CurrentFileInFolder) {
        List<BlobData> blobList = i_CurrentFolder.GetBlobList();
        BlobData resultBlob = null;
        for (BlobData blob : blobList) {
            if (blob.GetPath().equals(i_CurrentFileInFolder.toString())) {
                resultBlob = blob;
                break;
            }
        }

        return resultBlob;
    }

    private static String getCurrentBasicData(File i_CurrentFileInFolder, BlobData i_CurrentFolderBlob) {
        String sha1String = getBlobByFile(i_CurrentFolderBlob.GetCurrentFolder(), i_CurrentFileInFolder).GetSHA1();
        String type = i_CurrentFileInFolder.isFile() ? "file" : "folder";
        String basicDataString = String.format(
                "%s,%s,%s",
                i_CurrentFileInFolder.getName(),
                type,
                sha1String
        );

        return basicDataString;
    }

    private static String getFolderDescriptionFilePathStaring(Path i_FolderPath) {
        return i_FolderPath.toString() + "\\" + i_FolderPath.toFile().getName() + ".txt";
    }

    private static String getLastUpdateTime(Boolean i_IsUpdateTimeNotNeeded, BlobData i_CurrentBlob, File i_File) {
        String lastUpdateTime;
        if (i_IsUpdateTimeNotNeeded) {
            lastUpdateTime = i_CurrentBlob.GetLastChangedTime();
        } else {
            lastUpdateTime = ConvertLongToSimpleDateTime(i_File.lastModified());
        }
        return lastUpdateTime;
    }

    private static Boolean isFileExistByHisSha1(String i_Sha1, String i_RepositoryPath) {
        File testFile = Paths.get(i_RepositoryPath + s_ObjectsFolderDirectoryString + "\\" + i_Sha1 + ".zip").toFile();
        return testFile.exists();
    }

    private static String getStringForFolderSHA1(BlobData i_BlobDataOfCurrentFolder, Path i_FolderPath, String i_UserName, String i_FolderDescriptionFilePathString, boolean i_IsGeneratedFromXml, List<BlobData> i_AllFilesFromCurrentRootFolder) {
        File currentFolderFile = i_FolderPath.toFile();
        String stringForSha1 = "";
        String basicDataString;
        String fullDataString = "";
        String sha1 = "";
        FileWriter outputFile;
        BufferedWriter bf = null;
        String lastUpdateTime;
        Path repositoryPath = i_BlobDataOfCurrentFolder.GetRepositoryPath();
        try {
            outputFile = new FileWriter(i_FolderDescriptionFilePathString);
            bf = new BufferedWriter(outputFile);

            for (File file : Objects.requireNonNull(currentFolderFile.listFiles())) {
                if (isFileValidForScanning(file, i_FolderDescriptionFilePathString, i_FolderPath)) {
                    BlobData currentBlob = getBlobByFile(i_BlobDataOfCurrentFolder.GetCurrentFolder(), file);
                    basicDataString = getCurrentBasicData(file, i_BlobDataOfCurrentFolder);
                    stringForSha1 = stringForSha1.concat(basicDataString);
                    Boolean isFileExist = isFileExistByHisSha1(currentBlob.GetSHA1(), repositoryPath.toString());
                    Boolean isUpdateTimeNotNeeded = isFileExist || i_IsGeneratedFromXml;
                    lastUpdateTime = getLastUpdateTime(isUpdateTimeNotNeeded, currentBlob, file);
                    String lastChangedBy = currentBlob.GetLastChangedBY().isEmpty() ? i_UserName : currentBlob.GetLastChangedBY();//****
                    fullDataString = fullDataString.concat(basicDataString + "," + lastChangedBy + "," + lastUpdateTime + '\n');
                }
            }

            sha1 = DigestUtils.sha1Hex(stringForSha1);
            setUnchangedFolderDetailed(i_BlobDataOfCurrentFolder, repositoryPath, i_FolderPath, sha1, i_AllFilesFromCurrentRootFolder);
            bf.write('\n' + fullDataString);

        } catch (IOException e) {
            System.out.println("Action failed");
        } finally {
            try {
                bf.close();
            } catch (IOException e) {
                System.out.println("Action failed");
            }
        }
        return sha1;
    }

    private static void setUnchangedFolderDetailed(BlobData i_BlobDataOfCurrentFolder, Path i_RepositoryPath, Path i_FolderPath, String i_Sha1, List<BlobData> io_AllFilesFromCurrentRootFolder) {
        File testFile = Paths.get(i_RepositoryPath.toString() + s_ObjectsFolderDirectoryString + "\\" + i_Sha1 + ".zip").toFile();
        if (testFile.exists() && io_AllFilesFromCurrentRootFolder != null && !i_FolderPath.toFile().getAbsolutePath().equals(i_RepositoryPath.toString())) {
            BlobData blobDataOfTestFile = findBlobByPathInRootFolder(io_AllFilesFromCurrentRootFolder, Paths.get(i_BlobDataOfCurrentFolder.GetPath()));
            if (blobDataOfTestFile != null) {
                i_BlobDataOfCurrentFolder.SetLastChangedTime(blobDataOfTestFile.GetLastChangedTime());
                i_BlobDataOfCurrentFolder.SetLastChangedBY(blobDataOfTestFile.GetLastChangedBY());
            }
        }
    }

    public static String CreateFolderDescriptionFile(BlobData i_BlobDataOfCurrentFolder, Path i_RepositoryPath, Path i_FolderPath, String i_UserName, String i_TestFolderName, boolean i_IisGeneratedFromXml, List<BlobData> i_AllFilesFromCurrentRootFolder) {
        String folderDescriptionFilePathString = getFolderDescriptionFilePathStaring(i_FolderPath);
        String sha1 = getStringForFolderSHA1(i_BlobDataOfCurrentFolder, i_FolderPath, i_UserName, folderDescriptionFilePathString, i_IisGeneratedFromXml, i_AllFilesFromCurrentRootFolder);
        createZipFileIntoObjectsFolder(i_RepositoryPath, Paths.get(folderDescriptionFilePathString), sha1, i_TestFolderName);
        Paths.get(folderDescriptionFilePathString).toFile().delete();

        return sha1;
    }

    private static boolean isFileValidForScanning(File i_File, String i_FolderDescriptionFilePathString, Path i_FolderPath) {
        return (!isFileOrDirectoryEmpty(i_File) && !i_File.toString().equals(i_FolderPath.toString() + "\\.magit")
                && (!(i_File.toString()).equals(i_FolderDescriptionFilePathString)));
    }

    public static String ReadTextFileContent(String i_FilePath) {
        String returnValue = "";
        try {
            returnValue = FileUtils.readFileToString(Paths.get(i_FilePath).toFile(), "utf-8");
        } catch (IOException e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }
        return returnValue;
    }

    public static String ReadContentWithoutNewLines(String i_FilePath) {
        String text = ReadTextFileContent(i_FilePath);
        text = text.replace("\n", "").replace("\r", "");
        return text;
    }

    public static List<String> GetCommitData(String i_CommitSha1, String i_RepositoryPath) {
        List<String> lines = ReadZipIntoString(i_RepositoryPath + "\\.magit\\objects\\" + i_CommitSha1 + ".zip");
        //lines.remove(1);
        if (lines.size() == 1 && lines.get(0).equals(""))
            return null;
        return lines;
    }

    public static String GetHeadBranchSha1(String i_RepositoryPath) {
        Path path = Paths.get(i_RepositoryPath + "\\.magit\\branches\\HEAD.txt");
        String sha1 = "";

        try {
            sha1 = FileUtils.readFileToString(path.toFile(), "utf-8");
        } catch (IOException e) {
            System.out.println("Action failed");
        }
        return sha1;
    }

    public static List<String> GetBranchesList(String i_RepositoryPath) {

        File branchesFolder = Paths.get(i_RepositoryPath + "\\.magit\\branches").toFile();
        List<String> branchesList = new LinkedList<>();
        File remoteBranchesFolder = null;

        for (File file : Objects.requireNonNull(branchesFolder.listFiles())) {
            if (!file.getName().equals("HEAD.txt") && !file.isDirectory()) {
                branchesList.add(FilenameUtils.removeExtension(file.getName()) + ',' + ReadTextFileContent(file.getPath()));
            }

            if (file.isDirectory()) {
                remoteBranchesFolder = file;
            }
        }

        if (remoteBranchesFolder != null) {
            String remoteName = remoteBranchesFolder.getName();
            for (File file : Objects.requireNonNull(remoteBranchesFolder.listFiles())) {
                branchesList.add(remoteName + "\\" + FilenameUtils.removeExtension(file.getName()) + ',' + ReadTextFileContent(file.getPath()));
            }
        }

        return branchesList;
    }

    public static String GetFileNameInZipFromObjects(String i_CommitSha1, String i_RepositoryPath) {
        return FilenameUtils.removeExtension(getFileNameInZip(i_RepositoryPath + "\\.magit\\objects\\" + i_CommitSha1 + ".zip"));
    }

    public static String GetRemoteBranchFileNameByTrackingBranchName(String i_TrackingBranchName, Path i_RepositoryPath) throws IOException {
        File trackingDirectory = Paths.get(i_RepositoryPath.toString() + s_GitDirectory + s_TrackingFolderName).toFile();
        String remoteBranchName = null;
        for (File file : Objects.requireNonNull(trackingDirectory.listFiles())) {
            List<String> lines = IOUtils.readLines(new StringReader(ReadTextFileContent(file.getAbsolutePath())));
            for (String line : lines) {
                if (line.equals(i_TrackingBranchName)) {
                    remoteBranchName = getRemoteName(i_RepositoryPath) + "\\" + FilenameUtils.removeExtension(file.getName());
                    break;
                }
            }

            if (remoteBranchName != null) {
                break;
            }
        }

        return remoteBranchName;
    }

    private static String getRemoteName(Path i_RepositoryPath) {
        File directory = Paths.get(i_RepositoryPath.toString() + s_BranchesFolderDirectoryString).toFile();
        String foundFileName = null;
        for (File file : Objects.requireNonNull(directory.listFiles())) {
            if (file.isDirectory()) {
                foundFileName = file.getName();
                break;
            }
        }

        return foundFileName;
    }


    private static String getFileNameInZip(String i_Path) {
        String fileName = "";

        try (ZipFile zipFile = new ZipFile(i_Path)) {
            Enumeration zipEntries = zipFile.entries();
            fileName = ((ZipEntry) zipEntries.nextElement()).getName();
        } catch (IOException e) {
            System.out.println("Action failed in method:GetFileNameInZip class: FileManagement line: 535 with path:" + i_Path);
        }

        return fileName;
    }

    public static List<String> GetDataFilesList(String i_RepositoryPath, String i_RootSha1) {
        List<String> lines = ReadZipIntoString(i_RepositoryPath + "\\.magit\\objects\\" + i_RootSha1 + ".zip");

        if (lines.size() == 1 && lines.get(0).equals(""))
            return null;
        return lines;
    }

    public static List<String> GetDataFilesListOfZipByPath(String i_Path) {
        List<String> lines = ReadZipIntoString(i_Path);
        if (lines.size() == 1 && lines.get(0).equals(""))
            return null;
        return lines;
    }

    public static File FindFileByNameInZipFileInPath(String i_NameFile, Path i_Path) {
        File fileToReturn = null;
        List<File> files = Arrays.asList(Objects.requireNonNull(i_Path.toFile().listFiles()));
        files.sort(Comparator.comparingLong(File::lastModified));
        Collections.reverse(files);
        for (File zipFile : files) {
            if (FilenameUtils.getExtension(zipFile.getName()).equals("zip")) {
                if (getFileNameInZip(zipFile.getAbsolutePath()).equals(i_NameFile)) {
                    fileToReturn = zipFile;
                    break;
                }
            }
        }
        return fileToReturn;
    }

    public static void CopyAllTXTFiles(Path i_SourcePath, Path i_DestinationPath) {
        for (File file : Objects.requireNonNull(i_SourcePath.toFile().listFiles())) {
            if (!file.isDirectory()) {
                CopyTXTFile(Paths.get(file.getAbsolutePath()), i_DestinationPath);
            }
        }
    }

    public static void CopyTXTFile(Path i_SourceFilePath, Path i_DestinationPath) {
        try {
            FileUtils.copyFileToDirectory(i_SourceFilePath.toFile(), i_DestinationPath.toFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void DeleteFolder(String destination){
        try {
            FileUtils.deleteDirectory(new File(destination));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}


