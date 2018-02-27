package com.lo.web;

import com.korem.heatmaps.Legend;
import com.korem.openlayers.parameters.IBaseParameters;
import com.korem.requestHelpers.GenericServlet;
import com.lo.hotspot.HotSpotMethod;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author jduchesne
 */
public class GetHotSpotLegend extends GenericServlet<IBaseParameters> {

    @Override
    protected String getJSON(HttpServletRequest req, IBaseParameters params) throws Exception {
        Legend legend = HotSpotMethod.getHotSpotFactory(req.getSession(), params).getLastHeatMapLegend();
        if (legend != null) {
            return legend.toString();
        }
        return null;
    }
}
