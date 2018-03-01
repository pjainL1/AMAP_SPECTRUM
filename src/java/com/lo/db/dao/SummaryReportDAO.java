package com.lo.db.dao;

import com.lo.ContextParams;
import com.lo.db.om.SummaryReport;
import java.rmi.RemoteException;
import java.util.ArrayList;
import com.lo.config.Confs;
import com.lo.db.helper.SimpleOraGeometryHelper;
import com.lo.db.om.SponsorGroup;
import com.lo.util.PreparedStatementLogger;
import com.lo.util.SponsorFilteringManager;
import com.spinn3r.log5j.Logger;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.oracle.OraWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import oracle.jdbc.OracleConnection;
import oracle.sql.STRUCT;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanMapHandler;
import org.apache.tomcat.dbcp.dbcp.DelegatingConnection;

/**
 *
 * @author slajoie
 */
public class SummaryReportDAO {

    private static final int MAX_QUERY_LOCATIONS = 999;

    private static final Logger log = Logger.getLogger();

    private static final String UNION = "UNION";

    private final AirMilesDAO dao;

    public SummaryReportDAO(AirMilesDAO dao) {
        this.dao = dao;
    }
    
    private String formatSponsorKeysPlaceholders(String query, List<Integer> sponsorKeys, Integer minTransactions, Integer minSpend, Integer minUnit) {
        String fragment = "";
        if (null != minTransactions || null != minSpend || null != minUnit) {
            fragment = Confs.QUERIES.taMinimumValuesFragment();
        }
        return String.format(query, AirMilesDAO.prepareInFragment(sponsorKeys.size()), fragment);
    }

    public List<SummaryReport> getSummaryReports(SponsorGroup sponsorGroup, Date from, Date to, double x, double y,
            List<Integer> sponsorKeys, Geometry geo, String mapInstanceKey, int key, Integer minTransactions, Integer minSpend , Integer minUnit, ContextParams cp) throws RemoteException {

        Connection conn = null;

        Map<BigDecimal, SummaryReport> summaryReports = new HashMap<>();

        try {
            conn = dao.getRunner(sponsorGroup).getDataSource().getConnection();

            Connection dconn = ((DelegatingConnection) conn).getInnermostDelegate();
            OraWriter writer = new OraWriter((OracleConnection) dconn);
            STRUCT struct = writer.write(geo);

            /* 1st query */
            Object[] params1 = buildParams(false, 0, from, to,x, y,  sponsorKeys, struct,minTransactions, minSpend, minUnit);
            String firstQuery = formatSponsorKeysPlaceholders(Confs.QUERIES.reportsSummarySummaryReport(), sponsorKeys, minTransactions, minSpend, minUnit);
            firstQuery = SponsorFilteringManager.get().replaceSponsorKeysInQuery(firstQuery, cp);
            PreparedStatementLogger.log("SummaryReport query", log, firstQuery, buildParams(false, 0, from, to,x, y,  sponsorKeys, struct, geo, minTransactions, minSpend, minUnit));
            
            final long locationsStart = System.currentTimeMillis();
            summaryReports = dao.getRunner(sponsorGroup).query(firstQuery, new BeanMapHandler<BigDecimal, SummaryReport>(SummaryReport.class, "locationKey"), params1);
            log.debug(String.format("SummaryReport query completed in %sms", System.currentTimeMillis() - locationsStart));

            /* 2nd query */
            List<String> locationsList = getLocationKeys(summaryReports.keySet().toArray());
            int i = 0;
            String fragment = "";
            if (null != minTransactions || null != minSpend || null != minUnit) {
                fragment = Confs.QUERIES.taMinimumValuesFragment();
            }
            String locationsQuery = String.format(Confs.QUERIES.reportsSummaryLocationTotal().replace("%FRAGMENT%", fragment), locationsList.get(i++));

            for (; i < locationsList.size(); i++) {
                String loc = String.format(Confs.QUERIES.reportsSummaryLocationTotal().replace("%FRAGMENT%", fragment), locationsList.get(i));
                locationsQuery += " " + UNION + " " + loc;
            }

            final Map<BigDecimal, SummaryReport> finalSummaryReports = summaryReports;

            // create params for the query: 1 sponsor key and 2 dates for each SQL clause
            Object[] params2 = buildParams(true, locationsList.size(), from, to,x ,y ,sponsorKeys, struct, minTransactions , minSpend , minUnit);
            locationsQuery = SponsorFilteringManager.get().replaceSponsorKeysInQuery(locationsQuery, cp);
            PreparedStatementLogger.log("Summary report totals query", log, locationsQuery, buildParams(true, locationsList.size(), from, to,x ,y ,sponsorKeys, struct, geo, minTransactions, minSpend, minUnit));
            
            final long totalsStart = System.currentTimeMillis();
            dao.getRunner(sponsorGroup).query(locationsQuery, new ResultSetHandler() {

                @Override
                public Object handle(ResultSet rs) throws SQLException {
                    log.debug(String.format("Summary report totals query completed in %sms", System.currentTimeMillis() - totalsStart));
                    
                    long parseStart = System.currentTimeMillis();
                    // loop through the result set
                    while (rs.next()) {
                        // get the location key
                        BigDecimal key = rs.getBigDecimal("locationKey");

                        // get the SummaryReport value associated with the previous key
                        SummaryReport sr = finalSummaryReports.get(key);

                        // set the totals 
                        sr.setTotalCollectors(rs.getBigDecimal("totalCollectors").intValue());
                        sr.setTotalTransactions(rs.getBigDecimal("totalTransactions").intValue());
                        sr.setTotalSpends(rs.getBigDecimal("totalSpends").intValue());
                        sr.setTotalUnits(rs.getBigDecimal("totalUnits").intValue());
                    }
                    log.debug(String.format("Summary report totals parsing completed in %sms", System.currentTimeMillis() - parseStart));
                    return null;
                }

            }, params2);

            log.debug("query completed");
        } catch (SQLException ex) {
            dao.log("An error occured retreiving summary report data.", ex.getMessage());
        } finally {
            DbUtils.closeQuietly(conn);
        }

        // creates an ArrayList from the Collection of summaryReport values set
        List<SummaryReport> results = new ArrayList(summaryReports.values());

        return results;
    }
    
    private Object[] buildParams(boolean isLocationTotal, int locationTotalSize, Date from, Date to, Double x, Double y, List<Integer> sponsorKeys, STRUCT struct, Integer minTransactions, Integer minSpend , Integer minUnit) {
        return buildParams(isLocationTotal, locationTotalSize, from, to, x, y, sponsorKeys, struct, null, minTransactions, minSpend, minUnit);
    }

    private Object[] buildParams(boolean isLocationTotal, int locationTotalSize, Date from, Date to, Double x, Double y, List<Integer> sponsorKeys, STRUCT struct, Geometry geomForDebugPrint,  Integer minTransactions, Integer minSpend , Integer minUnit) {
        List<Object> vargs = new ArrayList<Object>();

        if (isLocationTotal) {
            // This means we want to execute the 2nd query to get the totals.
            for (int i = 0; i < locationTotalSize; i++) {
                if (null != minTransactions || null != minSpend || null != minUnit) {
                    vargs.add(new java.sql.Date(from.getTime()));
                    vargs.add(new java.sql.Date(to.getTime()));
                    vargs.add(minTransactions == null ? 0 : minTransactions);
                    vargs.add(minSpend == null ? 0 : minSpend);
                    vargs.add(minUnit == null ? 0 : minUnit);
                }
                vargs.add(new java.sql.Date(from.getTime()));
                vargs.add(new java.sql.Date(to.getTime()));
            }
        } else {
            vargs.add(x);
            vargs.add(y);
            for (Integer sponsorKey: sponsorKeys) {
                vargs.add(sponsorKey);
            }
            vargs.add(new java.sql.Date(from.getTime()));
            vargs.add(new java.sql.Date(to.getTime()));
            if (geomForDebugPrint != null) {
                vargs.add(SimpleOraGeometryHelper.getInstance().getOracleGeometry(geomForDebugPrint));
            } else {
                vargs.add(struct);
            }
            if (null != minTransactions || null != minSpend || null != minUnit) {
                vargs.add(new java.sql.Date(from.getTime()));
                vargs.add(new java.sql.Date(to.getTime()));
                vargs.add(minTransactions == null ? 0 : minTransactions);
                vargs.add(minSpend == null ? 0 : minSpend);
                vargs.add(minUnit == null ? 0 : minUnit);
            }
        }

        return vargs.toArray(new Object[0]);
    }

    private List<String> getLocationKeys(Object[] locationKeys) {

        List<String> list = new ArrayList<>();
        int length = locationKeys.length;
        int i = 0;
        while (true) {
            int start = i;
            int finish = i + Math.min(length - i, MAX_QUERY_LOCATIONS);
            if (start >= length) {
                break; // last index inlocationKeys passed
            }

            // create a string from location keys in the index range (start,finish)
            StringBuilder locations = new StringBuilder("(");
            String firstLocationKey = Double.toString(((BigDecimal) locationKeys[start]).doubleValue());
            locations.append(firstLocationKey);

            for (int j = start + 1; j < finish; j++) {
                locations = locations.append(", ").append(locationKeys[j]);
            }
            locations = locations.append(")");
            list.add(locations.toString());

            // increase i
            i += MAX_QUERY_LOCATIONS;
        }

        return list;

    }

}
