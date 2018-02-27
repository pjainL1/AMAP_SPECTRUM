package com.lo.web;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.korem.requestHelpers.GenericServlet;
import com.lo.ContextParams;
import com.lo.analysis.nwatch.NWatchMethod;
import com.lo.analysis.storeLevelAnalysis.StoreLevelAnalysisMethod;
import com.lo.analysis.tradearea.TradeAreaMethod;
import com.lo.db.dao.PDFReportDAO;
import com.lo.hotspot.HotSpotMethod;
import com.lo.hotspot.SetDatesMethod;
import com.lo.hotspot.SetMinValuesMethod;
import com.lo.pdf.PDFBean;
import com.lo.pdf.PDFProcessProgressListenerKey;
import com.lo.qa.QualityOfDataMethod;
import com.lo.report.ReportMethod;
import com.lo.util.ProgressListenerUtils;
import com.spinn3r.log5j.Logger;

/**
 *
 * @author jduchesne
 */
public class Apply extends GenericServlet<Apply.IParams> {
    private static final Logger LOGGER = Logger.getLogger();
    private static final int GET_PROGRESS_TIMEOUT = 30000; // max milliseconds to wait for progress update before sending current progress.

    protected static interface IParams {

        String methods();
    }

    public enum Method {
        hotspot, tradearea, nwatch, report, setDates, setMinimumValues, generatePC, storeLevelAnalysis;
    };

    private static Map<Method, IProgressAware> methods;
    private static List<Method> requiresQARule = Arrays.asList(new Method[]{
        Method.hotspot, Method.tradearea, Method.nwatch});

    public Apply() {
        methods = Collections
                .synchronizedMap(new EnumMap<Method, IProgressAware>(
                                Method.class));
        methods.put(Method.hotspot, new HotSpotMethod());
        methods.put(Method.tradearea, new TradeAreaMethod());
        methods.put(Method.nwatch, new NWatchMethod());
        methods.put(Method.report, new ReportMethod());
        methods.put(Method.setDates, new SetDatesMethod());
        methods.put(Method.setMinimumValues, new SetMinValuesMethod());
        methods.put(Method.generatePC, new GeneratePCMethod());
        methods.put(Method.storeLevelAnalysis, new StoreLevelAnalysisMethod());
    }

    @Override
    protected String getJSON(final HttpServletRequest req, final IParams params)
            throws Exception {
        GetHeatMap.clearBackbuffer(req.getSession());

        /**
         * Set PDFReport in database and keep it in static array in
         * ProgressListenerUtils
         */
        final ContextParams cp = ContextParams.get(req.getSession());

        if (cp != null && cp.getUser() != null) {

            final IProgressAware[] methodsArray = getMethods(params.methods());
            final Object[] methodsParams = parseRequest(methodsArray, req);
            final ProgressListener listener = new ProgressListener(
                    methodsArray.length);

            PDFProcessProgressListenerKey processKey = ProgressListenerUtils
                    .set(req, listener, methodsArray);

            new PDFReportDAO().saveUpdate(cp, null, processKey);
            cp.setPdfProcessing(true);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    PDFBean b = null;
                    for (int i = 0; i < methodsArray.length; ++i) {
                        LOGGER.debug(String.format("Running method %s (%s/%s).", methodsArray[i].getClass().getSimpleName(), i + 1, methodsArray.length));
                        methodsArray[i].execute(listener, methodsParams[i]);
                        if (methodsArray[i] instanceof ReportMethod) {
                            ReportMethod rm = (ReportMethod) methodsArray[i];
                            b = rm.getPdfBean();
                        }
                    }
                    cp.setPdfProcessing(false);
                    if (b != null) {
                        new PDFReportDAO().saveUpdate(cp, b, null);
                    } else {
                        new PDFReportDAO().delete(cp);
                    }
                }
            }).start();
        }
        return null;
    }

    private Object[] parseRequest(IProgressAware[] methodsArray,
            HttpServletRequest req) {
        Object[] parsedRequest = new Object[methodsArray.length];
        for (int i = 0; i < methodsArray.length; ++i) {
            parsedRequest[i] = methodsArray[i].parseRequest(req);
        }
        return parsedRequest;
    }

    /**
     * Parse method string and extract corresponding IProgressAware methods
     * handler for each analysis.
     *
     * The report method behaves differently. It replaces the algorithm. Instead
     * of calling each method, it takes controls and calls methods as it
     * pleases.
     *
     * @param methodsAsString
     * @return
     */
    private IProgressAware[] getMethods(String methodsAsString) {
        String[] splittedMethods = methodsAsString.split(",");
        List<IProgressAware> methodsList = new ArrayList<IProgressAware>();
        boolean forceQARule = false;
        for (int i = 0; i < splittedMethods.length; ++i) {
            Method method = Method.valueOf(splittedMethods[i]);
            methodsList.add(methods.get(method));
            if (requiresQARule.contains(method)) {
                forceQARule = true;
            }
        }
        if (forceQARule) {
            methodsList.add(new QualityOfDataMethod());
        }
        return methodsList.toArray(new IProgressAware[]{});
    }

    public static class ProgressListener {

        private final Object mutex;
        private boolean updated;
        private double progress;
        private int methodCount;
        private double previousProgress;
        private boolean cancel;

        private ProgressListener(int methodCount) {
            mutex = new Object();
            updated = false;
            progress = 0;
            this.methodCount = methodCount;
            previousProgress = 0;
            this.cancel = false;
        }

        public void update(double progress) {
            synchronized (mutex) {
                double currentProgress = (double) progress / methodCount;
                this.progress = previousProgress + currentProgress;
                LOGGER.debug(String.format("Updating current progress to %s%% (method progress %s%%)", this.progress, progress));
                if (progress >= 100) {
                    previousProgress += currentProgress;
                    LOGGER.debug(String.format("Method progress up to %s%%, updating total progress to %s%%", progress, previousProgress));
                    if (progress > 100) {
                        LOGGER.info(String.format("Method progress is above 100%%!(%s%%)", progress));
                    }
                }
                updated = true;
                mutex.notify();
            }
        }

        int getProgress() {
            synchronized (mutex) {
                if (!updated && !cancel) {
                    try {
                        mutex.wait(GET_PROGRESS_TIMEOUT);
                    } catch (Exception e) {
                        int i =1;
                    }
                }
                updated = false;
                if (cancel) {
                    return 100;
                }
                return (int) Math.ceil(progress);
            }
        }

        public boolean isCancel() {
            return cancel;
        }

        public void cancel() {
            synchronized (mutex) {
                this.cancel = true;
                mutex.notify();
            }
        }
    }

    public static interface IProgressAware {

        Object parseRequest(HttpServletRequest request);

        void execute(ProgressListener listener, Object params);
    }
}
