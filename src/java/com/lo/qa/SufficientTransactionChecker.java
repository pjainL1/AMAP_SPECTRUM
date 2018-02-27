package com.lo.qa;

import com.korem.openlayers.parameters.IApplyParameters;
import com.lo.ContextParams;
import com.lo.config.Confs;
import com.lo.db.dao.AirMilesDAO;
import com.lo.util.DateParser;
import com.lo.util.DateType;
import com.lo.util.PreparedStatementLogger;
import com.lo.util.SponsorFilteringManager;
import com.spinn3r.log5j.Logger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;

/**
 *
 * @author YDumais
 */
public class SufficientTransactionChecker {

    private static final Logger log = Logger.getLogger();
    private final ContextParams contextParams;
    private final IApplyParameters params;
    private final QueryRunner runner;
    private final DateParser dp = new DateParser();

    public SufficientTransactionChecker(IApplyParameters params, ContextParams contextParams) {
        this.params = params;
        this.contextParams = contextParams;
        this.runner = new AirMilesDAO().getRunner(contextParams);
    }

    public boolean check(List<Double> locations) {
        boolean check = check(locations, params.from(), params.to());
        if(!check&&DateType.valueOf(params.dateType())==DateType.comparison){
            check = check(locations, params.compareFrom(), params.compareTo());
        }
        return check;
    }
    
    private boolean check(List<Double> locations, String from, String to) {
        boolean result = false;
        try {
            String baseQuery = Confs.QUERIES.sufficientTransactionChecker();
            StringBuilder sb = new StringBuilder();
            List<Object> varargs = new ArrayList();
            varargs.add(dp.parse(from));
            varargs.add(dp.parse(to));
            if (locations.size() > 0) {
                sb.append(" ");
                sb.append(Confs.QUERIES.sufficientTransactionCheckerToken());
                sb.append(" ");
                sb.append(AirMilesDAO.prepareInFragment(locations.size()));
                for (Double location : locations) {
                    varargs.add(location);
                }
            } else {
                sb.append(" ").append(SponsorFilteringManager.get().replaceSponsorKeysInQuery(Confs.QUERIES.sufficientTransactionCheckerTokenBySponsorKey(), contextParams));
            }
            if (params.minTransactions() != null || params.minSpend() != null || params.minUnit() != null){
                varargs.add(dp.parse(from));
                varargs.add(dp.parse(to));
                varargs.add(params.minTransactions()== null ? 0 : params.minTransactions());
                varargs.add(params.minSpend() == null ? 0 : params.minSpend());
                varargs.add(params.minUnit()== null ? 0 : params.minUnit());
                sb.append(SponsorFilteringManager.get().replaceSponsorKeysInQuery(Confs.QUERIES.taMinimumValuesFragmentChecker(), contextParams));
            }
            String query = String.format(baseQuery, sb.toString());
            Object[] args = varargs.toArray(new Object[]{});
            PreparedStatementLogger.log(log, query, args);
            Integer count = runner.query(
                    query,
                    new ResultSetHandler<Integer>() {

                        @Override
                        public Integer handle(ResultSet rs) throws SQLException {
                            if (rs.next()) {
                                return 1;
                            }
                            
                            return 0;
                        }
                    },
                    args);
            result = count == 0;
        } catch (Exception ex) {
            log.warn("Error checking for sufficient transaction quality of data rule. Skiping validation.", ex);
        }
        return result;
    }
}
