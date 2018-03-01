package com.lo.web;

import com.korem.openlayers.IMapProvider;
import com.korem.openlayers.kms.Layer;
import com.korem.openlayers.parameters.IBaseParameters;
import com.korem.requestHelpers.GenericServlet;
import java.util.Collection;
import javax.servlet.http.HttpServletRequest;
import net.sf.json.util.JSONBuilder;
import net.sf.json.util.JSONStringer;

/**
 *
 * @author jduchesne
 */
public class GetLayersZoomVisibility extends GenericServlet<IBaseParameters> {

    @Override
    protected String getJSON(HttpServletRequest req, IBaseParameters params) throws Exception {
        IMapProvider mapProvider = GetOpenLayers.getMapProvider();
        return toJSON(mapProvider.getLayers(params, GetLayers.LAYER_FILTER));
    }

    private String toJSON(Collection<Layer> layers) {
        JSONBuilder layersAsJSON = new JSONStringer().array();
        for (Layer layer : layers) {
            layer.appendZoomJSON(layersAsJSON);
        }
        return layersAsJSON.endArray().toString();
    }
}
