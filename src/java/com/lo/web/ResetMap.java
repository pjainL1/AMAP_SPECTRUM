package com.lo.web;

import com.korem.openlayers.IMapProvider;
import com.korem.openlayers.parameters.IBaseParameters;
import com.korem.openlayers.parameters.ILayerVisibilityParameters;
import com.korem.requestHelpers.GenericServlet;
import com.lo.analysis.Analysis;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author jduchesne
 */
public class ResetMap extends GenericServlet<IBaseParameters> {

    @Override
    protected String getJSON(HttpServletRequest req, IBaseParameters params) throws Exception {
        IMapProvider mapProvider = GetOpenLayers.getMapProvider();
        RemoveAnalysis.doGetJSON(req, params);
        InitAction.initLayers(mapProvider, params);
        mapProvider.setLabelVisibility(toILayerVisibilityParameters(params));
        return null;
    }

    private ILayerVisibilityParameters toILayerVisibilityParameters(final IBaseParameters params){
        return new ILayerVisibilityParameters() {

            @Override
            public String id() {
                return "";
            }

            @Override
            public String name() {
                return Analysis.LOCATIONS.toString();
            }

            @Override
            public Boolean visibility() {
                return false;
            }

            @Override
            public String mapInstanceKey() {
                return params.mapInstanceKey();
            }

            @Override
            public String parent() {
                return "";
            }

            @Override
            public String getLabelField() {
                return "";
            }
        };
    }
}
