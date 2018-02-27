package com.lo.web;

import com.korem.openlayers.IMapProvider;
import com.korem.openlayers.parameters.ILayerVisibilityParameters;
import com.korem.requestHelpers.GenericServlet;
import com.lo.analysis.Analysis;
import com.lo.util.WSClient;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 * @author jduchesne
 */
public class SetLayerVisibility extends GenericServlet<SetLayerVisibility.Params> {
    public interface Params {
        String mapInstanceKey();
        JSONArray layers();
    }

    @Override
    protected String getJSON(HttpServletRequest req, final Params params) throws Exception {
        IMapProvider mapProvider = GetOpenLayers.getMapProvider();
        List <ILayerVisibilityParameters> layerList = new ArrayList<ILayerVisibilityParameters>() ;
        for (Object layer : params.layers()) {
            final JSONObject jsonLayer = (JSONObject)layer;
            ILayerVisibilityParameters pa = new ILayerVisibilityParameters() {

                @Override
                public String id() {
                    return jsonLayer.getString("id");
                }

                @Override
                public String name() {
                    return jsonLayer.getString("name");
                }

                @Override
                public Boolean visibility() {
                    return jsonLayer.getBoolean("visibility");
                }

                @Override
                public String mapInstanceKey() {
                    return params.mapInstanceKey();
                }

                @Override
                public String parent() {
                    return jsonLayer.getString("parent");
                }
                
                @Override
                public String getLabelField() {
                    return jsonLayer.getString("labelField");
                }
            };
       
           layerList.add(pa);
        }
        
        overrideParentLayerOpacities(layerList, params);
        
        for (ILayerVisibilityParameters layerElement : layerList) {
            setLayerVisiblity(mapProvider, layerElement);
        }

        return null;
    }
    
    // override the opacity of thematics' parent layers, so that the info tool works on those even if the parent is
    // not manually enabled by users.
    private void overrideParentLayerOpacities(List <ILayerVisibilityParameters> layerList, final Params params) throws RemoteException {
        for (ILayerVisibilityParameters layerElem : layerList) {
            if (!layerElem.parent().isEmpty()){
                boolean found = false;
                int parent = Integer.valueOf(layerElem.parent());
                for (final ILayerVisibilityParameters otherLayer : layerList) {
                    if (Integer.valueOf(otherLayer.id()).equals(parent)) {
                        found = true;
                       
                        ILayerVisibilityParameters newParent = new ILayerVisibilityParameters() {

                            @Override
                            public String id() {
                                return otherLayer.id();
                            }

                            @Override
                            public String name() {
                                return otherLayer.name();
                            }

                            @Override
                            public Boolean visibility() {
                                return true;
                            }

                            @Override
                            public String parent() {
                                return otherLayer.parent();
                            }

                            @Override
                            public String mapInstanceKey() {
                                return otherLayer.mapInstanceKey();
                            }
                            
                            @Override
                            public String getLabelField() {
                                return otherLayer.getLabelField();
                            }                            
                        };
                        if (layerElem.visibility()) {
                            layerList.set(layerList.indexOf(otherLayer), newParent);
                        }
                    }
                }
                
                if (!found) {
                    // parent layer is not found within layer control (not a _CL layer).
                    // in that case, we override its opacity directly.
                    WSClient.getLayerService().setVisible(params.mapInstanceKey(), layerElem.parent(), layerElem.visibility());
                }
            }
        }
    }
   
    private void setLayerVisiblity(IMapProvider mapProvider, ILayerVisibilityParameters params) throws Exception {
        if (mapProvider.isLabelOnly(params.name())) {
                mapProvider.setLabelVisibility(params);
            } else {
                mapProvider.setLayerVisibility(params);
            }
    }
}
