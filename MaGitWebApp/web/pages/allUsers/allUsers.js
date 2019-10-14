const refreshRate = 2000; //milli seconds
const ALL_USERS_URL = buildUrlWithContextPath("allusers");
let lastIndexSelected = -1;


function ajaxAllUsers() {
    $.ajax({
        url: ALL_USERS_URL,
        //data: "chatversion=" + chatVersion,
        //dataType: 'json',
        success: function (usersList) {
            /*
             data will arrive in the next form:
             {
                "entries": [
                    {
                        "chatString":"Hi",
                        "username":"bbb",
                        "time":1485548397514
                    },
                    {
                        "chatString":"Hello",
                        "username":"bbb",
                        "time":1485548397514
                    }
                ],
                "version":1
             }
             */
            //console.log("Server chat version: " + data.version + ", Current chat version: " + chatVersion);
            // if (data.version !== chatVersion) {
            //     chatVersion = data.version;
            //     appendToChatArea(data.entries);
            // }
            setAllUserList(usersList)

            // triggerAjaxAllUsers();
        },
        error: function (error) {
            //triggerAjaxAllUsers();
        }
    });
}

function setAllUserList(usersList) {
    //users-list
    let currentUserName=$("#userName").text().trim();
    $("#users-list").empty();
    $("#users-list-content").empty();

    const usersKeys = Object.keys(usersList)

    for (const userKey of usersKeys) {
        if (userKey !== currentUserName) {

            let userIndex = usersKeys.indexOf(userKey);
            let repositoriesDataList = usersList[userKey].m_RepositoriesDataList;

            appendUsersList(userIndex, userKey);

            let userElementId = $("#user-element" + userIndex);
            let userContentId = $("#user-element" + userIndex + "-user-content" + userIndex);

            userElementId.on(
                'click',
                function () {
                    handleUserElementClick(userElementId, userContentId);
                }
            );

            setUserRepositoriesList(repositoriesDataList, userContentId, userKey);

            if (lastIndexSelected === userIndex) {
                userElementId.toggleClass('active');
                userContentId.toggleClass('show');
            }
        }
    }
}

function handleUserElementClick(userElementId, userContentId) {
    userElementId.toggleClass('active');
    userContentId.toggleClass('show');

    if (userElementId.hasClass("active")) {
        if (lastIndexSelected != -1) {
            $("#user-element" + lastIndexSelected).toggleClass('active')

            $("#user-element" + lastIndexSelected + "-user-content" + lastIndexSelected).toggleClass('show')
        }
        lastIndexSelected = parseInt(userElementId.attr('key'));
    } else if (!userElementId.hasClass("active")) {
        lastIndexSelected = -1;
    }

}


function appendUsersList(userIndex, userKey) {
    $("#users-list")
        .append(
            $('<a class="list-group-item list-group-item-action" data-toggle="list" role="tab" >'
                + '<h3 class="mb-1">' + userKey + '</h3>' + '</a>')
                .attr({
                    'id': "user-element" + userIndex,
                    'href': "#user-element" + userIndex + "-user-content" + userIndex,
                    'key': userIndex
                }));

    $("#users-list-content")
        .append(
            $('<div class="list-group tab-pane" role="tabpanel"> ' +
                '</div>').attr({
                'id': "user-element" + userIndex + "-user-content" + userIndex,
                'aria-labelledby': 'user-element' + userIndex

            }));
}

function triggerAjaxAllUsers() {
    setTimeout(ajaxAllUsers, refreshRate);
}

$(function () {
    setInterval(ajaxAllUsers, refreshRate);

    // triggerAjaxAllUsers();
});

