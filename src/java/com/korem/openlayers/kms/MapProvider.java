package com.korem.openlayers.kms;

import com.korem.XMLHelper;
import com.korem.map.ws.search.client.FeatureResult;
import com.korem.map.ws.search.client.LayerResult;
import com.korem.map.ws.search.client.SearchResult;
import com.korem.openlayers.Bounds;
import com.korem.openlayers.IInfoFilter;
import com.korem.openlayers.IFilter;
import com.korem.openlayers.IMapProvider;
import com.korem.openlayers.parameters.IBoundsParameters;
import com.korem.openlayers.parameters.IBaseParameters;
import com.korem.openlayers.parameters.IDeviceBoundsParameters;
import com.korem.openlayers.parameters.IImageParameters;
import com.korem.openlayers.parameters.IInfoWithinRegion;
import com.korem.openlayers.parameters.IInitParameters;
import com.korem.openlayers.parameters.ILayerNameParameters;
import com.korem.openlayers.parameters.ILayerVisibilityParameters;
import com.korem.openlayers.parameters.ILonLatSelectionParameters;
import com.korem.openlayers.parameters.IMousePositionParameters;
import com.korem.openlayers.parameters.IPixelSelectionParameters;
import com.korem.openlayers.parameters.IPositionParameters;
import com.korem.openlayers.parameters.ISelectByAttributesParameters;
import com.lo.analysis.Analysis;
import com.lo.analysis.SpectrumLayer;
import com.lo.config.Confs;
import com.lo.layer.LocationLayerUtils;
import com.lo.layer.LocationSelectionLayer;
import com.lo.util.WSClient;
import com.lo.util.WSClientLone;
import com.lo.web.FeaturePreference;
import com.lo.web.ListTiles;
import com.lo.web.SpectrumRenderMap;
import com.lo.web.SpectrumRenderTile;
import com.mapinfo.midev.service.feature.v1.SearchBySQLRequest;
import com.mapinfo.midev.service.feature.v1.SearchBySQLResponse;
import com.mapinfo.midev.service.feature.ws.v1.FeatureServiceInterface;
import com.mapinfo.midev.service.featurecollection.v1.AttributeDefinition;
import com.mapinfo.midev.service.featurecollection.v1.AttributeValue;
import com.mapinfo.midev.service.featurecollection.v1.DecimalValue;
import com.mapinfo.midev.service.featurecollection.v1.DoubleValue;
import com.mapinfo.midev.service.featurecollection.v1.FeatureCollection;
import com.mapinfo.midev.service.featurecollection.v1.FeatureCollectionMetadata;
import com.mapinfo.midev.service.featurecollection.v1.FloatValue;
import com.mapinfo.midev.service.featurecollection.v1.IntValue;
import com.mapinfo.midev.service.featurecollection.v1.StringValue;
import com.mapinfo.midev.service.mapping.v1.MapImage;
import com.mapinfo.midev.service.namedresource.v1.ListNamedResourceResponse;
import com.mapinfo.midev.service.namedresource.v1.NamedResource;
import com.spinn3r.log5j.Logger;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author jduchesne
 */
public class MapProvider implements IMapProvider {

    private static final Logger log = Logger.getLogger();
    private static final String XPATH_MINX = "//coord[1]/X/text()";
    private static final String XPATH_MINY = "//coord[1]/Y/text()";
    private static final String XPATH_MAXX = "//coord[2]/X/text()";
    private static final String XPATH_MAXY = "//coord[2]/Y/text()";
    private Set<String> labelOnlyLayersName;
    private Map<String, String> labelOnlyLayers;
    private String selectionResult = null;
    
    
    public MapProvider() throws ParserConfigurationException {
        labelOnlyLayersName = new HashSet<String>();
        labelOnlyLayersName.add(Analysis.LOCATIONS.toString().toUpperCase());
        labelOnlyLayers = new HashMap<String, String>();
    }

    @Override
    public void removeMarker(IBaseParameters parameters, long uniqueId, String layerId) throws RemoteException {
        WSClient.getAnnotationService().removeFeature(parameters.mapInstanceKey(),
                layerId, String.valueOf(uniqueId));
    }

    @Override
    public byte[] getImage(String mapInstanceKey, IBoundsParameters boundsParams, HttpServletRequest request, String format, int width, int height) throws RemoteException, ServletException, IOException {
        //return WSClient.getMapService().getImage(mapInstanceKey, format, width, height);
        // SpectrumRenderTile tile = new SpectrumRenderTile();
        //return tile.createImageFromTiles();
        byte[] img = null;
        List<SpectrumLayer> analysisLayers = (List<SpectrumLayer>) request.getSession().getAttribute("SPEC_ANALYSIS_LAYERS");
        //SpectrumLayer locationLayer = (SpectrumLayer) request.getSession().getAttribute("SPEC_LOCATION_LAYER");
        //analysisLayers.add(locationLayer);
        System.out.println("MAPPROVIDE GETIMAGE : NO OF ANALYSIS LAYERS = " + analysisLayers.size());
        if (analysisLayers != null && analysisLayers.size() > 0) {
            SpectrumRenderMap specMap = SpectrumRenderMap.getInstance();

            try {
                img = specMap.SpecRenderMap(analysisLayers, mapInstanceKey, boundsParams, width, height);
            } catch (Exception ex) {
                java.util.logging.Logger.getLogger(MapProvider.class.getName()).log(Level.SEVERE, null, ex);
            }

        } else {
            img = getTransparentTile();
        }

        return img;
    }

    private byte[] getTransparentTile() {
        try {
            BufferedImage finalTile = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);

            byte[] imageInByte = null;

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(finalTile, "png", baos);
            baos.flush();
            imageInByte = baos.toByteArray();
            baos.close();

            return imageInByte;
        } catch (Exception e) {
            //LOGGER.error("Error while getting Empty tile", e);
            return null;
        }
    }

    @Override
    public void setBounds(IBoundsParameters parameters) throws RemoteException, SAXException, IOException, XPathExpressionException {
        // WSClient.getMapService().setBounds(parameters.mapInstanceKey(),
        //       parameters.xmin(), parameters.ymin(),
        //     parameters.xmax(), parameters.ymax());
    }

    @Override
    public void writeImage(IImageParameters parameters, HttpServletResponse response) throws RemoteException, IOException {
        response.setContentType(parameters.format());
        byte[] image = null;
        try {
            //image = getImage(
            //        parameters.mapInstanceKey(), parameters.format(),
            //      parameters.width(), parameters.height());
        } catch (Exception e) {
            log.error(null, e);
        }
        try {
            response.getOutputStream().write(image);
        } catch (Exception e) {
            log.error(null, e);
        }
    }

    @Override
    public void setDeviceBounds(IDeviceBoundsParameters parameters) throws RemoteException {
        WSClient.getMapService().setDeviceBounds(parameters.mapInstanceKey(), parameters.width(),
                parameters.height());
    }

    @Override
    public Bounds getBounds(IBaseParameters parameters) throws RemoteException, SAXException, IOException, XPathExpressionException, ParserConfigurationException {
        String boundsAsXML = WSClient.getMapService().getBounds(parameters.mapInstanceKey());
        Document doc = XMLHelper.get().newDocument(boundsAsXML);
        return new Bounds(getCoord(doc, XPATH_MINX), getCoord(doc, XPATH_MINY),
                getCoord(doc, XPATH_MAXX), getCoord(doc, XPATH_MAXY));
    }

    @Override
    public void removeSelection(IBaseParameters parameters) throws RemoteException {
        WSClient.getFeatureSetService().removeSelection(parameters.mapInstanceKey());
    }

    private String getCoord(Document doc, String xPathQuery) throws XPathExpressionException {
        return XMLHelper.get().getString(doc, xPathQuery);
    }
    //Pjain
//    public static void initWorkspaceProperties(String mapInstanceKey) throws RemoteException {
//        WSClient.getMapService().setWorkspaceProperty(mapInstanceKey, "com.mapinfo.render.backgroundTransparency", "0");
//    }

    @Override
    public void init(IInitParameters parameters, HttpServletRequest request) throws Exception {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssS");
        String timeStamp = dateFormat.format(new Date());
        String mapInstanceKey = request.getSession().getId() + timeStamp;
        List<SpectrumLayer> analysisLayers = new ArrayList<SpectrumLayer>();
        Collection<Layer> layers = new ArrayList<Layer>();
        // SpectrumLayer locationLayer = null ;

        request.getSession().setAttribute("SPEC_LAYERS", layers);

        request.getSession().setAttribute("SPEC_ANALYSIS_LAYERS", analysisLayers);

//
        //String mapInstanceKey = WSClient.getMappingSessionService().createMapInstanceKey(parameters.workspaceKey()); 
//Pjain       
        //initWorkspaceProperties(mapInstanceKey);
        parameters.setMapInstanceKey(mapInstanceKey);
        //parameters.setSpecMapInstanceKey(specMapInstanceKey);
        log.debug("workspaceKey [" + parameters.workspaceKey() + "] mapInstanceKey: " + mapInstanceKey);
        //log.debug("Spectrum mapInstanceKey: " + specMapInstanceKey);
        (new LocationLayerUtils()).createGlobalLocationLayer(parameters, request);
        //request.getSession().setAttribute("SPEC_LOCATION_LAYER",locationLayer);
    }

    @Override
    public String getKeyFromWorspaceName(String workspaceName) throws Exception {
        return WSClient.getSystemService().stringEncode(workspaceName);
    }

    @Override
    public void clearSelection(IBaseParameters parameters,HttpSession session) throws RemoteException {
        //WSClient.getFeatureSetService().removeSelection(parameters.mapInstanceKey());
        removeSpecLayer("LOCATIONSELECTIONLAYER",session);
        
    }

    @Override
    public void setZoomAndCenter(IPositionParameters parameters) throws Exception {
        WSClient.getMapService().setZoomAndCenter(parameters.mapInstanceKey(), parameters.getZoom(),
                parameters.getLongitude(), parameters.getLatitude());
    }

    @Override
    public void setSelectionByAttributes(ISelectByAttributesParameters parameters) {
        try {
            WSClientLone.getFeatureSetService().setSelections(parameters.mapInstanceKey(),
                    parameters.getLayerName(), parameters.getAttributeColumns(),
                    parameters.getAttributeValues(), false);
        } catch (RemoteException ex) {
            // exception is masked because it comes from "normal" utilisaton, aka target layer is empty.
            //log.debug("setSelectionByAttributes raised a remove exception.");
        }
    }

    @Override
    public String getLayerId(ILayerNameParameters parameters) throws Exception {
        String[] ids = WSClient.getMapService().getLayersIdByName(parameters.mapInstanceKey(), parameters.getLayerName());
        return (ids.length > 0) ? ids[0] : String.valueOf(0);
    }

    @Override
    public int setSelection(ILonLatSelectionParameters parameters) throws Exception {
        int resultCount = 0;
        for (String layerId : parameters.getLayerIds()) {
            try {
                if ((resultCount = WSClient.getFeatureSetService().setSelectionWithRegion(
                        parameters.mapInstanceKey(),
                        layerId,
                        parameters.getLonLatSelectionBounds(),
                        parameters.getSrsName(),
                        false)) > 0) {
                    break;
                }
                double[] pixelList = parameters.getLonLatSelectionBounds();
            } catch (RemoteException e) {
                log.info(null, e);
            }
        }
        return resultCount;
    }

    @Override
    public String setSelection(IPixelSelectionParameters parameters, Boolean append,HttpServletRequest req) throws Exception {
        int resultCount = 0;
//        try {
////            String layerId = WSClient.getMapService().getLayersIdByName(parameters.mapInstanceKey(),
////                    parameters.getLayerName())[0];
//            String layerId = "38";
//            WSClient.getFeatureSetService().setSelectionWithRegion(
//                    parameters.mapInstanceKey(),
//                    layerId,
//                    parameters.getPixelSelectionBounds(),
//                    append);
//        } catch (RemoteException e) {
//            log.info(null, e);
//        }
        
        String xyTable = Confs.CONFIG.xyTableSPONSOR_LOCATION();


        double[] pixelList = parameters.getPixelSelectionBounds();
        String pixelCoords = Arrays.toString(pixelList).replace("[","").replace("]", "");
        
        FeatureServiceInterface featureService = FeaturePreference.getServiceInterface();
        
        SearchBySQLRequest request = createSearchBySQLRequest(pixelCoords);

        SearchBySQLResponse response = featureService.searchBySQL(request);
        FeatureCollection featureCollection = response.getFeatureCollection();

        String idColumn = "SPONSOR_LOCATION_KEY";
        String sponsorLocCode = "SPONSOR_LOCATION_CODE";
        int columnIndex = getColumnIndex(featureCollection, idColumn);
        int columnIndexLocCode = getColumnIndex(featureCollection, sponsorLocCode);
        //AttributeDefinition columnDef = featureCollection.getFeatureCollectionMetadata().getAttributeDefinitionList().getAttributeDefinition().get(columnIndex);
        //List<Object> featuresIds = new ArrayList<>();
        String locationKeys = "0.0";
        String result = "[[\"0.0\",\"0\"]";

        List<com.mapinfo.midev.service.featurecollection.v1.Feature> allFeatures = featureCollection.getFeatureList().getFeature();
        for (com.mapinfo.midev.service.featurecollection.v1.Feature feature : allFeatures) {
            AttributeValue attr = feature.getAttributeValue().get(columnIndex);
            AttributeValue attrLocCode = feature.getAttributeValue().get(columnIndexLocCode);
            //locationKeys = locationKeys + ',' + ;
//            if (attr instanceof StringValue) {
//                featuresIds.add(((StringValue) attr).getValue());
//            } else if (attr instanceof DecimalValue) {
//                featuresIds.add(((DecimalValue) attr).getValue());
//            } else if (attr instanceof DoubleValue) {
                locationKeys = locationKeys + ',' + ((DoubleValue) attr).getValue().toString();
                result = result + ",[" + "\"" + ((DoubleValue) attr).getValue().toString() + "\"" + ',' +  "\"" +  ((StringValue) attrLocCode).getValue().toString() + "\"]";
//                featuresIds.add(((DoubleValue) attr).getValue());
//            } else if (attr instanceof FloatValue) {
//                featuresIds.add(((FloatValue) attr).getValue());
//            } else if (attr instanceof IntValue) {
//                featuresIds.add(((IntValue) attr).getValue());
//            } else {
//                throw new Exception(String.format("Got an attribute (%s) of unsupported type: %s", idColumn, attr.getClass().getName()));
//            }

        }
       
        result = result + "]";
        System.out.println("SET SELECTION RESULT : " + result);
        System.out.println("SET SELECTION LOCATIONKEYS : " + locationKeys);
        
        setSelectionResult(result);
        
        ResourceBundle rb = ResourceBundle.getBundle("com.lo.layer.location");
        String query = String.format("select obj, sponsor_location_key, sponsor_location_code,customer_location_code, sponsor_location_name, sponsor_code, city, postal_code, 666.666 as value from \"/AMAP_DEV/MLCC/XYTable/SPONSOR_LOCATION\" where sponsor_code  in ('MLCC') and ( last_active >= StringToDate('03/14/2016','mm/dd/yyyy') and first_active <= StringToDate('03/17/2018','mm/dd/yyyy') ) and sponsor_location_key in (%s)"
               ,locationKeys);
        req.getSession().setAttribute("SELECTED_LOCATIONS", locationKeys);
        LocationSelectionLayer locSelLayer = LocationSelectionLayer.getInstance(parameters.mapInstanceKey(),req);

        locSelLayer.setQuery(query);

        List<SpectrumLayer> analysisLayers = (List<SpectrumLayer>) req.getSession().getAttribute("SPEC_ANALYSIS_LAYERS");
        analysisLayers.add(locSelLayer);
        req.getSession().setAttribute("SPEC_ANALYSIS_LAYERS", analysisLayers);
        

        return result;
    }


    private int getColumnIndex(FeatureCollection featureCollection, String colName) throws Exception {
        FeatureCollectionMetadata metadata = featureCollection.getFeatureCollectionMetadata();
        Iterator<AttributeDefinition> iterator = metadata.getAttributeDefinitionList().getAttributeDefinition().iterator();

        int i = 0;
        while (iterator.hasNext()) {
            AttributeDefinition def = iterator.next();
            if (colName.equals(def.getName())) {
                return i;
            }
            i++;
        }

        throw new Exception(String.format("Column %s not found in feature results metadata.", colName));
    }
    
    public void removeSpecLayer(String layerType,HttpSession session){
        List<SpectrumLayer> analysisLayers = (List<SpectrumLayer>) session.getAttribute("SPEC_ANALYSIS_LAYERS");
        ListIterator listIter = analysisLayers.listIterator();
        System.out.println("NO OF ANALYSIS LAYERS BEFORE REMOVE = " + analysisLayers.size());
        while(listIter.hasNext()){
            SpectrumLayer layer = (SpectrumLayer) listIter.next();
            if(layer.getSpecDynamicLayerClass().equals(layerType)){
                listIter.remove();
            }
        }
        System.out.println("NO OF ANALYSIS LAYERS AFTER REMOVE = " + analysisLayers.size());
        session.setAttribute("SPEC_ANALYSIS_LAYERS",analysisLayers);
    }
    
    private SearchBySQLRequest createSearchBySQLRequest(String pixelCoords) {
        SearchBySQLRequest request = new SearchBySQLRequest();
        request.setId("SearchBySQL");
        String query =  String.format("SELECT * FROM \"/AMAP_DEV/MLCC/XYTable/SPONSOR_LOCATION\" WHERE MI_Intersects(Obj, MI_Polygon('%s' , 'epsg:900913')) = TRUE", pixelCoords);
        request.setSQL(query);
        return request;
    }
    
    
    private void setSelectionResult(String result){
        this.selectionResult = result;
    }
    
    public String getSelectionResult(){
        return selectionResult;
    }
    
    @Override
    public Map<String, Collection<Map<String, Object>>> getInfo(IMousePositionParameters parameters, IFilter filter,HttpServletRequest request) throws Exception {
        //SearchResult result = WSClient.getSearchService().searchAtPoint(
          //      parameters.mapInstanceKey(),
            //    parameters.lon(),
            //    parameters.lat());
            
        ArrayList<Layer> specLayers = (ArrayList<Layer>) request.getSession().getAttribute("SPEC_LAYERS");
        List<SpectrumLayer> analysisLayers = (List<SpectrumLayer>) request.getSession().getAttribute("SPEC_ANALYSIS_LAYERS");
        
        for(Layer specLayer : specLayers){
            String table = specLayer.getSourceTable();
        }
        
        
        
        
        
        
        
        
        
        
        Map<String, Collection<Map<String, Object>>> infos = new LinkedHashMap<String, Collection<Map<String, Object>>>();
//        for (LayerResult layerResult : result.getLayers()) {
//            if (filter.isNeeded(layerResult)) {
//                Collection<Map<String, Object>> layer = new ArrayList<Map<String, Object>>(layerResult.getResults().length);
//                for (FeatureResult featureResult : layerResult.getResults()) {
//                    Map<String, Object> info = new LinkedHashMap<String, Object>();
//                    for (int i = 0; i < layerResult.getFeatureResultInfo().getColumnsInfo().length; ++i) {
//                        String column = layerResult.getFeatureResultInfo().getColumnsInfo()[i].getName();
//                        Object value = featureResult.getFeatureValues()[i].getValue();
//                        info.put(column, value);
//                    }
//                    layer.add(info);
//                }
//                infos.put(layerResult.getName(), layer);
//            }
//        }
        WSClient.getMapService().setNumericCoordSys(parameters.mapInstanceKey(), Confs.STATIC_CONFIG.webCoordsysEpsg());

        return infos;
    }

    @Override
    public Collection<Map<String, Map<String, Object>>> getInfoWithinRegion(IInfoWithinRegion parameters, IInfoFilter filter) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public long addMarker(IPositionParameters parameters, String layerName, String label) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void viewEntireLayer(IBaseParameters parameters) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Collection<Feature> getSelection(IBaseParameters parameters) throws Exception {
        try {
            String xml = WSClientLone.getFeatureSetService().getSelection(parameters.mapInstanceKey(), false);
            Document doc = XMLHelper.get().newDocument(xml);
            NodeList nodes = XMLHelper.get().getList(doc, "//MIFeature");
            Collection<Feature> features = new ArrayList<Feature>(nodes.getLength());
            for (int i = 0; i < nodes.getLength(); ++i) {
                features.add(new Feature((Element) nodes.item(i)));
            }
            return features;
        } catch (RemoteException remoteException) {
            log.error(null, remoteException);
        }
        return new LinkedList<Feature>();
    }

    @Override
    public Collection<Layer> getLayers(IBaseParameters parameters, IFilter filter, HttpServletRequest request) throws Exception {

//        Document doc = XMLHelper.get().newDocument(WSClient.getMapService().getMapInfo(parameters.mapInstanceKey()));
//        NodeList nodes = XMLHelper.get().getList(doc, "//LayerInterface");
//        Collection<Layer> layers = new ArrayList<Layer>(nodes.getLength());
//        for (int i = 0; i < nodes.getLength(); ++i) {
//            Element el = (Element) nodes.item(i);
//            Layer layer = new Layer(el,
//                    Double.parseDouble(XMLHelper.get().getString(doc, "//MapZoom/text()")));
//            if (filter == null || filter.isNeeded(layer)) {
////                log.debug(String.format("layer cntl - add layer [%s]", layer.getName()));
//                layers.add(layer);
//                String name = layer.getName().toUpperCase();
//                if (labelOnlyLayersName.contains(name)) {
//                    layer.setVisibilityBasedOnLabel(el);
//                    
//                    if (!labelOnlyLayers.containsKey(name)) {
//                        labelOnlyLayers.put(layer.getId(), null);
//                        
//                        layer.getLabelFields().add("SPONSOR_LOCATION_CODE");
//                        layer.getLabelFields().add("CUSTOMER_LOCATION_CODE");
//                        layer.getLabelFields().add("SPONSOR_LOCATION_NAME");
//                        
//                        layer.getLabelDisplays().add("AM Loc. Code");
//                        layer.getLabelDisplays().add("Location ID");
//                        layer.getLabelDisplays().add("Location Name");
//                    }
//                }
//        }
//        }
        ListTiles tiledObj = ListTiles.getInstance();
        ListNamedResourceResponse tiles = tiledObj.getResp();
        //HashMap<NamedResource,Boolean> layerWithVisiblity =new HashMap<NamedResource,Boolean>();
        Collection<Layer> layers = new ArrayList<Layer>(tiles.getNamedResource().size());
        for (NamedResource tile : tiles.getNamedResource()) {
            //final int localI = ++i;
            Layer specLayer = new Layer(tile);
            layers.add(specLayer);
            // layerWithVisiblity.put(tile, false);
        }
//        

        //HttpSession specSession = request.getSession();
        request.getSession().setAttribute("SPEC_LAYERS", layers);
        //List<SpectrumLayer>
        //specSession.setAttribute("SPEC_ANALYSIS_LAYERS", List<SpectrumLayer>);
        return layers;

    }

    @Override
    public boolean isLabelOnly(String value) throws Exception {
        return labelOnlyLayersName.contains(value.toUpperCase());
    }

    @Override
    public void setLabelVisibility(ILayerVisibilityParameters params) throws Exception {
        String layerID = WSClient.getMapService().getLayersIdByName(params.mapInstanceKey(), params.name())[0];

        if (layerID != null && !layerID.equals("-1")) {
            WSClientLone.getLayerService().setLabelVisibility(params.mapInstanceKey(), layerID, params.visibility(), params.getLabelField());
        }
    }

    @Override
    public void setLayerVisibility(ILayerVisibilityParameters params, HttpServletRequest request) throws Exception {
        //WSClient.getLayerService().setVisible(params.mapInstanceKey(), params.id(), params.visibility());
        ArrayList<Layer> layerWithVisiblity = (ArrayList<Layer>) request.getSession().getAttribute("SPEC_LAYERS");
        String layer = params.name();
        Boolean visible = params.visibility();
        for (Layer tile : layerWithVisiblity) {
            String tileName = tile.getName();
            if (tileName.equals(layer)) {
                //layerWithVisiblity.put(tile,visible);
                tile.setVisibility(visible);
            }
        }

        request.getSession().setAttribute("SPEC_LAYERS", layerWithVisiblity);

    }
}
