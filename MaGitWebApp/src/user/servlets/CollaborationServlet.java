package user.servlets;

import engine.AppManager;
import engine.logic.CollaborationManager;
import engine.logic.RepositoryManager;
import user.constants.Constants;
import user.utils.ServletUtils;
import user.utils.SessionUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Path;

import static user.constants.Constants.*;
import static user.constants.Constants.BRANCH_NAME_PARAMETER;

@WebServlet(name = "CollaborationServlet", urlPatterns = {"/collaboration"})

public class CollaborationServlet extends HttpServlet {
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
//    const data = {
//        "originRepositoryName": parametersData.repositoryName,
//                "originRepositoryUserName":parametersData.username,
//                "functionName": "fork",
//                "newRepositoryName": newRepositoryName
//    };
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = SessionUtils.getUsername(request);
        AppManager appManager = ServletUtils.getAppManager(getServletContext(), username);

        if (username == null) {
            response.sendRedirect(SIGN_UP_URL);
        }

        String functionName = request.getParameter("functionName");
        String errorsString = "";

        if (functionName != null) {
            if (functionName.equals("fork")) {
                handleForkRequest(request, username, appManager);
            } else if (functionName.equals("pull")) {
                errorsString = handlePullRequest(request, appManager);
            } else if (functionName.equals("push")) {
                errorsString = handlePushRequest(request, appManager);
            } else if (functionName.equals("pushLocalBranch")) {
                handlePushLocalBranchRequest(request, appManager);
            } else if (functionName.equals("pullRequest")) {
                errorsString= handlePullRequestRequest(request, appManager);
            } else if (functionName.equals("acceptPullRequest")) {
                handleAcceptPullRequest(request, appManager);
            }else if(functionName.equals("rejectPullRequest")){
                handleRejectPullRequest(request, appManager);
            }
        }

        if (errorsString != null && !errorsString.equals("")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, errorsString);
        }
    }

    private void handleRejectPullRequest(HttpServletRequest request, AppManager i_AppManager){
        String index = request.getParameter("index");
        String time = request.getParameter("time");
        String repositoryName = request.getParameter("repositoryName");
        String userName = SessionUtils.getUsername(request);
        String askingUserName = request.getParameter("askingUserName");
        String targetBranchName = request.getParameter("targetBranchName");
        String baseBranchName = request.getParameter("baseBranchName");
        String rejectionComment = request.getParameter("rejectionComment");

        i_AppManager.HandleRejectPullRequest(index, time, repositoryName, userName, askingUserName, targetBranchName, baseBranchName, rejectionComment);
    }

    private void handleAcceptPullRequest(HttpServletRequest request, AppManager i_AppManager) {
        String index = request.getParameter("index");
        String time = request.getParameter("time");
        String repositoryName = request.getParameter("repositoryName");
        String userName = SessionUtils.getUsername(request);
        String askingUserName = request.getParameter("askingUserName");
        String targetBranchName = request.getParameter("targetBranchName");
        String baseBranchName = request.getParameter("baseBranchName");

        i_AppManager.HandleAcceptPullRequest(index, time, repositoryName, userName, askingUserName, targetBranchName, baseBranchName);


    }

    private void handlePushLocalBranchRequest(HttpServletRequest request, AppManager i_AppManager) {
        String localUsername = request.getParameter("localUsername");
        String localRepositoryName = request.getParameter("localRepositoryName");
        RepositoryManager localRepository = i_AppManager.GetRepositoryByName(localUsername, localRepositoryName);

        if (localRepository.GetRemoteReference() != null) {
            i_AppManager.HandlePushLocalBranch(localUsername, localRepositoryName);
        }
    }

    private String handlePushRequest(HttpServletRequest request, AppManager i_AppManager) {
        String localUsername = request.getParameter("localUsername");
        String localRepositoryName = request.getParameter("localRepositoryName");
        String errors = "";

        RepositoryManager localRepository = i_AppManager.GetRepositoryByName(localUsername, localRepositoryName);

        if (localRepository.GetRemoteReference() != null) {
            errors = i_AppManager.HandlePush(localUsername, localRepositoryName);
        }

        return errors;
    }

    private String handlePullRequest(HttpServletRequest request, AppManager i_AppManager) {
        String localUsername = request.getParameter("localUsername");
        String localRepositoryName = request.getParameter("localRepositoryName");
        String errors = "";

        RepositoryManager localRepository = i_AppManager.GetRepositoryByName(localUsername, localRepositoryName);
        if (localRepository.GetRemoteReference() != null) {
            errors = i_AppManager.HandlePull(localUsername, localRepositoryName);
        }

        return errors;
    }

    private void handleForkRequest(HttpServletRequest request, String i_UserName, AppManager i_AppManager) {
        String originRepositoryName = request.getParameter("originRepositoryName");
        String originRepositoryUserName = request.getParameter("originRepositoryUserName");
        String newRepositoryName = request.getParameter("newRepositoryName");
        String time = request.getParameter("time");

        RepositoryManager originRepository = i_AppManager.GetRepositoryByName(originRepositoryUserName, originRepositoryName);

        if (originRepository != null) {
            i_AppManager.HandleClone(originRepositoryUserName, originRepositoryName, i_UserName, newRepositoryName);
            i_AppManager.GetAllUserMap().get(originRepositoryUserName).AppendNewNotification(time, i_UserName + " forked your repository: " + originRepositoryName);
        }
    }

    private String handlePullRequestRequest(HttpServletRequest request, AppManager i_AppManager) {
        String localUsername = request.getParameter("localUsername");
        String localRepositoryName = request.getParameter("localRepositoryName");
        String baseBranchName = request.getParameter("baseBranchName");
        String targetBranchName = request.getParameter("targetBranchName");
        String message = request.getParameter("message");
        String time = request.getParameter("time");
        String errors ="";

        RepositoryManager localRepository = i_AppManager.GetRepositoryByName(localUsername, localRepositoryName);

        if (localRepository.GetRemoteReference() != null) {
            errors = i_AppManager.HandlePullRequest(localUsername, localRepositoryName, baseBranchName, targetBranchName, message, time);
            // i_AppManager.GetAllUserMap().get(originRepositoryUserName).AppendNewNotification(time, i_UserName + " forked your repository: " + originRepositoryName);
        }

        return errors;
    }


    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
