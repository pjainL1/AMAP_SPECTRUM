package com.lo.db.dao;

import com.lo.Config;
import com.lo.ContextParams;
import com.lo.config.Confs;
import com.lo.db.om.SponsorGroup;
import com.lo.db.om.Transaction;
import com.lo.util.PreparedStatementLogger;
import com.lo.util.SponsorFilteringManager;
import com.spinn3r.log5j.Logger;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.oracle.OraWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import oracle.jdbc.OracleConnection;
import java.sql.Connection;
import oracle.sql.STRUCT;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.tomcat.dbcp.dbcp.DelegatingConnection;

/**
 *
 * @author slajoie
 */
public class TransactionDAO {

    private static final Logger log = Logger.getLogger();
    private static final String SPACE = " ";
    private static final String UNION = "union";
    public static final Double[] DISTANCE_BANDS = new Double[]{2.0, 5.0, 10.0, 15.0, 25.0, 30.0, 50.0};
    private AirMilesDAO dao;

    public TransactionDAO(AirMilesDAO dao) {
        this.dao = dao;
    }

    public List<Transaction> getProjectedAMTransactions(SponsorGroup sponsorGroup, List<Geometry> geoms){
        List<Transaction> result = new ArrayList();

        Connection conn = null;
        try {
            conn = dao.getRunner(sponsorGroup).getDataSource().getConnection();
            Connection dconn = ((DelegatingConnection) conn).getInnermostDelegate();
            OraWriter writer = new OraWriter((OracleConnection) dconn);

            List<STRUCT> structs = new ArrayList();
            for (Geometry geom : geoms) {
                structs.add(writer.write(geom));
            }

            String query = buildProjectedAMQuery();
            List<Object> paramsForPrint = new ArrayList<>();
            Object[] params = buildProjectedAMParams(structs, geoms, paramsForPrint);
            
            PreparedStatementLogger.log("getProjectedAMTransactions", log, query, paramsForPrint.toArray());

            result = dao.getRunner(sponsorGroup).query(query, new BeanListHandler<>(Transaction.class), params);
        } catch (SQLException ex) {
            dao.log("An error occured retreiving summary report data.", ex.getMessage());
        } finally {
            DbUtils.closeQuietly(conn);
        }
        return result;
    }

    public List<Transaction> getProjectedSponsorTransactions(Date from, Date to, List<Integer> sponsorKeys, List<Geometry> geoms, SponsorGroup sponsorGroup, String sponsorKeysList,Integer minTransactions, Integer minSpend, Integer minUnit){
        List<Transaction> result = new ArrayList();

        Connection conn = null;
        try {
            conn = dao.getRunner(sponsorGroup).getDataSource().getConnection();

            Connection dconn = ((DelegatingConnection) conn).getInnermostDelegate();
            OraWriter writer = new OraWriter((OracleConnection) dconn);

            List<STRUCT> structs = new ArrayList();
            for (Geometry geom : geoms) {
                structs.add(writer.write(geom));
            }

            String query = buildProjectedSponsorQuery(sponsorKeysList, minTransactions , minSpend , minUnit);
            List<Object> paramsForPrint = new ArrayList<>();
            Object[] params = buildProjectedSponsorParams(from, to, structs, geoms, paramsForPrint, minTransactions , minSpend , minUnit, sponsorKeysList );
            
            PreparedStatementLogger.log("getProjectedSponsorTransactions", log, query, paramsForPrint.toArray());
            long start = System.currentTimeMillis();
            result = dao.getRunner(sponsorGroup).query(query, new BeanListHandler<>(Transaction.class), params);
            log.debug(String.format("Completed getProjectedSponsorTransactions in %sms", System.currentTimeMillis() - start));
        } catch (SQLException ex) {
            dao.log("An error occured retreiving summary report data.", ex.getMessage());
        } finally {
            DbUtils.closeQuietly(conn);
        }
        return result;
    }

    public List<Transaction> getLocationTransactions(Date from, Date to, List<Integer> sponsorKeys, double locationKey, SponsorGroup sponsorGroup, Integer minTransactions, Integer minSpend, Integer minUnit, ContextParams cp) {
        List<Transaction> result = new ArrayList<Transaction>();
        try {
            String query = buildLocationQuery(minTransactions, minSpend, minUnit);
            Object[] params = buildLocationParams(from, to, locationKey, minTransactions, minSpend, minUnit);
            query = SponsorFilteringManager.get().replaceSponsorKeysInQuery(query, cp);
            PreparedStatementLogger.log("getLocationTransactions", log, query, params);
            long start = System.currentTimeMillis();
            result = dao.getRunner(sponsorGroup).query(query, new BeanListHandler<Transaction>(Transaction.class), params);
            log.debug(String.format("Completed getLocationTransactions in %sms", System.currentTimeMillis() - start));
        } catch (SQLException ex) {
            log.error("Error retreiving collectors. Returning an empty list.", ex);
        }
        return result;
    }

    private Object[] buildProjectedAMParams(List<STRUCT> structs, List<Geometry> geoms, List<Object> paramsForPrint) {
        List<Object> vargs = new ArrayList<Object>();
        for (int i = 0; i < structs.size(); i++) {
            vargs.add(i);
            paramsForPrint.add(i);
            vargs.add(structs.get(i));
            paramsForPrint.add(geoms.get(i));
        }
        return vargs.toArray(new Object[0]);
    }

    private Object[] buildProjectedSponsorParams(Date from, Date to, List<STRUCT> structs, List<Geometry> geoms, List<Object> paramsForPrint, Integer minTransactions ,Integer minSpend ,Integer minUnit, String sponsorKeysList) {
        List<Object> vargs = new ArrayList<Object>();
        for (int i = 0; i < structs.size(); i++) {
            Geometry geom = geoms.get(i);
            vargs.add(i);
            paramsForPrint.add(i);
            
            vargs.add(new java.sql.Date(from.getTime()));
            paramsForPrint.add(new java.sql.Date(from.getTime()));
            vargs.add(new java.sql.Date(to.getTime()));
            paramsForPrint.add(new java.sql.Date(to.getTime()));
            
            vargs.add(structs.get(i));
            paramsForPrint.add(geom);
            
            if (null != minTransactions || null != minSpend || null != minUnit) {
                vargs.add(new java.sql.Date(from.getTime()));
                vargs.add(new java.sql.Date(to.getTime()));
                vargs.add(null != minTransactions ? minTransactions : 0);
                vargs.add(null != minSpend ? minSpend : 0);
                vargs.add(null != minUnit ? minUnit : 0);
                
                paramsForPrint.add(new java.sql.Date(from.getTime()));
                paramsForPrint.add(new java.sql.Date(to.getTime()));
                paramsForPrint.add(null != minTransactions ? minTransactions : 0);
                paramsForPrint.add(null != minSpend ? minSpend : 0);
                paramsForPrint.add(null != minUnit ? minUnit : 0);
            }
        }
        return vargs.toArray(new Object[0]);
    }

    private Object[] buildLocationParams(Date from, Date to, Double locationKey,Integer minTransactions, Integer minSpend, Integer minUnit) {
        List<Object> vargs = new ArrayList<Object>();
        Integer[] driveDistances = Config.getInstance().getDriveDistances();
        for (int i = 0; i < driveDistances.length - 1; i++) {
            vargs.add(i);
            vargs.add(locationKey);
            vargs.add(driveDistances[i]);
            vargs.add(driveDistances[i + 1]);
            vargs.add(new java.sql.Date(from.getTime()));
            vargs.add(new java.sql.Date(to.getTime()));
            if (null != minTransactions || null != minSpend || null != minUnit) {
                vargs.add(new java.sql.Date(from.getTime()));
                vargs.add(new java.sql.Date(to.getTime()));
                vargs.add(null != minTransactions ? minTransactions : 0);
                vargs.add(null != minSpend ? minSpend : 0);
                vargs.add(null != minUnit ? minUnit : 0);
            }

        }
        vargs.add(7);
        vargs.add(locationKey);
        vargs.add(50);
        vargs.add(new java.sql.Date(from.getTime()));
        vargs.add(new java.sql.Date(to.getTime()));
        if (null != minTransactions || null != minSpend || null != minUnit) {
            vargs.add(new java.sql.Date(from.getTime()));
            vargs.add(new java.sql.Date(to.getTime()));
            vargs.add(null != minTransactions ? minTransactions : 0);
            vargs.add(null != minSpend ? minSpend : 0);
            vargs.add(null != minUnit ? minUnit : 0);
        }

        vargs.add(8);
        vargs.add(locationKey);
        vargs.add(-1);
        vargs.add(new java.sql.Date(from.getTime()));
        vargs.add(new java.sql.Date(to.getTime()));
        if (null != minTransactions || null != minSpend || null != minUnit) {
            vargs.add(new java.sql.Date(from.getTime()));
            vargs.add(new java.sql.Date(to.getTime()));
            vargs.add(null != minTransactions ? minTransactions : 0);
            vargs.add(null != minSpend ? minSpend : 0);
            vargs.add(null != minUnit ? minUnit : 0);
        }

        return vargs.toArray(new Object[0]);
    }

    private String buildProjectedAMQuery() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            sb.append(Confs.QUERIES.reportsDistanceDecayProjectedAmTransactions()).append(SPACE).append(UNION).append(SPACE);
        }
        sb.append(Confs.QUERIES.reportsDistanceDecayProjectedAmTransactions()).append(SPACE).append("order by 2");
        return sb.toString();
    }
    
    private String getQueryWithSponsorKeys(String query, String sponsorKeysList) {
        return String.format(query, sponsorKeysList);
    }

    private String buildProjectedSponsorQuery(String sponsorKeysList, Integer minTransactions, Integer minSpend, Integer minUnit) {
        String fragment = "";
        if (null != minTransactions || null != minSpend || null != minUnit) {
            fragment = SponsorFilteringManager.get().replaceSponsorKeysInQuery(Confs.QUERIES.taMinimumValuesFragment(), sponsorKeysList);
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            sb.append(getQueryWithSponsorKeys(Confs.QUERIES.reportsDistanceDecayProjectedSponsor().replace("%FRAGMENT%", fragment), sponsorKeysList)).append(SPACE).append(UNION).append(SPACE);
        }
        sb.append(getQueryWithSponsorKeys(Confs.QUERIES.reportsDistanceDecayProjectedSponsor().replace("%FRAGMENT%", fragment), sponsorKeysList)).append(SPACE).append("order by 2");
        return sb.toString();
    }

    private String buildLocationQuery(Integer minTransactions, Integer minSpend, Integer minUnit) {
        String fragment = "";
        if (null != minTransactions || null != minSpend || null != minUnit) {
            fragment = Confs.QUERIES.taMinimumValuesFragment();
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            sb.append(Confs.QUERIES.reportsDistanceDecayLocationTransactionsFirst().replace("%FRAGMENT%", fragment)).append(SPACE).append(UNION).append(SPACE);
        }
        sb.append(Confs.QUERIES.reportsDistanceDecayLocationTransactionsFirst().replace("%FRAGMENT%", fragment)).append(SPACE).append(UNION).append(SPACE);
        sb.append(Confs.QUERIES.reportsDistanceDecayLocationTransactionsSecond().replace("%FRAGMENT%", fragment)).append(SPACE).append(UNION).append(SPACE);
        sb.append(Confs.QUERIES.reportsDistanceDecayLocationTransactionsThird().replace("%FRAGMENT%", fragment)).append(SPACE).append("order by 5");
        return sb.toString();
    }
}
