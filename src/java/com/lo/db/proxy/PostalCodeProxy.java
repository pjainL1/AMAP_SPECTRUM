package com.lo.db.proxy;

import com.korem.Proxy;
import com.lo.ContextParams;
import com.lo.analysis.tradearea.TradeArea;
import static com.lo.analysis.tradearea.TradeArea.Type.projected;
import com.lo.config.Confs;
import com.lo.db.helper.OraReaderWriterHelper;
import com.lo.db.om.SponsorGroup;
import com.lo.util.PreparedStatementLogger;
import com.spinn3r.log5j.Logger;
import com.vividsolutions.jts.geom.Geometry;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import oracle.sql.STRUCT;

/**
 * @author Charles St-Hilaire for Korem inc.
 */
public class PostalCodeProxy extends Proxy {

    private static final Logger LOGGER = Logger.getLogger();

    public PostalCodeProxy(SponsorGroup sponsorGroup) throws SQLException {
        super(sponsorGroup);
    }

    public List<String[]> getPostalCodeDistance(Double sponsorKey, Geometry geom, Map<Double, List<String>> postalCodesSeen) throws SQLException {
        List<String[]> info = new ArrayList<>();
        PreparedStatement ps = super.prepare(Confs.QUERIES.postalCodesDistanceSelect());
        List<String> seenList = getSeenList(postalCodesSeen, sponsorKey);

        if (ps != null) {
            ps.setString(1, sponsorKey.intValue() + "");
            ps.setObject(2, OraReaderWriterHelper.getInstance().getOracleGeometry(geom));
            PreparedStatementLogger.log("Get postal codes", LOGGER, Confs.QUERIES.postalCodesDistanceSelect(), new Object[]{sponsorKey.intValue() + "", geom});
            try (ResultSet rs = ps.executeQuery()) {
                if (rs != null) {
                    while (rs.next()) {
                        double distance = rs.getDouble("DISTANCE");
                        String postalCode = rs.getString("POSTAL_CODE");
                        if (!seenList.contains(postalCode)) {
                            seenList.add(postalCode);
                            String roundedDistance = String.valueOf((double) Math.round(distance * 1000) / 1000);
                            info.add(new String[]{
                                postalCode,
                                rs.getString("SPONSOR_LOCATION_CODE"),
                                roundedDistance
                            });
                        }
                    }
                }
            }
        }
        return info;
    }

    private List<String> getSeenList(Map<Double, List<String>> postalCodesSeen, Double locationKey) {
        List<String> list = postalCodesSeen.get(locationKey);
        if (list == null) {
            list = new ArrayList<>();
            postalCodesSeen.put(locationKey, list);
        }

        return list;
    }

    public List<String[]> getPostalCodeOnlyOrProjected(TradeArea ta, Geometry geom, Map<Double, List<String>> postalCodesSeen) throws SQLException {
        List<String[]> info = new LinkedList<>();

        PreparedStatement ps;
        if (ta.isProjected()) {
            ps = prepare(Confs.QUERIES.postalCodesAndDistanceForProjectedLocations());
        } else {
            ps = prepare(Confs.QUERIES.postalCodesOnlySelect());
        }
        
        List<String> seenList = getSeenList(postalCodesSeen, null);
        if (ps != null) {
            int i = 0;
            if (ta.isProjected()) {
                ps.setDouble(++i, ta.getProjectedLongitude());
                ps.setDouble(++i, ta.getProjectedLatitude());
            }
            ps.setObject(++i, OraReaderWriterHelper.getInstance().getOracleGeometry(geom));

            PreparedStatementLogger.log("Get postal codes only", LOGGER, Confs.QUERIES.postalCodesOnlySelect(), new Object[]{geom});
            try (ResultSet rs = ps.executeQuery()) {
                if (rs != null) {
                    while (rs.next()) {
                        String postalCode = rs.getString("POSTAL_CODE");
                        String roundedDistance = "";
                        if (ta.isProjected()) {
                            double distance = rs.getDouble("DISTANCE");
                            roundedDistance = String.valueOf((double) Math.round(distance * 1000) / 1000);
                        }
                        if (!seenList.contains(postalCode)) {
                            info.add(new String[]{postalCode, ta.isProjected() ? "projected" : "", ta.isProjected() ? roundedDistance : ""});
                        }
                    }
                }
            } 
        }
        return info;
    }

    public int getTotalHouseholds(Geometry geom) throws SQLException {
        return getTotalHouseholds(OraReaderWriterHelper.getInstance().getOracleGeometry(geom), geom);
    }

    public int getTotalHouseholds(STRUCT struct, Geometry geom) throws SQLException {
        int totalHH = 0;
        LOGGER.debug("Getting total households values for trade area polygon.");
        long time = System.currentTimeMillis();
        PreparedStatement ps = prepare(Confs.QUERIES.postalCodesHouseHoldsSelect());
        ps.setObject(1, struct);
        PreparedStatementLogger.log("Get total households", LOGGER, Confs.QUERIES.postalCodesHouseHoldsSelect(), new Object[]{geom});
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                totalHH += rs.getInt("TOTALHH");
            }
        }
        LOGGER.debug(String.format("Total households found in %sms.", System.currentTimeMillis() - time));
        return totalHH;
    }
}
