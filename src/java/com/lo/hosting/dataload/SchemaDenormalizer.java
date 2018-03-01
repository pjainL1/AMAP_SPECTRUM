package com.lo.hosting.dataload;

import com.korem.AbstractLanguageManager;
import com.lo.db.dao.AirMilesDAO;
import com.lo.hosting.watchdog.LoaderStatus;
import com.lo.hosting.watchdog.LoadingResult;
import com.spinn3r.log5j.Logger;
import java.sql.SQLException;
import org.apache.commons.dbutils.QueryRunner;

/**
 *
 * @author ydumais
 */
public class SchemaDenormalizer {

    private static final Logger LOGGER = Logger.getLogger();
    private final QueryRunner runner;
    private final LoadingResult loadingResult;
    private final AbstractLanguageManager lm;
    private final String schemaName;

    public SchemaDenormalizer(String schemaName, LoadingResult lr, AbstractLanguageManager lm) {
        this.schemaName = schemaName;
        this.runner = new AirMilesDAO().getRunner(schemaName);
        this.loadingResult = lr;
        this.lm = lm;
    }
    public boolean go() {
        if (refresh()) {
            try {
                wrap();
            } catch (SQLException e) {
                LOGGER.error("Error in SchemaDenormalizer wrap()", e);
                return false;
            }
        } else {
            undo();
        }

        return true;
    }

    public boolean refresh(){
        boolean success = false;
        try {
            exec("refresh_universe");
            exec("refresh_lonlat_meters");
            exec("refresh_location_active");
            exec("refresh_nwatch");
            exec("refresh_hotspot_location");
            exec("refresh_hotspot_sponsor");
            success = true;
        } catch (SQLException ex){
            String msg = String.format(lm.get("emailLoadingAlert.denormalizerError"), schemaName);
            loadingResult.addException(new LoadingResult.ExceptionItem(ex, msg));
            LOGGER.error("A refresh procedure failed.", ex);
        }
        return success;
    }
    
    private void recompile(String procedure) throws SQLException{
        String query = String.format("alter procedure %s compile", procedure);
        LOGGER.info(String.format("Recompiling procedure: %s", query));
        long time = System.currentTimeMillis();
        runner.update(query);
        LOGGER.info(String.format("Procedure recompiled %s sec.", ( (System.currentTimeMillis() - time) / 1000.0 )));
    }

    private void exec(String procedure) throws SQLException{
        recompile(procedure);
        
        String query = String.format("{call %s}", procedure);
        LOGGER.info(String.format("Executing query: %s", query));
        long time = System.currentTimeMillis();
        runner.update(query);
        LOGGER.info(String.format("Query completed in %s sec.", ( (System.currentTimeMillis() - time) / 1000.0 )));
    }
    
    private void wrap() throws SQLException {
        LOGGER.info("Removing all new flags from db");
        runner.update("{call reset_isnew}");
    }
    
    private void undo() {
//        LOGGER.warn("Removing all new record from db");
//        try {
//            runner.update("{call delete_isnew}");
//        } catch (SQLException ex) {
//            LOGGER.error("Exception raised undoing loading process.", ex);
//        }
    }
}
