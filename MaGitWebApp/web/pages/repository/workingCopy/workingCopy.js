let lastIndexSelected = -1;

function setFilesList() {
    //users-list
    let repository = JSON.parse(sessionStorage.getItem("repository"));
    $("#files-list").empty();
    $("#files-content-list").empty();

    for (let i = 0; i < repository.m_CurrentWCFilesList.length; i++) {
        appendFileToFilesList(i);

         let fileElementId = $("#file" + i);
         let fileContentId = $("#file" + i + "-content");

        fileElementId.on(
            'click',
            function () {
                handleFileElementClick(fileElementId, fileContentId);
            }
        );

        // if (lastIndexSelected === i) {
        //     fileElementId.toggleClass('active');
        //     fileContentId.toggleClass('show');
        //     fileContentId.toggleClass('active');
        // }
    }

}


function handleFileElementClick(fileElementId, fileContentId) {
    fileElementId.toggleClass('active');
    fileContentId.toggleClass('show');
    fileContentId.toggleClass('active');

    if (fileElementId.hasClass("active")) {
        if (lastIndexSelected != -1) {
            $("#file" + lastIndexSelected).toggleClass('active')

            $("#file" + lastIndexSelected + "-content").toggleClass('show')
            $("#file" + lastIndexSelected + "-content").toggleClass('active')

        }
        lastIndexSelected = parseInt(fileElementId.attr('key'));
    } else if (!fileElementId.hasClass("active")) {
        lastIndexSelected = -1;
    }

}

function appendFileToFilesList(index) {
    let repository = JSON.parse(sessionStorage.getItem("repository"));
    let shortPath = repository.m_CurrentWCFilesList[index].m_Path.substring(repository.m_CurrentWCFilesList[index].m_Path.indexOf(repository.m_RepositoryName));

    $("#files-list")
        .append($(
            '<a class="list-group-item list-group-item-action" data-toggle="tab" role="tab" aria-selected="false">' + shortPath + '</a>')
            .attr({
                'id': "file" + index,
                'href': "#file" + index + "-content",
                'key': index,
                'aria-controls': "file" + index + "-content"
            }));

    $("#files-content-list")
        .append($(
            '<div class="tab-pane fade" role="tabpanel" >'
            + '</div>'
        ).attr({
            'id': "file" + index + "-content",
            'aria-controls': "file" + index
        }));

    $("#file" + index + "-content").append(
        '<form>'
        + '<div class="form-group" id=\"file' + index + "-content-textarea-wrapper"+ '\">'
        + '<label >File content</label>'
        + '<textarea readonly class="form-control" cols="100" rows="30" id=\"file'+index+"-content-textarea"+'\"></textarea>'
        + '</div>'
        + '</form>'
    )

    $('#file'+index+'-content-textarea').append(
        repository.m_CurrentWCFilesList[index].m_Content
    )
}

$(function () {
    setFilesList();
});