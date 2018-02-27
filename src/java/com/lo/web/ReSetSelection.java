/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.lo.web;

import com.korem.openlayers.IMapProvider;
import com.korem.openlayers.parameters.IBaseParameters;
import com.korem.requestHelpers.GenericServlet;
import com.lo.ContextParams;
import com.lo.util.SelectionReplicator;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author YDumais
 */
public class ReSetSelection extends GenericServlet<IBaseParameters> {

    @Override
    protected String getJSON(HttpServletRequest req, IBaseParameters params) throws Exception {
        SelectionReplicator selectionUtils = new SelectionReplicator(ContextParams.get(req.getSession()));
        IMapProvider mapProvider = GetOpenLayers.getMapProvider();
        selectionUtils.reapply(mapProvider, params);
        return selectionUtils.createResult(mapProvider, params);
    }
}
