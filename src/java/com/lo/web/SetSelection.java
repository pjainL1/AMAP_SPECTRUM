package com.lo.web;

import com.korem.openlayers.IMapProvider;
import com.korem.openlayers.parameters.IPixelSelectionParameters;
import com.korem.requestHelpers.GenericServlet;
import com.lo.ContextParams;
import com.lo.analysis.Analysis;
import com.lo.util.SelectionReplicator;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author jduchesne
 */
public class SetSelection extends GenericServlet<SetSelection.IParams> {

    protected static interface IParams {

        String mapInstanceKey();

        String geometry();

        Boolean append();
    }

    @Override
    protected String getJSON(HttpServletRequest req, IParams params) throws Exception {
        IMapProvider mapProvider = GetOpenLayers.getMapProvider();
        IPixelSelectionParameters parameters = createParameters(params);
        parameters.setLayerName(Analysis.LOCATIONS.toString());
        mapProvider.setSelection(parameters, params.append());
        SelectionReplicator selectionUtils = new SelectionReplicator(ContextParams.get(req.getSession()));
        return selectionUtils.createResult(mapProvider, parameters);
    }

    private IPixelSelectionParameters createParameters(final IParams params) {
        String[] boundsStr = params.geometry().split(",");
        final int[] bounds = new int[boundsStr.length];
        for (int i = 0; i < bounds.length; ++i) {
            bounds[i] = Integer.parseInt(boundsStr[i]);
        }
        return new IPixelSelectionParameters() {

            String layerName;

            @Override
            public int[] getPixelSelectionBounds() {
                return bounds;
            }

            @Override
            public String mapInstanceKey() {
                return params.mapInstanceKey();
            }

            @Override
            public String getLayerName() {
                return layerName;
            }

            @Override
            public void setLayerName(String layerName) {
                this.layerName = layerName;
            }
        };
    }
}
