package user.servlets;

import com.google.gson.Gson;
import engine.repositories.Notification;
import engine.repositories.UserData;
import engine.repositories.UserNameObj;
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

@WebServlet(name = "NotificationsServlet", urlPatterns = {"/notifications"})
public class NotificationsServlet extends HttpServlet {
    private void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        try (PrintWriter out = response.getWriter()) {
            Gson gson = new Gson();
            String userName = SessionUtils.getUsername(request);
            UserData userData = ServletUtils.getAppManager(getServletContext(), userName).GetUserData(userName);
            List<Notification> notifications = null;
            if (userData != null) {
                notifications = userData.GetNewNotifications();
                NotificationsData notificationsData = new NotificationsData(notifications, userData.GetNotificationsVersion(), userData.GetLastNotificationsVersionSeen());
                String json = gson.toJson(notificationsData);
                //String json = gson.toJson(userName);
                out.println(json);
                out.flush();
            }

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

    private class NotificationsData {
        List<Notification> m_Notifications;
        int m_Version;
        int m_LastVersionSeen;

        private NotificationsData(List<Notification> i_Notifications, int i_Version, int i_LastVersionSeen) {
            m_Notifications = i_Notifications;
            m_Version = i_Version;
            m_LastVersionSeen = i_LastVersionSeen;
        }

    }
}

