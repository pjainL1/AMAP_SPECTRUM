package com.lo.hosting.dataload;

import com.lo.db.dao.AirMilesDAO;
import com.lo.hosting.LoadingProxy;
import com.lo.hosting.om.Extract;
import com.lo.hosting.watchdog.LoaderStatus;
import com.lo.hosting.watchdog.LoadingResult;
import com.spinn3r.log5j.Logger;
import java.sql.SQLException;

/**
 *
 * @author ydumais
 */
public abstract class Loader {
    
    private static final Logger log = Logger.getLogger();
    public abstract void load() throws SQLException;
    public abstract void finalizeLoading();
    private final Extract extract;
    private final AirMilesDAO dao = new AirMilesDAO();
    private static final int SQL_ERROR = -1;
    private final LoadingResult loadingResult;
    private final LoaderStatus loaderStatus;

    public Loader(Extract extract, LoadingResult loadingResult, LoaderStatus loaderStatus) {
        this.extract = extract;
        this.loadingResult = loadingResult;
        this.loaderStatus = loaderStatus;
    }

    protected LoadingResult getLoadingResult() {
        return loadingResult;
    }

    public Extract getExtract() {
        return extract;
    }

    public AirMilesDAO getDao() {
        return dao;
    }
    
    public long getCount() {
        
        try( LoadingProxy lp = new LoadingProxy(extract.getDatasourceName()) ) {
            String tablePrefix = extract.getSchemaName().isEmpty() ? "" : extract.getSchemaName() + ".";
            return lp.getCount( tablePrefix + extract.getTableName() );
        } catch (SQLException ex) {
            log.fatal(String.format("Error while counting rows for extract %s", extract), ex);
            return SQL_ERROR;
        }
        
        
    }

    public LoaderStatus getLoaderStatus() {
        return loaderStatus;
    }
}
