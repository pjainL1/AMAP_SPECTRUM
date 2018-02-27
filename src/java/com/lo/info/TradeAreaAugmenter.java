package com.lo.info;

import com.korem.openlayers.parameters.IInfoParameters;
import com.lo.ContextParams;
import com.lo.analysis.Analysis;
import com.lo.analysis.tradearea.TradeArea;
import com.lo.config.Confs;
import com.lo.db.helper.OraReaderWriterHelper;
import com.lo.db.proxy.PostalCodeProxy;
import com.lo.util.DateParser;
import com.lo.util.Envelope;
import com.lo.util.Formatter;
import com.lo.util.PointInPolygonLocator;
import com.lo.util.PreparedStatementLogger;
import com.lo.util.SponsorFilteringManager;
import com.lo.util.WSClient;
import com.lo.util.WSClientLone;
import com.spinn3r.log5j.Logger;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import oracle.sql.STRUCT;
import org.apache.commons.dbutils.DbUtils;

/**
 *
 * @author YDumais
 */
public class TradeAreaAugmenter extends Augmenter {

    private static final Logger log = Logger.getLogger();
    private static ResourceBundle conf = ResourceBundle.getBundle("loLocalString");
    private static final PrecisionModel PRECISION_MODEL = new PrecisionModel(PrecisionModel.FLOATING);
    private static final int EARTH_RADIUS = 6378137;
    private static final int STMT_PREFETCH_SIZE = 150;
    private static final int SUMS_STMT_PREFETCH_SIZE = 2500;

    public static final String COLUMN_PK = "mipk";
    public static final String COLUMN_CODE = "Location code";
    public static final String COLUMN_KEY = "location_key";
    public static final String COLUMN_TA_TYPE = "ta_type";
    public static final String COLUMN_CUSTOMER_LOC_CODE = "customer_location_code";
    public static final String COLUMN_SPONSOR_CODE = "sponsor_code";
    private static final String LABEL_SIZE = "Trade Area Size";
    
    private static final String LABEL_INDENT = "&nbsp;";
    private static final String LABEL_MAIL = "Mailable";
    private static final String LABEL_EMAIL = "Emailable";
    private static final String LABEL_WEBACTIVE = "Web active";
    private static final String LABEL_MOBILE = "Mobile active";
    private static final String SPACER = "_SPACER_";
    private static final String LABEL_TOTALHOUSEHOLDS = "Households (Can. Post)";

    private PreparedStatement txnStmt, sumsStmt;
    private ContextParams contextParams;
    
    private static class TradeAreaCounts {
        private int totalMail;
        private int totalEmail;
        private int totalWebactive;
        private int totalMobile;
        private int total;
        private Formatter formatter;
        
        public TradeAreaCounts() {
            this(0, 0, 0, 0, 0);
        }

        public TradeAreaCounts(int totalMail, int totalEmail, int totalWebactive, int totalMobile, int total) {
            formatter = new Formatter();
            this.totalMail = totalMail;
            this.totalEmail = totalEmail;
            this.totalWebactive = totalWebactive;
            this.totalMobile = totalMobile;
            this.total = total;
        }
        
        public void addEntry(int totalMail, int totalEmail, int totalWebactive, int totalMobile) {
            this.totalMail += totalMail;
            this.totalEmail += totalEmail;
            this.totalWebactive += totalWebactive;
            this.totalMobile += totalMobile;
            this.total++;
        }

        public int getTotalMail() {
            return totalMail;
        }

        public int getTotalEmail() {
            return totalEmail;
        }

        public int getTotalWebactive() {
            return totalWebactive;
        }

        public int getTotalMobile() {
            return totalMobile;
        }

        public int getTotal() {
            return total;
        }
        
        public String percentageCount(int value){
            return String.format("%s (%s%%)", formatter.getNumberNumberFormat().format(value), Math.round(((float)value / total) * 100));
        }
    }

    @Override
    public void prepare(Connection connection, ContextParams cp, IInfoParameters params) throws Exception {
        contextParams = cp;
        txnStmt = connection.prepareStatement(getFirstQuery(cp, params));
        System.out.println(getFirstQuery(cp, params));
        txnStmt.setFetchSize(STMT_PREFETCH_SIZE);
        sumsStmt = connection.prepareStatement(getThirdQuery());
        sumsStmt.setFetchSize(SUMS_STMT_PREFETCH_SIZE);
    }
    
    private String getFirstQuery(ContextParams cp, IInfoParameters params) throws Exception{
        String fragment = "";
        if (null != params.minTransactions() || null != params.minSpend() || null != params.minUnit()){
            fragment = cp.getSponsorKeyFilter().replaceAll("sponsor_key", "u.sponsor_key") + " " + Confs.QUERIES.taMinimumValuesFragment();
        }else {
            fragment = cp.getSponsorKeyFilter().replaceAll("sponsor_key", "u.sponsor_key");
        }
        String initialFormat = String.format(Confs.QUERIES.infoTradeAreaSelectOnTransactions().replaceAll(" sponsor_key", " u.sponsor_key"), fragment);
        String query = initialFormat;
        return SponsorFilteringManager.get().replaceSponsorKeysInQuery(query, cp);
    }
    
    private String getThirdQuery() {
        return Confs.QUERIES.infoTradeAreaSumsOnCollectors();
    }

    @Override
    public void augment(Map<String, Object> info, IInfoParameters params, ContextParams cp) throws Exception {
        Formatter formatter = new Formatter();
        
        String layerID = WSClient.getMapService().getLayersIdByName(params.mapInstanceKey(),
                Analysis.TRADE_AREA.toString())[0];
        Integer mipk = (Integer) info.get(COLUMN_PK);
        double[] points = WSClientLone.getLayerService().getPointLists(
                params.mapInstanceKey(),
                layerID,
                "mipk",
                "" + mipk,
                Confs.STATIC_CONFIG.kmsCoordsys())[0];
        PointInPolygonLocator locator = new PointInPolygonLocator(points);
        Point centroid = locator.getPolygon().getCentroid();

        // prepare arguments
        java.sql.Date from = DateParser.parse(params.from());
        java.sql.Date to = DateParser.parse(params.to());

        String theLocationKey = (String) info.get(COLUMN_KEY);
        TradeArea.Type taType = TradeArea.Type.valueOf((String) info.get(COLUMN_TA_TYPE));
        String customerLocationCode = info.get(COLUMN_CUSTOMER_LOC_CODE) == null ? "" : (String) info.get(COLUMN_CUSTOMER_LOC_CODE);
        String sponsorCode = info.get(COLUMN_SPONSOR_CODE) == null ? "" : (String) info.get(COLUMN_SPONSOR_CODE);
        // we don't want sponsor_code to appear in the TA info. (but we still want it in the location info)
        info.remove(COLUMN_SPONSOR_CODE);
        info.remove(COLUMN_CUSTOMER_LOC_CODE);
        
        int idx = theLocationKey.lastIndexOf(".");
        if (idx != -1) {
            theLocationKey = theLocationKey.substring(0, idx);
        }
        STRUCT struct = OraReaderWriterHelper.getInstance().getOracleGeometry(locator.getPolygon());
        Envelope envelope = locator.getEnvelope();
        int pstmtIdx = 1;
   
        if (null != params.minTransactions() || null != params.minSpend() || null != params.minUnit()){
            txnStmt.setDate(pstmtIdx++, from);
            txnStmt.setDate(pstmtIdx++, to);
            txnStmt.setInt(pstmtIdx++, null == params.minTransactions() ? 0 : params.minTransactions());
            txnStmt.setInt(pstmtIdx++, null == params.minSpend() ? 0 : params.minSpend());
            txnStmt.setInt(pstmtIdx++, null == params.minUnit() ? 0 : params.minUnit());  
        }
            //txnStmt.setObject(pstmtIdx++, struct);
        txnStmt.setDouble(pstmtIdx++, envelope.getMinX());
        txnStmt.setDouble(pstmtIdx++, envelope.getMaxX());
        txnStmt.setDouble(pstmtIdx++, envelope.getMinY());
        txnStmt.setDouble(pstmtIdx++, envelope.getMaxY());
        
        txnStmt.setDate(pstmtIdx++, from);
        txnStmt.setDate(pstmtIdx++, to);
        log.debug("Search point in polygon on universe...");
        if (Confs.CONFIG.debug()) {
            Object[] args;
            if (null != params.minTransactions() || null != params.minSpend() || null != params.minUnit()){
               args = new Object[]{
                from, to, null == params.minTransactions() ? 0 : params.minTransactions(), null == params.minSpend() ? 0 : params.minSpend(), null == params.minUnit() ? 0 : params.minUnit(),
                envelope.getMinX(), envelope.getMaxX(), envelope.getMinY(), envelope.getMaxY(),
                from, to
            }; 
            }else {
                args = new Object[]{
                envelope.getMinX(), envelope.getMaxX(), envelope.getMinY(), envelope.getMaxY(),
                from, to
            }; 
            }

            PreparedStatementLogger.log(log, getFirstQuery(cp, params), args);
        }
        
        double dimension = 0.0;
        double sponsorSpend = 0;
        double locationSpend = 0;
        double sponsorUnit = 0;
        double locationUnit = 0;
        Set<Double> locationCollectorsSet = new HashSet<>(500);
        Set<Double> sponsorCollectorsSet = new HashSet<>(1000);
        
        long start = System.currentTimeMillis();
        txnStmt.execute();
        try (ResultSet rs = txnStmt.getResultSet()) {
            log.debug(String.format("Query transactions completed in %sms", System.currentTimeMillis() - start));
            
            start = System.currentTimeMillis();
            while (rs.next()) {
                if (!locator.contains(rs.getDouble("longitude"), rs.getDouble("latitude"))) {
                    continue;
                }
                
                double spend = rs.getDouble("spend");
                double unit = rs.getDouble("unit");
                double size = rs.getDouble("actual_distance");
                String locationKey = rs.getString("Location");
                final String collector = rs.getString("Collector");
                boolean isLocationTxn = locationKey.equals(theLocationKey);
                
                double collectorKey = Double.parseDouble(collector);
                sponsorCollectorsSet.add(collectorKey);
                if (isLocationTxn) {
                    locationCollectorsSet.add(collectorKey);
                }
                
                sponsorSpend += spend;
                sponsorUnit += unit;
                
                if (isLocationTxn) {
                    locationSpend += spend;
                    locationUnit += unit;
                    if (!useCentroidForDistanceCalculation(taType)) {
                        dimension = Math.max(size, dimension);
                    }
                }
                if (useCentroidForDistanceCalculation(taType)) {
                    double longitude = rs.getDouble("longitude");
                    double latitude = rs.getDouble("latitude");
                    double distanceToCentroid = getDistance(centroid, longitude, latitude);
                    dimension = Math.max(distanceToCentroid, dimension);
                }
            }
            log.debug(String.format("UNIVERSE Parse completed in %sms", System.currentTimeMillis() - start));
        }
        
        TradeAreaCounts taCounts = getTradeAreaCounts(struct, locator);
        int amrpCollectors = taCounts.getTotal();

        log.debug("Getting total households.");
        int totalHH = getTotalHouseHolds(struct, locator.getPolygon());
        log.debug("Total households query completed.");

        if (!sponsorCode.isEmpty()) {
            info.put(conf.getString("augmenter.label_sponsor_code"), sponsorCode);
        }
        info.put(LABEL_SIZE, formatter.getNumberNumberFormat().format(dimension) + " km");
        info.put(LABEL_TOTALHOUSEHOLDS, formatter.getNumberNumberFormat().format(totalHH));
        info.put(conf.getString("augmenter.label_amrpcollectors"), formatter.getNumberNumberFormat().format(amrpCollectors));
        
        info.put(LABEL_INDENT + LABEL_MAIL, taCounts.percentageCount(taCounts.getTotalMail()) );
        info.put(LABEL_INDENT + LABEL_EMAIL, taCounts.percentageCount(taCounts.getTotalEmail()) );
        info.put(LABEL_INDENT + LABEL_WEBACTIVE, taCounts.percentageCount(taCounts.getTotalWebactive()));
        info.put(LABEL_INDENT + LABEL_MOBILE, taCounts.percentageCount(taCounts.getTotalMobile()) );
        info.put("spacer1", SPACER);
        
        info.put(MessageFormat.format(conf.getString("augmenter.label_sponsorcollectors"),cp.getSponsor().getRollupGroupCode()), formatter.getNumberNumberFormat().format(sponsorCollectorsSet.size()));
        if (amrpCollectors > 0) {
            info.put(MessageFormat.format(conf.getString("augmenter.label_sponsor_penetration"),cp.getSponsor().getRollupGroupCode()), formatter.getPercentNumberFormat().format((double) sponsorCollectorsSet.size() / amrpCollectors));
        }
        info.put(MessageFormat.format(conf.getString("augmenter.label_spend"),cp.getSponsor().getRollupGroupCode()), formatter.getCurrencyNumberFormat().format(sponsorSpend));
        info.put(MessageFormat.format(conf.getString("augmenter.label_unit"),cp.getSponsor().getRollupGroupCode()), formatter.getNumberNumberFormat().format((int)sponsorUnit));
        info.put("spacer2", SPACER);
        info.put(MessageFormat.format(conf.getString("augmenter.label_locationcollectors"), customerLocationCode ), formatter.getNumberNumberFormat().format(locationCollectorsSet.size()));
        if (sponsorCollectorsSet.size() > 0) {
            info.put(MessageFormat.format(conf.getString("augmenter.label_location_sponsor_penetration"),customerLocationCode), formatter.getPercentNumberFormat().format((double) locationCollectorsSet.size() / sponsorCollectorsSet.size()));
        }
        info.put(MessageFormat.format(conf.getString("augmenter.label_lacationamspend"),customerLocationCode), formatter.getCurrencyNumberFormat().format(locationSpend));
        info.put(MessageFormat.format(conf.getString("augmenter.label_locationamunit"),customerLocationCode), formatter.getNumberNumberFormat().format((int)locationUnit));
    }
    
    private boolean useCentroidForDistanceCalculation(TradeArea.Type taType) {
        return taType == TradeArea.Type.custom || taType == TradeArea.Type.projected;
    }

    private TradeAreaCounts getTradeAreaCounts(STRUCT struct, PointInPolygonLocator locator) throws SQLException {
        long start = System.currentTimeMillis();
        
        Envelope envelope = locator.getEnvelope();
        PreparedStatementLogger.log("Running count query", log, getThirdQuery(), new Object[] {envelope.getMinX(), envelope.getMaxX(),
                envelope.getMinY(), envelope.getMaxY()});
        log.debug(String.format("LONGITUDE BETWEEN %s AND %s AND LATITUDE BETWEEN %s AND %s", 
                envelope.getMinX(), envelope.getMaxX(),
                envelope.getMinY(), envelope.getMaxY()));
        
        int i = 1;
        //sumsStmt.setObject(++i, struct);
        sumsStmt.setDouble(i++, envelope.getMinX());
        sumsStmt.setDouble(i++, envelope.getMaxX());
        sumsStmt.setDouble(i++, envelope.getMinY());
        sumsStmt.setDouble(i++, envelope.getMaxY());
        sumsStmt.execute();
        log.debug(String.format("Count Query completed in %sms", System.currentTimeMillis() - start));
        
        start = System.currentTimeMillis();
        try (ResultSet rs = sumsStmt.getResultSet()) {
            int mailableCount = 0;
            int emailableCount = 0;
            int webCount = 0;
            int mobileCount = 0;
            int totalCount = 0;
            // seems faster to count in java then doing sums in SQL query.
            while (rs.next()) {
                if (!locator.contains(rs.getDouble("longitude"), rs.getDouble("latitude"))) {
                    continue;
                }
                
                mailableCount += rs.getInt("PROMO_MAILABLE_FLAG");
                emailableCount += rs.getInt("EMAILABLE_FLAG");
                webCount += rs.getInt("WEB_ACTIVITY_FLAG");
                mobileCount += rs.getInt("MOBILE_APP_ACTIVITY_FLAG");
                totalCount += rs.getInt("total");
            }
            return new TradeAreaCounts(
                    mailableCount, 
                    emailableCount, 
                    webCount, 
                    mobileCount, 
                    totalCount);
        } finally {
            log.debug(String.format("Parse count completed in %sms", System.currentTimeMillis() - start));
        }
    }

    private int getTotalHouseHolds(STRUCT struct, Geometry geom) {
        try (PostalCodeProxy proxy = new PostalCodeProxy(contextParams.getSponsor())) {
            return proxy.getTotalHouseholds(struct, geom);
        } catch (SQLException e) {
            log.error("Can't get total households", e);
        }
        
        return -1;
    }
    
    private double getDistance(Point centroid, double longitude, double latitude) {
        GeometryFactory factory = new GeometryFactory(PRECISION_MODEL, Confs.STATIC_CONFIG.SRID());
        Coordinate[] coords = new Coordinate[]{new Coordinate(longitude, latitude)};
        Point point = new Point(new CoordinateArraySequence(coords), factory);
        return angularToMeters(centroid.distance(point)) / 1000d;
    }

    private double angularToMeters(double angular) {
        return angular * (Math.PI / 180) * EARTH_RADIUS;
    }

    @Override
    public void terminate() {
        try {
            DbUtils.close(txnStmt);
        } catch (SQLException ex) {
            log.error(null, ex);
        }
        try {
            DbUtils.close(sumsStmt);
        } catch (SQLException ex) {
            log.error(null, ex);
        }
    }
}
