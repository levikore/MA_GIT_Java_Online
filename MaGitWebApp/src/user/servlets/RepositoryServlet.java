package user.servlets;

import com.google.gson.Gson;
import engine.AppManager;
import engine.logic.RepositoryManager;
import engine.repositories.RepositoryData;
import engine.repositories.UserData;
import user.utils.ServletUtils;
import user.utils.SessionUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import static user.constants.Constants.*;
import static user.constants.Constants.BRANCH_NAME_PARAMETER;

@WebServlet(name = "RepositoryServlet", urlPatterns = {"/repository"})
public class RepositoryServlet extends HttpServlet {
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = SessionUtils.getUsername(request);
        if (username == null) {
            response.sendRedirect(SIGN_UP_URL);
        }
        String userOfRepositoryNameString = request.getParameter(USERNAME);
        String repositoryNameString = request.getParameter(REPOSITORY_NAME_PARAMETER);

        response.setContentType("application/json");
        try (PrintWriter out = response.getWriter()) {
            Gson gson = new Gson();

            Map<String, UserData> allusers = ServletUtils.getAppManager(getServletContext(), SessionUtils.getUsername(request)).GetAllUserMap();
            RepositoryData repositoryData = allusers.get(userOfRepositoryNameString).GetRepositoryDataByName(repositoryNameString);

            String json = gson.toJson(repositoryData);
//            if (userData == null) {
//                UserNameObj userNameObj = new UserNameObj(userName);
//                json = gson.toJson(userNameObj);
//            } else {
//                json = gson.toJson(userData);
//            }
            //String json = gson.toJson(userName);
            out.println(json);
            out.flush();
        }
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
        protected void doGet (HttpServletRequest request, HttpServletResponse response)
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
        protected void doPost (HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
            processRequest(request, response);
        }

        /**
         * Returns a short description of the servlet.
         *
         * @return a String containing servlet description
         */
        @Override
        public String getServletInfo () {
            return "Short description";
        }// </editor-fold>
    }

