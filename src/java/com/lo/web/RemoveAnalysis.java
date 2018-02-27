/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lo.web;

import com.korem.openlayers.parameters.IBaseParameters;
import com.korem.requestHelpers.GenericServlet;
import com.lo.ContextParams;
import com.lo.analysis.Analysis;
import com.lo.util.WSClient;
import com.spinn3r.log5j.Logger;
import java.rmi.RemoteException;
import javax.servlet.http.HttpServletRequest;
import net.sf.json.util.JSONStringer;

/**
 *
 * @author ydumais
 */
public class RemoveAnalysis extends GenericServlet<IBaseParameters> {

    private static final Logger log = Logger.getLogger();
    private static final Analysis[] VOLATILE_LAYER_NAMES = new Analysis[]{
        Analysis.NEIBOURHOOD_WATCH,
        Analysis.TRADE_AREA,
        Analysis.POSTAL_CODE
    };

    @Override
    protected String getJSON(HttpServletRequest req, IBaseParameters params) throws Exception {
        return doGetJSON(req, params);
    }

    static String doGetJSON(HttpServletRequest req, IBaseParameters params) throws Exception {
        ContextParams cp = ContextParams.get(req.getSession());
        int cnt = removeLayersByName(cp, params.mapInstanceKey());
        cp.set(req.getSession());
        return new JSONStringer().object().key("refresh").value(
                (cnt > 0 ? true : false)).endObject().toString();
    }

    /**
     * @deprecated prefer removeLayers by name
     * @param cp
     * @param mapInstanceKey
     * @return
     */
    private static int removeLayers(ContextParams cp, String mapInstanceKey) {
        int cnt = 0;
//        EnumMap<Analysis, String> layerIds = cp.getLayerIds(mapInstanceKey);
//        for (String id : layerIds.values()) {
//            try {
//                if (id != null && !"-1".equals(id)) {
//                    WSClient.getMapService().removeLayer(mapInstanceKey, id);
//                    ++cnt;
//                }
//            } catch (RemoteException ex) {
//                log.warn(String.format("Error removing layer id : %s. Might be a bug.", id));
//            }
//        }
//        layerIds.clear();
        return cnt;
    }

    private static int removeLayersByName(ContextParams cp, String mapInstanceKey) {
        int cnt = 0;
        for (Analysis analysis : VOLATILE_LAYER_NAMES) {
            try {
                String[] ids = WSClient.getMapService().getLayersIdByName(mapInstanceKey, analysis.toString());
                for (String id : ids) {
                    if (id != null && !"-1".equals(id) && !"".equals(id)) {
                        WSClient.getMapService().removeLayer(mapInstanceKey, id);
                        ++cnt;
                    }
                }
            } catch (RemoteException ex) {
                log.warn(String.format("Error removing layer named %s.", analysis.toString()));
            }
        }
        return cnt;
    }
}
