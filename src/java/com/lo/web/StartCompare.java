/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.lo.web;

import com.korem.openlayers.IMapProvider;
import com.korem.openlayers.parameters.IBaseParameters;
import com.korem.requestHelpers.GenericJSONServlet;
import com.korem.requestHelpers.GenericServlet;
import com.lo.ContextParams;
import com.lo.util.SelectionReplicator;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author YDumais
 */
@WebServlet("/startCompare.safe")
public class StartCompare extends GenericJSONServlet<IBaseParameters> {
    @Override
    protected String getJSON(HttpServletRequest req, IBaseParameters params) throws Exception {
        ContextParams.get(req.getSession()).setSelectionPKs(null);
        
        return SUCCESS;

    }
}
