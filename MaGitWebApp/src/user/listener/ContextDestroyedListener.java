package user.listener;

import engine.logic.FilesManagement;
import user.constants.Constants;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class ContextDestroyedListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        System.out.println("My web app is being initialized :)");
    }
    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        FilesManagement.DeleteFolder(Constants.REPOSITORIES_FOLDER_PATH.toString());
        System.out.println("My web app is being destroyed :(");
    }
}
