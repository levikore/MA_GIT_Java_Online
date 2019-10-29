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
        String originRepositoryName = request.getParameter("originRepositoryName");
        String originRepositoryUserName = request.getParameter("originRepositoryUserName");
        String newRepositoryName = request.getParameter("newRepositoryName");
        String time = request.getParameter("time");

        RepositoryManager originRepository = appManager.GetRepositoryByName(originRepositoryUserName, originRepositoryName);


        if (originRepository != null) {
            if (functionName.equals("fork")) {
                appManager.HandleClone(originRepositoryUserName, originRepositoryName, username, newRepositoryName);
                appManager.GetAllUserMap().get(originRepositoryUserName).AppendNewNotification(time, username + " forked your repository: " + originRepositoryName);
            }
        }
        //appManager.GetUserData(username).UpdateSpecificRepositoryData(repository, null);
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
