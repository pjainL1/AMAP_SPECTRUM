package com.korem.requestHelpers;

import com.spinn3r.log5j.Logger;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author jduchesne
 * @deprecated 
 */
public abstract class PlainGenericServlet<T> extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(PlainGenericServlet.class);
    
    public static interface INoParams {}
    
    private void execute(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            execute(req, resp, RequestParser.parse(req, getDataStoreType()));
        } catch (Exception e) {
            LOGGER.error("Error processing servlet", e);
            throw new ServletException(e);
        }
    }

    protected Class<T> getDataStoreType() {
        return (Class<T>)((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    protected abstract void execute(HttpServletRequest request, HttpServletResponse response, T params)
            throws Exception;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        execute(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        execute(req, resp);
    }
}