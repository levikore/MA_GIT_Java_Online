package user.servlets;

import com.google.gson.Gson;
import engine.repositories.UserData;
import engine.repositories.UserNameObj;
import user.utils.ServletUtils;
import user.utils.SessionUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

public class AllUsersServlet extends HttpServlet {
    private void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        try (PrintWriter out = response.getWriter()) {
            Gson gson = new Gson();

            Map<String, UserData> allusers = ServletUtils.getAppManager(getServletContext(), SessionUtils.getUsername(request)).GetAllUserMap();
            String json = gson.toJson(allusers);
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

    private void logServerMessage(String message){
        System.out.println(message);
    }

//    private static class usersListAndVersion {
//        final private List<SingleChatEntry> entries;
//        final private int version;
//
//        public ChatAndVersion(List<SingleChatEntry> entries, int version) {
//            this.entries = entries;
//            this.version = version;
//        }
//    }


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
