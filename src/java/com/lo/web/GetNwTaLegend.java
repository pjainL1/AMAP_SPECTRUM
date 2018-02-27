package com.lo.web;

import com.korem.requestHelpers.GenericDBBoundJSONServlet;
import com.korem.requestHelpers.GenericJSONServlet;
import com.lo.ContextParams;
import com.lo.analysis.NwTaLegendManager;
import com.lo.analysis.tradearea.TradeArea;
import com.lo.db.proxy.NwTaLegendProxy;
import com.lo.util.Painter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import net.sf.json.JSONArray;

/**
 *
 * @author maitounejjar
 */
@WebServlet("/getNwTaLegend")
public class GetNwTaLegend extends GenericJSONServlet<GetNwTaLegend.Params> {

    public interface Params {

        String mapInstanceKey();
    }
    
    private boolean hasProjectedTa(Map<Double, List<TradeArea>> tradeAreas) {
        List<TradeArea> nullTradeAreas = tradeAreas.get(null);
        
        if (nullTradeAreas != null) {
            for (TradeArea ta : nullTradeAreas) {
                if (ta.getType() == TradeArea.Type.projected) {
                    return true;
                }
            }
        }
        
        return false;
    }

    @Override
    protected String getJSON(HttpServletRequest request, Params params) throws Exception {
        ContextParams cp = ContextParams.get(request.getSession());
        try (NwTaLegendProxy proxy = new NwTaLegendProxy(cp)) {
            NwTaLegendManager lm = NwTaLegendManager.get();
            List<Long> locKeys = lm.getSelectedLocations(request.getSession(), params.mapInstanceKey());
            List<NwTaLegendProxy.LocationColor> list = proxy.getNwTaLegend(locKeys);


            Map<Double, List<TradeArea>> tradeAreas = cp.getTradeAreas();
            int locationsCount = tradeAreas.keySet().size();

            if (locationsCount == 1 && !hasProjectedTa(tradeAreas) && !list.isEmpty()) {
                list.get(0).setTaColor(getDefaultTaColor());
            }

            JSONArray obj = JSONArray.fromObject(list);
            return obj.toString();
        }
    }

    private String getDefaultTaColor() {
        Painter painter = new Painter();
        
        return painter.getColor(0).substring(1);
    }

}
