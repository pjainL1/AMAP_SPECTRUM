package com.lo.info;

import com.korem.openlayers.parameters.IInfoParameters;
import com.lo.ContextParams;
import com.lo.analysis.Analysis;
import com.lo.config.Confs;
import com.lo.db.LODataSource;
import com.lo.util.DateParser;
import com.lo.util.Formatter;
import com.lo.util.PreparedStatementLogger;
import com.lo.util.SponsorFilteringManager;
import com.spinn3r.log5j.Logger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import org.apache.commons.dbutils.DbUtils;

/**
 *
 * @author jduchesne
 */
public class InfoAugmenter {

    private static final Logger log = Logger.getLogger(InfoAugmenter.class);
    private static ResourceBundle conf = ResourceBundle.getBundle("loLocalString");
    private Map<String, Augmenter> augmenters;

    public InfoAugmenter() {
        long start = System.currentTimeMillis();
        augmenters = new HashMap<String, Augmenter>();
        augmenters.put("FSA", createFSAAugmenter());
        augmenters.put(Analysis.TRADE_AREA.toString(), new TradeAreaAugmenter());
        augmenters.put(Analysis.LOCATIONS.toString(), new LocationsAugmenter());
        log.info((System.currentTimeMillis() - start) + " ms spent to execute InfoAugmenter constructor.");
    }

    public void augment(Map<String, Collection<Map<String, Object>>> infos,
            IInfoParameters params, ContextParams cp) throws Exception {
        long start = System.currentTimeMillis();
        Connection connection = LODataSource.getDataSource(cp).getConnection();
        try {
            for (Map.Entry<String, Collection<Map<String, Object>>> entry : infos.entrySet()) { 
                Augmenter augmenter = augmenters.get(entry.getKey());
                if (augmenter != null) {
                    try {                        
                        augmenter.prepare(connection, cp, params);
                        for (Map<String, Object> info : entry.getValue()) {
                            augmenter.augment(info, params, cp);
                        }
                    } catch (Exception e) {
                        log.error(null, e);
                    } finally {
                        augmenter.terminate();
                    }
                }
            }
        } finally {
            DbUtils.closeQuietly(connection);
            log.info((System.currentTimeMillis() - start) + " ms spent to execute augment method.");
        }
    }

    private Augmenter createFSAAugmenter() {
        return new Augmenter() {

            private PreparedStatement pstmt1;
            private PreparedStatement pstmt2;

            @Override
            public void prepare(Connection connection, ContextParams cp, IInfoParameters params) throws Exception {
                pstmt1 = connection.prepareStatement(getFirstQuery(cp, params));
                pstmt2 = connection.prepareStatement(Confs.QUERIES.infoAugmenterCreateFSAAugmenterThree());
            }
            
            private String getFirstQuery(ContextParams cp, IInfoParameters params) {
                String query = Confs.QUERIES.infoAugmenterCreateFSAAugmenterOne();
                String fragment = "";
                if (null != params.minTransactions() || null != params.minSpend() || null != params.minUnit()) {
                    fragment = SponsorFilteringManager.get().replaceSponsorKeysInQuery(Confs.QUERIES.taMinimumValuesFragment(), cp);
                }
                return String.format(query, cp.getSponsorKeysList(),fragment);
            }

            @Override
            public void augment(Map<String, Object> info, IInfoParameters params,
                    ContextParams cp) throws Exception {
                
                Formatter formatter = new Formatter();

                // prepare arguments
                DateParser dp = new DateParser();
                java.sql.Date from = dp.parse(params.from());
                java.sql.Date to = dp.parse(params.to());

                int amrpCollectors = 0;
                int sponsorCollectors = 0;
                String sponsorCode = "";

                int pstmtidx = 1;
                pstmt1.setString(pstmtidx++, (String) info.get("FSA"));
                pstmt1.setDate(pstmtidx++, from);
                pstmt1.setDate(pstmtidx++, to);
                if (null != params.minTransactions() || null != params.minSpend() || null != params.minUnit()) {
                    pstmt1.setDate(pstmtidx++, from);
                    pstmt1.setDate(pstmtidx++, to);
                    pstmt1.setInt(pstmtidx++, null == params.minTransactions() ? 0 : params.minTransactions());
                    pstmt1.setInt(pstmtidx++, null == params.minSpend() ? 0 : params.minSpend());
                    pstmt1.setInt(pstmtidx++, null == params.minUnit() ? 0 : params.minUnit());
                }
                pstmt2.setString(1, (String) info.get("FSA"));

                log.debug("FSA - anyinteract against universe...");
                long start = System.currentTimeMillis();
                PreparedStatementLogger.log(log, getFirstQuery(cp, params), new Object[] {info.get("FSA"), from, to});
                ResultSet rs = pstmt1.executeQuery();
                log.debug(String.format("FSA - done in %sms", System.currentTimeMillis() - start));
                try {
                    double spend = 0;
                    double unit = 0;
                    while (rs.next()) {
                        sponsorCollectors += rs.getInt("Collectors");
                        spend += rs.getDouble("Spend");
                        unit += rs.getDouble("Unit");
                        sponsorCode = rs.getString("Code");
                    }
                    info.put(MessageFormat.format(conf.getString("augmenter.label_sponsorcollectors"), cp.getSponsor().getRollupGroupCode()), formatter.getNumberNumberFormat().format(sponsorCollectors));
                    info.put(MessageFormat.format(conf.getString("augmenter.label_spend"), cp.getSponsor().getRollupGroupCode()), formatter.getCurrencyNumberFormat().format(spend));
                    info.put(MessageFormat.format(conf.getString("augmenter.label_unit"), cp.getSponsor().getRollupGroupCode()), formatter.getNumberNumberFormat().format((int)(int)unit));
                } finally {
                    rs.close();
                }

                start = System.currentTimeMillis();
                log.debug("FSA AMBASE - anyinteract against collectors...");
                PreparedStatementLogger.log(log, Confs.QUERIES.infoAugmenterCreateFSAAugmenterThree(), new Object[] {info.get("FSA")});
                rs = pstmt2.executeQuery();
                log.debug(String.format("FSA AMBASE - done in %sms", System.currentTimeMillis() - start));
                try {
                    while (rs.next()) {
                        amrpCollectors += rs.getInt("Collectors");
                    }
                } finally {
                    rs.close();
                }

                if (amrpCollectors > 0) {
                    info.put(conf.getString("augmenter.label_amrpcollectors"), formatter.getNumberNumberFormat().format(amrpCollectors));
                    info.put(MessageFormat.format(conf.getString("augmenter.label_sponsor_penetration"), cp.getSponsor().getRollupGroupCode()), formatter.getPercentNumberFormat().format((double) sponsorCollectors / amrpCollectors));
                }
            }

            @Override
            public void terminate() {
                try {
                    if (pstmt1 != null) {
                        pstmt1.close();
                    }
                } catch (Exception e) {
                }
                try {
                    if (pstmt2 != null) {
                        pstmt2.close();
                    }
                } catch (Exception e) {
                }
            }
        };
    }

}
