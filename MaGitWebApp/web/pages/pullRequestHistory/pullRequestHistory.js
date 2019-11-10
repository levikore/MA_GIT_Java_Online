const PR_LIST_URL = buildUrlWithContextPath("pullRequests");
const COLLABORATION_URL = buildUrlWithContextPath("collaboration");
const refreshPRListRate = 2000;
let PRList = null;


let TENP = "REJECTED!!!"

function ajaxPRList() {
    $.ajax({
        url: PR_LIST_URL,
        success: function (i_PRList) {
            if (i_PRList !== undefined) {
                PRList = i_PRList;
                const id = $("#pr-list");
                setPRList(i_PRList, id);
            }
        }
    });
}

function setUserName(userName) {
    $("#userName").append(userName);
}


function setPRList(i_PRList, i_PRListId) {
    i_PRListId.empty();

    for (let i = 0; i < i_PRList.length; i++) {

        let time = i_PRList[i].m_Time;
        let repositoryName = i_PRList[i].m_RepositoryName;
        let askingUserName = i_PRList[i].m_AskingUserName;
        let targetBranchName = i_PRList[i].m_TargetBranchName;
        let baseBranchName = i_PRList[i].m_BaseBranchName;
        let description = i_PRList[i].m_Description;
        let isOpen = i_PRList[i].m_IsOpen;
        let isRejected = i_PRList[i].m_IsRejected;
        let rejectionDescription = i_PRList[i].m_RejectionDescription;

        let status;

        if (!isOpen && !isRejected) {
            status = "Approved"
        } else if (isRejected) {
            status = "Rejected";
        } else {
            status = "Open";
        }


        i_PRListId.append(
            $('<a href="#" class="list-group-item list-group-item-action align-items-start"> </a>')
                .attr({
                    'id': "pr-element" + i//,
                    //   'href': new_url
                })
        );

        $('<div class="w-100 justify-content-between">' +
            '<h3 class="mb-1">Repository Name: ' + repositoryName + '</h3>' +
            '<p class="mb-1">User Name: ' + askingUserName + '</p>' +
            '<p class="mb-1">Target Branch: ' + targetBranchName + '</p>' +
            '<p class="mb-1">Base Branch: ' + baseBranchName + '</p>' +
            '<p class="mb-1">Creation Date: ' + time + '</p>' +
            '<p class="mb-1">Status: ' + status + '</p>' +
            '<button class="btn btn-success" id="btn-Accept' + i + '\"  onclick="handleAccept(\'' + i + '\')">Accept</button>' +
            '<button class="btn btn-danger" id="btn-Reject' + i + '\" onclick="handleReject(\'' + i + '\', \'' + TENP + '\')">Reject</button>' +
            '</div>')
            .appendTo($("#pr-element" + i));
    }

}

function handleReject(i, rejectionComment) {
    const pullRequest = PRList[i];
    const data = {
        "index": i,
        "time": getCurrentTime(),
        "repositoryName": pullRequest.m_RepositoryName,
        "askingUserName": pullRequest.m_AskingUserName,
        "targetBranchName": pullRequest.m_TargetBranchName,
        "baseBranchName": pullRequest.m_BaseBranchName,
        "rejectionComment": rejectionComment,
        "functionName": "rejectPullRequest"
    }

    post(data);
}

function handleAccept(i) {
    const pullRequest = PRList[i];
    const data = {
        "index": i,
        "time": getCurrentTime(),
        "repositoryName": pullRequest.m_RepositoryName,
        "askingUserName": pullRequest.m_AskingUserName,
        "targetBranchName": pullRequest.m_TargetBranchName,
        "baseBranchName": pullRequest.m_BaseBranchName,
        "functionName": "acceptPullRequest"
    }

    post(data);
}

function post(data){
    $.ajax({
        url: COLLABORATION_URL,
        data: data,
        contentType: "application/json; charset=utf-8",
        dataType: "json",
        success: function () {

        }
    })
}

function getCurrentTime() {
    const today = new Date();
    const date = today.getFullYear() + '-' + (today.getMonth() + 1) + '-' + today.getDate();
    const time = today.getHours() + ":" + today.getMinutes() + ":" + today.getSeconds();
    const dateTime = date + ' ' + time;

    return dateTime;
}


$(function () {
    setInterval(ajaxPRList, refreshPRListRate);
    let userName = "";
    if (sessionStorage.getItem("userName") !== null && sessionStorage.getItem("userName") !== "") {
        userName = sessionStorage.getItem("userName");
    }

    setUserName(userName);
});
