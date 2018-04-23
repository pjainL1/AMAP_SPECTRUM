/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lo.analysis.nwatch;

import com.lo.Config;
import com.lo.ContextParams;
import com.lo.analysis.Analysis;
import com.lo.analysis.SpectrumLayer;
import com.lo.analysis.nwatch.NWatchMethod.IParams;
import com.lo.config.Confs;
import com.lo.db.dao.AirMilesDAO;
import com.lo.db.dao.LocationDAO;
import com.lo.util.DateParser;
import com.lo.util.LocationUtils;
import com.lo.util.Painter;
import com.lo.util.SponsorFilteringManager;
import com.lo.util.WSClient;
import com.lo.web.Apply.ProgressListener;
import com.spinn3r.log5j.Logger;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import javax.servlet.http.HttpSession;

/**
 *
 * @author ydumais
 */
public class NWatchLayerCreator {

    private static final String INVALID_LAYER_ID = "-1";
    private static final Logger log = Logger.getLogger();
    private ResourceBundle rb = ResourceBundle.getBundle("com.lo.analysis.nwatch.nwatch");
    private String layerId = INVALID_LAYER_ID;
    private Painter painter = new Painter();
    private String mapInstanceKey;
    private List<Double> locationKeys;
    private final IParams params;
    private final ContextParams contextParams;
    private DateParser dateParser = new DateParser();

    public NWatchLayerCreator(IParams p, ContextParams contextParams) {
        this.mapInstanceKey = p.mapInstanceKey();
        this.locationKeys = LocationUtils.parseList(p.locations());
        this.params = p;
        this.contextParams = contextParams;
    }

    public String apply(ProgressListener pl, ContextParams contextParams,HttpSession session) throws ParseException {
        if (locationKeys.isEmpty()) {
            throw new IllegalStateException("empty list of location code.");
        }
        try {
            String sponsorGrp = contextParams.getSponsor().getRollupGroupCode();
            addLayer(session, sponsorGrp, params.from(), params.to(), params.nwatchtype(), params.minTransactions(), params.minSpend(), params.minUnit(), contextParams.getSponsorKeysList());
            pl.update(50);
            pl.update(75);
            log.debug("neighbourhood watch layer id: " + layerId);
        } catch (RemoteException ex) {
            log.error("Error creating neighbourhood watch layer.", ex);
        }
        return layerId;
    }

    private void addLayer(HttpSession session, String sponsorGrp,String from, String to, String nwType, Integer minTransactions, Integer minSpend, Integer minUnit, String sponsorKeysList) throws RemoteException {
        
        
        String xmlProperties = MessageFormat.format(rb.getString("nw.xml.properties"),
                Config.getInstance().getMipool(), 
                Analysis.NEIBOURHOOD_WATCH);
        String query = (nwType.equalsIgnoreCase("unit"))? rb.getString("nw." + params.nwatch() + ".query").replace("%TOTAL%", "total_unit"): 
                                                           rb.getString("nw." + params.nwatch() + ".query").replace("%TOTAL%", "total_spend" );
        
        if (null != minTransactions || null != minSpend || null != minUnit){
            String subQuery = rb.getString("nw.minimumValues.fragment");
            subQuery = SponsorFilteringManager.get().replaceSponsorKeysInQuery(subQuery, contextParams);
            subQuery = (null != minTransactions) ? subQuery.replace("%count", String.valueOf(minTransactions)) : subQuery.replace("%count", "0");
            subQuery = (null != minSpend) ? subQuery.replace("%spend", String.valueOf(minSpend)) : subQuery.replace("%spend", "0");
            subQuery = (null != minUnit) ? subQuery.replace("%units", String.valueOf(minUnit)) : subQuery.replace("%units", "0");
            subQuery = subQuery.replace("%date1", dateParser.prepareOracleWhenFragment(from));
            subQuery = subQuery.replace("%date2", dateParser.prepareOracleWhenFragment(to));
            query = query.replace("%TABLE%", subQuery);
        }else {
            query = query.replace("%TABLE%",sponsorGrp+".nwatch");
        }
        query = (nwType.equalsIgnoreCase("unit"))? query.replace("%nwatchType%", "units") : query.replace("%nwatchType%", "spend");
        //query = query.replace("%mapInstanceKey%", params.mapInstanceKey());
        //query = String.format(query,params.mapInstanceKey(),prepareRenditionFragment(),prepareLocationFragment(),dateParser.prepareOracleWhenFragment(from),dateParser.prepareOracleWhenFragment(to));
        query = String.format(query,params.mapInstanceKey(),prepareLocationFragment(),dateParser.prepareOracleWhenFragment(from),dateParser.prepareOracleWhenFragment(to));
        log.debug("QUERY FOR NW RESULTS : " + query);
        LocationDAO locationDAO = new LocationDAO(new AirMilesDAO());
                //Delete rows with same mapinstancekey
                
        int rowsDeleted = 0;
        try {
            rowsDeleted = locationDAO.deleteNWResults(params.mapInstanceKey());
        } catch (SQLException ex) {
            java.util.logging.Logger.getLogger(NWatchLayerCreator.class.getName()).log(Level.SEVERE, null, ex);
        }
        log.debug("No of rows deleted from LIM_NW_RESULTS : "+ rowsDeleted);
                
        int rowsInserted = 0;
        try {
            rowsInserted = locationDAO.insertNWResults(query);
        } catch (SQLException ex) {
            java.util.logging.Logger.getLogger(NWatchLayerCreator.class.getName()).log(Level.SEVERE, null, ex);
        }
        log.debug("No of Rows Inserted in LIM_NW_RESULTS TABLE : " + rowsInserted);
        
        //Add New NW layer to session
        if (rowsInserted > 0){
            List<SpectrumLayer> analysisLayers = (List<SpectrumLayer>) session.getAttribute("SPEC_ANALYSIS_LAYERS");
            SpectrumNWLayer specNWLayer =  SpectrumNWLayer.getInstance(params.mapInstanceKey());
            analysisLayers.add(specNWLayer);
            session.setAttribute("SPEC_ANALYSIS_LAYERS",analysisLayers);
        }
        
        //WSClient.getMapService().addDynamicLayer(mapInstanceKey, xmlProperties, query);
        //layerId = WSClient.getMapService().getLayersIdByName(mapInstanceKey, Analysis.NEIBOURHOOD_WATCH.toString())[0];
    }

    private String prepareRenditionFragment() {
        StringBuilder sb = new StringBuilder("");
        String colorFromDB = "";
        for (Double location : locationKeys) {
            colorFromDB = getLocationColorFromDB(location, "nwatch");
            sb.append("when ").append(location).append(" then '").append(painter.getNWatchRendition(location, colorFromDB)).append("'");
        }
        return sb.toString();
    }

    private String getLocationColorFromDB(double locationID, String colorFlag) {
        LocationDAO locationDAO = new LocationDAO(new AirMilesDAO());
        int sponsorLocationID = (int) locationID;
        return "#"+locationDAO.getNWatchColorLocation(contextParams, sponsorLocationID);
    }

    private String prepareLocationFragment() {
        StringBuilder sb = new StringBuilder("in (");
        for (Double location : locationKeys) {
            sb.append(location).append(",");
        }
        sb.deleteCharAt(sb.lastIndexOf(","));
        sb.append(")");
        return sb.toString();
    }
}
