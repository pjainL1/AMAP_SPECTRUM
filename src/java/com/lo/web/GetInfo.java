package com.lo.web;

import com.korem.map.ws.search.client.LayerResult;
import com.korem.openlayers.IFilter;
import com.korem.openlayers.IMapProvider;
import com.korem.openlayers.kms.Layer;
import com.korem.openlayers.parameters.IInfoParameters;
import com.korem.requestHelpers.GenericServlet;
import com.lo.ContextParams;
import com.lo.info.InfoAugmenter;
import com.lo.info.TradeAreaAugmenter;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import javax.servlet.http.HttpServletRequest;
import net.sf.json.JSONSerializer;

/**
 *
 * @author jduchesne
 */
public class GetInfo extends GenericServlet<IInfoParameters> {

    @Override
    protected String getJSON(HttpServletRequest req, IInfoParameters params) throws Exception {
        IMapProvider mapProvider = GetOpenLayers.getMapProvider();
        Map<String, Collection<Map<String, Object>>> infos = mapProvider.getInfo(params, INFO_FILTER,req );
        InfoAugmenter infoAugmenter = new InfoAugmenter();
        infoAugmenter.augment(infos, params, ContextParams.get(req.getSession()));
        
        filterOutIgnoredProperties(infos);
        
        return JSONSerializer.toJSON(infos).toString();
    }
    
    private void filterOutIgnoredProperties(Map<String, Collection<Map<String, Object>>> infos) {
        for (Entry<String, Collection<Map<String, Object>>> entry : infos.entrySet()) {
            Collection<Map<String, Object>> values = entry.getValue();
            
            for (Map<String, Object> map : values) {
                Iterator<Entry<String, Object>> mapIterator = map.entrySet().iterator();
                while (mapIterator.hasNext()) {
                    Entry<String, Object> mapEntry = mapIterator.next();
                    String mapKey = mapEntry.getKey();
                    if (mapKey != null && mapKey.equals(TradeAreaAugmenter.COLUMN_TA_TYPE)) {
                        mapIterator.remove();
                    }
                }
            }
        }
    }
    
    private static IFilter INFO_FILTER = new IFilter() {

        @Override
        public boolean isNeeded(Object obj) {
            LayerResult layer = (LayerResult) obj;
            layer.setName(Layer.removeSuffix(layer.getName()));
            return true;
        }
    };
}
