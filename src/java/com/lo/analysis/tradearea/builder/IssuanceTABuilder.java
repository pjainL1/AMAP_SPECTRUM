package com.lo.analysis.tradearea.builder;

import com.lo.ContextParams;
import com.lo.analysis.tradearea.TradeArea;
import com.lo.config.Confs;
import com.lo.db.dao.AirMilesDAO;
import com.lo.db.om.Collector;
import com.lo.db.om.Location;
import com.lo.db.om.SponsorGroup;
import com.lo.qa.QualityOfData;
import com.lo.qa.QualityOfData.Rule;
import com.lo.util.PreparedStatementLogger;
import com.lo.util.SponsorFilteringManager;
import com.spinn3r.log5j.Logger;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.dbutils.handlers.BeanListHandler;

/**
 *
 * @author ydumais
 */
public class IssuanceTABuilder extends LocationBasedTABuilder {

    private static final Logger log = Logger.getLogger();
    private final double steps;
    private double progress;
    private final TradeArea.Type type;

    public IssuanceTABuilder(Object[] params, ContextParams cp, SponsorGroup sponsorGroup, List<Double> locations, TradeArea.Type type) {
        super(cp, sponsorGroup, params);
        setLocations(locations);
        this.steps = (80f / locations.size());
        this.progress = 0;
        this.type = type;
    }

    @Override
    protected List<TradeArea> drawTradeAreas() {
        List<TradeArea> result = new ArrayList<>();
        try {
            chrono.start("select collectors");
            List<Location> locationsWithCollectors = selectCollectors();
            chrono.stop();
            for (Location loc : locationsWithCollectors) {
                double key = loc.getKey();
                List<Collector> collectors = loc.getCollectors();
                TradeArea tradeArea = new TradeArea(loc.getCode(), key,
                        drawConvexHull(collectors), collectors.size(), TradeArea.Type.issuance, loc.getSponsorKey(), loc.getCustomerLocationCode(), loc.getSponsorCode());
                result.add(tradeArea);
            }
        } catch (SQLException ex) {
            log.error("Error retreiving distance collector. Returning an empty list.", ex);
        }
        return result;
    }
    
    private List<Location> selectCollectors() throws SQLException {
        List<Location> retrievedLocations = retrieveLocations();
        List<Location> finalList = new ArrayList<>(retrievedLocations.size());
        
        AirMilesDAO dao = new AirMilesDAO();
        for (Location loc : retrievedLocations) {
            Object[] args = prepareArgs(loc.getKey());
            
            String param = this.type.equals(TradeArea.Type.units) ? "unit" : "spend";
            
            String fragment = "";
            if (args.length > 4){
                fragment = SponsorFilteringManager.get().replaceSponsorKeysInQuery(Confs.QUERIES.taMinimumValuesFragment(), getContextParams());
            }
            String query = String.format(Confs.QUERIES.taBuilderIssuance(), param, param, param, fragment);
            dao.log("select issuance trade area collectors", query, args);
            PreparedStatementLogger.log(log, query, args);
            List<Collector> alist = dao.getRunner(getSponsorGroup()).query(query, new BeanListHandler<>(Collector.class), args);
            if (alist.size() > 3) {
                loc.setCollectors(alist);
                finalList.add(loc);
                if (alist.size() < 100) {
                    QualityOfData.set(getContextParams(), Rule.lowForTradeArea);
                }
            } else {
                QualityOfData.set(getContextParams(), Rule.insufficientForTradeArea);
            }
            updateListener(progress += steps);
        }
        return finalList;
    }

    private Object[] prepareArgs(Double location) {
        List<Object> args = new ArrayList<>();
        args.add(location);
        args.add(params[0]);
        args.add(params[1]);

        if (null != params[3] || null != params[4] || null != params[5]) {
            args.add(params[0]);
            args.add(params[1]);
            args.add(params[3] != null ? Integer.valueOf(params[3].toString()) : 0);
            args.add(params[4] != null ? Integer.valueOf(params[4].toString()) : 0);
            args.add(params[5] != null ? Integer.valueOf(params[5].toString()) : 0);
        }
        args.add(params[2]);
        return args.toArray(new Object[]{});
    }
}
