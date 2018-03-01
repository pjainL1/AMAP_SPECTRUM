package com.lo.hosting.dataload;

import com.lo.config.Confs;
import com.lo.db.LODataSource;
import com.lo.hosting.om.Extract;
import com.lo.hosting.watchdog.LoaderStatus;
import com.lo.hosting.watchdog.LoadingResult;
import com.spinn3r.log5j.Logger;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;

/**
 * 
 * @author ydumais
 */
@SuppressWarnings({"TooBroadCatch"})
public class FullExtractLoader extends Loader {
    private static final int RANDOM_INDEX_KEY_LENGTH = 5;

    public static final String BAK = "BAK_";
    private static final Logger log = Logger.getLogger();

    private ResourceBundle rb;
    private String table;
    private final String backup;


    public FullExtractLoader(Extract extract, LoadingResult loadingResult, LoaderStatus loaderStatus) {
        super(extract, loadingResult, loaderStatus);
        this.rb = ResourceBundle.getBundle("com.lo.hosting.dataload.sql");
        this.table = extract.getTableName();
        this.backup = BAK + this.table;
    }

    @Override
    public void load() throws SQLException {
        backup();
        create();
        try {
            insert();
            index();
            finalizeLoading();
        } catch (Exception e) {
            String msg = String.format("An error occurred while loading extract file %s. "
                    + "Reverting to previous table state.", getExtract().getFile());
            getLoaderStatus().addException(new LoadingResult.ExceptionItem(e, msg));
            log.error(msg, e);
            restore();
        }
    }

    private void backup() throws SQLException {
        drop(backup);
        move(table, backup);
    }
    
    private String getRandomId() {
        return RandomStringUtils.randomAlphanumeric(RANDOM_INDEX_KEY_LENGTH).toUpperCase();
    }

    private void create() throws SQLException {
        
        String createTable = rb.getString(getExtract().getType() + ".create");
        String createQuery = MessageFormat.format(createTable, table, getRandomId());
        update(createQuery);
        String grantTable = rb.getString(getExtract().getType() + ".grant");
        String grantQuery = MessageFormat.format(grantTable, table, StringUtils.join(Confs.CONFIG.dbSponsorsSchemaArray(), ','));
        update(grantQuery);
    }

    private void insert() throws SQLException {
        String template = rb.getString(getExtract().getType() + ".insert");
        String query = MessageFormat.format(template, table);
        CSVBatchLoader batcher = new CSVBatchLoader(getExtract(), query, LODataSource.LONE_DATASOURCE);
        batcher.go();
    }
    
    private String[] splitQueries(String queries) {
        return queries.split("[|]");
    }

    private void index() throws SQLException {
        if (getExtract().createIndex()) {
            String dropIndexTemplate = rb.getString(getExtract().getType() + ".dropindex");
            for (String dropQuery : splitQueries(dropIndexTemplate)) {
                try {
                    String dropIndexQuery = MessageFormat.format(dropQuery, table);
                    update(dropIndexQuery);
                } catch (SQLException e){
                    log.debug("Masking exception as index might not exists, " + e.getMessage());
                }
            }
            String createIndexTemplate = rb.getString(getExtract().getType() + ".index");
            for (String createQuery : splitQueries(createIndexTemplate)) {
                String createIndexQuery = MessageFormat.format(createQuery, table);
                update(createIndexQuery);
            }
        }
    }

    private void update(String query) throws SQLException {
        log.debug(query);
        getDao().getLoneRunner().update(query);
    }

    private void restore() throws SQLException {
        drop(table);
        move(backup, table);
    }

    private void drop(String table) {
        try {
            update(MessageFormat.format(rb.getString("drop"), table));
        } catch (SQLException e) {
            log.debug("Masking exception on drop table since it might not exists, " + e.getMessage());
        }
    }

    private void move(String from, String to) throws SQLException {
        try {
            update(MessageFormat.format(rb.getString("move"), from, to));
        } catch (SQLException e) {
            log.warn(String.format("Attempt to move table %s to %s failed, unexpected.", from, to), e);
        }
    }
    
    @Override
    public void finalizeLoading() {}
    
}
