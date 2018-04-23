package com.lo.layer;

import com.korem.openlayers.IMapProvider;
import com.korem.openlayers.kms.MapProvider;
import com.korem.openlayers.parameters.IInitParameters;
import com.lo.Config;
import com.lo.analysis.Analysis;
import com.lo.analysis.SpectrumLayer;
import com.lo.analysis.storeLevelAnalysis.StoreLevelAnalysisLayerCreator;
import com.lo.config.Confs;
import com.lo.db.dao.AirMilesDAO;
import com.lo.util.DateParser;
import com.lo.util.WSClient;
import com.lo.util.WSClientLone;
import com.lo.web.GetOpenLayers;
import com.spinn3r.log5j.Logger;
import java.io.IOException;
import java.io.StringReader;
import java.rmi.RemoteException;
import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author ydumais
 */
public class LocationLayerUtils {

    private static final Logger log = Logger.getLogger();
    private static final ResourceBundle rb = ResourceBundle.getBundle("com.lo.layer.location");
    
    private static final LocationLayerUtils locationLayerUtils = new LocationLayerUtils();
    
    public static LocationLayerUtils get(){
        return locationLayerUtils;
    }
    /**
     * bubble the location layer on top of all layers
     *
     * @param mapInstanceKey
     */
    public void bubble(String mapInstanceKey) {
//        try {
////            String[] ids = WSClient.getMapService().getLayersIdByName(mapInstanceKey,
////                    Analysis.LOCATIONS.toString());
////            String[] idThemes = WSClient.getMapService().getLayersIdByName(mapInstanceKey,
////                    StoreLevelAnalysisLayerCreator.INVISIBLE_BASE_LAYER_NAME);
////            if (ids.length > 0) {
////                WSClient.getLayerService().move(mapInstanceKey, ids[0], 0);
////            }
//        } catch (RemoteException e) {
//            log.error("Error bubbling location layer on top.", e);
//        }
    }

    public void createGlobalLocationLayer(IInitParameters parameters,HttpServletRequest request)
            throws RemoteException {
         createGlobalLocationLayer(parameters.mapInstanceKey(), parameters.sponsorCodes(),
                DateParser.prepareSpecWhenFragment(parameters.from()),
                DateParser.prepareSpecWhenFragment(parameters.to()),
                parameters.logo(),request.getSession());
    }

    public void createGlobalLocationLayer(String mapInstanceKey, List<String> sponsorCodes,
            String fromFragment, String toFragment, String logo, HttpSession session)
            throws RemoteException {
        
        //String[] idsLocations = WSClient.getMapService().getLayersIdByName(mapInstanceKey, Analysis.LOCATIONS.toString());
        
//        String styleRendition;
//        if (null != logo){
//            styleRendition=  MessageFormat.format(rb.getString("location.logo"), logo, Math.random());
//        }else {
//            styleRendition= rb.getString("defaultLocation.logo");
//        }
        
        
        String sponsorList = AirMilesDAO.prepareInFragment(sponsorCodes.size());
        for (String code : sponsorCodes) {
            sponsorList = sponsorList.replaceFirst("[?]", "'" + code + "'");
        }
        
        String xyTable = Confs.CONFIG.xyTableSPONSOR_LOCATION();
        
        String query = String.format(rb.getString("location.query"),
                xyTable,
                sponsorList,
                fromFragment,
                toFragment);
        
        LocationLayer locLayer = LocationLayer.getInstance(mapInstanceKey);
        
        locLayer.setQuery( query);
        locLayer.setLogo( logo);
        
        List<SpectrumLayer> analysisLayers = (List<SpectrumLayer>) session.getAttribute("SPEC_ANALYSIS_LAYERS");
        analysisLayers.add(locLayer);
        session.setAttribute("SPEC_ANALYSIS_LAYERS",analysisLayers);
        log.debug("adding layer on map [" + mapInstanceKey + "]: " + query);
        
//        if(idsLocations==null||idsLocations.length==0){
//            String xmlProperties = MessageFormat.format(
//                    rb.getString("location.xml.properties"), 
//                    Config.getInstance().getMipool(), 
//                    Analysis.LOCATIONS);
//            WSClient.getMapService().addDynamicLayer(mapInstanceKey, xmlProperties, query);
//        }else{
//            WSClientLone.getLayerService().setQuery(mapInstanceKey, idsLocations[0], query);
//        }
//
//        String layerId = WSClient.getMapService().getLayersIdByName(mapInstanceKey, Analysis.LOCATIONS.toString())[0];
//        log.debug("location layer id: " + layerId);
        //return layerId;
    }

    public void updateRange(String mapInstanceKey, String startDate, String endDate,
            List<String> sponsorCodes, String logo,HttpSession session) {
        try {
            //WSClient.getFeatureSetService().removeSelection(mapInstanceKey);
            //String layerID = WSClient.getMapService().getLayersIdByName(mapInstanceKey,
                 //   Analysis.LOCATIONS.toString())[0];
           // if (layerID != null && !layerID.equals("-1")) {
                //WSClient.getMapService().removeLayer(mapInstanceKey, layerID);
            IMapProvider mapProvider;
            try {
                mapProvider = GetOpenLayers.getMapProvider();
                mapProvider.removeSpecLayer("LOCATIONLAYER",session);
            } catch (Exception ex) {
                java.util.logging.Logger.getLogger(LocationLayerUtils.class.getName()).log(Level.SEVERE, null, ex);
            }
                
                createGlobalLocationLayer(mapInstanceKey,
                        sponsorCodes,
                        DateParser.prepareSpecWhenFragmentFromPickers(startDate),
                        DateParser.prepareSpecWhenFragmentFromPickers(endDate),logo,session);
           // }

        } catch (RemoteException ex) {
            log.error("Error occured updating location layer.", ex);
        }
    }
    
    public LabelSettings getCurrentLabelSetting(String mapInstanceKey){
        try{
            String locationLayerId = WSClient.getMapService().getLayersIdByName(mapInstanceKey, Analysis.LOCATIONS.toString())[0];
            String layerDesc = WSClient.getLayerService().getLayerDesc(mapInstanceKey, locationLayerId);
            log.debug("layerDesc="+layerDesc);
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(layerDesc)));
            Element textlabel = (Element)doc.getElementsByTagName("TextLabels").item(0);
            String labelField = textlabel.getElementsByTagName("AttName").item(0).getTextContent();
            boolean labelEnabled = true;
            NodeList nl = textlabel.getElementsByTagName("IsEnabled");
            if(nl.getLength()>0&&nl.item(0).getTextContent().equals("false")){
                labelEnabled = false;
            }
            return new LabelSettings(labelField, labelEnabled);
        }catch(ParserConfigurationException | SAXException | IOException | DOMException | NullPointerException e){
            log.warn("", e);
            return null;
        }
    }
    
    public static class LabelSettings{
        String labelField;
        boolean labelEnabled;
        public LabelSettings(String labelField, boolean labelEnabled){
            this.labelField = labelField;
            this.labelEnabled = labelEnabled;
        }
        public String getLabelField() {
            return labelField;
        }
        public boolean isLabelEnabled() {
            return labelEnabled;
        }
    }
}
