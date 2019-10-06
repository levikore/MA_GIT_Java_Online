const refreshRate = 2000; //milli seconds
const userNameRefreshRate = 200; //milli seconds
const USER_LIST_URL = buildUrlWithContextPath("userslist");
const USER_URL = buildUrlWithContextPath("user");

function ajaxUsersName() {
    $.ajax({
        url: USER_URL,
        success: function(userName) {
            setUserName(userName);
        }
    });
}

function setUserName(userName){
    $("#userName").append(userName).append("<br>");
   if(userName==="")
   {
       setTimeout(ajaxUsersName, refreshRate);
   }
}

$(function() {
     setTimeout(ajaxUsersName, userNameRefreshRate);

});