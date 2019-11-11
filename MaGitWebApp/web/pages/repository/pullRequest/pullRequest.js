const COLLABORATION_URL = buildUrlWithContextPath("collaboration");
const repository = JSON.parse(sessionStorage.getItem("repository"));

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

    let errors = getInputErrors(baseInput, targetInput);

    if (errors === "") {
        $.ajax({
            url: COLLABORATION_URL,
            data: data,
            contentType: "application/json; charset=utf-8",
            dataType: "json",
            success: function () {

            }
        });
        if ($("#errors").length > 0) {
            $("#errors").remove();
        }

        pullRequestWrapper.append('<p id="success">success</p>')
    } else {
        if ($("#success").length > 0) {
            $("#success").remove();
        }

        if ($("#errors").length > 0) {
            $("#errors").remove();
        }

        pullRequestWrapper.append('<p id="errors">' + errors + '</p>')
    }
}

function getInputErrors(baseBranchName, targetBranchName) {
    let errors = "";
    if (!isBranchExist(baseBranchName)) {
        errors = errors.concat("base branch does not exist, ");
    }

    if (!isBranchExist(targetBranchName)) {
        errors = errors.concat("\ntarget branch does not exist, ");
    }

    if (isBranchExist(targetBranchName) && !isBranchModifiable(targetBranchName)) {
        errors = errors.concat("\ntarget branch must be localy created and pushed, ");
    }

    if (isBranchExist(targetBranchName) && !isBranchRTB(baseBranchName)) {
        errors = errors.concat("\nbase branch must exist in remote (write only name of branch), ");
    }

    return errors;
}

function isBranchExist(branchName) {
    let branch = repository.m_BranchesNamesList.find(branch => branch === branchName);
    return branch === branchName;
}

function isBranchModifiable(branchName) {
    let branchData = repository.m_BranchesList.find(branch => branch.m_BranchName === branchName);
    return branchData.m_IsModifiable;
}

function isBranchRTB(branchName) {
    let branchData = repository.m_BranchesList.find(branch => branch.m_BranchName === branchName);
    return branchData.m_TrackingAfter !== "none";

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