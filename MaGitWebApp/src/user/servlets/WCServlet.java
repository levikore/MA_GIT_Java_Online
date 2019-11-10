package user.servlets;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import engine.AppManager;
import engine.logic.RepositoryManager;
import engine.logic.UnCommittedChange;
import engine.repositories.RepositoryData;
import engine.logic.FilesManagement;
import user.utils.ServletUtils;
import user.utils.SessionUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static user.constants.Constants.*;


@WebServlet(name = "WCServlet", urlPatterns = {"/WC"})
public class WCServlet extends HttpServlet {
    private class Data {
        String action;
        String path;
        String content;

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }


    }

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    protected synchronized void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String userName = SessionUtils.getUsername(request);
        AppManager appManager = ServletUtils.getAppManager(getServletContext(), userName);
        if (userName == null) {
            response.sendRedirect(SIGN_UP_URL);
        }
        String json = request.getParameter("openChanges");
        String repositoryName = request.getParameter("repositoryName");
        String currentWCFilesListName= request.getParameter("currentWCFilesList");

        JsonArray openChangesArray = new JsonParser().parse(json).getAsJsonArray();
        JsonArray currentWCFilesList = new JsonParser().parse(currentWCFilesListName).getAsJsonArray();

        appManager.ChangeFiles(openChangesArray, repositoryName, userName);
        RepositoryManager repository = appManager.GetRepositoryByName(userName, repositoryName);
        appManager.GetUserData(userName).UpdateSpecificRepositoryData(repository,currentWCFilesList);

    }

//        while(request.getParameterNames().hasMoreElements())
//        {
//            path
//        }

//        String functionNameString = request.getParameter(FUNCTION_NAME_PARAMETER);
//        String repositoryNameString = request.getParameter(REPOSITORY_NAME_PARAMETER);
//        String branchNameString = request.getParameter(BRANCH_NAME_PARAMETER);
//        RepositoryManager repository = appManager.GetRepositoryByName(username, repositoryNameString);
//
//        if (repository != null) {
//            if (functionNameString.equals("branch")) {
//                repository.HandleBranch(branchNameString, repository.GetHeadBranch().GetBranch().GetCurrentCommit(), null);
//            } else if (functionNameString.equals("checkout")) {
//                repository.HandleCheckout(branchNameString);
//            }
//        }
//        appManager.GetUserData(username).UpdateSpecificRepositoryData(repository);

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
    }// </editor-
}



