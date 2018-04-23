package com.lo.web;

import com.korem.openlayers.IMapProvider;
import com.korem.openlayers.parameters.IBaseParameters;
import com.korem.requestHelpers.GenericServlet;
import com.lo.ContextParams;
import com.lo.util.SelectionReplicator;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author jduchesne
 */
public class ClearSelection extends GenericServlet<IBaseParameters> {

    @Override
    protected String getJSON(HttpServletRequest req, IBaseParameters params) throws Exception {
        IMapProvider mapProvider = GetOpenLayers.getMapProvider();
        mapProvider.clearSelection(params,req.getSession());
        SelectionReplicator selectionUtils = new SelectionReplicator(ContextParams.get(req.getSession()));
        selectionUtils.clear();
        return null;
    }
}
