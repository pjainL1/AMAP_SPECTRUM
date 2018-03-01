package com.lo.web;

import com.korem.requestHelpers.GenericServlet;
import com.lo.ContextParams;
import com.lo.FindManager;
import java.awt.geom.Rectangle2D;
import javax.servlet.http.HttpServletRequest;
import net.sf.json.JSONObject;
import net.sf.json.util.JSONStringer;

/**
 *
 * @author jduchesne
 */
public class FindFSA extends GenericServlet<FindFSA.IParams> {

    protected static interface IParams {

        String term();
    }

    @Override
    protected String getJSON(HttpServletRequest req, IParams params) throws Exception {
        ContextParams cp = ContextParams.get(req.getSession());
        
        Rectangle2D bounds = FindManager.get().getFSABounds(cp, params.term());
        if (bounds == null) {
            return new JSONObject(true).toString();
        }
        return new JSONStringer().object().
                key(FindManager.P_MINX).value(bounds.getMinX()).
                key(FindManager.P_MINY).value(bounds.getMinY()).
                key(FindManager.P_MAXX).value(bounds.getMaxX()).
                key(FindManager.P_MAXY).value(bounds.getMaxY()).
                endObject().toString();
    }
}
