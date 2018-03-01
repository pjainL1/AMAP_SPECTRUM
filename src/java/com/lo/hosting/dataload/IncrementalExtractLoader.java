package com.lo.hosting.dataload;

import com.lo.hosting.om.Extract;
import com.lo.hosting.watchdog.LoaderStatus;
import com.lo.hosting.watchdog.LoadingResult;
import com.spinn3r.log5j.Logger;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.ResourceBundle;

/**
 *
 * @author ydumais
 */
public class IncrementalExtractLoader extends Loader {

    private static final Logger log = Logger.getLogger();
    private ResourceBundle rb;


    public IncrementalExtractLoader(Extract extract, LoadingResult loadingResult, LoaderStatus loaderStatus) {
        super(extract, loadingResult, loaderStatus);
        this.rb = ResourceBundle.getBundle("com.lo.hosting.dataload.sql");
    }

    @Override
    public void load() throws SQLException {
        try {
            insert();
        } catch (Exception ex) {
            String msg = String.format("An error occured during inserts for extract %s.", getExtract());
            getLoaderStatus().addException(new LoadingResult.ExceptionItem(ex, msg));
            log.error("An error occured, rollback inserts.", ex);
            undo();
        }

    }

    private void insert() throws Exception {
        String template = rb.getString(getExtract().getType() + ".insert");
        String query = MessageFormat.format(template, getExtract().getTableName());
        CSVBatchLoader batcher = new CSVBatchLoader(getExtract(), query, getExtract().getDatasourceName());
        batcher.go();
    }

    private void undo() throws SQLException {
        String template = rb.getString("delete");
        String query = MessageFormat.format(template, getExtract().getTableName());
        Object[] args = new Object[]{
            new java.sql.Date(getExtract().getTime().getTime())
        };
        log.debug(String.format("Undo insert query: %s %s", query, Arrays.toString(args)));
        getDao().getRunner(getExtract().getDatasourceName()).update(query, args);
    }
    
    @Override
    public void finalizeLoading() {}
}
