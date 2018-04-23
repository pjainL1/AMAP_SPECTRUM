package com.lo.analysis.storeLevelAnalysis;

import com.korem.heatmaps.Properties;
import com.korem.openlayers.IMapProvider;
import com.lo.ContextParams;
import com.lo.analysis.Analysis;
import com.lo.analysis.SpectrumLayer;
import com.lo.analysis.nwatch.SpectrumNWLayer;
import com.lo.analysis.storeLevelAnalysis.StoreLevelAnalysisMethod.IParams;
import com.lo.config.Confs;
import com.lo.db.dao.AirMilesDAO;
import com.lo.db.dao.LocationDAO;
import com.lo.hotspot.HotSpotQueries;
import com.lo.layer.LocationLayer;
import com.lo.layer.LocationLayerUtils;
import com.lo.layer.LocationLayerUtils.LabelSettings;
import com.lo.layer.LocationSLALayer;
import com.lo.util.DateParser;
import com.lo.util.DateType;
import com.lo.util.LocationUtils;
import com.lo.util.Painter;
import com.lo.util.SelectionReplicator;
import com.lo.util.SponsorFilteringManager;
import com.lo.util.WSClient;
import com.lo.util.WSClientLone;
import com.lo.web.Apply;
import com.lo.web.GetOpenLayers;
import com.spinn3r.log5j.Logger;
import java.io.StringReader;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

/**
 *
 * @author smukena
 */
public class StoreLevelAnalysisLayerCreator { 
    private static final DateFormat legendDateFormat = new SimpleDateFormat("MM/dd/yyyy");//todo move that somewhere more global?
    private static final String INVALID_LAYER_ID = "-1";
    private static final Logger log = Logger.getLogger();
    private static ResourceBundle rb = ResourceBundle.getBundle("com.lo.analysis.storeLevelAnalysis.storeLevelAnalysis");
    private static ResourceBundle rbloc = ResourceBundle.getBundle("com.lo.layer.location");
    private static ResourceBundle conf = ResourceBundle.getBundle("loLocalString");
    private String layerId = INVALID_LAYER_ID;
    private Painter painter = new Painter();
    private String mapInstanceKey;
    private List<Double> locationKeys;
    private final IParams params;
    private final ContextParams contextParams;
    private DateParser dateParser = new DateParser();
    private static final int ENDCOLOR = 65280;
    private static final int STARTCOLOR = 16711680;
    private static final int RANG = 5;
    private static final double ROUNDBY = 1;
    private static final int DISTRIBUTION = 1;
    private static final String FONT = "{\"desc\": \"Arial\", \"size\": 12, \"color\": 0}";
    private static final boolean NOZERO = false;
    private static final String LOCAL = "{\"language\": \"en\", \"country\": \"CA\"}";
    private static final String SPACE = " ";
    private static final String HYPHEN = " - ";
    private static final String LEFT_BRACKET = "(";
    private static final String RIGTH_BRACKET = ")";
    private static final String VERSUS = " vs ";
    
    public static final String INVISIBLE_BASE_LAYER_NAME = "sla";

    private enum Values {

        COLLECTORES("collectors", "count(distinct u.collector_key)"),
        SPEND("spend", "sum(u.spend)"),
        TRANSACTIONS("transactions", "sum(u.count)"),
        UNITS("units", "sum(u.unit)");

        String value;
        String key;

        private Values(String name, String value) {
            this.key = name;
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }

        public String getKey() {
            return this.key;
        }

        private static String getValue(String value) {
            for (Values v : Values.values()) {
                if (v.getKey().toUpperCase().equals(value.toUpperCase())) {
                    return v.getValue();
                }
            }
            return null;
        }

    }

    public StoreLevelAnalysisLayerCreator(IParams p, ContextParams contextParams) {
        this.mapInstanceKey = p.mapInstanceKey();
        this.locationKeys = LocationUtils.parseList(p.locations());
        this.params = p;
        this.contextParams = contextParams;
    }

    public String apply(Apply.ProgressListener pl,HttpSession session) throws Exception {
        try {
            if (this.contextParams != null) {
                this.contextParams.setSlaTansactionValue(getLegendTitle(params.dateType(), params.slaTransactionValue()));
            }
            addLayer(params, pl,session);
        } catch (RemoteException ex) {
            log.error("Error creating store level analysis.", ex);
        }
        return layerId;
    }

    private void addLayer(IParams params, Apply.ProgressListener pl,HttpSession session) throws Exception {
        pl.update(25);
        //String locationLayerId = WSClient.getMapService().getLayersIdByName(mapInstanceKey, Analysis.LOCATIONS.toString())[0];
        String styleRendition;
        if (null != contextParams.getSponsor().getLogoURL()){
            styleRendition=  MessageFormat.format(rbloc.getString("location.logo"), contextParams.getSponsor().getLogoURL());
        }else {
            styleRendition= rbloc.getString("defaultLocation.logo");
        }

        String query;
        if(DateType.valueOf(params.dateType())==DateType.single){
            String baseQuery = rb.getString("sla.single.query").replace("%COMPARAISON_RANGE_FRAGMENT%", "");
            query = getSingleQuery(baseQuery,mapInstanceKey, params.slaTransactionValue(), params.from(), params.to(), params.compareFrom(), params.compareTo(), params.minTransactions(), params.minSpend(), params.minUnit(), styleRendition).replace("%COMPARAISON_RANGE_FRAGMENT%", "");
        } else {
            boolean isCollectorBased = params.slaTransactionValue() != null && params.slaTransactionValue().equals("collectors");
            String baseQuery = rb.getString("sla.single.query").replace("%COMPARAISON_RANGE_FRAGMENT%", "");
            String subquery1 = getComparedQuery(baseQuery,mapInstanceKey, params.slaTransactionValue(), params.from(), params.to(), params.compareFrom(), params.compareTo(), params.minTransactions(), params.minSpend(), params.minUnit(), styleRendition, isCollectorBased, HotSpotQueries.QueryType.COMPARE_FIRST);
            String subquery2 = getComparedQuery(baseQuery,mapInstanceKey, params.slaTransactionValue(), params.compareFrom(), params.compareTo(), params.from(), params.to(), params.minTransactions(), params.minSpend(), params.minUnit(), styleRendition, isCollectorBased, HotSpotQueries.QueryType.COMPARE_SECOND);
            query = rb.getString("sla." + params.dateType() + ".query").replace("%subquery1", subquery1).replace("%subquery2", subquery2);
        }

        log.debug("adding SLA layer: " + query);
        
        //WSClientLone.getLayerService().setQuery(mapInstanceKey, locationLayerId, query);
        //createAnnotationLayer(query, styleRendition);

        LocationDAO locationDAO = new LocationDAO(new AirMilesDAO());
        int rowsDeleted = 0;
        try {
            rowsDeleted = locationDAO.deleteSLAResults(params.mapInstanceKey());
        } catch (SQLException ex) {
            java.util.logging.Logger.getLogger(StoreLevelAnalysisLayerCreator.class.getName()).log(Level.SEVERE, null, ex);
        }
        log.debug("No of rows deleted from LIM_SLA_RESULTS : "+ rowsDeleted);
                
        int rowsInserted = 0;
        try {
            rowsInserted = locationDAO.insertSLAResults(query);
        } catch (SQLException ex) {
            java.util.logging.Logger.getLogger(StoreLevelAnalysisLayerCreator.class.getName()).log(Level.SEVERE, null, ex);
        }
        log.debug("No of Rows Inserted in LIM_SLA_RESULTS TABLE : " + rowsInserted);
        
        //Add New SLA layer to session
        if (rowsInserted > 0){
            List<SpectrumLayer> analysisLayers = (List<SpectrumLayer>) session.getAttribute("SPEC_ANALYSIS_LAYERS");
            LocationSLALayer specSLALayer =   LocationSLALayer.getInstance(mapInstanceKey,session);
             
            String nameSLATable = Confs.CONFIG.namedTableLIM_SLA_RESULTS();
            String nameSpecLocTable = Confs.CONFIG.xyTableSPONSOR_LOCATION();
            String specBaseQuery = rb.getString("sla.spec.query");
            String specQuery = String.format(specBaseQuery,nameSLATable,mapInstanceKey);
            specSLALayer.setQuery(specQuery);
            
            Map<String, String> attributes = this.contextParams.getUser().getAttributes();
        
            String keyBase = "SLA." + params.slaTransactionValue();
            if (DateType.valueOf(params.dateType()) == DateType.comparison) {
                keyBase += "Comparison";
            };
            boolean isAttributeActive = Boolean.parseBoolean(attributes.get(keyBase + "Active"));
            String range = "";
            if (isAttributeActive){
                range = this.createRangedTheme(attributes.get(keyBase));
            }
            specSLALayer.setRangeClasses(range);
            specSLALayer.setAttributeActive(isAttributeActive);
            
            
            analysisLayers.add(specSLALayer);
            session.setAttribute("SPEC_ANALYSIS_LAYERS",analysisLayers);
        }
        
        
        
        
        
        pl.update(50);
        
//       if(isAttributeActive){        
//            WSClientLone.getLoneThematicService().createRangedTheme(
//                mapInstanceKey, 
//                new String[]{Analysis.STORE_ANALYSIS_LEVEL_THEME.toString()}, 
//                new String[]{Analysis.LOCATIONS.toString()},  
//                "value", 
//                STARTCOLOR, 
//                ENDCOLOR, 
//                RANG, 
//                ROUNDBY, 
//                0, 
//                range,
//                FONT, 
//                getLegendTitle(params.dateType(), params.slaTransactionValue()), 
//                getLegendSubTitlev(params), 
//                NOZERO, 
//                LOCAL,
//                1,
//                "Symbol (35,16777215,16,\"MapInfo Symbols\",16,0)",
//                false,
//                0,
//                new int[]{16711680,12599296, 8421376, 4243456, 65280},
//                true
//            );      
//        } else {
//            WSClientLone.getLoneThematicService().createRangedTheme(
//                mapInstanceKey, 
//                new String[]{Analysis.STORE_ANALYSIS_LEVEL_THEME.toString()}, 
//                new String[]{Analysis.LOCATIONS.toString()}, 
//                "value", 
//                STARTCOLOR, 
//                ENDCOLOR, 
//                RANG, 
//                ROUNDBY, 
//                DISTRIBUTION, 
//                null,
//                FONT, 
//                getLegendTitle(params.dateType(), params.slaTransactionValue()), 
//                getLegendSubTitlev(params), 
//                NOZERO, 
//                LOCAL,
//                1,
//                "Symbol (35,16777215,16,\"MapInfo Symbols\",16,0)",
//                false,
//                0,
//                null,
//                true
//            );        
//        }
//       
        reapplySelection(params,session);

        pl.update(75);
    }
    
    private void createAnnotationLayer(String query, String rendition) throws Exception{
        String locationLayerId = WSClient.getMapService().getLayersIdByName(mapInstanceKey, Analysis.LOCATIONS.toString())[0];
        LabelSettings labelSettings = new LocationLayerUtils().getCurrentLabelSetting(mapInstanceKey);
        WSClient.getMapService().removeLayer(mapInstanceKey, locationLayerId);

        String name = StringEscapeUtils.escapeXml(Analysis.LOCATIONS.toString());
            
        StringBuilder labelsb = new StringBuilder();
        labelsb.append("<LabelProperties>");
        labelsb.append("<DuplicationAllowed>true</DuplicationAllowed>");
        labelsb.append("<HorizontalAlignment>LEFT</HorizontalAlignment>");
        labelsb.append("<VerticalAlignment>CENTER</VerticalAlignment>  ");
        labelsb.append("<Priority>5</Priority>");
        labelsb.append("<OverlapAllowed>false</OverlapAllowed>");
        labelsb.append("<LabelExpression>").append(labelSettings!=null?labelSettings.getLabelField():"SPONSOR_LOCATION_CODE").append("</LabelExpression>");
        labelsb.append("<LabelFollowingPath>false</LabelFollowingPath>");
        labelsb.append("<ZoomMin linearUnit=\"km\">0</ZoomMin>");
        labelsb.append("<ZoomMax linearUnit=\"km\">50000000</ZoomMax>");
        labelsb.append("<HorizontalOffset>10</HorizontalOffset>");
        labelsb.append("<VerticalOffset>0</VerticalOffset>");
        labelsb.append("<Rendition><![CDATA[");
        labelsb.append("<Style>");
        labelsb.append("<rendition>");
        labelsb.append("<style stroke=\"black\" font-family=\"Verdana\" filter=\"halo\" font-weight=\"2.0\" font-size=\"14.0\" symbol-mode=\"font\" symbol-background=\"white\" symbol-foreground=\"black\" symbol-foreground-opacity=\"1\"/>");
        labelsb.append("</rendition>");
        labelsb.append(" </Style>");
        labelsb.append("]]></Rendition>");
        labelsb.append("</LabelProperties>");

        StringBuilder tableInfo = new StringBuilder();
        tableInfo.append("<TableInfo>");
        tableInfo.append("<TableName>").append(name).append("</TableName>");
        tableInfo.append("<CoordSys><code>4269</code><codeSpace>epsg</codeSpace></CoordSys>");
        tableInfo.append("<Column PrimaryKey=\"true\" Type=\"int\">SPONSOR_LOCATION_KEY</Column>");
        tableInfo.append("<Column Type=\"string\">SPONSOR_LOCATION_CODE</Column>");  
        tableInfo.append("<Column Type=\"string\">CUSTOMER_LOCATION_CODE</Column>");  
        tableInfo.append("<Column Type=\"string\">SPONSOR_LOCATION_NAME</Column>");  
        tableInfo.append("<Column Type=\"string\">SPONSOR_CODE</Column>");  
        tableInfo.append("<Column Type=\"string\">CITY</Column>");  
        tableInfo.append("<Column Type=\"string\">POSTAL_CODE</Column>");  
        tableInfo.append("<Column Type=\"double\">VALUE</Column>");  
        tableInfo.append("<HasRaster>false</HasRaster>");
        tableInfo.append("</TableInfo>");
    
        StringBuilder features = new StringBuilder();    
        features.append("<MIFeatureSet>\r\n");
        features.append("<Name>").append(name).append("</Name>\r\n");
        features.append("<AttNameTypedTuple>\r\n");
        features.append("<AttNameTyped PrimaryKey=\"true\" Type=\"int\">SPONSOR_LOCATION_KEY</AttNameTyped>");
        features.append("<AttNameTyped Type=\"string\">SPONSOR_LOCATION_CODE</AttNameTyped>");  
        features.append("<AttNameTyped Type=\"string\">CUSTOMER_LOCATION_CODE</AttNameTyped>");  
        features.append("<AttNameTyped Type=\"string\">SPONSOR_LOCATION_NAME</AttNameTyped>");  
        features.append("<AttNameTyped Type=\"string\">SPONSOR_CODE</AttNameTyped>");  
        features.append("<AttNameTyped Type=\"string\">CITY</AttNameTyped>");  
        features.append("<AttNameTyped Type=\"string\">POSTAL_CODE</AttNameTyped>");  
        features.append("<AttNameTyped Type=\"double\">VALUE</AttNameTyped>");  
        features.append("</AttNameTypedTuple>\r\n");
        features.append("<CoordinateReferenceSystem><Identifier><code>4269</code><codeSpace>epsg</codeSpace></Identifier></CoordinateReferenceSystem><KeyColumns>\r\n");
        features.append("<AttName>sponsor_location_key</AttName>\r\n");
        features.append("</KeyColumns>\r\n");
        Set<Integer> set = new HashSet<>();
        try(Connection connection = Properties.get().getDatasource(contextParams).getConnection()){
            try(Statement stmt = connection.createStatement()){
                try(ResultSet rs = stmt.executeQuery(query)){
                    while(rs.next()){
                        int key = rs.getInt("sponsor_location_key");
                        if(!set.contains(key)){//just in case.. jdbc layer don't crash when it happens, but annotation layer do...
                            set.add(key);
                            features.append("<MIFeature>");
                            features.append("<AttValueTuple>");
                            features.append("<AttValue isNull=\"false\">").append(key).append("</AttValue>");
                            features.append("<AttValue isNull=\"false\">").append(StringEscapeUtils.escapeXml(rs.getString("sponsor_location_code"))).append("</AttValue>");
                            features.append("<AttValue isNull=\"false\">").append(StringEscapeUtils.escapeXml(rs.getString("customer_location_code"))).append("</AttValue>");
                            features.append("<AttValue isNull=\"false\">").append(StringEscapeUtils.escapeXml(rs.getString("sponsor_location_name"))).append("</AttValue>");
                            features.append("<AttValue isNull=\"false\">").append(StringEscapeUtils.escapeXml(rs.getString("sponsor_code"))).append("</AttValue>");
                            features.append("<AttValue isNull=\"false\">").append(StringEscapeUtils.escapeXml(rs.getString("city"))).append("</AttValue>");
                            features.append("<AttValue isNull=\"false\">").append(StringEscapeUtils.escapeXml(rs.getString("postal_code"))).append("</AttValue>");
                            features.append("<AttValue isNull=\"false\">").append(rs.getDouble("value")).append("</AttValue>");
                            features.append("</AttValueTuple>\r\n");
                            features.append("<Point><coord><X>").append(rs.getDouble("longitude")).append("</X><Y>").append(rs.getDouble("latitude")).append("</Y></coord></Point></MIFeature>");     
                        }else{
                            log.error("duplicate primary key while creating annotation layer: "+key);
                        }
                    }
                }
            }
        }
        features.append("</MIFeatureSet>\r\n");
        log.debug("tableInfo: "+tableInfo.toString());
        log.debug("rendition: "+rendition);
        log.debug("features: "+features.toString());
        WSClient.getMapService().addAnnotationLayer(mapInstanceKey, tableInfo.toString(), rendition, features.toString(), labelsb.toString());
        String newLocationLayerId = WSClient.getMapService().getLayersIdByName(mapInstanceKey, Analysis.LOCATIONS.toString())[0];
        WSClientLone.getLayerService().changeLayerId(mapInstanceKey, newLocationLayerId, locationLayerId);
        if(labelSettings==null||!labelSettings.isLabelEnabled()){
            WSClient.getLayerService().setLabelVisibility(mapInstanceKey, locationLayerId, false);
        }
        WSClient.getLayerService().setSelectable(mapInstanceKey, locationLayerId, true);
    }
    
    private void reapplySelection(IParams params,HttpSession session) throws Exception {
        IMapProvider mapProvider = GetOpenLayers.getMapProvider();
        
        SelectionReplicator selectionUtils = new SelectionReplicator(contextParams);
        selectionUtils.reapply(mapProvider, params,session);
    }
    
    private String createRangedTheme(String attributes){
        //loop through the attributes and create json like string
        //String rangedTheme = "[{\"min\": "+Integer.MIN_VALUE+", \"max\":%s},{\"min\": %s, \"max\":%s},{\"min\": %s, \"max\":%s},{\"min\": %s, \"max\":%s},{\"min\": %s, \"max\":"+Integer.MAX_VALUE+"}]";
        
        String rangedTheme = Integer.MIN_VALUE + "~%s," + "%s~%s," +"%s~%s,"+"%s~%s,"+"%s~" + Integer.MAX_VALUE;
        //attributes = new StringBuilder(attributes).reverse().toString();
        String[] values = attributes.split(",");
        ArrayUtils.reverse(values);
        rangedTheme = String.format(rangedTheme, (Object[])values);
        return rangedTheme;
    }
    private String getComparedQuery(String baseQuery,String mapInstanceKey, String value, String from, String to, String comparedFrom, String comparedTo, Integer minTransactions, Integer minSpend, Integer minUnit, String styleRendition, boolean isCollectorBased, HotSpotQueries.QueryType type) {
        String minFragment = getMinValuesFragment(from, to, comparedFrom, comparedTo, minTransactions, minSpend, minUnit, isCollectorBased, type);
        String query = baseQuery.replace("%value", Values.getValue(value))
                .replace("%MIN_VALUES_FRAGMENT", minFragment)
                .replace("%rendition", styleRendition)
                .replaceAll("%subCondition%", Confs.HOTSPOT_CONFIG.queryBaseSponsorKeyClause())
                .replaceAll("%sponsors%", StringUtils.join(contextParams.getSelectedSponsorKeys(), ","));
        
        query = String.format(query, mapInstanceKey,
                contextParams.getSponsor().getRollupGroupCode(),
                DateParser.prepareOracleWhenFragment(from), 
                DateParser.prepareOracleWhenFragment(to),
                contextParams.getSponsorKeysList(),
                DateParser.prepareOracleWhenFragment(from), 
                DateParser.prepareOracleWhenFragment(to));
        
        return query;
    }
    
    private String getSingleQuery(String baseQuery,String mapInstanceKey, String value, String from, String to, String comparedFrom, String comparedTo, Integer minTransactions, Integer minSpend, Integer minUnit, String styleRendition) {
        return getComparedQuery(baseQuery,mapInstanceKey, value, from, to, comparedFrom, comparedTo, minTransactions, minSpend, minUnit, styleRendition, false, HotSpotQueries.QueryType.SINGLE);
    }

    private String getMinValuesFragment(String from, String to, String comparedFrom, String comparedTo, Integer minTransactions, Integer minSpend, Integer minUnit, boolean isCollectorBased, HotSpotQueries.QueryType queryType) {
        String fragment = "";
        if (!isCollectorBased && (minTransactions != null || minSpend != null || minUnit != null)) {
            fragment = Confs.QUERIES.minimumValuesFragment();
            switch (queryType) {
                case COMPARE_FIRST:
                case COMPARE_SECOND:
                    fragment = Confs.QUERIES.minimumValuesFragmentSumAnalysis();
                    break;
            }
            fragment = fragment.replace("%count", getMinValue(minTransactions));
            fragment = fragment.replace("%spend ", getMinValue(minSpend));
            fragment = fragment.replace("%units", getMinValue(minUnit));
            fragment = fragment.replace("%date1 ", dateParser.prepareOracleWhenFragment(from));
            fragment = fragment.replace("%date2", dateParser.prepareOracleWhenFragment(to));
            // second date range is the compared range.
            fragment = fragment.replace("%date1 ", dateParser.prepareOracleWhenFragment(comparedFrom));
            fragment = fragment.replace("%date2", dateParser.prepareOracleWhenFragment(comparedTo));
            fragment = SponsorFilteringManager.get().replaceSponsorKeysInQuery(fragment, contextParams);
        }
        return fragment;
    }

    private String getMinValue(Integer minValue) {
        return (minValue != null) ? minValue.toString() : "0";
    }

    private String getLegendTitle(String dateType, String value) {
        if (DateType.valueOf(params.dateType())==DateType.comparison) {
            value += SPACE + StringUtils.capitalize(dateType);
        }
        return MessageFormat.format(conf.getString("sla.theme.legendTitle"), value);
    }

    private static String legendFormat(String date) throws Exception{
        return legendDateFormat.format(DateParser.parse(date));
    }
    
    private String getLegendSubTitlev(IParams params) throws Exception {
        StringBuilder subTitle = new StringBuilder();
        if (DateType.valueOf(params.dateType())==DateType.single) {
            subTitle.append(legendFormat(params.from())).append(HYPHEN).append(legendFormat(params.to()));
        } else {
            subTitle.append(LEFT_BRACKET).append(legendFormat(params.from())).append(HYPHEN).append(legendFormat(params.to())).append(RIGTH_BRACKET);
            subTitle.append(VERSUS).append(LEFT_BRACKET).append(legendFormat(params.compareFrom())).append(HYPHEN).append(legendFormat(params.compareTo())).append(RIGTH_BRACKET);
        }
        return subTitle.toString();
    }
}
