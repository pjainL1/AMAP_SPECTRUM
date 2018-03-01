package com.lo.db.helper;

import com.korem.openlayers.IFilter;
import com.korem.openlayers.IMapProvider;
import com.korem.openlayers.kms.Layer;
import com.korem.openlayers.parameters.IBaseParameters;
import com.lo.analysis.Analysis;
import com.lo.db.proxy.LayerGroupProxy;
import com.spinn3r.log5j.Logger;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * Thiss class is about synchronization of LayerGroup part of BD.
 * It use KMS Layers information to update the BD in order to have a BD that
 * reflecting KMS information. It will adjust LAYER_GROUP and LAYER table of
 * LONE schema.
 * 
 * @author Charles St-Hilaire for Korem inc.
 */
public class LayerGroupSynchronizer{
    private static final Logger LOGGER = Logger.getLogger();
    private static LayerGroupSynchronizer instance;
    private LayerGroupSynchronizer(){ super(); }
    
    public static LayerGroupSynchronizer getInstance() throws SQLException{
        if (instance == null){
            instance = new LayerGroupSynchronizer();
        }
        return instance;
    }
    
    public long doSynchronize(final IMapProvider mp, final String mapInstanceKey, final String sponsorName, final LayerGroupProxy proxy){
        long duration = System.currentTimeMillis();
        try{
            List<String> kmsLayers = getKmsLayers(mp, mapInstanceKey);
            if (kmsLayers.isEmpty()){
                proxy.flushLayer(sponsorName);
            }else{
                proxy.mergeLayer(kmsLayers, sponsorName);
            }
        }catch (Exception e){
            LOGGER.error("Failed to synchronize LayerGroup: "+e.getMessage(),e);
        }finally{
            duration = System.currentTimeMillis() - duration;
            LOGGER.debug(String.format("LayerGroup Synchronization processed in %d min, %d sec, %d ms",
                    TimeUnit.MILLISECONDS.toMinutes(duration), 
                    TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)),
                    TimeUnit.MILLISECONDS.toMillis(duration)));
        }
        return duration;
    }
    
    private List<String> getKmsLayers(final IMapProvider mp, final String mapInstanceKey) throws Exception{
        List<String> kmsLayers = new ArrayList();
        Collection<Layer> layers = mp.getLayers(new IBaseParameters() {
            @Override public String mapInstanceKey() { return mapInstanceKey; }
        }, LayerGroupSynchronizer.LAYER_FILTER);
        for(Layer layer : layers){
            kmsLayers.add(layer.getName());
        }
        return kmsLayers;
    }
    
    private static final IFilter LAYER_FILTER = new IFilter() {
        @Override
        public boolean isNeeded(Object obj) {
            Layer layer; String name;
            boolean result = 
                    obj != null && obj instanceof Layer 
                    && Layer.ACCEPTED_SUFFIX.length > 1 ? 
                    (
                        (name = (layer = (Layer)obj).getName()) != null 
                        && (
                            name.endsWith(Layer.ACCEPTED_SUFFIX[0]) || name.endsWith(Layer.ACCEPTED_SUFFIX[1]) || (layer != null && layer.isTheme())
                        )
                        && !Analysis.isDynamicLayer(name)
                    ) 
                    : false;
            return result;
        }
    };
}
