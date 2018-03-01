/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.lo.web;

import com.korem.openlayers.parameters.IBaseParameters;
import com.korem.requestHelpers.GenericServlet;
import com.lo.layer.LocationLayerUtils;
import com.spinn3r.log5j.Logger;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author ydumais
 */
public class BubbleLocations extends GenericServlet<IBaseParameters> {

    private static final Logger log = Logger.getLogger();

    @Override
    protected String getJSON(HttpServletRequest req, IBaseParameters params) throws Exception {
        log.debug("Bubble location layer on top.");
        LocationLayerUtils llu = new LocationLayerUtils();
        llu.bubble(params.mapInstanceKey());
        return null;
    }
}
