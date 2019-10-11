const userNameRefreshRate = 200; //milli seconds
const USER_LIST_URL = buildUrlWithContextPath("userslist");
const USER_URL = buildUrlWithContextPath("user");

function ajaxUserData() {
    $.ajax({
        url: USER_URL,
        success: function (i_UserData) {
            //var userData= JSON.parse(i_UserData);
            let userName = i_UserData.m_UserName;
            setUserName(userName);
            if (i_UserData.m_RepositoriesDataList !== undefined) {
                const id=$("#user-repositories-list");
                setUserRepositoriesList(i_UserData.m_RepositoriesDataList, id, userName);
            }
        }
    });
}

function setUserName(userName) {
    $("#userName").append(userName).append("<br>");
}

function setUserRepositoriesList(repositoriesDataList, id, usetName) {
    for (let i = 0; i < repositoriesDataList.length; i++) {
        let repositoryName = repositoriesDataList[i].m_RepositoryName;
        let activeBranchName = repositoriesDataList[i].m_ActiveBranchName;
        let numberOfBranches = repositoriesDataList[i].m_NumOfBranches;
        let lastCommitComment = repositoriesDataList[i].m_LastCommitComment;
        let lastCommitDate = repositoriesDataList[i].m_LastCommitDate;

        id.append(
                $('<a href="#" class="list-group-item list-group-item-action align-items-start"> </a>').attr('id', usetName+"repository-element" + i)
            );

        $('<div class="d-flex w-100 justify-content-between">' +
            '<h3 class="mb-1">'
            +
            repositoryName
            +
            '</h3>' +
            '<p class="mb-1">Active Branch: '+activeBranchName+'</p>'+
            '<p class="mb-1">Number of Branches: '+numberOfBranches+'</p>'+
            '<p class="mb-1">Last Commit Comment: '+lastCommitComment+'</p>'+
            '<p class="mb-1">Last Commit Date: '+lastCommitDate+'</p>'+
        '</div>')
            .appendTo($("#" + usetName+"repository-element" + i));
    }
}

$(function () {
    ajaxUserData();
});