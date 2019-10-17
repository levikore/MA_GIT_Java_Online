const refreshRepositoryRate = 2000; //milli seconds
const REPOSITORY_URL = buildUrlWithContextPath("repository");
const BRANCH_URL = buildUrlWithContextPath("branch");
let lastIndexSelected = -1;
let repositoryName;

function ajaxRepository() {
    $.ajax({
        url: REPOSITORY_URL,
        data: getParametersData(),
        dataType: 'json',
        success: function (repository) {
            setRepositoryData(repository);
            sessionStorage.setItem("repository", JSON.stringify(repository));
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
    ajaxRepository();
    const branchVal = $('#checkout-button').val();
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
        $('#checkout-button').val("");
        setInterval(ajaxRepository, refreshRepositoryRate);
    } else {
        checkoutWrapperId.addClass("has-error");
        appendErrorSign(checkoutWrapperId, "checkout-error-string");
        if (isElementExist(errorString)) {
            checkoutWrapperId.append('<p class="control-label" id="checkout-error-string">This branch doesnt exist</p>');
        }
    }
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
    ajaxRepository();
    const branchVal = $('#branch-button').val();
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
        $('#branch-button').val("");
        setInterval(ajaxRepository, refreshRepositoryRate);
    } else {
        branchWrapperId.addClass("has-error");
        appendErrorSign(branchWrapperId, "branch-error-string");
        if (isElementExist(errorString)) {
            branchWrapperId.append('<p class="control-label" id="branch-error-string">This branch already exist</p>');
        }
    }
}

function getRepositoryByName(repositoriesList, repositoryName) {
    return repositoriesList.find(repository => repository.m_RepositoryName === repositoryName);
}

function setRepositoryData(repository) {
    repositoryName = repository.m_RepositoryName;
    $("#repository-name-label").empty();
    $("#repository-name-label").append('<h3 class="display-4">' + repository.m_RepositoryName + '</h3>')
    $("#repository-name-label").append('<a onclick="return PopupCenter(\'workingCopy/workingCopy.html\',\'test\',\'1920\',\'750\')" class="display-4">Working Copy </a>')
    setBranchesList(repository.m_BranchesList);
    setCommitsList(repository.m_HeadBranchCommitsList);
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

function PopupCenter(url, title, w, h) {
    const dualScreenLeft = window.screenLeft != undefined ? window.screenLeft : window.screenX;
    const dualScreenTop = window.screenTop != undefined ? window.screenTop : window.screenY;

    const width = window.innerWidth ? window.innerWidth : document.documentElement.clientWidth ? document.documentElement.clientWidth : screen.width;
    const height = window.innerHeight ? window.innerHeight : document.documentElement.clientHeight ? document.documentElement.clientHeight : screen.height;

    const systemZoom = width / window.screen.availWidth;
    const left = (width - w) / 2 / systemZoom + dualScreenLeft
    const top = (height - h) / 2 / systemZoom + dualScreenTop
    const newWindow = window.open(url, title, 'scrollbars=yes, width=' + w / systemZoom + ', height=' + h / systemZoom + ', top=' + top + ', left=' + left);

    // Puts focus on the newWindow
    if (window.focus) newWindow.focus();
}


$(function () {
    setInterval(ajaxRepository, refreshRepositoryRate);
});