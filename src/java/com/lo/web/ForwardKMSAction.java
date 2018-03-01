/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lo.web;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

/**
 * 
 * @author ydumais
 */
public class ForwardKMSAction extends Action {

	@Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		try {
			Context initCtx = (Context) new InitialContext()
					.lookup("java:comp/env");
			String url = (String) initCtx.lookup("kms/url");
			
			ActionForward ac = new ActionForward();
			ac.setName("kms");
			ac.setPath(url);
			ac.setRedirect(true);
			return ac;
		} catch (Exception e) {
		
		}
		return null;
	}
}
