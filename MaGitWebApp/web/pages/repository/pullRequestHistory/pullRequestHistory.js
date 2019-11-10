const PR_LIST_URL=buildUrlWithContextPath("pullRequests");
const refreshPRListRate=2000;
function ajaxPRList() {
    $.ajax({
        url:PR_LIST_URL,
        success: function (i_PRList) {
            if (i_PRList !== undefined) {
                const id = $("#pr-list");
                setPRList(i_PRList, id);
            }
        }
    });
}

function setUserName(userName) {
    $("#userName").append(userName);
}


function setPRList(i_PRList, i_PRListId)
{
    i_PRListId.empty();

    for (let i = 0; i < i_PRList.length; i++) {

        /*private String m_RepositoryName;
        private String m_AskingUserName;
        private String m_TargetBranchName;
        private String m_BaseBranchName;
        private String m_Description;
        private Boolean m_IsOpen = true;
        private Boolean m_IsRejected = false;
        private String m_RejectionDescription ="";*/

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

        if(!isOpen && !isRejected){
            status = "Approved"
        }else if(isRejected){
            status = "Rejected";
        }else {
            status="Open";
        }



        i_PRListId.append(
            $('<a href="#" class="list-group-item list-group-item-action align-items-start"> </a>')
                .attr({
                    'id':"pr-element" + i//,
                 //   'href': new_url
                })
        );

        $('<div class="w-100 justify-content-between">' +
            '<h3 class="mb-1">Repository Name: '+ repositoryName + '</h3>' +
            '<p class="mb-1">User Name: ' + askingUserName + '</p>' +
            '<p class="mb-1">Target Branch: ' + targetBranchName + '</p>' +
            '<p class="mb-1">Base Branch: ' + baseBranchName + '</p>' +
            '<p class="mb-1">Creation Date: ' + time + '</p>' +
            '<p class="mb-1">Status: ' + status + '</p>' +

            '</div>')
            .appendTo($("#pr-element" + i));
    }

}


$(function () {
    setInterval(ajaxPRList, refreshPRListRate);
    let userName="";
    if(sessionStorage.getItem("userName")!==null&&sessionStorage.getItem("userName")!=="")
    {
        userName=sessionStorage.getItem("userName");
    }

    setUserName(userName);
});
