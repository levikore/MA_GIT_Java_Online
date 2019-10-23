package user.servlets;

import engine.AppManager;
import engine.logic.RepositoryManager;
import user.utils.ServletUtils;
import user.utils.SessionUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static user.constants.Constants.*;

@WebServlet(name = "BranchServlet", urlPatterns = {"/branch"})
public class BranchServlet extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = SessionUtils.getUsername(request);
        AppManager appManager = ServletUtils.getAppManager(getServletContext(), username);

        if (username == null) {
            response.sendRedirect(SIGN_UP_URL);
        }

        String functionNameString = request.getParameter(FUNCTION_NAME_PARAMETER);
        String repositoryNameString = request.getParameter(REPOSITORY_NAME_PARAMETER);
        String branchNameString = request.getParameter(BRANCH_NAME_PARAMETER);
        RepositoryManager repository = appManager.GetRepositoryByName(username, repositoryNameString);

        if (repository != null) {
            if (functionNameString.equals("branch")) {
                repository.HandleBranch(branchNameString, repository.GetHeadBranch().GetBranch().GetCurrentCommit(), null);
            } else if (functionNameString.equals("checkout")) {
                repository.HandleCheckout(branchNameString);
            }
        }
        appManager.GetUserData(username).UpdateSpecificRepositoryData(repository,null);
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


