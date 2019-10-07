package user.servlets;

//taken from: http://www.servletworld.com/servlet-tutorials/servlet3/multipartconfig-file-upload-example.html
// and http://docs.oracle.com/javaee/6/tutorial/doc/glraq.html

import engine.AppManager;
import engine.logic.FilesManagement;
import engine.logic.RepositoryManager;
import engine.logic.XMLManager;
import org.xml.sax.SAXException;
import user.utils.ServletUtils;
import user.utils.SessionUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;

@WebServlet("/upload")
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 1024 * 1024 * 5, maxRequestSize = 1024 * 1024 * 5 * 5)
public class FileUploadServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.sendRedirect("fileupload/form.html");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        Collection<Part> parts = request.getParts();
        String errorsContent = "";
        String userName=null;
        InputStream inputStreamOfXML=null;
        AppManager appManager = null;
        for (Part part : parts) {
            inputStreamOfXML = part.getInputStream();
            userName = SessionUtils.getUsername(request);
            errorsContent = getErrorsOfXML(inputStreamOfXML);
        }

        if (!errorsContent.isEmpty()) {
            out.println(errorsContent);
        } else {
            appManager = ServletUtils.getAppManager(getServletContext(), userName);
            appManager.CreateRepositoryFromXml(inputStreamOfXML, userName);
            out.println("the xml contains 0 errors");
        }

    }

    private String getErrorsOfXML(InputStream i_InputStreamOfXML) {
        String errorsContent = "";
        List<String> errors = null;
        try {
            errors = XMLManager.GetXMLFileErrors(i_InputStreamOfXML);
            for (String error : errors) {
                errorsContent = errorsContent.concat(error + "\n");
            }

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return errorsContent;
    }

    public String readFromInputStream(InputStream inputStream) {
        return new Scanner(inputStream).useDelimiter("\\Z").next();
    }

}