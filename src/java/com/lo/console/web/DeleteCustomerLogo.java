/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lo.console.web;

import com.korem.requestHelpers.Servlet;
import com.lo.Config;
import com.lo.db.dao.AirMilesDAO;
import com.lo.db.dao.SponsorDAO;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author akriaa
 */
@WebServlet(name = "DeleteCustomerLogo", urlPatterns = {"/console/deleteCustomerLogo.safe"})
public class DeleteCustomerLogo extends Servlet{

    @Override
    protected void execute(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        SponsorDAO sdao = new SponsorDAO(new AirMilesDAO());
        String id = req.getParameter("id");
        sdao.deleteCustomerLogo(id);
        resp.sendRedirect(Config.getBaseUrl(req) + "console/InitMonitoring.do#tabs-2");
    }
    
}
