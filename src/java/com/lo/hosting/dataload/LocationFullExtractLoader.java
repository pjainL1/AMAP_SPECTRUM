package com.lo.hosting.dataload;

import com.lo.db.dao.AirMilesDAO;
import com.lo.hosting.om.Extract;
import com.lo.hosting.watchdog.LoaderStatus;
import com.lo.hosting.watchdog.LoadingResult;
import com.spinn3r.log5j.Logger;

import com.lo.util.Painter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;

/**
 *
 * @author rarif
 */
public class LocationFullExtractLoader extends FullExtractLoader {

    private final QueryRunner runner;
    private ResourceBundle rb;
    private static final Logger LOGGER = Logger.getLogger();

    public LocationFullExtractLoader(Extract extract, LoadingResult lr, LoaderStatus loaderStatus) {
        super(extract, lr, loaderStatus);
        this.runner = new AirMilesDAO().getLoneRunner();
        rb = ResourceBundle.getBundle("com.lo.hosting.dataload.sql");
    }

    @Override
    public void finalizeLoading() {
        {
            long start = System.currentTimeMillis();
            setLocationsColor();
            LOGGER.info("Updated SPONSOR_LOCATION_COLORS table in %sms.", System.currentTimeMillis() - start);
        }
        
        try {
            long start  = System.currentTimeMillis();
            restoreActiveDates();
            LOGGER.info("Restored last active dates in SPONSOR_LOCATION table in %sms.", System.currentTimeMillis() - start);
        } catch (SQLException e) {
            LOGGER.error("Error while restoring last active dates", e);
            getLoaderStatus().addException(new LoadingResult.ExceptionItem(e, "Error while restoring last active dates"));
        }
    }
    
    private void restoreActiveDates() throws SQLException {
        runner.update(rb.getString("Locations.restoreFirstActive"));
        runner.update(rb.getString("Locations.restoreLastActive"));
    }

    private void setLocationsColor() {
        try {
            ResultSetHandler<Integer> handler = new ResultSetHandler<Integer>() {
                @Override
                public Integer handle(ResultSet rs) throws SQLException {
                    Painter p = new Painter();
                    String neighbourhoodWatchColor;
                    String tradeAreaColor;
                    Integer inserts = 0;
                    while (rs.next()) {
                        int hashCode = rs.getString("SPONSOR_LOCATION_KEY").hashCode();
                        neighbourhoodWatchColor = p.getColor(hashCode);
                        tradeAreaColor = p.getColor(hashCode + 1); // changes the hashCode to get the next random color.
                        p.resetUsedColors();
                        inserts = runner.update(rb.getString("LocationColors.insert"), 
                                rs.getString("SPONSOR_LOCATION_KEY"), 
                                neighbourhoodWatchColor.replace("#", ""), 
                                tradeAreaColor.replace("#", ""));
                    }

                    return inserts;
                }
            };
            runner.query(rb.getString("LocationWithoutColors.select"), handler);
        } catch (SQLException ex) {
            String msg = "Error while setting location colors";
            getLoaderStatus().addException(new LoadingResult.ExceptionItem(ex, msg));
            LOGGER.error(msg, ex);
        }
    }
}
