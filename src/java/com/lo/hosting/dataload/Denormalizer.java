package com.lo.hosting.dataload;

import com.lo.config.Confs;
import com.lo.db.dao.AirMilesDAO;
import com.lo.hosting.watchdog.LoadingResult;
import com.spinn3r.log5j.Logger;
import java.sql.SQLException;
import org.apache.commons.dbutils.QueryRunner;

/**
 *
 * @author ydumais
 */
public class Denormalizer {

    private static final Logger log = Logger.getLogger();
    private final QueryRunner runner;

    public Denormalizer() {
        this.runner = new AirMilesDAO().getLoneRunner();
    }

    public boolean go(LoadingResult lr){
        boolean success = false;
        try {
            exec("refresh_collector_active");
            grantPermissions();
            exec("refresh_lonlat_meters");
            exec("refresh_hotspot_ambase");
            success = true;
        } catch (SQLException ex){
            log.error("A refresh procedure failed.", ex);
            lr.addException(new LoadingResult.ExceptionItem(ex, "A refresh procedure failed."));
        }
        return success;
    }
    
    private void recompile(String procedure) throws SQLException{
        String query = String.format("alter procedure %s compile", procedure);
        log.info(String.format("Recompiling procedure: %s", query));
        long time = System.currentTimeMillis();
        runner.update(query);
        log.info(String.format("Procedure recompiled %s sec.", ( (System.currentTimeMillis() - time) / 1000.0 )));
    }

    private void exec(String procedure) throws SQLException{
        recompile(procedure);
        
        String query = String.format("{call %s}", procedure);
        log.info(String.format("Executing query: %s", query));
        long time = System.currentTimeMillis();
        runner.update(query);
        log.info(String.format("Query completed in %s sec.", ( (System.currentTimeMillis() - time) / 1000.0 )));
    }
    
    private void grantPermissions() throws SQLException {
        for (String schema : Confs.CONFIG.dbSponsorsSchemaArray()) {
            runner.update(String.format(Confs.QUERIES.loadingGrantCollectorActive(), schema));
            runner.update(String.format(Confs.QUERIES.loadingGrantGroupedCollectorSums(), schema));
        }
    }
}
