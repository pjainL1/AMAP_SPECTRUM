/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lo.web;

import com.korem.requestHelpers.GenericServlet;
import com.spinn3r.log5j.Logger;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author mdube
 */
public class MaxInactiveIntervalAction extends GenericServlet<MaxInactiveIntervalAction.IParams> {
    
    private static final Logger log = Logger.getLogger(MaxInactiveIntervalAction.class);
   
    @Override
    protected String getJSON(HttpServletRequest req, MaxInactiveIntervalAction.IParams params) {
        try {
//            req.getSession().invalidate();
        } catch (IllegalStateException ex) {
            log.info("Session already invalidate");
        }
        return "";
    }
    
    protected static interface IParams {
        
    }
}
