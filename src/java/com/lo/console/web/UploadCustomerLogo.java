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
import java.io.InputStream;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

/**
 *
 * @author akriaa
 */
@WebServlet(name = "UploadCustomerLogo", urlPatterns = {"/console/uploadCustomerLogo.safe"})
@MultipartConfig
public class UploadCustomerLogo extends Servlet{

    @Override
    protected void execute(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        final Part filePart = req.getPart("file");
        String id = req.getParameter("id");
        InputStream logo = filePart.getInputStream();
        SponsorDAO sdao = new SponsorDAO(new AirMilesDAO());
        sdao.setCustomerLogo(id, logo);
        resp.sendRedirect(Config.getBaseUrl(req) + "console/InitMonitoring.do#tabs-2");
    }  
}
