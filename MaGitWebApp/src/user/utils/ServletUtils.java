package user.utils;

//import engine.chat.ChatManager;
import engine.AppManager;
import engine.users.ConnectedUsersManager;

import javax.servlet.ServletContext;

//import static chat.constants.Constants.INT_PARAMETER_ERROR;

public class ServletUtils {

	private static final String USER_MANAGER_ATTRIBUTE_NAME = "userManager";
	private static final String APP_MANAGER_ATTRIBUTE_NAME = "appManager";

	/*
	Note how the synchronization is done only on the question and\or creation of the relevant managers and once they exists -
	the actual fetch of them is remained un-synchronized for performance POV
	 */
	private static final Object userManagerLock = new Object();
	private static final Object appManagerLock = new Object();

	public static ConnectedUsersManager getUserManager(ServletContext servletContext) {

		synchronized (userManagerLock) {
			if (servletContext.getAttribute(USER_MANAGER_ATTRIBUTE_NAME) == null) {
				servletContext.setAttribute(USER_MANAGER_ATTRIBUTE_NAME, new ConnectedUsersManager());
			}
		}
		return (ConnectedUsersManager) servletContext.getAttribute(USER_MANAGER_ATTRIBUTE_NAME);
	}

	public static AppManager getAppManager(ServletContext servletContext, String userName) {
		synchronized (appManagerLock) {
			if (servletContext.getAttribute(APP_MANAGER_ATTRIBUTE_NAME) == null) {
				servletContext.setAttribute(APP_MANAGER_ATTRIBUTE_NAME, new AppManager(userName));
			}
		}
		return (AppManager) servletContext.getAttribute(APP_MANAGER_ATTRIBUTE_NAME);
	}
//
//	public static int getIntParameter(HttpServletRequest request, String name) {
//		String value = request.getParameter(name);
//		if (value != null) {
//			try {
//				return Integer.parseInt(value);
//			} catch (NumberFormatException numberFormatException) {
//			}
//		}
//		return INT_PARAMETER_ERROR;
//	}
}
