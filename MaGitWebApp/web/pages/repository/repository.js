const refreshRepositoryRate = 2000; //milli seconds
const REPOSITORY_URL = buildUrlWithContextPath("repository");
const BRANCH_URL = buildUrlWithContextPath("branch");
const COLLABORATION_URL=buildUrlWithContextPath("collaboration")
let lastIndexSelected = -1;
let repositoryName;

function ajaxRepository() {
    $.ajax({
        url: REPOSITORY_URL,
        data: getParametersData(),
        dataType: 'json',
        success: function (repository) {
            sessionStorage.setItem("repository", JSON.stringify(repository));
            setRepositoryData(repository);
        }
    })
}

function getParametersData() {
    let searchParams = new URLSearchParams(window.location.search)
    //Does sent exist?
    const userName = searchParams.get('userName');
    const repositoryName = searchParams.get('repositoryName');
    return {"username": userName, "repositoryName": repositoryName}
}


function isBranchExist(branchName) {
    const repository = JSON.parse(sessionStorage.getItem("repository"));
    let branch = repository.m_BranchesNamesList.find(branch => branch === branchName);
    return branch === branchName;
}

function postBranchFunctionsData(dataToPost) {
    $.ajax({
        url: BRANCH_URL,
        data: dataToPost,
        contentType: "application/json; charset=utf-8",
        dataType: "json",
        success: function () {

        }
    })
}

function postCheckout() {
    //ajaxRepository();
    const branchVal = $('#checkout-input').val();
    const checkoutWrapperId = $('#checkout-wrapper');
    const checkoutErrorSign = $('#checkout-error-sign');
    const errorString = $('#checkout-error-string')
    if (isBranchExist(branchVal)) {
        const data = {
            "functionName": "checkout",
            "repositoryName": repositoryName,
            "branchName": branchVal
        };
        postBranchFunctionsData(data);
        cleanErrorSign(checkoutWrapperId, checkoutErrorSign);
        errorString.remove();
        $('#checkout-input').val("");
        //setInterval(ajaxRepository, refreshRepositoryRate);
    } else {
        checkoutWrapperId.addClass("has-error");
        appendErrorSign(checkoutWrapperId, "checkout-error-string");
        if (isElementExist(errorString)) {
            checkoutWrapperId.append('<p class="control-label" id="checkout-error-string">This branch doesnt exist</p>');
        }
    }
    setTimeout(ajaxRepository, refreshRepositoryRate);
    // location.reload();
}

function  postForkFunctionsData(dataToPost) {
    $.ajax({
        url: COLLABORATION_URL,
        data: dataToPost,
        contentType: "application/json; charset=utf-8",
        dataType: "json",
        success: function () {

        }
    })
}

function cleanErrorSign(elementId, signElementId) {
    if (elementId.hasClass('has-error')) ;
    {
        elementId.removeClass('has-error');
        signElementId.remove();
    }
}

function appendErrorSign(elementId, signElementIdString) {
    elementId.addClass("has-error");
    if (isElementExist(elementId)) {
        elementId.append(' <span id=' + signElementIdString + ' class="glyphicon glyphicon-remove form-control-feedback" aria-hidden="true"></span>')
    }
}

function isElementExist(elementId) {
    return elementId.length === 0;
}

function postBranch() {
    const branchVal = $('#branch-input').val();
    const branchWrapperId = $('#branch-wrapper');
    const branchErrorSign = $('#branch-error-sign');
    const errorString = $('#branch-error-string')
    if (!isBranchExist(branchVal)) {
        const data = {
            "functionName": "branch",
            "repositoryName": repositoryName,
            "branchName": branchVal
        };
        postBranchFunctionsData(data);
        cleanErrorSign(branchWrapperId, branchErrorSign);
        errorString.remove();
        $('#branch-input').val("");

        //setInterval(ajaxRepository, refreshRepositoryRate);
    } else {
        branchWrapperId.addClass("has-error");
        appendErrorSign(branchWrapperId, "branch-error-string");
        if (isElementExist(errorString)) {
            branchWrapperId.append('<p class="control-label" id="branch-error-string">This branch already exist</p>');
        }
    }
    setTimeout(ajaxRepository, refreshRepositoryRate);
}

function getRepositoryByName(repositoriesList, repositoryName) {
    return repositoriesList.find(repository => repository.m_RepositoryName === repositoryName);
}

function isUncommitedFilesInRepository(repository) {
    return repository.m_UncommittedFilesList.length > 0;
}

function isRepositoryOfCurrentUser() {
    const repository = JSON.parse(sessionStorage["repository"]);
    return sessionStorage["userName"] === repository.m_Owner;
}

function setRepositoryData(repository) {
    repositoryName = repository.m_RepositoryName;
    setButtons(repository);
    $("#repository-name-label").empty();
    $("#repository-name-label").append('<h3 class="display-4">' + repository.m_RepositoryName + '</h3>')

    if (isRepositoryOfCurrentUser()) {
        $("#repository-name-label").append('<a onclick="return PopupCenter(\'workingCopy/workingCopy.html\',\'test\',\'1920\',\'500\')" class="display-4">Working Copy </a>')

        $("#unCommitted-files-list").on(
            'click',
            function () {
                handleUnCommittedChangesClick( );
            }
        );
    }else {

        $("#fork-button").on(
            'click',
            function () {
                handleForkClick();
            }
        );
    }

    setBranchesList(repository.m_BranchesList);
    setCommitsList(repository.m_HeadBranchCommitsList);

}

function handleUnCommittedChangesClick() {
    $('#unCommitted-files-list-modal').modal('show');
}

function hideModal(modalId) {
   // const unCommittedFilesModal = $("#unCommitted-files-list-modal");
    const body = $('body');

    modalId.removeClass("in");
    $(".modal-backdrop").remove();
    body.removeClass('modal-open');
    body.css('padding-right', '');
    modalId.hide();
}

function appendButtonsOfRepositoryOwner(repository) {
    const buttonsId = $("#buttons");
    buttonsId.append(
        '<div class="row" id="uncommitedFilesButtonRow">'

        + '<button disabled="true" id="unCommitted-files-list" type="button" class="btn btn-default btn-lg" data-toggle="modal" >'
        + '<span class="glyphicon glyphicon-list" aria-hidden="true">'
        + '</span>Uncommitted Files <span class="badge badge-dark">'
        + repository.m_UncommittedFilesList.length + '</span>' +
        '  <span class="sr-only">number of changes</span></button>'
        + '<div class="modal fade" id="unCommitted-files-list-modal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel">'
        + '<div class="modal-dialog" role="document">'
        + '<div class="modal-content">'
        + '<div class="modal-header">'
        + '<button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>'
        + '<h4 class="modal-title">Uncommitted Changes List</h4>'
        + '</div>'
        + '<div id="unCommitted-files-list-modal-body"  class="modal-body">'
        + '</div>'
        + '<div class="modal-footer">'
        + '</div>'
        + '</div>'
        + '</div>'
        + '</div>'

        + '</div>'

        + '<div class="row">'

        + '<div class="form-inline" >'
        + '<div id="commit-wrapper" class="form-group has-feedback">'
        + '<label id="commit-label" class="control-label">Commit</label>'
        + '<input disabled="true" type="text" class="form-control" id="commit-input">'
        + '<button disabled="true" onclick="postCommit()" id="commit-button" class="btn btn-default">Submit</button>'
        + '</div>'
        + '</div>'

        + '</div>'

        + '<div class="row">'

        + '<div class="form-inline" >'
        + '<div id="branch-wrapper" class="form-group has-feedback">'
        + '<label id="branch-label" class="control-label">New Branch</label>'
        + '<input type="text" class="form-control" id="branch-input">'
        + '<button onclick="postBranch()" id="branch-button" class="btn btn-default" >Submit</button>'
        + '</div>'
        + '</div>'

        + '</div>'

        + '<div class="row">'

        + '<div class="form-inline" >'
        + '<div id="checkout-wrapper" class="form-group has-feedback">'
        + '<label id="checkout-label" class="control-label">Checkout</label>'
        + '<input type="text" class="form-control" id="checkout-input">'
        + '<button onclick="postCheckout()" id="checkout-button" class="btn btn-default">Submit</button>'
        + '</div>'
        + '</div>'

        + '</div>'
    )
    if (isUncommitedFilesInRepository(repository)) {
        $("#unCommitted-files-list").attr("disabled", false);
        $("#commit-button").attr("disabled", false);
        $("#commit-input").attr("disabled", false);
        $("#branch-button").attr("disabled", true);
        $("#branch-input").attr("disabled", true);
        $("#checkout-button").attr("disabled", true);
        $("#checkout-input").attr("disabled", true);
        setUnCommittedFilesList();
    }
}

function handleForkClick() {
    $('#fork-modal').modal('show');
}


function appendButtonsOfGuest(repository) {
    const buttonsId = $("#buttons");
    buttonsId.append(
    '<div class="row">'

    + '<button id="fork-button" type="button" class="btn btn-default btn-lg" data-toggle="modal" >'
    + '<span class="glyphicon" aria-hidden="true">'
    + '</span>Fork</button>'
    + '<div class="modal fade" id="fork-modal" tabindex="-1" role="dialog" aria-labelledby="myModalLabel">'
    + '<div class="modal-dialog" role="document">'
    + '<div class="modal-content">'
    + '<div class="modal-header">'
    + '<button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>'
    + '<h4 class="modal-title">Fork'+repository.m_RepositoryName+'</h4>'
    + '</div>'
    + '<div id="fork-modal-body"  class="modal-body">'
        + '<div class="row">'

        + '<div class="form-inline" >'
        + '<div id="repository-name-fork-modal-wrapper" class="form-group has-feedback">'
        +'<h4 class="text-info">Please select name of the new repository.</h4>'
        + '<label id="repository-name-fork-modal-label" class="control-label">Repository name</label>'
        + '<input type="text" class="form-control" id="repository-name-fork-modal-input">'//to add check for name
        + '<button onclick="postFork()" id="fork-button" class="btn btn-default" >Submit</button>'
        + '</div>'
        + '</div>'

        + '</div>'
    + '</div>'
    + '<div class="modal-footer">'
    + '</div>'
    + '</div>'
    + '</div>'
    + '</div>'

    + '</div>');

}

function setButtons(repository) {
    const buttonsId = $("#buttons");
    buttonsId.empty();
    if (isRepositoryOfCurrentUser(repository)) {
      appendButtonsOfRepositoryOwner(repository);
    } else {
        appendButtonsOfGuest(repository);
    }
    // $("#buttons").append()
}

function postFork(){
    const newRepositoryNameId=$('#repository-name-fork-modal-input');
    const newRepositoryName = newRepositoryNameId.val();
    const parametersData=getParametersData();
    //const branchErrorSign = $('#branch-error-sign');
    // const errorString = $('#branch-error-string')
    const data = {
        "originRepositoryName": parametersData.repositoryName,
        "originRepositoryUserName":parametersData.username,
        "functionName": "fork",
        "newRepositoryName": newRepositoryName
    };
    postForkFunctionsData(data);
    newRepositoryNameId.val("");

    //setInterval(ajaxRepository, refreshRepositoryRate);
    setTimeout(ajaxRepository, refreshRepositoryRate);
}

function postCommit() {
    const commitVal = $('#commit-input').val();
    const commitWrapperId = $('#branch-wrapper');
    //const branchErrorSign = $('#branch-error-sign');
    // const errorString = $('#branch-error-string')
    const data = {
        "repositoryName": repositoryName,
        "functionName": "commit",
        "commitName": commitVal
    };
    postBranchFunctionsData(data);
    $('#commit-input').val("");

    //setInterval(ajaxRepository, refreshRepositoryRate);
    setTimeout(ajaxRepository, refreshRepositoryRate);
}

function setCommitsList(commitsList) {
    const commitsListId = $('#commits-list');
    const filesListId = $('#files-list');
    commitsListId.empty();
    filesListId.empty();

    for (let i = 0; i < commitsList.length; i++) {
        const commitData = commitsList[i];
        appendCommit(commitsListId, commitData, i);
        let commitElementId = $("#commit-element" + i);
        let commitContentId = $("#commit-element-files-List" + i);

        commitElementId.on(
            'click',
            function () {
                handleCommitElementClick(commitElementId, commitContentId);
            }
        );

        setFilesList(commitData.m_FilesList, commitContentId);


        if (lastIndexSelected === i) {
            commitElementId.toggleClass('active');
            commitContentId.toggleClass('show');
        }
    }
}

function setFilesList(i_FilesList, i_CommitContentId) {
    for (let i = 0; i < i_FilesList.length; i++) {
        let shortPath = i_FilesList[i].substring(i_FilesList[i].indexOf(repositoryName));
        i_CommitContentId.append('<p class="mb-1">' + shortPath + '</p>');
    }
}

function handleCommitElementClick(commitElementId, commitContentId) {
    commitElementId.toggleClass('active');
    commitContentId.toggleClass('show');

    if (commitElementId.hasClass("active")) {
        if (lastIndexSelected != -1) {
            $("#commit-element" + lastIndexSelected).toggleClass('active')
            $("#commit-element-files-List" + lastIndexSelected).toggleClass('show')
        }
        lastIndexSelected = parseInt(commitElementId.attr('key'));
    } else if (!commitElementId.hasClass("active")) {
        lastIndexSelected = -1;
    }

}

function appendCommit(commitsListId, commitData, commitIndex) {
    commitsListId.append(
        $('<a class="list-group-item list-group-item-action align-items-start" data-toggle="list" role="tab"> </a>')
            .attr({
                'id': "commit-element" + commitIndex,
                'href': "#commit-element-files-List" + commitIndex,
                'key': commitIndex
            })
    );

    $('#files-list').append($('<div class="list-group tab-pane" role="tabpanel"> ' +
        '</div>').attr({
        'id': "commit-element-files-List" + commitIndex,
        'aria-labelledby': "commit-element" + commitIndex
    }));

    const commitDataLines = commitData.m_CommitDescription.split(/\r\n|\r|\n/g);


    $('<divclass="w-100 justify-content-between"> </div>').attr('id', "commit-element-wrapper" + commitIndex)
        .appendTo($("#commit-element" + commitIndex));

    for (let j = 0; j < commitDataLines.length; j++) {
        $('<p class="mb-1">'
            + commitDataLines[j] +
            '</p>'
        ).appendTo($("#commit-element-wrapper" + commitIndex))
    }
    if (commitData !== undefined && commitData.m_PointedByList.length > 0) {
        $('<p class="mb-1">'
            + "this commit pointed by:" +
            '</p>'
        ).appendTo($("#commit-element-wrapper" + commitIndex))

        for (let j = 0; j < commitData.m_PointedByList.length; j++) {
            $('<p class="mb-1">'
                + commitData.m_PointedByList[j] +
                '</p>'
            ).appendTo($("#commit-element-wrapper" + commitIndex))
        }
    }
}

function setBranchesList(branchesList) {
    const branchesListId = $('#branches-list');
    branchesListId.empty()
    for (let i = 0; i < branchesList.length; i++) {
        const branchData = branchesList[i];

        branchesListId.append(
            $('<a class="list-group-item list-group-item-action align-items-start"> </a>')
                .attr({
                    'id': "branch-element" + i,
                })
        );

        const branchDataLines = branchData.split(/\r\n|\r|\n/g);


        $('<divclass="w-100 justify-content-between"> </div>').attr('id', "branch-element-wrapper" + i)
            .appendTo($("#branch-element" + i));

        for (let j = 0; j < branchDataLines.length; j++) {
            $('<p class="mb-1">'
                + branchDataLines[j] +
                '</p>'
            ).appendTo($("#branch-element-wrapper" + i))
        }
    }
}

function getUncomitedFilesList() {
    const repository = JSON.parse(sessionStorage["repository"]);
    return repository.m_UncommittedFilesList;
}

function setUnCommittedFilesList() {
    const unCommittedFilesListId = $('#unCommitted-files-list-modal-body');
    const unCommittedFilesList = getUncomitedFilesList();

    unCommittedFilesListId.empty()
    for (let i = 0; i < unCommittedFilesList.length; i++) {
        const uncommitedData = unCommittedFilesList[i];

        unCommittedFilesListId.append(
            $('<a class="list-group-item list-group-item-action align-items-start"> </a>')
                .attr({
                    'id': "Uncommitted-element" + i,
                })
        );

        const uncommittedElementId = $("#Uncommitted-element" + i);
        $('<divclass="w-100 justify-content-between"> </div>').attr('id', "Uncommitted-element-wrapper" + i)
            .appendTo(uncommittedElementId);

        $('<h4 class="mb-1">'
            + "file name:" + uncommitedData.m_fileContent.m_Path +
            '</h4>'
        ).appendTo(uncommittedElementId)

        $('<p class="mb-1">'
            + "Change type:" + uncommitedData.m_ChangeType +
            '</p>'
        ).appendTo(uncommittedElementId)

    }
}


let popupWindow = null;

function PopupCenter(url, title, w, h) {
    const dualScreenLeft = window.screenLeft != undefined ? window.screenLeft : window.screenX;
    const dualScreenTop = window.screenTop != undefined ? window.screenTop : window.screenY;

    const width = window.innerWidth ? window.innerWidth : document.documentElement.clientWidth ? document.documentElement.clientWidth : screen.width;
    const height = window.innerHeight ? window.innerHeight : document.documentElement.clientHeight ? document.documentElement.clientHeight : screen.height;

    const systemZoom = width / window.screen.availWidth;
    const left = (width - w) / 2 / systemZoom + dualScreenLeft
    const top = (height - h) / 2 / systemZoom + dualScreenTop
    const newWindow = window.open(url, title, 'scrollbars=yes, width=' + w / systemZoom + ', height=' + h / systemZoom + ', top=' + top + ', left=' + left);
    popupWindow = newWindow;
    // Puts focus on the newWindow
    if (window.focus) {
        newWindow.focus();
    }
}

function parent_disable() {
    if (popupWindow && !popupWindow.closed)
        popupWindow.focus();
}


$(function () {
    setTimeout(ajaxRepository, refreshRepositoryRate);
});