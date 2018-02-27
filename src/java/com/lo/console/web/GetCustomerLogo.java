/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lo.console.web;

import com.korem.requestHelpers.Servlet;
import com.lo.db.dao.AirMilesDAO;
import com.lo.db.dao.SponsorDAO;
import com.lo.hosting.Config;
import com.lo.util.WSClient;
import java.net.URLEncoder;
import java.sql.Blob;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author akriaa
 */
@WebServlet(name = "GetCustomerLogo", urlPatterns = {"/console/getCustomerLogo.safe/*"})
public class GetCustomerLogo extends Servlet{

    private static final ResourceBundle rb = ResourceBundle.getBundle("com.lo.layer.location");
    
    @Override
    protected void execute(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String id = req.getPathInfo().substring(1);
        SponsorDAO sdao = new SponsorDAO(new AirMilesDAO());
        Blob logo = sdao.getSponsor(id).getLogo();
        if (logo.length() != 0){
            byte[] bytes = logo.getBytes(1, (int) logo.length());
            String filename = "console/images/";

            resp.setContentType("image/png");
            resp.setContentLength((int) logo.length());
            resp.setHeader("Content-Disposition", "inline; filename=\"" + filename + "\"");
            resp.getOutputStream().write(bytes);
        }else {
            //from KMS
            com.lo.Config con = com.lo.Config.getInstance();
            String styleEncoded = URLEncoder.encode(rb.getString("defaultLocation.logo"), "utf-8");
            resp.sendRedirect(con.getKmsUrl()+"/map.graphics.samples.StyleOverviewImage.do?refresh=1443725897459&w=25&h=25&rendition="+ styleEncoded);
        }
    }

}
