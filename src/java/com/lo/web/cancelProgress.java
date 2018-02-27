/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.lo.web;

import com.korem.requestHelpers.GenericServlet;
import com.lo.ContextParams;
import com.lo.db.dao.PDFReportDAO;
import com.lo.db.om.PDFReport;
import com.lo.web.Apply.IParams;
import com.lo.web.Apply.ProgressListener;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author slajoie
 */
public class cancelProgress extends GenericServlet<Apply.IParams> {
   
    @Override
    protected String getJSON(HttpServletRequest req, IParams params) throws Exception {
        String ln = ProgressListener.class.getSimpleName();
        
        // find listerner name
        ContextParams cp = ContextParams.get(req.getSession());
        PDFReport pr = new PDFReportDAO().find(cp);
        if (pr != null && pr.getProcessKey() != null) {
            ln = pr.getProcessKey().getName();
        }
        
        // find progress listener and cancel if present
        ProgressListener pl = (ProgressListener)req.getSession().getAttribute(ln);
        if (pl != null) {
            pl.cancel();
        }
        
        // delete PFDReport persistence
        new PDFReportDAO().delete(cp);
        return null;
    }
}
