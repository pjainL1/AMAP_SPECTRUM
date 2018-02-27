/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.lo.web;

import com.lo.util.WSClient;
import com.spinn3r.log5j.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

/**
 *
 * @author ydumais
 */
public class getZoom extends org.apache.struts.action.Action {

    private static final Logger log = Logger.getLogger();
    
    /* forward name="success" path="" */
    private static final String SUCCESS = "success";
    
    /**
     * This is the action called from the Struts framework.
     * @param mapping The ActionMapping used to select this instance.
     * @param form The optional ActionForm bean for this request.
     * @param request The HTTP Request we are processing.
     * @param response The HTTP Response we are processing.
     * @throws java.lang.Exception
     * @return
     */
    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        String mikey = request.getParameter("mikey");
        double zoom = WSClient.getMapService().getZoom(mikey);
        log.debug("getZoom " + zoom);
        return null;
    }
}
