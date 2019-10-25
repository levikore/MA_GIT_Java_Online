package user.constants;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Constants {
        public final static Path REPOSITORIES_FOLDER_PATH= Paths.get("c:\\magit-ex3");
        public static final String USERNAME = "username";
        public static final String APPNAME="app";
        public static final String USER_NAME_ERROR = "username_error";

        public static final String CHAT_PARAMETER = "userstring";
        public static final String CHAT_VERSION_PARAMETER = "chatversion";

        public static final String HOME_ROOM_URL = "../home/home.html";
        public static final String SIGN_UP_URL = "../signup/signup.html";
        public static final String LOGIN_ERROR_URL = "/pages/loginerror/login_attempt_after_error.jsp";

        public static final int INT_PARAMETER_ERROR = Integer.MIN_VALUE;

        public static final String FUNCTION_NAME_PARAMETER = "functionName";
        public static final String REPOSITORY_NAME_PARAMETER="repositoryName";
        public static final String BRANCH_NAME_PARAMETER = "branchName";
}
