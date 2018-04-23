package com.lo.analysis.tradearea;

import com.lo.util.Painter;
import com.vividsolutions.jts.geom.Coordinate;
import com.lo.ContextParams;
import com.lo.analysis.Analysis;
import com.lo.analysis.AnalysisControler;
import com.lo.analysis.SpectrumLayer;
import com.lo.analysis.tradearea.TradeAreaMethod.IParams;
import com.lo.analysis.tradearea.builder.CustomTABuilder;
import com.lo.analysis.tradearea.builder.TABuilder;
import com.lo.config.Confs;
import com.lo.db.dao.AirMilesDAO;
import com.lo.db.dao.LocationDAO;
import com.lo.db.helper.OraReaderWriterHelper;
import com.lo.db.om.SponsorGroup;
import com.lo.layer.PostalCodeLayerManager;
import com.lo.util.StyleUtils;
import com.lo.util.WSClientLone;
import com.lo.web.Apply.ProgressListener;
import com.spinn3r.log5j.Logger;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import oracle.sql.STRUCT;

/**
 *
 * @author ydumais
 */
public class TradeAreaControler implements AnalysisControler {

    private static final Logger LOGGER = Logger.getLogger();
    private final IParams params;
    private final ProgressListener listener;
    private final ContextParams contextParams;
    private final SponsorGroup sponsorGroup;
    private final List<TradeArea.Type> taTypes;
    private final List<TradeArea> tradeAreas;

    public TradeAreaControler(String[] taTypesParam, IParams ip, ProgressListener listener, ContextParams contextParams, SponsorGroup sponsorGroup) {
        this.taTypes = new ArrayList();
        for (String ta : taTypesParam) {
            if (!ta.isEmpty()) {
                taTypes.add(TradeArea.Type.valueOf(ta));
            }
        }
        this.params = ip;
        this.listener = listener;
        this.contextParams = contextParams;
        this.sponsorGroup = sponsorGroup;
        this.tradeAreas = new ArrayList<>();
        
    }

    public List<TradeArea> getTradeAreas() {
        return tradeAreas;
    }

    
    @Override
    public String createLayer(HttpSession session) {
        String layerID = null;
        List<SpectrumLayer> analysisLayers = (List<SpectrumLayer>) session.getAttribute("SPEC_ANALYSIS_LAYERS");
        tradeAreas.clear();
        
        try {
            List<TABuilder> builders = TABuilderFactory.createTradeAreaBuilder(taTypes, params, contextParams, sponsorGroup);
            for (TABuilder builder : builders) {
                tradeAreas.addAll(builder.drawTradeAreas(listener));
            }
            if (contextParams != null) {
                contextParams.setTradeAreas(tradeAreas);
            }
            
             createAnnotationLayer(tradeAreas);
            
            
            
            PostalCodeLayerManager.get().createPostalCodeLayer(params.mapInstanceKey(), tradeAreas, false);
        } catch (Exception e) {
            LOGGER.error(null, e);
        }
        int[] mipk = generateFakeIds(tradeAreas.size());
        try {
            if (contextParams != null) {
                LocationDAO locationDAO = new LocationDAO(new AirMilesDAO());
                //Delete rows with same mapinstancekey
                int rowsDeleted = locationDAO.deleteTradeAreaPolygon(params.mapInstanceKey());
                LOGGER.info("No of rows deleted from LIM_TA_POLYGON : "+ rowsDeleted);
                
                int rowsInserted = locationDAO.insertTradeAreaPolygon(params.mapInstanceKey(),tradeAreas,mipk);
                LOGGER.info("No of rows inserted in LIM_TA_POLYGON : "+ rowsInserted);
                locationDAO.insertToHistoryTable(tradeAreas, 
                        this.contextParams, this.params.from(), this.params.to(), 
                        this.params.polygon(), this.params.locations(), params);
                
                if (rowsInserted > 0){
                SpectrumTradeAreaLayer specTALayer =  SpectrumTradeAreaLayer.getInstance(params.mapInstanceKey());
                analysisLayers.add(specTALayer);
                session.setAttribute("SPEC_ANALYSIS_LAYERS",analysisLayers);
                }

                
            }
        } catch (NumberFormatException | SQLException | ParseException ex) {
            LOGGER.error("Error inserting trade area to history table.", ex);
        }
        
        return layerID;
    }
    
    

    private void createAnnotationLayer(List<TradeArea> tradeAreas) {
        String layerID = null;
        if (tradeAreas.size() > 0) {
            String[] columns = {"mipk", "Location code", "location_key", "ta_type", "customer_location_code", "sponsor_code"};
            int[] mipk = generateFakeIds(tradeAreas.size());
            List<double[]> points = new ArrayList<>();
            List<String> renditions = new ArrayList<>();
            List<String[]> labels = new ArrayList<>();

            Painter painter = new Painter();

            for (int i = 0; i < tradeAreas.size(); i++) {
                TradeArea tradeArea = tradeAreas.get(i);
                labels.add(new String[]{tradeArea.getLocationCode(), "" + tradeArea.getLocationKey(), tradeArea.getType().toString(), tradeArea.getCustomerLocationCode(), tradeArea.getSponsorCode()});
                points.add(extractPoints(tradeArea.getGeometry().getCoordinates()));
                String rendition = getRendition(tradeArea, tradeAreas, painter);
                renditions.add(rendition);
            }
           // layerID = createAnnotationLayer(columns, mipk, labels, points, renditions);
        }
        //return layerID;
    }

    private String getLayerName() {
        if (this.params.optionalLayerName() != null && !"".equals(this.params.optionalLayerName())) {
            return this.params.optionalLayerName();
        } else {
            return Analysis.TRADE_AREA.toString();
        }
    }
    
    private boolean useDefaultColor(TradeArea tradeArea, List<TradeArea> tradeAreas) {
        if (tradeArea.getType() == TradeArea.Type.projected) {
            return true;
        }
        
        int i = 0;
        for (TradeArea ta : tradeAreas) {
            if (ta.getType() != TradeArea.Type.custom) {
                if (++i > 1) {
                    return false;
                }
            }
        }
        
        return true;
    }

    private String getRendition(TradeArea tradeArea, List<TradeArea> tradeAreas, Painter painter) {
        String rendition = null;
            if (tradeArea.getType() == TradeArea.Type.custom) {
                rendition = painter.getCustomTARendition();
            } else if (useDefaultColor(tradeArea, tradeAreas)) {
                rendition = painter.getDefaultRendition();
            } else {
                String color = getLocationColorFromDB(tradeArea.getLocationKey(), "ta");
                rendition = painter.getTradeAreaRendition(tradeArea.getLocationCode(), color);
            }
            tradeArea.setRendition(rendition);
            return rendition;
    }

    private String getLocationColorFromDB(double locationID, String colorFlag) {
        int sponsorLocationID = (int) locationID;
        LocationDAO locationDAO = new LocationDAO(new AirMilesDAO());
        return "#" + locationDAO.getTradeAreaColorLocation(sponsorGroup, sponsorLocationID);

    }

    private String createAnnotationLayer(String[] columns, int[] mipk, List<String[]> labels, List<double[]> points, List<String> renditions) {
        String id = "";
        try {
            id = WSClientLone.getMapService().createAnnotationLayer(
                    params.mapInstanceKey(),
                    getLayerName(),
                    columns,
                    mipk,
                    labels.toArray(new String[][]{}),
                    null,
                    points.toArray(new double[][]{}),
                    renditions.toArray(new String[]{}),
                    3);
            LOGGER.debug(String.format("AnnotationLayer id %s.", id));
        } catch (RemoteException ex) {
            LOGGER.error("Remote error occured creating trade area annocation layer. Layer was not added", ex);
        }
        return id;
    }

    private double[] extractPoints(Coordinate[] coordinates) {
        double[] result = new double[coordinates.length * 2];
        for (int i = 0; i < coordinates.length; i++) {
            result[i * 2] = coordinates[i].x;
            result[i * 2 + 1] = coordinates[i].y;
        }
        return result;
    }

    private int[] generateFakeIds(int size) {
        int[] pks = new int[size];
        for (int i = 0; i < pks.length; i++) {
            pks[i] = i;
        }
        return pks;
    }
}
