package com.lo.web;

import com.korem.openlayers.IFilter;
import com.korem.openlayers.IMapProvider;
import com.korem.openlayers.kms.Layer;
import com.korem.openlayers.parameters.IBaseParameters;
import com.korem.requestHelpers.GenericServlet;
import com.lo.ContextParams;
import com.lo.analysis.Analysis;
import com.lo.db.helper.LayerGroupSynchronizer;
import com.lo.db.proxy.LayerGroupProxy;
import com.lo.db.proxy.LayerGroupProxy.LayerGroupDTO;
import com.spinn3r.log5j.Logger;
import java.util.Collection;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import net.sf.json.util.JSONBuilder;
import net.sf.json.util.JSONStringer;

/**
 *
 * @author jduchesne
 */
public class GetLayers extends GenericServlet<IBaseParameters> {
    private static final Logger LOGGER = Logger.getLogger();

    @Override
    protected String getJSON(HttpServletRequest request, IBaseParameters params) throws Exception {
        ContextParams cp = ContextParams.get(request.getSession());
        IMapProvider mapProvider = GetOpenLayers.getMapProvider();
        Map<String, LayerGroupDTO> layerGroups;
        try(LayerGroupProxy proxy = new LayerGroupProxy()){
            LayerGroupSynchronizer.getInstance().doSynchronize(mapProvider, params.mapInstanceKey(), cp.getSponsor().getRollupGroupCode(), proxy);
            layerGroups = proxy.getLayerGroupsMap(cp.getSponsor().getRollupGroupCode());
        }
        return toJSON(mapProvider.getLayers(params, LAYER_FILTER), layerGroups);
    }

    private String toJSON(Collection<Layer> layers, Map<String, LayerGroupDTO> layerGroups) {
        JSONBuilder layersAsJSON = new JSONStringer().array();
        //Append Dynamic Layer At first
        for (Layer layer : layers) {
            if (!layerGroups.containsKey(layer.getName())){
                layer.appendJSON(layersAsJSON, null);
            }
        }
        //Append all Group Layer
        for(LayerGroupDTO lg : layerGroups.values()){
            for (Layer layer : layers) {
                if(layer.getName().equals(lg.getLayerName())){
                    layer.appendJSON(layersAsJSON, lg);
                    break;
                }
            }
        }
        String result = layersAsJSON.endArray().toString();
        return result;
    }
    static IFilter LAYER_FILTER = new IFilter() {

        @Override
        public boolean isNeeded(Object obj) {
            Layer layer = (Layer) obj;
            String name = layer.getName().toUpperCase();
            boolean result =
                    !"SELECTION".equals(name)
                    && (Analysis.isDynamicLayer(name)
                    || name.endsWith(Layer.ACCEPTED_SUFFIX[0])
                    || name.endsWith(Layer.ACCEPTED_SUFFIX[1])
                    || layer.isTheme());
            return result;
        }
    };
}
