const COLLABORATION_URL = buildUrlWithContextPath("collaboration");

function postPullRequest() {
    const baseInput = $('#Base-input').val();
    const targetInput = $('#Target-input').val();
    const messageInput = $('#pr-message-textarea').val();
    const localRepository = JSON.parse(sessionStorage.getItem("repository"));
    const pullRequestWrapper = $('#pull-request-wrapper');
    const pullRequestErrorSign = $('#pull-request-error-sign');
    const errorString = $('#pr-error-string');
    const successString = $('#pr-success-string');
    const data = {
        "localUsername": localRepository.m_Owner,
        "localRepositoryName": localRepository.m_RepositoryName,
        "baseBranchName": baseInput,
        "targetBranchName": targetInput,
        "message": messageInput,
        "functionName": "pullRequest",
        "time": getCurrentTime()
    }

    $.ajax({
        url: COLLABORATION_URL,
        data: data,
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

function getCurrentTime() {
    const today = new Date();
    const date = today.getFullYear() + '-' + (today.getMonth() + 1) + '-' + today.getDate();
    const time = today.getHours() + ":" + today.getMinutes() + ":" + today.getSeconds();
    const dateTime = date + ' ' + time;

    return dateTime;
}