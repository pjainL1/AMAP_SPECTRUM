package com.lo.analysis.tradearea;

import com.korem.openlayers.parameters.IApplyParameters;
import com.korem.openlayers.parameters.IBaseParameters;
import com.korem.requestHelpers.RequestParser;
import com.lo.ContextParams;
import com.lo.analysis.Analysis;
import com.lo.analysis.AnalysisControler;
import com.lo.analysis.NwTaLegendManager;
import com.lo.util.LoggingUtil;
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
public class TradeAreaMethod implements IProgressAware {

    public static final String CONFIG = "com.lo.pdf.config";

    public static interface IParams extends IApplyParameters {

        String tradearea();

        String taLayerId();

        String locations();

        String locationsCode();

        Double issuance();

        Double distance();

        Double projected();

        Double longitude();

        Double latitude();
        
        String polygon();

        String from();

        String to();

        String optionalLayerName();
    }

    @Override
    public Object parseRequest(HttpServletRequest request) {
        return new Object[] {RequestParser.persistentParse(request, IParams.class),
            request.getSession()};
    }

    @Override
    public void execute(ProgressListener listener, Object params) {
        IParams ip = (IParams) ((Object[])params)[0];
        ContextParams cp = ContextParams.get((HttpSession)((Object[])params)[1]);
        try {
            String[] ids = WSClient.getMapService().getLayersIdByName(
                    ip.mapInstanceKey(),
                    Analysis.TRADE_AREA.toString());
            for (String id : ids) {
                if (id != null && !"-1".equals(id) && !"".equals(id)) {
                    WSClient.getMapService().removeLayer(ip.mapInstanceKey(), id);
                }
            }
        } catch (RemoteException ex) {
        }
        if (ip.tradearea() != null) {
            String locations = ip.locations();
            String tradeareas = ip.tradearea();
            if ((locations != null && locations.length() > 0) || tradeareas.contains("projected") || tradeareas.contains("custom")) {
                AnalysisControler controler = new TradeAreaControler(tradeareas.split(","), ip, listener, cp, cp.getSponsor());
                controler.createLayer((HttpSession)((Object[])params)[1]);
                for (String ta : tradeareas.split(",")){
                    if (!"".equals(ta)){
                        LoggingUtil.log(cp.getUser(), cp.getSponsor(), LoggingUtil.getTradeAreaMessage(TradeArea.Type.valueOf(ta), ip));
                    }
                }
                // save list of selected locations
                NwTaLegendManager lm = NwTaLegendManager.get();
                HttpSession session = (HttpSession) ((Object[]) params)[1];
                String key = ip.mapInstanceKey();
                lm.setSelectedLocations(session, key, locations);
                
            }
        }
        listener.update(100);
    }
}
