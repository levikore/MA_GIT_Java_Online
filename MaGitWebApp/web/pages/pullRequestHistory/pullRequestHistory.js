const PR_LIST_URL = buildUrlWithContextPath("pullRequests");
const COLLABORATION_URL = buildUrlWithContextPath("collaboration");
const refreshPRListRate = 2000;
let PRList = null;

let focusedElementId = null;
const textAreaContentList = [];

function ajaxPRList() {
    updateBackupContent();
    $.ajax({
        url: PR_LIST_URL,
        success: function (i_PRList) {
            if (i_PRList !== undefined) {
                PRList = i_PRList;
                const id = $("#pr-list");
                setPRList(i_PRList, id);
                recoverPrevData();
            }
        }
    });


}

function recoverPrevData() {
    if (document.getElementById(focusedElementId) !== null) {
        document.getElementById(focusedElementId).focus();
        focusedElementId = null;
    }

    if (PRList !== null) {
        for (let i = 0; i < PRList.length; i++) {
            if (PRList[i].m_IsOpen) {
                $('#pr-reject-message-textarea' + i).val(textAreaContentList[i]);
            }
        }
    }
}

function updateBackupContent() {
    if (document.activeElement !== null) {
        focusedElementId = document.activeElement.id;
    }

    if (PRList !== null) {
        for (let i = 0; i < PRList.length; i++) {
            if (PRList[i].m_IsOpen) {
                textAreaContentList[i] = $('#pr-reject-message-textarea' + i).val();
            }
        }
    }
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
            $('<a onclick="handlePRClick(\'' + i + '\')" class="list-group-item list-group-item-action align-items-start"> </a>')
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
            '<div id="open-pr' + i + '\">'+
            '</div>'+
            '</div>')
            .appendTo($("#pr-element" + i));

        if (i_PRList[i].m_IsOpen) {
            $(
                '<button class="btn btn-success" id="btn-Accept' + i + '\"  onclick="handleAccept(\'' + i + '\')">Accept</button>' +
                '<button class="btn btn-danger" id="btn-Reject' + i + '\" onclick="handleReject(\'' + i + '\')">Reject</button>' +
                '<label id="pr-reject-message-label" class="control-label">Reject Message</label>' +
                '<textarea class="form-control" cols="10" rows="5" id="pr-reject-message-textarea' + i + '\">' +
                '</textarea>').appendTo($("#open-pr" + i));
        }
    }

}

function handlePRClick(i) {
sessionStorage.setItem("PRDeltaCommitsList",JSON.stringify(PRList[i].m_CommitsDeltaList))
    return PopupCenter('prDelta/prDelta.html','PR Delta','800','500')
}

function handleReject(i) {
    const rejectMessageInput = $('#pr-reject-message-textarea' + i).val();
    const pullRequest = PRList[i];
    const data = {
        "index": i,
        "time": getCurrentTime(),
        "repositoryName": pullRequest.m_RepositoryName,
        "askingUserName": pullRequest.m_AskingUserName,
        "targetBranchName": pullRequest.m_TargetBranchName,
        "baseBranchName": pullRequest.m_BaseBranchName,
        "rejectionComment": rejectMessageInput,
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

function post(data) {
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


$(function () {
    setInterval(ajaxPRList, refreshPRListRate);
    let userName = "";
    if (sessionStorage.getItem("userName") !== null && sessionStorage.getItem("userName") !== "") {
        userName = sessionStorage.getItem("userName");
    }

    setUserName(userName);
});
