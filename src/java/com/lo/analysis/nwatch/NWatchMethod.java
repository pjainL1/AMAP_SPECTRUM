package com.lo.analysis.nwatch;

import com.korem.openlayers.parameters.IApplyParameters;
import com.korem.requestHelpers.RequestParser;
import com.lo.ContextParams;
import com.lo.analysis.Analysis;
import com.lo.analysis.NwTaLegendManager;
import com.lo.analysis.nwatch.NWatchMethod.IParams;
import com.lo.util.LoggingUtil;
import com.spinn3r.log5j.Logger;
import com.lo.util.WSClient;
import com.lo.web.Apply.IProgressAware;
import com.lo.web.Apply.ProgressListener;
import java.rmi.RemoteException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 *
 * @author ydumais
 */
public class NWatchMethod implements IProgressAware {

    private static final Logger log = Logger.getLogger();

    public static interface IParams extends IApplyParameters {
        String nwatchtype();
        String nwatch();
    }

    @Override
    public Object parseRequest(HttpServletRequest request) {
        return new Object[]{RequestParser.persistentParse(request, IParams.class),
            request.getSession()};
    }

    @Override
    public void execute(ProgressListener listener, Object params) {
        IParams applyParams = (IParams) ((Object[]) params)[0];
        ContextParams cp = ContextParams.get((HttpSession) ((Object[]) params)[1]);
//        try {
////            String[] ids = WSClient.getMapService().getLayersIdByName(
////                    applyParams.mapInstanceKey(),
////                    Analysis.NEIBOURHOOD_WATCH.toString());
////            for (String id : ids) {
////                if (id != null && !"-1".equals(id) && !"".equals(id)) {
////                    WSClient.getMapService().removeLayer(applyParams.mapInstanceKey(), id);
////                }
////            }
//        } catch (RemoteException ex) {
//        }
        if (applyParams.nwatch() == null) {
            listener.update(100);
        } else {
            NWatchController controller = new NWatchController(applyParams, listener, cp);
            controller.createLayer((HttpSession) ((Object[]) params)[1]);
            LoggingUtil.log(cp.getUser(), cp.getSponsor(), LoggingUtil.getNWMessage((IParams) ((Object[]) params)[0]));

            // save list of selected locations
            HttpSession session = (HttpSession) ((Object[]) params)[1];
            String key = applyParams.mapInstanceKey();
            StringBuilder sb = new StringBuilder();
            for (String locCode : cp.getSelectionPKs()) {
                sb.append(locCode).append(",");
            }
            String locationCodes = sb.toString();

            NwTaLegendManager lm = NwTaLegendManager.get();
            lm.setSelectedLocations(session, key, locationCodes);

        }
    }
}
