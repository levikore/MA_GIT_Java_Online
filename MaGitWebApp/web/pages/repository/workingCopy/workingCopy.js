let lastIndexSelected = -1;
let originalRepository;
let repository;
const openChangesMap = new Map();
let isFilesListDisabled = false;
let originalValOfTextAreaGlobalVar;
const WC_URL = buildUrlWithContextPath("WC");


function handlePostOpenChanges() {
    // const dataToPost = openChangesMap;
    // const dataToPost = Object.assign({}, ...[...openChangesMap.entries()].map(([k, v], index) => ({[index]: v})));
    const dataToPost = Array.from(openChangesMap.values());
    postOpenChanges(dataToPost);
}

function postOpenChanges(dataToPost) {
    $.ajax({
        url: WC_URL,
        type: 'POST',
        data: {"openChanges": JSON.stringify(dataToPost), "repositoryName": repository.m_RepositoryName, "currentWCFilesList":JSON.stringify(repository.m_CurrentWCFilesList)},
        dataType: "json",
        success: function () {

        }
    })
}

function setFilesList() {
    //users-list
    if (repository === undefined) {
        repository = JSON.parse(sessionStorage.getItem("repository"));
        originalRepository = JSON.parse(sessionStorage.getItem("repository"));
    }
    lastIndexSelected = -1
    $("#files-list").empty();
    $("#files-content-list").empty();

    for (let i = 0; i < repository.m_CurrentWCFilesList.length; i++) {
        appendFileToFilesList(i);

        let fileElementId = $("#file" + i);
        let fileContentId = $("#file" + i + "-content");

        fileElementId.on(
            'click',
            function () {
                handleFileElementClick(fileElementId, fileContentId);
            }
        );

        setButtonsClickFunctions(i);
        setButtonsOfCreateFileModal(i);

        // if (lastIndexSelected === i) {
        //     fileElementId.toggleClass('active');
        //     fileContentId.toggleClass('show');
        //     fileContentId.toggleClass('active');
        // }
    }

}

function disableAllFilesList() {
    for (let i = 0; i < repository.m_CurrentWCFilesList.length; i++) {
        $("#file" + i).addClass("disabled");
    }
    isFilesListDisabled = true;
}

function enableAllFilesList() {
    for (let i = 0; i < repository.m_CurrentWCFilesList.length; i++) {
        $("#file" + i).removeClass("disabled");
    }
    isFilesListDisabled = false;
}

function handleSaveButtonClick() {
    handlePostOpenChanges();
    openChangesMap.clear();
    $("#save-icon-file").attr("disabled", true);

    sessionStorage.setItem("repository", JSON.stringify(repository));
    originalRepository = JSON.parse(sessionStorage.getItem("repository"));
}

function setButtonsClickFunctions(index) {
    const editButton = $("#edit-icon-file" + index);
    const removeButton = $("#remove-icon-file" + index);
    const createFile = $("#create-icon-file" + index);
    const saveButton = $("#save-icon-file");
    const doneButton = $("#check-icon-file" + index);
    const textArea = $("#file" + index + "-content-textarea");
    const cancelButton = $("#cancel-icon-file" + index);

    saveButton.on(
        'click',
        function () {
            handleSaveButtonClick();
        }
    );

    if (repository.m_CurrentWCFilesList[index].m_Path !== repository.m_RepositoryPath) {
        removeButton.on(
            'click',
            function () {
                handleRemoveButtonClick(index);
            }
        );
        editButton.on(
            'click',
            function () {
                handleEditButtonClick(index);
            }
        );
        cancelButton.on(
            'click',
            function () {
                handleCancelButtonClick(index);
            }
        );
        doneButton.on(
            'click',
            function () {
                handleDoneButtonClick(index);
            }
        );


        let oldVal = "";
        textArea.on("change keyup paste", function () {
            let currentVal = $(this).val();


            if (currentVal.trim() === originalValOfTextAreaGlobalVar) {
                doneButton.attr("disabled", true);
                editButton.attr("disabled", false);
                // removeButton.attr("disabled", false);
                return; //check to prevent multiple simultaneous triggers
            }
            if (currentVal.trim() === oldVal) {
                return;
            }

            oldVal = currentVal.trim();
            doneButton.attr("disabled", false);
            editButton.attr("disabled", true);

            //removeButton.attr("disabled", true);

        });
    } else {
        removeButton.attr("disabled", true);
        editButton.attr("disabled", true);
        createFile.attr("disabled", false);
    }
    if (repository.m_CurrentWCFilesList[index].m_IsFolder) {
        editButton.attr("disabled", true);
        createFile.attr("disabled", false);
        createFile.on(
            'click',
            function () {
                handleCreateNewFileClick(index);
            }
        );
    }
}

function setButtonsOfCreateFileModal(index) {
    $("#modal-file" + index + "-Name-input").on("change", function () {
        handleChangeInFileName(index);
    });

    $("#createFileButton" + index).on("click", function () {
        handleCreateModalButtonClick(index);
    })
}

function handleDoneButtonClick(index) {
    const textArea = $("#file" + index + "-content-textarea");
    const saveButton = $("#save-icon-file");

    originalValOfTextAreaGlobalVar = textArea.val();
    repository.m_CurrentWCFilesList[index].content = originalValOfTextAreaGlobalVar;
    //openChangesMap.push(getOpenChangeObjToPost("create", path))
    const path = repository.m_CurrentWCFilesList[index].m_Path;

    if (openChangesMap.has(path)) {
        if (openChangesMap.get(path).action === "create") {
            openChangesMap.set(path, getOpenChangeObjToPost("create", path, false, originalValOfTextAreaGlobalVar));
        } else if (openChangesMap.get(path).action === "edit") {
            if (originalValOfTextAreaGlobalVar !== originalRepository.m_CurrentWCFilesList[index].m_Content) {
                openChangesMap.set(path, getOpenChangeObjToPost("edit", path, false, originalValOfTextAreaGlobalVar));
            } else {
                openChangesMap.delete(path);
            }
        }
    } else {
        openChangesMap.set(path, getOpenChangeObjToPost("edit", path, false, originalValOfTextAreaGlobalVar));
    }

    saveButton.attr("disabled", false);

    handleCancelButtonClick(index);
}

function handleCancelButtonClick(index) {
    const cancelSign = $("#cancel-icon-file" + index);
    const editButton = $("#edit-icon-file" + index);
    const removeButton = $("#remove-icon-file" + index);
    const textArea = $("#file" + index + "-content-textarea");
    const doneButton = $("#check-icon-file" + index);

    textArea.val(originalValOfTextAreaGlobalVar);
    originalValOfTextAreaGlobalVar = undefined;
    enableAllFilesList();
    editButton.removeClass("active");
    editButton.attr("disabled", false);
    cancelSign.attr("disabled", true);
    removeButton.attr("disabled", false);
    textArea.attr("readonly", true);
    doneButton.attr("disabled", true);
}

function handleEditButtonClick(index) {
    const editButton = $("#edit-icon-file" + index);
    const removeButton = $("#remove-icon-file" + index);
    const createFile = $("#create-icon-file" + index);
    const saveButton = $("#save-icon-file");
    const doneButton = $("#check-icon-file" + index);
    const textArea = $("#file" + index + "-content-textarea");
    const cancelSign = $("#cancel-icon-file" + index);

    if (originalValOfTextAreaGlobalVar === undefined) {
        originalValOfTextAreaGlobalVar = textArea.val().trim();
    }

    if (!editButton.hasClass("active")) {
        editButton.addClass("active");
        removeButton.attr("disabled", true);
        if (textArea.attr("readonly")) {
            textArea.attr("readonly", false);
        }
        if (!isFilesListDisabled) {
            disableAllFilesList();
        }
        cancelSign.attr("disabled", false)
    } else if (textArea.val() === originalValOfTextAreaGlobalVar) {
        originalValOfTextAreaGlobalVar = undefined;
        if (!textArea.attr("readonly")) {
            textArea.attr("readonly", true);
        }
        editButton.removeClass("active");
        removeButton.attr("disabled", false);
        if (isFilesListDisabled) {
            enableAllFilesList();
        }
        cancelSign.attr("disabled", true)

    }

}

function handleRemoveButtonClick(index) {
    const saveButton = $("#save-icon-file");
    const path = repository.m_CurrentWCFilesList[index].m_Path;
    const isFolder = repository.m_CurrentWCFilesList[index].m_IsFolder;
    const shortName = getValueAfterLastSlash(path)
    const parentPath = path.split("\\" + shortName)[0];
    const folderIndex = repository.m_CurrentWCFilesList.findIndex(file => file.m_Path === parentPath);
    const folder = repository.m_CurrentWCFilesList[folderIndex];
    repository.m_CurrentWCFilesList[folderIndex].m_Content = removeLineByFirstWordInLine(shortName, folder.m_Content);
    removeAllFilesWithSpecificPath(path);
    if (openChangesMap.has(path) && openChangesMap.get(path).action === "edit" || !openChangesMap.has(path)) {
        openChangesMap.set(path, getOpenChangeObjToPost("delete", path, isFolder));
    } else if (openChangesMap.has(path)) {
        openChangesMap.delete(path);
    }
    if (openChangesMap.size > 0) {
        saveButton.attr("disabled", false);
    } else {
        saveButton.attr("disabled", true);
    }

    setFilesList();
}

function handleCreateNewFileClick(index) {
    $('#createFileModal-file' + index).modal('show');

}

function getOpenChangeObjToPost(actionType, path, isFolder, content) {
    //actionType=delete or create or edit
    //if actionType is create, content must contain value
    let obj;
    if (actionType === "delete") {
        obj = {"action": actionType, "isFolder": isFolder, "path": path};
    } else {
        obj = {"action": actionType, "isFolder": isFolder, "path": path, "content": content}
    }

    return obj;
}

function removeAllFilesWithSpecificPath(path) {
    repository.m_CurrentWCFilesList = repository.m_CurrentWCFilesList.filter(file=>!isPathContainParentPath(file.m_Path, path));
}

function isPathContainParentPath(path, parentPath) {
    let result = false;
    while (path.length >= parentPath.length) {
        if (path === parentPath) {
            result = true;
            break;
        }
        path = getParentPath(path)
    }
    return result;
}

function getParentPath(path) {
    const lastSlashIndex = path.lastIndexOf("\\");
    return path.substring(0, lastSlashIndex);
}

function removeLineByFirstWordInLine(fileName, content) {
    const fileName2 = fileName.concat(",");
    const re = new RegExp("^" + fileName2 + ".*\n?", "m");
    const newContent = content.replace(re, '');
    return newContent;
}

function getValueAfterLastSlash(url) {
    const n = url.lastIndexOf('\\');
    const result = url.substring(n + 1);
    return result;
}


function handleFileElementClick(fileElementId, fileContentId) {
    if (!fileElementId.hasClass("disabled")) {
        fileElementId.toggleClass('active');
        fileContentId.toggleClass('show');
        fileContentId.toggleClass('active');

        if (fileElementId.hasClass("active")) {
            if (lastIndexSelected !== -1) {
                const lastElementSelected = $("#file" + lastIndexSelected + "-content");
                $("#file" + lastIndexSelected).toggleClass('active')

                lastElementSelected.toggleClass('show')
                lastElementSelected.toggleClass('active')
            }
            lastIndexSelected = parseInt(fileElementId.attr('key'));
        } else if (!fileElementId.hasClass("active")) {
            lastIndexSelected = -1;
        }
    }
}

function handleCreateModalButtonClick(index) {
    const saveButton = $("#save-icon-file");
    const createFileButton = $("#createFileButton" + index);
    const nameFile = $("#modal-file" + index + "-Name-input").val();
    const isFolder = $("#folder-input" + index).hasClass("active");
    const type = isFolder ? "folder" : "file";
    const parentPath = repository.m_CurrentWCFilesList[index].m_Path;
    const path = parentPath + "\\" + nameFile;
    repository.m_CurrentWCFilesList[index].m_Content = repository.m_CurrentWCFilesList[index].m_Content.concat("\n" + nameFile + "," + type + ",," + repository.m_Owner + "(unsaved file)");
    const fileObj = {"m_Path": path, "m_Content": "", "m_IsFolder": isFolder};
    repository.m_CurrentWCFilesList.push(fileObj);
    openChangesMap.set(path, getOpenChangeObjToPost("create", path, isFolder));
    saveButton.attr("disabled", false);
    resetModal(index);
    setFilesList();
    hideModal(index);
}


function hideModal(index) {
    const createFileModal = $("#createFileModal-file" + index);
    const body = $('body');

    createFileModal.removeClass("in");
    $(".modal-backdrop").remove();
    body.removeClass('modal-open');
    body.css('padding-right', '');
    createFileModal.hide();
}

function handleFolderRadioButton(index) {
    $("#file-input" + index).addClass("active");
    $("#folder-input" + index).removeClass("active");

    handleChangeInFileName(index);
}

function isFileExist(path, isFile) {
    let fileElement;
    if (isFile) {
        fileElement = repository.m_CurrentWCFilesList.find(file => !file.m_IsFolder && file.m_Path === path);
    } else {
        fileElement = repository.m_CurrentWCFilesList.find(file => file.m_IsFolder && file.m_Path === path);
    }

    return fileElement !== undefined;
}

function handleChangeInFileName(index) {
    const fileName = $("#modal-file" + index + "-Name-input").val();
    const button = $("#createFileButton" + index);
    const fileNameWrapper = $("#file-name-wrapper" + index);
    const stringFileNameError = $("#file-name-error-string" + index);
    const stringIsExist = $("#file-name-is-exist" + index);
    const path = repository.m_CurrentWCFilesList[index].m_Path + "\\" + fileName;
    let extension = "";
    if ($("#file-input" + index).hasClass("active")) {
        if (fileName.includes('.')) {
            extension = fileName.substr(fileName.lastIndexOf('.') + 1);
        }
        if (extension !== "") {
            if (!isFileExist(path, true)) {
                button.attr("disabled", false)
                removeFileNameErrorString(index)
                removeFileNameIsExist(index);
            } else {
                if (stringIsExist.length === 0) {
                    removeFileNameErrorString(index);
                    fileNameWrapper.append('<p class="control-label" id="file-name-is-exist' + index + '\">This file already exist</p>')
                }
            }
        } else {
            button.attr("disabled", true)
            if (stringFileNameError.length === 0) {
                removeFileNameIsExist(index);
                fileNameWrapper.append('<p class="control-label" id="file-name-error-string' + index + '\">The file name must include extension</p>')
            }
        }
    } else {
        removeFileNameErrorString(index);

        if (fileName !== "" && !isFileExist(path, false)) {
            button.attr("disabled", false)
            removeFileNameIsExist(index);
        } else {
            if (isFileExist(path, false))
                if (stringIsExist.length === 0) {
                    fileNameWrapper.append('<p class="control-label" id="file-name-is-exist' + index + '\">This folder already exist</p>')
                }
            button.attr("disabled", true)
        }
    }
}

function resetModal(index) {
    removeFileNameErrorString(index);
    $("#folder-input" + index).removeClass("active");
    $("#file-input" + index).addClass("active");
    $("#modal-file" + index + "-Name-input").val("");
}

function removeFileNameErrorString(index) {
    const stringFileNameError = $("#file-name-error-string" + index);

    if (stringFileNameError.length !== 0) {
        stringFileNameError.remove();
    }
}

function removeFileNameIsExist(index) {
    const stringIsExist = $("#file-name-is-exist" + index);

    if (stringIsExist.length !== 0) {
        stringIsExist.remove();
    }
}


function handleFileRadioButton(index) {
    removeFileNameErrorString(index);
    $("#folder-input" + index).addClass("active");
    $("#file-input" + index).removeClass("active");

    handleChangeInFileName(index);
}

function appendFileToFilesList(index) {
    let shortPath = repository.m_CurrentWCFilesList[index].m_Path.substring(repository.m_CurrentWCFilesList[index].m_Path.indexOf(repository.m_RepositoryName));

    $("#files-list")
        .append($(
            '<a class="list-group-item list-group-item-action" data-toggle="tab" role="tab" aria-selected="false">' + shortPath + '</a>')
            .attr({
                'id': "file" + index,
                'href': "#file" + index + "-content",
                'key': index,
                'aria-controls': "file" + index + "-content"
            }));

    $("#files-content-list")
        .append($(
            '<div class="tab-pane fade" role="tabpanel" >'
            + '</div>'
        ).attr({
            'id': "file" + index + "-content",
            'aria-controls': "file" + index
        }));

    $("#file" + index + "-content").append(
        '<form>'
        + '<div class="form-group" id=\"file' + index + "-content-textarea-wrapper" + '\">'
        + '<label >' + shortPath + ' content:</label>'
        + '<div>'

        + '<button id="edit-icon-file' + index + '\" type="button" class="pull-right btn btn-default btn-lg">'
        + '<span class="glyphicon glyphicon-edit" aria-hidden="true">'
        + '</span>Edit</button>'

        + '<button id="remove-icon-file' + index + '\" type="button" class="pull-right btn btn-default btn-lg">'
        + '<span class="glyphicon glyphicon-remove" aria-hidden="true">'
        + '</span>Remove</button>'

        + '<button  disabled="true" id="create-icon-file' + index + '\" type="button" class="pull-right btn btn-default btn-lg" data-toggle="modal" data-target="#createFileModal">'
        + '<span class="glyphicon glyphicon-plus" aria-hidden="true">'
        + '</span>Add new file <small>to current folder</small></button>'


        + '<div class="modal fade" id="createFileModal-file' + index + '\" tabindex="-1" role="dialog" aria-labelledby="myModalLabel">'
        + '<div class="modal-dialog" role="document">'
        + '<div class="modal-content">'
        + '<div class="modal-header">'
        + '<button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>'
        + '<h4 class="modal-title" id="createFileModal-file' + index + '\-label">Create new file in: ' + shortPath + '\"</h4>'
        + '</div>'
        + '<div class="modal-body">'

        + '<div class="row">'
        + '<div class="btn-group" data-toggle="buttons">'
        + ' <label id="folder-input' + index + '\"  class="btn btn-primary" onclick="handleFileRadioButton(\'' + index + '\')" >'
        + '<input type="radio"> Folder'
        + '</label>'
        + '<label id="file-input' + index + '\" class="btn btn-primary active" onclick="handleFolderRadioButton(\'' + index + '\')">'
        + '<input  type="radio" checked> File'
        + '</label>'
        + '</div>'
        + '</div>'

        + '<div class="row">'
        + '<div class="form-inline" >'
        + '<div id="file-name-wrapper' + index + '\" class="form-group has-feedback">'
        + '<label id="branch-label" class="control-label">File Name</label>'
        + '<input type="text" class="form-control" id="modal-file' + index + '\-Name-input">'
        + '</div>'
        + '</div>'
        + '</div>'
        + '</div>'
        + '<div class="modal-footer">'

        + '<button type="button" class="btn btn-default" data-dismiss="modal">'
        + '<span class="glyphicon glyphicon-minus-sign" aria-hidden="true">'
        + '</span>Cancel</button>'

        + '<button id="createFileButton' + index + '\" disabled="true" type="button" class="btn btn-success" >'

        + '<span class="glyphicon glyphicon-check" aria-hidden="true">'
        + '</span>Create</button>'

        + '</div>'
        + '</div>'
        + '</div>'
        + '</div>'

        + '</div>'
        + '</div>'
        + '<textarea readonly class="form-control" cols="100" rows="10" id=\"file' + index + "-content-textarea" + '\"></textarea>'

        + '<button  disabled="true" id="check-icon-file' + index + '\" type="button" class="pull-right btn btn-default btn-lg">'
        + '<span class="glyphicon glyphicon-check" aria-hidden="true">'
        + '</span>Done!</button>'

        + '<button disabled="true" id="cancel-icon-file' + index + '\" type="button" class="pull-right btn btn-default btn-lg">'
        + '<span class="glyphicon glyphicon-minus-sign" aria-hidden="true">'
        + '</span>Cancel</button>'

        + '</div>'
        + '</form>'
    )

    $('#file' + index + '-content-textarea').append(
        repository.m_CurrentWCFilesList[index].m_Content
    )
}

$(function () {
    setFilesList();
});