/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.lo.pdf.struct;

import com.lo.ContextParams;
import com.lo.db.dao.PDFReportDAO;
import com.lo.pdf.PDFBean;
import com.spinn3r.log5j.Logger;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.SocketException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

/**
 *
 * @author slajoie
 */
public class DownloadPDFAction extends org.apache.struts.action.Action {

    private static final Logger log = Logger.getLogger();

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response) throws IOException {

        OutputStream out = response.getOutputStream();

        ContextParams cp = ContextParams.get(request.getSession());
        try {

            PDFBean pdf =  cp.getPdf();
            File file = pdf.getFile();

            if (file.exists() && file.canRead()) {
                response.setContentType("application/pdf");
                response.setHeader("Content-Disposition", "attachment; filename=\"" + pdf.getName() + "\"");
                response.setHeader("Pragma", "cache");
                response.setHeader("Cache-control", "cache");

                response.setContentLength((int) file.length());
                FileInputStream in = new FileInputStream(file);
                byte[] buf = new byte[1024];
                int count = 0;
                while ((count = in.read(buf)) >= 0) {
                    out.write(buf, 0, count);
                }
                in.close();
                
            } else {
                log.error("Error reading generated pdf: " + file.getAbsolutePath());
                response.setContentType("text/html;charset=UTF-8");
                OutputStreamWriter writer = new OutputStreamWriter(out);
                writer.write("File not found");
                writer.close();
            }

        } catch (IOException ex) {
            log.error("Exception caugth.", ex);

        } finally {
            try {
                out.close();
            } catch (IOException ex) {
                if (ex.getCause() != null && ex.getCause() instanceof SocketException) {
                    log.warn("User canceled transfer : " + ex.getCause().getMessage()); // ClientAbortException...
                } else {
                    log.error("Error closing output stream.", ex);
                }
            }
            
            /**
             * Delete PDFReport from persistence
             */
            new PDFReportDAO().delete(cp);
        }
        return null;
    }
}