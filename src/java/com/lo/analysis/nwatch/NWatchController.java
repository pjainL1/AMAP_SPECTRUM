package com.lo.analysis.nwatch;

import com.lo.ContextParams;
import com.lo.analysis.Analysis;
import com.lo.analysis.AnalysisControler;
import com.lo.analysis.SpectrumLayer;
import com.lo.analysis.nwatch.NWatchMethod.IParams;
import com.lo.web.Apply.ProgressListener;
import com.spinn3r.log5j.Logger;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 *
 * @author ydumais
 */
public class NWatchController implements AnalysisControler {

    private static final Logger log = Logger.getLogger();
    private final IParams params;
    private final ProgressListener listener;
    private final ContextParams contextParams;

    public NWatchController(IParams params, ProgressListener listener, ContextParams contextParams) {
        this.params = params;
        this.listener = listener;
        this.contextParams = contextParams;
    }

    @Override
    public String createLayer(HttpSession session) {
        String id = "-1";
        listener.update(25);
        try {
            NWatchLayerCreator factory = new NWatchLayerCreator(params, contextParams);
            id = factory.apply(listener, contextParams,session);
            
        } catch (Exception e) {
            log.error("Error defining neibourhood watch.", e);
        } finally {
            listener.update(100);
        }
        return id;
    }
}
