package com.lo.analysis.storeLevelAnalysis;

import com.lo.ContextParams;
import com.lo.analysis.AnalysisControler;
import com.lo.analysis.storeLevelAnalysis.StoreLevelAnalysisMethod.IParams;
import com.lo.web.Apply;
import com.spinn3r.log5j.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 *
 * @author smukena
 */
public class StoreLevelAnalysisController implements AnalysisControler {

    private static final Logger log = Logger.getLogger();
    private final IParams params;
    private final Apply.ProgressListener listener;
    private final ContextParams contextParams;

    public StoreLevelAnalysisController(IParams params, Apply.ProgressListener listener, ContextParams contextParams) {
        this.params = params;
        this.listener = listener;
        this.contextParams = contextParams;
    }

    @Override
    public String createLayer(HttpSession session) {
        String id = "-1";
        listener.update(25);
        try {
            StoreLevelAnalysisLayerCreator factory = new StoreLevelAnalysisLayerCreator(params, contextParams);
            id = factory.apply(listener,session);
        } catch (Exception e) {
            log.error("Error defining store level analysis.", e);
        }
        return id;
    }

}
