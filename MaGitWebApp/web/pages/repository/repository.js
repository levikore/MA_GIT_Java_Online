const refreshRepositoryRate = 2000; //milli seconds
const ALL_USERS_LIST_URL2 = buildUrlWithContextPath("allusers");
const BRANCH_URL = buildUrlWithContextPath("branch");
let lastIndexSelected = -1;
let repositoryName;
let repositoryObject;

function ajaxRepository() {
    $.ajax({
        url: ALL_USERS_LIST_URL2,

        success: function (i_UsersList) {
            let searchParams = new URLSearchParams(window.location.search)
            //Does sent exist?
            const userName = searchParams.get('userName');
            const repositoryName = searchParams.get('repositoryName');
            const repository = getRepositoryByName(i_UsersList[userName].m_RepositoriesDataList, repositoryName);
            setRepositoryData(repository);
            repositoryObject = repository;
        }
    });
}

function isBranchExist(branchName) {
    let branch = repositoryObject.m_BranchesNamesList.find(branch => branch === branchName);
    return branch === branchName;
}

function postBranchFunctionsData(dataToPost) {
    $.ajax({
        url: BRANCH_URL,
        data:dataToPost,
        contentType: "application/json; charset=utf-8",
        dataType: "json",
        success: function () {

        }
    })
}

function postCheckout() {
    ajaxRepository();
    const branchVal = $('#checkout-button').val();
    if (isBranchExist(branchVal)) {
        const data = {
            "functionName": "checkout",
            "repositoryName": repositoryName,
            "branchName": branchVal
        };
        postBranchFunctionsData(data);
        setInterval(ajaxRepository, refreshRepositoryRate);
    } else {

    }

}

function postBranch() {

}

function getRepositoryByName(repositoriesList, repositoryName) {
    return repositoriesList.find(repository => repository.m_RepositoryName === repositoryName);
}

function setRepositoryData(repository) {
    repositoryName = repository.m_RepositoryName;
    $("#repository-name-label").empty();
    $("#repository-name-label").append('<h3 class="display-4">' + repository.m_RepositoryName + '</h3>')
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


$(function () {
    setInterval(ajaxRepository, refreshRepositoryRate);
});