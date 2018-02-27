package com.korem.requestHelpers;

import static com.korem.requestHelpers.GenericServlet.setContextParams;
import static com.korem.requestHelpers.JSONServlet.FAILURE_DETAILED;
import com.lo.config.Confs;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;

/**
 *
 * @author jduchesne
 */
public abstract class JSONServlet extends Servlet {
    
    private static final Logger LOG = Logger.getLogger(JSONServlet.class.getName());
    
    private static final String TYPE_JSON = "application/json";

    protected static final String PARAM_SUCCESS = "success";
    protected static final String PARAM_FAILURE = "failure";
    
    protected static final String SUCCESS = "{ \"success\": true }";
    protected static final String FAILURE = "{ \"success\": false }";
    protected static final String FAILURE_DETAILED = "{ \"success\": false, \"errorCode\": \"%s\" }";

    @Override
    public void execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Object misc = null;
        try {
            misc = doInit();
            response.setContentType(TYPE_JSON);
            response.setCharacterEncoding(Confs.STATIC_CONFIG.charset());
            PrintWriter writer = response.getWriter();
            String json;
            
            setContextParams(this, request);
            
            try {
                json = (misc == null) ? getJSON(request, response) : getJSONWithMisc(request, misc);
            } catch (JSONException e) {
                LOG.debug(e.getMessage(), e);
                json = String.format(FAILURE_DETAILED, StringEscapeUtils.escapeJavaScript(e.getCode()));
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
                json = FAILURE;
            }
            writer.print(json);
            writer.close();
        } finally {
            if (misc != null) {
                doFinally(misc);
            }
        }
    }

    protected Object doInit() throws Exception { return null; }

    protected void doFinally(Object misc) {}
    
    protected String getJSON(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return getJSON(request);
    }

    protected String getJSON(HttpServletRequest request) throws Exception {
        return FAILURE;
    }

    protected String getJSONWithMisc(HttpServletRequest request, Object misc) throws Exception {
        return FAILURE;
    }

    protected JSONObject setSuccessful(JSONObject json) {
        json.put(PARAM_SUCCESS, true);
        return json;
    }
}
