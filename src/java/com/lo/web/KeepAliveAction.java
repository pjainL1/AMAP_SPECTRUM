/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lo.web;

import java.rmi.RemoteException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Logger;

import com.lo.util.WSClient;
//import com.spinn3r.log5j.Logger;

/**
 *
 * @author ydumais
 */
public class KeepAliveAction extends org.apache.struts.action.Action {

    private static final Logger log = ESAPI.log();

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
            HttpServletRequest request, HttpServletResponse response) {
        try {
            String mapInstanceKey = request.getParameter("mapInstanceKey");
            log.debug(ESAPI.log().SECURITY,false,String.format("keep alive mapInstanceKey %s", mapInstanceKey));
            WSClient.getMapService().getZoom(mapInstanceKey);
        } catch (RemoteException ex) {
            log.error(ESAPI.log().SECURITY,false,"RemoteException raised, this is unexpected as KMS session-timeout should be infinite.", ex);
        }
        return null;
    }
}
