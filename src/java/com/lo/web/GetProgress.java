package com.lo.web;

import com.korem.requestHelpers.GenericServlet;
import com.lo.util.ProgressListenerUtils;
import com.lo.web.Apply.IParams;
import com.lo.web.Apply.ProgressListener;
import javax.servlet.http.HttpServletRequest;
import net.sf.json.util.JSONStringer;

/**
 *
 * @author jduchesne
 */
public class GetProgress extends GenericServlet<Apply.IParams> {

    @Override
    protected String getJSON(HttpServletRequest req, IParams params) throws Exception {        
        ProgressListener progressListener = ProgressListenerUtils.getWithMethodName(req, params.methods());
        if (progressListener == null) {
            return "100";
        }
        return new JSONStringer().object().
                key("progress").
                value(progressListener.getProgress()).
                endObject().toString();
    }
}
