package engine.logic;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class XMLManager {
    private final static String s_MagitBlobs = "MagitBlob";
    private final static String s_MagitFolders = "MagitSingleFolder";
    private final static String s_MagitCommits = "MagitSingleCommit";
    private final static String s_MagitBranches = "MagitSingleBranch";


    public static List<String> GetXMLFileErrors(InputStream i_XMLFile) throws ParserConfigurationException, SAXException, IOException {
        List<String> result = new LinkedList<>();
        //if (i_XMLFile.exists()) {
        //if (FilenameUtils.getExtension(i_XMLFile.toString()).equals("xml")) {
            if (isXMLIDRepeating(i_XMLFile)) {
                result.add("Contains elements with same id");
            }
            result.addAll(findErrorsInXMLFolders(i_XMLFile));
            result.addAll(findErrorsInXMLCommit(i_XMLFile));
            result.addAll(findErrorsInXMLBranches(i_XMLFile));
            result.addAll(findErrorsInRemoteReference(i_XMLFile));
//        } else {
//            result.add("file not xml");
//        }
//        } else {
//            result.add("file doesn't exist");
//        }

        return result;
    }

    private static List<String> findErrorsInXMLBranches(InputStream i_XMLFile) throws IOException, SAXException, ParserConfigurationException {
        Document xml = getXMLDocument(i_XMLFile);
        NodeList branchesList = xml.getElementsByTagName(s_MagitBranches);
        String headBranchName = xml.getElementsByTagName("head").item(0).getTextContent();
        boolean isHeadValid = false;
        List<String> errorList = new LinkedList<>();

        for (int i = 0; i < branchesList.getLength(); i++) {
            Element currentBranch = (Element) branchesList.item(i);
            String currentBranchName = currentBranch.getElementsByTagName("name").item(0).getTextContent();
            Element pointedCommit = (Element) currentBranch.getElementsByTagName("pointed-commit").item(0);
            String pointedCommitID = pointedCommit.getAttribute("id");
            Element xmlCommit = GetXMLElementByID(xml, s_MagitCommits, pointedCommitID);
            if (xmlCommit == null) {
                errorList.add(String.format("Commit id = %s in Branch name = %s invalid", pointedCommitID, currentBranchName));
            }

            if (headBranchName.equals(currentBranchName)) {
                isHeadValid = true;
            }

            errorList.addAll(findTrackingErrorsInBranch(currentBranch, branchesList));
        }

        if (!isHeadValid) {
            errorList.add("Head branch invalid");
        }

        return errorList;
    }

    private static List<String> findTrackingErrorsInBranch(Element i_BranchElement, NodeList i_BranchList) {
        List<String> errorList = new LinkedList<>();
        String branchName = i_BranchElement.getElementsByTagName("name").item(0).getTextContent();
        String isBranchTracking = i_BranchElement.getAttribute("tracking");
        if (isBranchTracking.equals("true")) {
            String trackingAfter = i_BranchElement.getElementsByTagName("tracking-after") != null ? i_BranchElement.getElementsByTagName("tracking-after").item(0).getTextContent() : "";
            Element remoteBranch = getBranchByName(i_BranchList, trackingAfter);
            if (!remoteBranch.getAttribute("is-remote").equals("true")) {
                errorList.add(branchName + "is tracking a non remote branch");
            }
        }

        return errorList;
    }

    private static Element getBranchByName(NodeList i_BranchList, String i_BranchName) {
        Element currentBranch = null;
        for (int i = 0; i < i_BranchList.getLength(); i++) {
            currentBranch = (Element) i_BranchList.item(i);
            String currentBranchName = currentBranch.getElementsByTagName("name").item(0).getTextContent();
            if (currentBranchName.equals(i_BranchName)) {
                break;
            }
        }

        return currentBranch;
    }

    private static List<String> findErrorsInRemoteReference(InputStream i_XMLFile) throws IOException, SAXException, ParserConfigurationException {
        Document xml = getXMLDocument(i_XMLFile);
        NodeList remoteReferenceList = xml.getElementsByTagName("MagitRemoteReference");
        List<String> errorList = new LinkedList<>();

        for (int i = 0; i < remoteReferenceList.getLength(); i++) {
            Element currentRemoteReference = (Element) remoteReferenceList.item(i);

            if (currentRemoteReference.hasChildNodes()) {
                String locationString = currentRemoteReference.getElementsByTagName("location").item(0).getTextContent();
                if (!FilesManagement.IsRepositoryExistInPath(locationString)) {
                    errorList.add("No remote repository in " + locationString);
                }
            }
        }

        return errorList;
    }

    private static List<String> findErrorsInXMLCommit(InputStream i_XMLFile) throws IOException, SAXException, ParserConfigurationException {
        Document xml = getXMLDocument(i_XMLFile);
        NodeList commitsList = xml.getElementsByTagName(s_MagitCommits);
        List<String> errorList = new LinkedList<>();

        for (int i = 0; i < commitsList.getLength(); i++) {
            Element currentCommit = (Element) commitsList.item(i);
            String currentCommitID = currentCommit.getAttribute("id");

            Element containedRootFolder = (Element) currentCommit.getElementsByTagName("root-folder").item(0);
            String containedRootFolderID = containedRootFolder.getAttribute("id");
            Element xmlFolder = GetXMLElementByID(xml, s_MagitFolders, containedRootFolderID);

            if (xmlFolder == null) {
                errorList.add(String.format("Folder id = %s in Commit id = %s invalid", containedRootFolderID, currentCommitID));
            } else if (!xmlFolder.getAttribute("is-root").equals("true")) {
                errorList.add(String.format("Folder id = %s in Commit id = %s is not root", containedRootFolderID, currentCommitID));
            }
        }

        return errorList;
    }

    private static Element GetXMLElementByID(Document i_XMLDocument, String i_Type, String i_ElementID) {
        NodeList nodeList = i_XMLDocument.getElementsByTagName(i_Type);
        Element result = null;

        for (int i = 0; i < nodeList.getLength(); i++) {
            Element currentElement = (Element) nodeList.item(i);
            String currentElementID = currentElement.getAttribute("id");
            if (i_ElementID.equals((currentElementID))) {
                result = currentElement;
                break;
            }
        }

        return result;
    }

    private static List<String> findErrorsInXMLFolders(InputStream i_XMLFile) throws IOException, SAXException, ParserConfigurationException {
        Document xml = getXMLDocument(i_XMLFile);
        NodeList foldersList = xml.getElementsByTagName(s_MagitFolders);
        List<String> errorList = new LinkedList<>();

        for (int i = 0; i < foldersList.getLength(); i++) {
            Element currentFolder = (Element) foldersList.item(i);
            String currentFolderID = currentFolder.getAttribute("id");
            NodeList containedItems = currentFolder.getElementsByTagName("item");
            for (int j = 0; j < containedItems.getLength(); j++) {
                Element containedItem = (Element) containedItems.item(j);
                String itemID = containedItem.getAttribute("id");
                String itemType = containedItem.getAttribute("type");
                if (itemType.equals(("blob"))) {
                    if (!isXMLElementExist(xml, containedItem, s_MagitBlobs)) {
                        errorList.add(String.format("Blob id = %s in Folder id = %s invalid", itemID, currentFolderID));
                    }
                } else if (itemType.equals("folder")) {
                    if (itemID.equals(currentFolderID)) {
                        errorList.add(String.format("Folder id = %s points to itself", currentFolderID));
                    } else if (!isXMLElementExist(xml, containedItem, s_MagitFolders)) {
                        errorList.add(String.format("Folder id = %s in Folder id = %s invalid", itemID, currentFolderID));
                    }
                }
            }
        }

        return errorList;
    }

    private static Boolean isXMLElementExist(Document i_XMLDocument, Element i_Element, String i_Type) {
        NodeList nodeList = i_XMLDocument.getElementsByTagName(i_Type);
        String elementID = i_Element.getAttribute("id");
        boolean isExist = false;

        for (int i = 0; i < nodeList.getLength(); i++) {
            Element currentElement = (Element) nodeList.item(i);
            String currentElementID = currentElement.getAttribute("id");
            if (elementID.equals((currentElementID))) {
                isExist = true;
                break;
            }
        }

        return isExist;
    }

    private static Boolean isXMLIDRepeating(InputStream i_XMLFile) throws IOException, SAXException, ParserConfigurationException {
        Document xml = getXMLDocument(i_XMLFile);
        NodeList blobsList = xml.getElementsByTagName(s_MagitBlobs);
        NodeList foldersList = xml.getElementsByTagName(s_MagitFolders);
        NodeList commitsList = xml.getElementsByTagName(s_MagitCommits);

        return isSameIDInNodeList(blobsList) || isSameIDInNodeList(foldersList) || isSameIDInNodeList(commitsList);
    }

    private static Document getXMLDocument(InputStream i_XMLFile) throws ParserConfigurationException, IOException, SAXException {
       i_XMLFile.reset();
       // i_XMLFile.skip(1);
        // InputStream data = new BufferedInputStream(i_XMLFile);
// data.markSupported() should return "true" now
        //data.mark(0);
        //data.reset();
        DocumentBuilderFactory db = DocumentBuilderFactory.newInstance();
        DocumentBuilder build = db.newDocumentBuilder();
        Document xml = build.parse(i_XMLFile);

        return xml;
    }

    private static Boolean isSameIDInNodeList(NodeList io_Entries) {
        boolean isSameIDInXML = false;

        for (int i = 0; i < io_Entries.getLength() && !isSameIDInXML; i++) {
            Element element1 = (Element) io_Entries.item(i);
            String element1ID = element1.getAttribute("id");

            for (int j = 0; j < io_Entries.getLength(); j++) {
                Element element2 = (Element) io_Entries.item(j);
                String element2ID = element2.getAttribute("id");

                if (i != j && element1ID.equals(element2ID)) {
                    isSameIDInXML = true;
                    break;
                }
            }
        }

        return isSameIDInXML;
    }

    public static Boolean IsEmptyRepository(InputStream i_XMLFile) throws IOException, SAXException, ParserConfigurationException {
        boolean isEmpty = false;
        Document xmlDocument = getXMLDocument(i_XMLFile);
        NodeList branchesNodeList = xmlDocument.getElementsByTagName(s_MagitBranches);
        String headBranchName = xmlDocument.getElementsByTagName("head").item(0).getTextContent();

        for (int i = 0; i < branchesNodeList.getLength(); i++) {
            Element branchElement = (Element) branchesNodeList.item(i);
            String currentBranchName = branchElement.getElementsByTagName("name").item(0).getTextContent();
            if (currentBranchName.equals(headBranchName)) {
                Element pointedCommit = (Element) branchElement.getElementsByTagName("pointed-commit").item(0);
                String pointedCommitID = pointedCommit.getAttribute("id");
                isEmpty = pointedCommitID.isEmpty() || pointedCommitID == null || pointedCommitID.equals("");
                break;
            }
        }

        return isEmpty;
    }

    public static String GetRepositoryNameFromXml(InputStream i_XMLFile) throws IOException, SAXException, ParserConfigurationException {
        Document xmlDocument = getXMLDocument(i_XMLFile);
        String repositoryName = ((Element) xmlDocument.getElementsByTagName("MagitRepository").item(0)).getAttribute("name");
        return repositoryName;
    }

    public static Path GetRepositoryPathFromXML(InputStream i_XMLFile) throws IOException, SAXException, ParserConfigurationException {
        Document xmlDocument = getXMLDocument(i_XMLFile);
        String repositoryPathString = xmlDocument.getElementsByTagName("location").item(0).getTextContent();
        String repositoryName = ((Element) xmlDocument.getElementsByTagName("MagitRepository").item(0)).getAttribute("name");
        return Paths.get(repositoryPathString);
    }

    //--------------------------------------------------------------------------------------------------------------------------------------------
    public static void BuildRepositoryObjectsFromXML(InputStream i_XMLFile, Path i_RepositoryPath) throws IOException, SAXException, ParserConfigurationException {
        Document xmlDocument = getXMLDocument(i_XMLFile);
        HashMap<String, Commit> commitHashMap = buildCommitsFromXMLDocument(xmlDocument, i_RepositoryPath);
        buildBranchesFromXMLDocument(xmlDocument, i_RepositoryPath, commitHashMap);
        FilesManagement.CreateRemoteReferenceFile(getRemoteReferencePath(xmlDocument), i_RepositoryPath);
    }

    private static Path getRemoteReferencePath(Document i_XmlDocument) {
        String locationString = null;
        NodeList remoteReferenceList = i_XmlDocument.getElementsByTagName("MagitRemoteReference");
        if (remoteReferenceList != null && remoteReferenceList.getLength() != 0) {
            Element currentRemoteReference = (Element) remoteReferenceList.item(0);
            Node locationNode = currentRemoteReference.getElementsByTagName("location").item(0);
            locationString = locationNode == null ? null : locationNode.getTextContent();
        }

        return locationString == null || locationString.isEmpty() ? null : Paths.get(locationString);
    }


    private static void buildBranchesFromXMLDocument(Document i_XMLDocument, Path i_RootPath, HashMap<String, Commit> i_CommitHashMap) {
        NodeList branchesNodeList = i_XMLDocument.getElementsByTagName(s_MagitBranches);
        String headBranchName = i_XMLDocument.getElementsByTagName("head").item(0).getTextContent();
        for (int i = 0; i < branchesNodeList.getLength(); i++) {
            Element branchElement = (Element) branchesNodeList.item(i);
            Boolean isRemote = getIsRemote(branchElement);
            String trackingAfter = getTrackingAfter(branchElement);
            String currentBranchName = branchElement.getElementsByTagName("name").item(0).getTextContent();
            Element pointedCommit = (Element) branchElement.getElementsByTagName("pointed-commit").item(0);
            String pointedCommitID = pointedCommit.getAttribute("id");
            Commit commit = i_CommitHashMap.get(pointedCommitID);
            Branch branch = new Branch(currentBranchName, commit, i_RootPath, true, null, isRemote, trackingAfter);

            if (currentBranchName.equals(headBranchName)) {
                HeadBranch headBranch = new HeadBranch(branch, i_RootPath, true, null);
            }
        }
    }

    private static Boolean getIsRemote(Element i_BranchElement) {
        String isRemoteString = i_BranchElement.getAttribute("is-remote");
        Boolean isRemote = isRemoteString.isEmpty() || isRemoteString == null ? false : isRemoteString.equals("true");
        return isRemote;
    }

    private static String getTrackingAfter(Element i_BranchElement) {
        String trackingAfter = null;
        String isBranchTracking = i_BranchElement.getAttribute("tracking");
        if (!isBranchTracking.isEmpty() && isBranchTracking != null) {
            if (isBranchTracking.equals("true")) {
                trackingAfter = i_BranchElement.getElementsByTagName("tracking-after") != null ? i_BranchElement.getElementsByTagName("tracking-after").item(0).getTextContent() : "";
            }
        }

        return trackingAfter;
    }

    private static HashMap<String, Commit> buildCommitsFromXMLDocument(Document i_XMLDocument, Path i_RootPath) throws FileNotFoundException, UnsupportedEncodingException {
        HashMap<String, Commit> commitHashMap = new HashMap<>();
        NodeList commitsNodeList = i_XMLDocument.getElementsByTagName((s_MagitCommits));
        for (int i = 0; i < commitsNodeList.getLength(); i++) {
            Element commitElement = (Element) commitsNodeList.item(i);
            String id = commitElement.getAttribute("id");
            String message = commitElement.getElementsByTagName("message").item(0).getTextContent();
            String author = commitElement.getElementsByTagName("author").item(0).getTextContent();
            String dateOfCreation = commitElement.getElementsByTagName("date-of-creation").item(0).getTextContent();
            Element rootFolderElement = (Element) commitElement.getElementsByTagName("root-folder").item(0);
            RootFolder rootFolder = buildRootFolderFromElement(rootFolderElement, i_XMLDocument, i_RootPath);

            Commit commit = new Commit(rootFolder, message, author, null, null, dateOfCreation);

            commitHashMap.put(id, commit);
            FilesManagement.CleanWC(i_RootPath);
        }

        HashMap<String, List<String>> commitIDChainMap = setCommitChains(commitHashMap, commitsNodeList);
        createCommitObjects(commitHashMap, commitIDChainMap, i_RootPath);

        return commitHashMap;
    }

    private static HashMap<String, List<String>> setCommitChains(HashMap<String, Commit> i_CommitHashMap, NodeList i_CommitsNodeList) {
        HashMap<String, List<String>> commitChainMap = new HashMap<>();
        for (int i = 0; i < i_CommitsNodeList.getLength(); i++) {
            Element currentCommitElement = (Element) i_CommitsNodeList.item(i);
            String currentCommitID = currentCommitElement.getAttribute("id");
            List<String> prevCommitIdsList = getListOfPreviousCommitIDs(currentCommitElement);
            if (!prevCommitIdsList.isEmpty()) {
                List<Commit> prevCommitsList = new LinkedList<>();
                prevCommitIdsList.forEach(commitId -> prevCommitsList.add(i_CommitHashMap.get(commitId)));
                i_CommitHashMap.get(currentCommitID).SetPrevCommitsList(prevCommitsList); //set prev commit
            } else {
                prevCommitIdsList = null;
            }

            commitChainMap.put(currentCommitID, prevCommitIdsList);
        }

        return commitChainMap;
    }

    private static List<String> getListOfPreviousCommitIDs(Element i_CommitElement) {
        List<String> listOfPrevCommitIDs = new LinkedList<>();
        NodeList precedingCommitsNodeList = i_CommitElement.getElementsByTagName("preceding-commit");

        for (int i = 0; i < precedingCommitsNodeList.getLength(); i++) {
            Element prevCommitElement = (Element) precedingCommitsNodeList.item(i);

            if (prevCommitElement != null) {
                String prevCommitID = prevCommitElement.getAttribute("id");
                listOfPrevCommitIDs.add(prevCommitID);
            }
        }

        return listOfPrevCommitIDs;
    }

    private static void createCommitObjects(HashMap<String, Commit> i_CommitHashMap, HashMap<String, List<String>> i_CommitIDChainMap, Path i_RootPath) {
        for (String id : i_CommitHashMap.keySet()) {// set head commit
            if (i_CommitHashMap.get(id).GetPrevCommitsList() == null) {
                String sha1 = FilesManagement.CreateCommitDescriptionFile(i_CommitHashMap.get(id), i_RootPath, true);
                i_CommitHashMap.get(id).SetCurrentCommitSHA1(sha1);
                break;
            }
        }

        for (String id : i_CommitHashMap.keySet()) {
            createCommitObjectsUntilHead(i_CommitHashMap, i_CommitIDChainMap, id, i_RootPath);
        }

    }

    private static void createCommitObjectsUntilHead(HashMap<String, Commit> i_CommitHashMap, HashMap<String, List<String>> i_CommitIDChainMap, String i_CurrentID, Path i_RootPath) {
        if (i_CommitHashMap.get(i_CurrentID).GetCurrentCommitSHA1() == null) {
            List<String> previousCommitIDsList = i_CommitIDChainMap.get(i_CurrentID);
            previousCommitIDsList.forEach(previousID -> createCommitObjectsUntilHead(i_CommitHashMap, i_CommitIDChainMap, previousID, i_RootPath));
        }

        String sha1 = FilesManagement.CreateCommitDescriptionFile(i_CommitHashMap.get(i_CurrentID), i_RootPath, true);
        i_CommitHashMap.get(i_CurrentID).SetCurrentCommitSHA1(sha1);
    }

    private static RootFolder buildRootFolderFromElement(Element i_RootFolderElement, Document i_XMLDocument, Path i_RootPath) throws FileNotFoundException, UnsupportedEncodingException {
        String containedRootFolderID = i_RootFolderElement.getAttribute("id");
        Element folderElement = GetXMLElementByID(i_XMLDocument, s_MagitFolders, containedRootFolderID);
        BlobData blobDataFolder = buildFolderFromElement(folderElement, i_RootPath, i_RootPath, i_XMLDocument);

        return new RootFolder(blobDataFolder, i_RootPath);
    }

    private static BlobData buildFolderFromElement(Element i_FolderElement, Path i_RootPath, Path i_Path, Document i_XMLDocument) throws FileNotFoundException, UnsupportedEncodingException {
        String lastUpdater = i_FolderElement.getElementsByTagName("last-updater").item(0).getTextContent();
        String lastUpdateDate = i_FolderElement.getElementsByTagName("last-update-date").item(0).getTextContent();
        String isRootFolder = i_FolderElement.getAttribute("is-root");
        String folderName = "";
        if (!isRootFolder.equals("true")) {
            folderName = i_FolderElement.getElementsByTagName("name").item(0).getTextContent();
            FilesManagement.CreateFolder(i_Path, folderName);
        }

        Folder folder = new Folder();
        Path folderPath = Paths.get(i_Path + "\\" + folderName);
        BlobData folderData = new BlobData(i_RootPath, folderPath.toString(), lastUpdater, lastUpdateDate, true, "", folder);
        NodeList containedItems = i_FolderElement.getElementsByTagName("item");
        for (int i = 0; i < containedItems.getLength(); i++) {
            Element currentItem = (Element) containedItems.item(i);
            String itemID = currentItem.getAttribute("id");
            String itemType = currentItem.getAttribute("type");
            if (itemType.equals("blob")) {
                Element blobElement = GetXMLElementByID(i_XMLDocument, s_MagitBlobs, itemID);
                folderData.GetCurrentFolder().AddBlobToList(buildBlobFromElement(blobElement, i_RootPath, folderPath));
            } else if (itemType.equals("folder")) {
                Element folderElement = GetXMLElementByID(i_XMLDocument, s_MagitFolders, itemID);
                BlobData containedFolder = buildFolderFromElement(folderElement, i_RootPath, folderPath, i_XMLDocument);
                folderData.GetCurrentFolder().AddBlobToList(containedFolder);
            }
        }

        String sha1 = FilesManagement.CreateFolderDescriptionFile(folderData, i_RootPath, folderPath, lastUpdater, "", true, null);
        folderData.SetSHA1(sha1);

        return folderData;
    }

    private static BlobData buildBlobFromElement(Element i_BlobElement, Path i_RepositoryPath, Path i_FilePath) throws FileNotFoundException, UnsupportedEncodingException {
        String fileName = i_BlobElement.getElementsByTagName("name").item(0).getTextContent();
        String lastUpdater = i_BlobElement.getElementsByTagName("last-updater").item(0).getTextContent();
        String lastUpdateDate = i_BlobElement.getElementsByTagName("last-update-date").item(0).getTextContent();
        String content = i_BlobElement.getElementsByTagName("content").item(0).getTextContent();
        PrintWriter writer = new PrintWriter(i_FilePath + "\\" + fileName, "UTF-8");
        writer.println(content);
        writer.close();

        BlobData blob = FilesManagement.CreateSimpleFileDescription(
                i_RepositoryPath,
                Paths.get(i_FilePath + "\\" + fileName),
                lastUpdater, lastUpdateDate,
                "",
                null,
                true);
        return blob;
    }


//-----------------------------------------------------------------------------------------------------------------------------

    public static void ExportRepositoryToXML(Path i_ExportPath, RepositoryManager i_Repository) {


    }
}