package com.lo.hosting.watchdog;

import com.korem.SessionlessLanguageManager;
import com.lo.config.Confs;
import com.lo.db.LODataSource;
import com.lo.db.proxy.TaHistoryProxy;
import com.lo.hosting.dataload.SchemaDenormalizer;
import com.lo.hosting.watchdog.LoadingResult.ExceptionItem;
import java.io.File;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.lo.hosting.Config.Directory;
import com.lo.hosting.dataload.Denormalizer;
import com.lo.hosting.dataload.Loader;
import com.lo.hosting.dataload.SimpleLoaderFactory;
import com.lo.hosting.om.Extract;
import com.lo.hosting.om.SimpleExtractFactory;
import com.spinn3r.log5j.Logger;

/**
 *
 * @author ydumais
 */
public class InboxWatchdog extends Watchdog {

    private static final Logger log = Logger.getLogger();
    public static final String LANGUAGE = "en";
    public SessionlessLanguageManager lm;
    private File inbox;
    private File error;
    private File processed;
    private final EnumMap<Extract.Type, Pattern> orderedPatterns;
    private final SimpleDateFormat sdf;

    public static volatile boolean used = false;

    public static boolean isUsed() {
        return used;
    }

    private static void setRunning() {
        used = true;
    }

    private static void notRunning() {
        used = false;
    }

    public InboxWatchdog() {
        super();
        initDirectories();
        lm = new SessionlessLanguageManager(LANGUAGE);
        orderedPatterns = new EnumMap<>(Extract.Type.class);
        orderedPatterns.put(Extract.Type.Sponsors, Pattern.compile(conf.getValue("extract.sponsor")));
        orderedPatterns.put(Extract.Type.Locations, Pattern.compile(conf.getValue("extract.location")));
        orderedPatterns.put(Extract.Type.Collectors, Pattern.compile(conf.getValue("extract.collector")));
        orderedPatterns.put(Extract.Type.Transactions, Pattern.compile(conf.getValue("extract.transaction")));
        orderedPatterns.put(Extract.Type.PostalCode, Pattern.compile(conf.getValue("extract.postal.code")));
        sdf = new SimpleDateFormat(conf.getValue("extract.file.date.format"));

    }

    private void initDirectories() {
        conf.loadDirConfig();
        inbox = conf.getDir(Directory.inbox);
        error = conf.getDir(Directory.error);
        processed = conf.getDir(Directory.processed);
    }
    
    synchronized private boolean canRun() {
        if (!isUsed()) {
            setRunning();
            return true;
        }
        
        log.info("Already loading");
        return false;
    }

    @Override
    public void run() {
        if (canRun()) {
            try {
                log.info("Load extract file now");
                boolean success = false;
                boolean loaded = false;

                Collection<File> files;
                List<Extract> serialSequenced;
                Collection<Extract> parallelSequenced;

                LoadingNotification ln = new LoadingNotification();

                initDirectories();

                LoadingResult lr = new LoadingResult();

                try {
                    log.debug("Looking for new files in dir "
                            + inbox.getAbsolutePath());
                    files = Arrays.asList(lookup(inbox));
                    serialSequenced = sequence(files);
                    parallelSequenced = parallelSequence(serialSequenced);

                    /* Email Alert: Data loading Started */
                    boolean isEmailSent = ln.sendStartEmail(serialSequenced, parallelSequenced);
                    if (!isEmailSent) {
                        log.debug("Data loading terminated: All expected files were missing");
                        return;
                    }

                    loaded = !serialSequenced.isEmpty();

                    // call load method with ALL files
                    load(serialSequenced, lr);
                    success = lr.isSuccess();

                    if (loaded && success) {
                        if (Confs.CONFIG.loadingRunDenormalizer()) {
                            log.info("Running denormalizer now.");
                            new Denormalizer().go(lr);
                        }
                    } else {
                        log.warn(String.format("Loaded: %s Success: %s, not running global denormalizer.", loaded, success));
                    }

                    if (success) {
                        parallelLoad(parallelSequenced, lr);
                    }

                    deleteTaHistoryOldRecords();

                } catch (Exception ex) {
                    log.fatal("A serious error ocurred.", ex);

                    lr.addException(new ExceptionItem(ex, lm.get("emailLoadingAlert.exceptionText")));
                }

                /* Email Alert: Data loading Finished */
                lr.setFinish(new Date());
                ln.sendFinishEmail(lr);
            } finally {
                notRunning();
            }
            log.info("Load extract file process terminated");
        }
    }

    private void deleteTaHistoryOldRecords() throws SQLException {
        /*delete six months old in TA history*/
        try (TaHistoryProxy taProxy = new TaHistoryProxy()) {
            taProxy.deleteOldTradeAreaHistory();
        }
    }

    @Override
    public String getName() {
        return "InboxWatchdog[load extract files]";
    }

    private Collection<Extract> parallelSequence(Collection<Extract> sequence) {
        Collection<Extract> parallelSequence = new ArrayList<>();
        Extract current;
        for (Iterator<Extract> ite = sequence.iterator(); ite.hasNext();) {
            current = ite.next();
            if (!current.getDatasourceName().equalsIgnoreCase(LODataSource.LONE_DATASOURCE)) {
                parallelSequence.add(current);
                ite.remove();
            }
        }
        return parallelSequence;
    }

    /**
     * Order files in a fix algorithmic order. Only files matching specified
     * regex are to be included
     *
     * @param files
     * @return
     */
    private List<Extract> sequence(Collection<File> files) {
        List<Extract> sequenced = new ArrayList<>();
        for (Extract.Type extract : orderedPatterns.keySet()) {
            Pattern p = orderedPatterns.get(extract);
            for (File file : files) {
                Matcher m = p.matcher(file.getName());
                if (m.matches()) {
                    String timeValue = m.group(1);
                    try {
                        Date time = sdf.parse(timeValue);
                        sequenced.add(SimpleExtractFactory.getInstance().get(
                                extract, file, time));
                    } catch (ParseException pe) {
                        log.warn(String.format(
                                "Error parsing time value of %s. ", timeValue),
                                pe);
                    }
                }
            }
        }
        log.info(String.format("%s extract files ready to be processed.",
                sequenced.size()));
        return sequenced;
    }

    private void load(List<Extract> sequenced, LoadingResult lr) throws Exception {

        boolean success = true;

        for (Extract extract : sequenced) {
            success = loadExtract(extract, lr) && success;
        }
    }
    
    private boolean loadExtracts(Collection<Extract> extracts, LoadingResult lr) {
        boolean success = true;
        for (Extract extract : extracts) {
            success = success && loadExtract(extract, lr);
        }
        
        return success;
    }

    private boolean loadExtract(Extract extract, LoadingResult lr) {
        boolean success = true;
        Loader loader = null;
        try {
            log.debug("Loading extract " + extract.getFile().getName());
            loader = SimpleLoaderFactory.getInstance().get(extract, lr);

            Extract.Type type = extract.getType();

            // Count existing rows before and after we load the data
            long oldCount = loader.getCount();
            loader.load();
            long newCount = loader.getCount();

            if (oldCount < 0 || newCount < 0) {
                // This means a SQLException occured in Loader.java where getCount returned -1
                log.warn(String.format("Could not get table rows count for %s.", type));
            }

            lr.addRow(new LoadingResult.DataRow(extract, oldCount, newCount));

            if (loader.getLoaderStatus().isSuccess()) {
                move(extract.getFile(), processed);
            } else {
                success = false;
                move(extract.getFile(), error);
            }

        } catch (Exception ex) {
            success = false;
            log.error(String.format("An error occured loading extract %s. Moving file %s to directory %s.", extract, extract.getFile().getName(), error), ex);
            move(extract.getFile(), error);
            String msg = String.format(lm.get("emailLoadingAlert.sqlExceptionText"), extract.getType().toString());

            if (loader != null) {
                loader.getLoaderStatus().addException(new ExceptionItem(ex, msg));
            } else {
                lr.addException(new ExceptionItem(ex, msg));
            }
        }
        return success;
    }

    private void parallelLoad(Collection<Extract> sequenced, LoadingResult lr) throws Exception {
        ExecutorService threadPool = Executors.newFixedThreadPool(Confs.CONFIG.loadingParallelThreadPool());

        List<Future<Boolean>> results = threadPool.invokeAll(wrapExtractsExecution(sequenced, lr));
        for (Future<Boolean> result : results) {
            if (!result.get()) {
                lr.setSuccess(false);
                break;
            }
        }
    }

    private Map<String, Collection<Extract>> groupSchemaExtracts(Collection<Extract> sequenced) {
        Map<String, Collection<Extract>> extractsBySchema = new HashMap<>();
        for (Extract extract : sequenced) {
            String databaseName = extract.getDatasourceName();
            Collection<Extract> dbExtracts = extractsBySchema.get(databaseName);
            if (dbExtracts == null) {
                dbExtracts = new ArrayList<>();
                extractsBySchema.put(databaseName, dbExtracts);
            }
            
            dbExtracts.add(extract);
        }
        
        return extractsBySchema;
    }
    
    private Collection<Callable<Boolean>> wrapExtractsExecution(Collection<Extract> sequenced, final LoadingResult lr) {
        Map<String, Collection<Extract>> groupedSchemaExtracts = groupSchemaExtracts(sequenced);
        Collection<Callable<Boolean>> wrappers = new ArrayList<>(sequenced.size());
        
        for (Entry<String, Collection<Extract>> entry : groupedSchemaExtracts.entrySet()) {
            final Collection<Extract> extracts = entry.getValue();
            final String dbName = entry.getKey();
            
            wrappers.add(new Callable<Boolean>() {

                @Override
                public Boolean call() throws Exception {
                    log.info(String.format("Starting new loading thread for extracts: %s", extracts.toString()));
                    boolean runDenormalizer = Confs.CONFIG.loadingRunDenormalizer();
                    return loadExtracts(extracts, lr)
                            && (runDenormalizer && new SchemaDenormalizer(dbName, lr, lm).go());
                }
            });
        }
        return wrappers;
    }

    private void move(File file, File dir) {
        boolean success = doMove(file, dir);
        if (!success) {
            log.warn(String.format("Deleting file %s", file));
            file.delete();
        }
    }

    private boolean doMove(File file, File dir) {
        boolean success = file.renameTo(new File(dir, file.getName()));
        if (!success) {
            log.warn(String.format("An error occured moving file %s to dir %s",
                    file, dir));
        }
        return success;
    }
}
