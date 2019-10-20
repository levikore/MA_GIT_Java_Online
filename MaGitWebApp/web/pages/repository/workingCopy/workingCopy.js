let lastIndexSelected = -1;
let repository;
const openChangesList = [];
let isFilesListDisabled = false;
let originalValOfTextArea;


function setFilesList() {
    //users-list
    if (repository === undefined) {
        repository = JSON.parse(sessionStorage.getItem("repository"));
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

        setButtonsClickFuctions(i);

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

function setButtonsClickFuctions(index) {
    const editButton = $("#edit-icon-file" + index);
    const removeButton = $("#remove-icon-file" + index);
    const createFile = $("#create-icon-file" + index);
    const saveButton = $("#save-icon-file");
    const doneButton = $("#check-icon-file" + index);
    const textArea = $("#file" + index + "-content-textarea");
    const cancelSign = $("#cancel-icon-file" + index);


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
        let oldVal = "";
        textArea.on("change keyup paste", function () {
            let currentVal = $(this).val();


            if (currentVal.trim() === originalValOfTextArea) {
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

        if (repository.m_CurrentWCFilesList[index].m_IsFolder) {
            editButton.attr("disabled", true);
            createFile.attr("disabled", false);
        }

    } else {
        removeButton.attr("disabled", true);
        editButton.attr("disabled", true);
        createFile.attr("disabled", false);
    }
}

function handleEditButtonClick(index) {
    const editButton = $("#edit-icon-file" + index);
    const removeButton = $("#remove-icon-file" + index);
    const createFile = $("#create-icon-file" + index);
    const saveButton = $("#save-icon-file");
    const doneButton = $("#check-icon-file" + index);
    const textArea = $("#file" + index + "-content-textarea");
    const prevContent = textArea.text();
    const cancelSign = $("#cancel-icon-file" + index);

    if (originalValOfTextArea === undefined) {
        originalValOfTextArea = textArea.text().trim();
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
    } else if (textArea.text() === originalValOfTextArea) {
        originalValOfTextArea = undefined;
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
    const path = repository.m_CurrentWCFilesList[index].m_Path;
    const shortName = getValueAfterLastSlash(path)
    const parentPath = path.split("\\" + shortName)[0];
    const folderIndex = repository.m_CurrentWCFilesList.findIndex(file => file.m_Path === parentPath);
    const folder = repository.m_CurrentWCFilesList[folderIndex];
    repository.m_CurrentWCFilesList[folderIndex].m_Content = removeLineByFirstWordInLine(shortName, folder.m_Content);
    removeAllFilesWithSpecificPath(path);
    openChangesList.push(getOpenChangeObjToPost("delete", path))
    setFilesList();
}

function getOpenChangeObjToPost(actionType, path, content) {
    //actionType=delete or create or edit
    //if actionType!=delete, content must contain value
    let obj;
    if (actionType === "delete") {
        obj = {"action": actionType, "path": path};
    } else {
        obj = {"action": actionType, "path": path, "content": content}
    }

    return obj;
}

function removeAllFilesWithSpecificPath(path) {
    repository.m_CurrentWCFilesList = repository.m_CurrentWCFilesList.filter(file => (file.m_Path.substring(0, path.length)) !== path);
}

function removeLineByFirstWordInLine(fileName, content) {
    const re = new RegExp("^" + fileName + ".*\n?", "m");
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

        + '<button  disabled="true" id="create-icon-file' + index + '\" type="button" class="pull-right btn btn-default btn-lg">'
        + '<span class="glyphicon glyphicon-plus" aria-hidden="true">'
        + '</span>Add new file <small>to current folder</small></button>'

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