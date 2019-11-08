function postPullRequest() {
    const localRepository = JSON.parse(sessionStorage.getItem("repository"));

    const data = {
        "localUsername": localRepository.m_Owner,
        "localRepositoryName": localRepository.m_RepositoryName,
        //"baseBranch":
        "functionName": "pullRequest"
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