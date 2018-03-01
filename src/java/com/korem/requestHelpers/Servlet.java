package com.korem.requestHelpers;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;

/**
 *
 * @author jduchesne
 */
public abstract class Servlet extends HttpServlet {

    private static final org.apache.log4j.Logger LOG = Logger.getLogger(Servlet.class);
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        exec(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        exec(req, resp);
    }

    private void exec(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            execute(req, resp);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }
    
    protected abstract void execute(HttpServletRequest req, HttpServletResponse resp) throws Exception;
}
