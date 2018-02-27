package com.lo.layer;

import com.lo.Config;
import com.lo.analysis.Analysis;
import com.lo.analysis.tradearea.TradeArea;
import com.lo.config.Confs;
import com.lo.db.helper.SimpleOraGeometryHelper;
import com.lo.util.WSClient;
import com.spinn3r.log5j.Logger;
import java.rmi.RemoteException;
import java.text.MessageFormat;
import java.util.List;

/**
 * Class to manage the PostalCodes Layers in KMS.
 * @author Charles St-Hilaire for Korem inc.
 */
public class PostalCodeLayerManager {
    private static final Logger LOGGER = Logger.getLogger();
    private static final String UNION = " UNION ";
    private static PostalCodeLayerManager instance;
    private PostalCodeLayerManager(){ super(); }
    public static PostalCodeLayerManager get(){
        if (instance == null){
            instance = new PostalCodeLayerManager();
        }
        return instance;
    }
    public void createPostalCodeLayer(String mapInstanceKey, List<TradeArea> tradeAreas, boolean visible){
        deletePostalCodeLayer(mapInstanceKey);
        if (tradeAreas != null && tradeAreas.size() > 0){
            addPostalCodeLayer(mapInstanceKey, tradeAreas);
        }
        setVisible(mapInstanceKey, visible);
    }
    public void deletePostalCodeLayer(String mapInstanceKey){
        try {
            String[] ids = WSClient.getMapService().getLayersIdByName(mapInstanceKey, Analysis.POSTAL_CODE.toString());
            for (String id : ids) {
                if (id != null && !"-1".equals(id) && !"".equals(id)) {
                    WSClient.getMapService().removeLayer(mapInstanceKey, id);
                }
            }
        } catch (RemoteException re) {
            LOGGER.error(re.getMessage(), re);
        }
    }
    private void addPostalCodeLayer(final String mapInstanceKey, final List<TradeArea> tradeAreas){
        try {
            String xmlProperties = MessageFormat.format(Confs.STATIC_CONFIG.postalcodeXmlProperties(), 
                    Config.getInstance().getMipool(), 
                    Analysis.POSTAL_CODE.toString());
            StringBuilder query = new StringBuilder("");
            
            int i = 0;
            for(TradeArea ta : tradeAreas){
                if (i++ > 0) {
                    query.append(UNION);
                }
                query.append(String.format(Confs.QUERIES.kmsDynamicLayersPostalCode(), 
                        SimpleOraGeometryHelper.getInstance().getOracleGeometry(ta.getGeometry())));
                
            }
            String[] layerIds = WSClient.getMapService().addDynamicLayer(mapInstanceKey, xmlProperties, new String[] {query.toString()});
            
            WSClient.getLayerService().setOverrideTheme(mapInstanceKey, layerIds[0], Confs.STATIC_CONFIG.postalcodeRendition());
        }catch(RemoteException re){
            LOGGER.error(re.getMessage(), re);
        }
    }
    
    public void setVisible(String mapInstanceKey, boolean visible){
        setVisible(mapInstanceKey, mapInstanceKey, visible);
    }
    
    public void setVisible(String fromMapInstanceKey, String mapInstanceKey, boolean visible){
        try{
            String[] ids = getLayersId(fromMapInstanceKey);
            for (String id : ids) {
                if (id != null && !"-1".equals(id) && !"".equals(id)) {
                    WSClient.getLayerService().setVisible(mapInstanceKey, id, visible);
                }
            }
        }catch (RemoteException re){
            LOGGER.error(re.getMessage(), re);
        }
    }
    
    public String[] getLayersId(String mapInstanceKey){
        try{
            return WSClient.getMapService().getLayersIdByName(mapInstanceKey, Analysis.POSTAL_CODE.toString());
        }catch (RemoteException re){
            LOGGER.error(re.getMessage(), re);
            return null;
        }
    }
}
