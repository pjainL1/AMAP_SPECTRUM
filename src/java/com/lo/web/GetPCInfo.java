package com.lo.web;

import com.lo.config.Confs;
import com.spinn3r.log5j.Logger;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * This Servlet is to return a CSV file of Postal code previously saved on disk.
 *
 * @author Charles St-Hilaire for Korem inc.
 */
@WebServlet(name = "getPCInfo", urlPatterns = {"/getPCInfo.safe"})
public class GetPCInfo extends HttpServlet {

    private static final Logger LOG = Logger.getLogger();
    private static final String FILE_NAME_PREFIX = "postalcode-%s.csv";
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doIt(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doIt(request, response);
    }

    private void doIt(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (request != null && response != null) {
            HttpSession hs = request.getSession();
            if (hs != null) {
                String fullPath = (String) hs.getAttribute(Confs.STATIC_CONFIG.tradeareaCsvDownloadSessionAttrPath());
                if (fullPath != null) {
                    File csv = new File(fullPath);
                    if (csv.exists()) {
                        ServletOutputStream sos = response.getOutputStream();
                        ServletContext context = getServletConfig().getServletContext();
                        String mimetype = context.getMimeType(fullPath);

                        if (mimetype == null) {
                            mimetype = "application/octet-stream";
                        }
                        response.setContentType(mimetype);
                        response.setContentLength((int) csv.length());
                        String fileName = csv.getName();
                        
                        DateFormat dateFormatter = new SimpleDateFormat(DATE_FORMAT);
                        Date date = new Date();
                        
                        response.setHeader("Content-Disposition", "attachment; filename=\"" + String.format(FILE_NAME_PREFIX, dateFormatter.format(date)) + "\"");

                        byte[] buffer = new byte[1024];
                        int lg;
                        DataInputStream dis = new DataInputStream(new FileInputStream(csv));
                        while ((lg = dis.read(buffer)) != -1) {
                            sos.write(buffer, 0, lg);
                        }

                        dis.close();
                        sos.close();
                        if (csv.delete()) {
                            request.getSession().removeAttribute(Confs.STATIC_CONFIG.tradeareaCsvDownloadSessionAttrPath());
                        } else {
                            LOG.error("Unable to delete: %s", request.getSession().getAttribute(Confs.STATIC_CONFIG.tradeareaCsvDownloadSessionAttrPath()));
                        }
                    } else {
                        throw new ServletException("Postal Code CSV File do not exist.");
                    }
                } else {
                    throw new ServletException("Unable to find CSV file path.");
                }
            } else {
                throw new ServletException("Invalid Session object.");
            }
        } else {
            throw new IllegalArgumentException("Invalid request and/or response object(s).");
        }
    }
}
