let lastIndexSelected = -1;

$(function () {
    const commitsDeltaList = JSON.parse(sessionStorage.getItem("PRDeltaCommitsList"));
    setCommitsDeltaList(commitsDeltaList);
});

function setCommitsDeltaList(commitsDeltaList) {
    //users-list
    for (let i = 0; i < commitsDeltaList.length; i++) {
        let commitFilesList = commitsDeltaList[i].m_FilesDeltaList;

        appendCommitFilesList(i, commitsDeltaList[i].m_CommitSha1, commitsDeltaList[i].m_CommitComment, commitsDeltaList[i].m_DataCreated, commitsDeltaList[i].m_CreatedBy);

        let prDeltaCommitElementId = $("#pr-delta-commit-element" + i);
        let prDeltaCommitElementContentId = $("#pr-delta-commit-element" + i + "-content" + i);

        prDeltaCommitElementId.on(
            'click',
            function () {
                handleCommitElementClick(prDeltaCommitElementId, prDeltaCommitElementContentId);
            }
        );

        setCommitFilesList(commitFilesList, prDeltaCommitElementContentId, i);

    }
}

function handleCommitElementClick(prDeltaCommitElementId, prDeltaCommitElementContentId) {
    prDeltaCommitElementId.toggleClass('active');
    prDeltaCommitElementContentId.toggleClass('show');

    if (prDeltaCommitElementId.hasClass("active")) {
        if (lastIndexSelected !== -1) {
            $("#pr-delta-commit-element" + lastIndexSelected).toggleClass('active')

            $("#pr-delta-commit-element" + lastIndexSelected + "-content" + lastIndexSelected).toggleClass('show')
        }
        lastIndexSelected = parseInt(prDeltaCommitElementId.attr('key'));
    } else if (!prDeltaCommitElementId.hasClass("active")) {
        lastIndexSelected = -1;
    }

}


function appendCommitFilesList(commitIndex, commitSha1, commitComment, dateCreated, createdBy) {
    $("#delta-commits-list")
        .append(
            $('<a class="list-group-item list-group-item-action" data-toggle="list" role="tab" >'
                + '<h3 class="mb-8">' + commitSha1 + '</h3>' +
                '<p class="mb-1">Commit Comment: ' + commitComment + '</p>' +
                '<p class="mb-1">Date Created: ' + dateCreated + '</p>' +
                '<p class="mb-1">Created By: ' + createdBy + '</p>' +
                '</a>')
                .attr({
                    'id': "pr-delta-commit-element" + commitIndex,
                    'href': "#pr-delta-commit-element" + commitIndex + "-content" + commitIndex,
                    'key': commitIndex
                }));

    $("#delta-commits-list-content")
        .append(
            $('<div class="list-group tab-pane" role="tabpanel"> ' +
                '</div>').attr({
                'id': "pr-delta-commit-element" + commitIndex + "-content" + commitIndex,
                'aria-labelledby': "pr-delta-commit-element" + commitIndex

            }));
}

function setCommitFilesList(commitFilesList, id, commitIndex) {
    for (let i = 0; i < commitFilesList.length; i++) {
        let changeType = commitFilesList[i].m_ChangeType;
        let file = commitFilesList[i].m_fileContent;
        let path = file.m_Path;
        let content= file.m_Content = "" || file.m_Content == null ? "Deleted" : file.m_Content;
        let isFolder= file.m_IsFolder;

        id.append(
            $('<a class="list-group-item list-group-item-action align-items-start"> </a>')
                .attr({
                    'id': "commit"+commitIndex + "file-element" + i
                })
        );

        $('<div class="w-100 justify-content-between">' +
            '<h3 class="mb-1">' + path + '</h3>' +
            '<p class="mb-1">Status: ' + changeType + '</p>' +
            '<p class="mb-1">Content: </p>' +
            '<p class="mb-1"> ' + content + '</p>' +
            '</div>')
            .appendTo($("#commit"+commitIndex + "file-element" + i));
    }
}