package com.lo.web;

import com.korem.requestHelpers.GenericServlet;
import com.lo.ContextParams;
import com.lo.FindManager;
import com.lo.web.FindFSA.IParams;
import javax.servlet.http.HttpServletRequest;
import net.sf.json.JSONSerializer;

/**
 *
 * @author jduchesne
 */
public class FindLocation extends GenericServlet<FindFSA.IParams> {

    @Override
    protected String getJSON(HttpServletRequest req, IParams params) throws Exception {
        ContextParams cp = ContextParams.get(req.getSession());
        return JSONSerializer.toJSON(FindManager.get().getLocationPosition(cp, params.term(),cp.getSponsor().getCodes())).toString();
    }
}
