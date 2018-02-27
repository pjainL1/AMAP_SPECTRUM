package com.korem.requestHelpers;

import com.lo.ContextParams;
import com.lo.util.SessionUtils;
import com.spinn3r.log5j.Logger;
import java.io.File;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.json.JSONObject;

/**
 *
 * @author jduchesne
 * @deprecated If possible, use new JSON/DBBounds GenericServlets
 */
public abstract class GenericServlet<T> extends PlainGenericServlet<T> {
    
    private static final Logger log = Logger.getLogger();

    private static final String RESP_ENCODING = "utf-8";
    private static final String TYPE_JSON = "application/json";
    private static final String PARAM_SUCCESS = "success";
    private static final String SUCCESS = "{ \"success\": true }";
    private static final String INVALID = "{ \"invalid\": true }";

    @Override
    protected void execute(HttpServletRequest req, HttpServletResponse resp, T params)
            throws Exception {
        try {
            resp.setContentType(TYPE_JSON);
            resp.setCharacterEncoding(RESP_ENCODING);
            PrintWriter writer = resp.getWriter();
            String json;
            try {
                json = getJSON(req, params);
            } catch (Exception e) {
                log.error("", e);
                throw new ServletException(e);
            }
            if (req.getRequestURI().contains("apply.safe") && !SessionUtils.isValid(req.getSession())) {
                writer.print(INVALID);
            } else {
                if (json == null) {
                    writer.print(SUCCESS);
                } else {
                    writer.print(json);
                }
            }
            writer.close();

            setContextParams(this, req);

        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
    
    static void setContextParams(HttpServlet servlet, HttpServletRequest req) {
        ContextParams cp = ContextParams.get(req.getSession());
        if (cp.getTempDir() == null) {
            cp.setTempDir((File) servlet.getServletContext().getAttribute("javax.servlet.context.tempdir"));
            cp.set(req.getSession());
        }
    }

    protected abstract String getJSON(HttpServletRequest req, T params) throws Exception;

    protected JSONObject setSuccessful(JSONObject json) {
        json.put(PARAM_SUCCESS, true);
        return json;
    }
}
