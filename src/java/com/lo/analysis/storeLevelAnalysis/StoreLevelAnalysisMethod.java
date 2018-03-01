
package com.lo.analysis.storeLevelAnalysis;

import com.korem.openlayers.kms.Feature;
import com.korem.openlayers.parameters.IApplyParameters;
import com.korem.openlayers.parameters.ISelectByAttributesParameters;
import com.korem.requestHelpers.RequestParser;
import com.lo.ContextParams;
import com.lo.analysis.Analysis;
import com.lo.layer.LocationLayerUtils;
import com.lo.layer.LocationLayerUtils.LabelSettings;
import com.lo.util.DateParser;
import com.lo.util.LoggingUtil;
import com.lo.util.WSClient;
import com.lo.util.WSClientLone;
import com.lo.web.Apply;
import com.lo.web.Apply.ProgressListener;
import com.lo.web.GetOpenLayers;
import com.spinn3r.log5j.Logger;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 *
 * @author smukena
 */
public class StoreLevelAnalysisMethod implements Apply.IProgressAware{
    private static final Logger log = Logger.getLogger();
    
    public static interface IParams extends IApplyParameters {
        String slaTransactionValue();
    }
     
    @Override
    public Object parseRequest(HttpServletRequest request) {
        return new Object[] {RequestParser.persistentParse(request, IParams.class),request.getSession()};
    }

    @Override
    public void execute(ProgressListener listener, Object params) {
        IParams applyParams = (IParams) ((Object[]) params)[0];
        ContextParams cp = ContextParams.get((HttpSession) ((Object[]) params)[1]);
        try {
            final String mapInstanceKey = applyParams.mapInstanceKey();
            final List<Double> selectionAsDoubles = new ArrayList<>();
            for (Feature feature : GetOpenLayers.getMapProvider().getSelection(applyParams)) {
                selectionAsDoubles.add(Double.valueOf(feature.getPK()));
            }
            
            String[] idsThemes = WSClient.getMapService().getLayersIdByName(applyParams.mapInstanceKey(), Analysis.STORE_ANALYSIS_LEVEL_THEME.toString());

            for (String id : idsThemes) {
                if (id != null && !"-1".equals(id) && !"".equals(id)) {
                    WSClient.getMapService().removeLayer(applyParams.mapInstanceKey(), id);
                }
            }
            
            if (applyParams.slaTransactionValue() != null) {
                StoreLevelAnalysisController controller = new StoreLevelAnalysisController(applyParams, listener, cp);
                long time = System.currentTimeMillis();
                controller.createLayer();
                log.debug("time to create SLA: "+(System.currentTimeMillis()-time));
                LoggingUtil.log(cp.getUser(), cp.getSponsor(), LoggingUtil.getStoreLevelAnalysisMessage(applyParams));
            } else if(idsThemes!=null&&idsThemes.length>0){
                LabelSettings labelSettings = LocationLayerUtils.get().getCurrentLabelSetting(applyParams.mapInstanceKey());
                String locationLayerId = WSClient.getMapService().getLayersIdByName(applyParams.mapInstanceKey(), Analysis.LOCATIONS.toString())[0];
                WSClient.getMapService().removeLayer(applyParams.mapInstanceKey(), locationLayerId);
                LocationLayerUtils.get().createGlobalLocationLayer(
                    applyParams.mapInstanceKey(), 
                    cp.getSelectedSponsorCodes(),
                    DateParser.prepareOracleWhenFragment(applyParams.from()),
                    DateParser.prepareOracleWhenFragment(applyParams.to()),
                    cp.getSponsor().getLogoURL());
                String newLocationLayerId = WSClient.getMapService().getLayersIdByName(applyParams.mapInstanceKey(), Analysis.LOCATIONS.toString())[0];
                WSClientLone.getLayerService().changeLayerId(applyParams.mapInstanceKey(), newLocationLayerId, locationLayerId);
                if(labelSettings!=null){
                    WSClientLone.getLayerService().setLabelVisibility(applyParams.mapInstanceKey(), locationLayerId, labelSettings.isLabelEnabled(), labelSettings.getLabelField());
            }
            }
            if(!selectionAsDoubles.isEmpty()){
            GetOpenLayers.getMapProvider().setSelectionByAttributes(
                new ISelectByAttributesParameters() {
                    @Override
                    public String[] getAttributeColumns() {
                        return new String[]{"sponsor_location_key"};
                    }

                    @Override
                    public Double[] getAttributeValues() {
                        return selectionAsDoubles.toArray(new Double[]{});
                    }

                    @Override
                    public String getLayerName() {
                        return Analysis.LOCATIONS.toString();
                    }

                    @Override
                    public String mapInstanceKey() {
                        return mapInstanceKey;
                    }
                }
            );
            }
            
        } catch (Exception ex) {
            log.error("", ex);
        } finally {
            listener.update(100);
        }
    }
    
}
