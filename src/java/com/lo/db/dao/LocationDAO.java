package com.lo.db.dao;

import com.lo.ContextParams;
import java.util.ArrayList;
import com.lo.analysis.tradearea.TradeArea;
import com.lo.analysis.tradearea.TradeAreaMethod;
import com.lo.analysis.tradearea.builder.CustomTABuilder;
import com.lo.config.Confs;
import com.lo.db.helper.OraReaderWriterHelper;
import com.lo.db.om.Location;
import com.lo.db.om.SponsorGroup;
import com.lo.util.StyleUtils;
import com.spinn3r.log5j.Logger;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Date;
import java.sql.SQLException;
import java.util.List;
import oracle.sql.STRUCT;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ResourceBundle;

/**
 *
 * @author ydumais
 */
public class LocationDAO {

    private static final Logger log = Logger.getLogger();
    private String query;
    private static final String SPACE = " ";
    private static final String UNION = "union";
    private Object[] params;
    private final AirMilesDAO dao;
    private ResultSet rsTradeAreaHistory;
    private static final String JSON_DATE_FORMAT = "yyyyMMdd";

    public ResultSet getRsTradeAreaHistory() {
        return rsTradeAreaHistory;
    }

    public void setRsTradeAreaHistory(ResultSet rsTradeAreaHistory) {
        this.rsTradeAreaHistory = rsTradeAreaHistory;
    }

    public LocationDAO(AirMilesDAO dao) {
        this.query = Confs.QUERIES.locationsDefaultQuery();
        this.dao = dao;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public void setParams(Object[] params) {
        this.params = params;
    }

    public List<Location> retrieveLocations(SponsorGroup sponsorGroup) {
        List<Location> result = new ArrayList<Location>();
        try {
            dao.log("retreiveLocations", query);
            result = dao.getRunner(sponsorGroup).query(query, new BeanListHandler<Location>(Location.class), params);
        } catch (SQLException ex) {
            dao.log("An error occured retreiving location list from db. Returning an empty list.", ex.getMessage());
        }
        return result;
    }

    public Location getLocation(SponsorGroup sponsorGroup, double locationKey) throws SQLException {
        Object[] qParams = new Object[]{locationKey};
        ResultSetHandler<Location> handler = new BeanHandler(Location.class);
        dao.log("getLocation", Confs.QUERIES.locationsQueryLocationKey());
        return dao.getRunner(sponsorGroup).query(Confs.QUERIES.locationsQueryLocationKey(), handler, qParams);
    }

    public Location getLocationByCode(ContextParams cp, String locationCode) throws SQLException {
        Object[] qParams = new Object[]{locationCode};
        ResultSetHandler<Location> handler = new BeanHandler(Location.class);
        dao.log("getLocation", Confs.QUERIES.locationsQueryLocationCode());
        return dao.getRunner(cp).query(Confs.QUERIES.locationsQueryLocationCode(), handler, qParams);
    }

    public void setColorLocation(ContextParams cp, String sponsorLocationKey, String color, String colorFlag) {
        try {
            if (colorFlag.equalsIgnoreCase("TA")) {
                dao.getLoneRunner().update(Confs.CONSOLE_QUERIES.tradeAreaLocationColorUpdateTaColor(), color, sponsorLocationKey);
            } else {
                dao.getLoneRunner().update(Confs.CONSOLE_QUERIES.tradeAreaLocationColorUpdateNwatchColor(), color, sponsorLocationKey);
            }
        } catch (SQLException ex) {
            dao.log("An error occured updating the SPONSOR_LOCATION_COLORS", ex.getMessage());
        }
    }

    public String getNWatchColorLocation(ContextParams cp, int locationID) {
        String colorCode = "";
        try {
            ResultSetHandler<String> handler = new ResultSetHandler<String>() {
                @Override
                public String handle(ResultSet rs) throws SQLException {
                    rs.next();
                    return rs.getString("NWATCH_COLOR");
                }
            };
            colorCode = dao.getRunner(cp).query(Confs.CONSOLE_QUERIES.tradeAreaLocationGetColor(), handler, locationID);
        } catch (SQLException ex) {
            dao.log("An error occured retreiving a NWatch color  from db.", ex.getMessage());
        }
        return colorCode;
    }

    public String getTradeAreaColorLocation(SponsorGroup sponsorGroup, int locationID) {
        String colorCode = "";
        try {
            ResultSetHandler<String> handler = new ResultSetHandler<String>() {
                @Override
                public String handle(ResultSet rs) throws SQLException {
                    rs.next();
                    return rs.getString("TA_COLOR");
                }
            };
            colorCode = dao.getRunner(sponsorGroup).query(Confs.CONSOLE_QUERIES.tradeAreaLocationGetColor(), handler, locationID);

        } catch (SQLException ex) {
            dao.log("An error occured retreiving a Trade Area color  from db.", ex.getMessage());
        }
        return colorCode;
    }
    
    public String getSpecTradeAreaColorLocation(String mapInstanceKey) {
        String colorCodes = "";
        final List<String> locationWithColor = new ArrayList<String>();
        try {
            ResultSetHandler<String> handler = new ResultSetHandler<String>() {
                @Override
                public String handle(ResultSet rs) throws SQLException {
                    while (rs.next()){
                        
                    String colorCode = rs.getString("SPONSOR_LOCATION_KEY") + "~#" + rs.getString("TA_COLOR"); // to create locationcode and color code string e.g 862~#FF0000
                    locationWithColor.add(colorCode);
                    }
                    String listString = String.join(",", locationWithColor);
                    return listString;
                }
            };
            colorCodes = dao.getLoneRunner().query(Confs.QUERIES.spectradeareaLocationGetColor(), handler, mapInstanceKey);

        } catch (SQLException ex) {
            dao.log("An error occured retreiving a Spectrum Trade Area color  from db.", ex.getMessage());
        }
        return colorCodes;
    }
    
    public Integer insertTradeAreaPolygon(String mapInstanceKeySpec,List<TradeArea> ta,int[] mipk) throws SQLException{
        String insertTradeAreaQuery = Confs.QUERIES.tradeareaInsertPolygon();
        Integer numberOfRecords = 0;
        Connection conn = null;
        //int[] mipk = generateFakeIds(ta.size());
        //LocationDAO dao = new LocationDAO(new AirMilesDAO());
        int i = 0;
        try {
            STRUCT struct;
            
            OraReaderWriterHelper oraWriter = OraReaderWriterHelper.getInstance();

            for (TradeArea elt : ta) {
                

                struct = oraWriter.getOracleGeometry(elt.getGeometry());

                dao.getLoneRunner().update(insertTradeAreaQuery, mapInstanceKeySpec,struct,mipk[i],elt.getLocationKey());
                i = i + 1;
            }
            numberOfRecords = i;
        } catch (SQLException ex) {
            dao.log("An error occured inserting a Trade Area to LIM_TA_POLYGON.", ex.getMessage());
        } 
        return numberOfRecords;
    }
    
    public Boolean truncateTradeAreaPolygon() throws SQLException{
        String truncateTradeAreaQuery = Confs.QUERIES.tradeareaTruncatePolygon();
        Boolean truncateStatus;
        //int[] mipk = generateFakeIds(ta.size());
        //LocationDAO dao = new LocationDAO(new AirMilesDAO());

        try {
                 dao.getLoneRunner().update(truncateTradeAreaQuery);
                 truncateStatus = true;
        } catch (SQLException ex) {
            dao.log("An error occured inserting a Trade Area to LIM_TA_POLYGON.", ex.getMessage());
            truncateStatus = false;
        } 
        return truncateStatus;
    }
    
    public Integer deleteTradeAreaPolygon(String mapInstanceKeySpec) throws SQLException{
        String deleteTradeAreaQuery = Confs.QUERIES.tradeareaDeletePolygon();
        Integer numberOfRecords = 0;
        Connection conn = null;
        //int[] mipk = generateFakeIds(ta.size());
        //LocationDAO dao = new LocationDAO(new AirMilesDAO());

        try {
                 numberOfRecords = dao.getLoneRunner().update(deleteTradeAreaQuery, mapInstanceKeySpec);
            
        } catch (SQLException ex) {
            dao.log("An error occured inserting a Trade Area to LIM_TA_POLYGON.", ex.getMessage());
        } 
        return numberOfRecords;
    }

    public Integer insertToHistoryTable(List<TradeArea> ta, ContextParams params, String from, String to, String bounds, String sponsorLocation, TradeAreaMethod.IParams ip) throws ParseException, SQLException {
        String insertToHistory = Confs.CONSOLE_QUERIES.taHistoryInsert();
        Integer numberOfRecords = 0;
        Connection conn = null;

        try {
            STRUCT struct;
            String typeDetail = "";
            OraReaderWriterHelper oraWriter = OraReaderWriterHelper.getInstance();

            SimpleDateFormat formatter = new SimpleDateFormat(JSON_DATE_FORMAT);
            Date fromDate = formatter.parse(from);
            java.sql.Date sqlFromDate = new java.sql.Date(fromDate.getTime());
            Date toDate = formatter.parse(to);
            java.sql.Date sqlToDate = new java.sql.Date(toDate.getTime());

            for (TradeArea elt : ta) {
                if (elt.getType() == TradeArea.Type.issuance) {
                    Double roundedValue = ip.issuance()* 100;
                    typeDetail = roundedValue.intValue()  + "%";
                } else {
                    if (elt.getType() == TradeArea.Type.distance) {
                        typeDetail = ip.distance() + "km";
                    } else if (elt.getType() == TradeArea.Type.projected) {
                        typeDetail = ip.projected() + "km";
                    } else {
                        typeDetail = "NA";
                    }
                }

                Double locationKey = null;
                Double defaultLocationKey = CustomTABuilder.getDefaultLocationKey();
                if (elt.getLocationKey() != null && !elt.getLocationKey().equals(defaultLocationKey)) {
                    locationKey = elt.getLocationKey();
                }

                struct = oraWriter.getOracleGeometry(elt.getGeometry());

                //conversion to mapBasic style
                String mapBasicStyle;

                if (elt.getType() != TradeArea.Type.custom) {
                    com.mapinfo.graphics.Rendition rendition = com.lo.util.RenditionUtil.create(elt.getRendition());
                    mapBasicStyle = StyleUtils.getPolygonRendition(rendition);
                } else {
                    ResourceBundle rb;
                    rb = ResourceBundle.getBundle("com.lo.util.color");
                    mapBasicStyle = rb.getString("ta.template.custom.map.basic");
                }
                String type = ip.tradearea().contains("units") ? "unitIssuance" : elt.getType().toString();
                numberOfRecords = dao.getLoneRunner().update(insertToHistory, 
                        params.getUser().getLogin(), 
                        type,
                        typeDetail, 
                        sqlFromDate, sqlToDate, 
                        locationKey, mapBasicStyle, struct, 
                        params.getSponsor().getRollupGroupCode());
            }
        } catch (SQLException ex) {
            dao.log("An error occured inserting a Trade Area to TA_HISTORY.", ex.getMessage());
        } 
        //finally {
//            if (conn != null) {
//                conn.close();
//            }
//        }
        return numberOfRecords;
    }
}
