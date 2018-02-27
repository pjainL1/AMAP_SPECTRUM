package com.lo.console.web;

import com.korem.requestHelpers.GenericDBBoundJSONServlet;
import javax.servlet.http.HttpServletRequest;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import com.lo.db.om.TradeAreaHistoryEntry;
import com.lo.db.proxy.GridProxy;
import com.lo.db.proxy.TaHistoryProxy;
import com.lo.util.GridParamsUtils;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;

/**
 *
 * @author rarif
 */
@WebServlet("/console/TradeAreaHistoryManagement.safe")
public class TaHistoryManagement extends GenericDBBoundJSONServlet<TaHistoryProxy, TaHistoryManagement.LocationsParams> {

    protected static interface LocationsParams {
        Integer limit();
        Integer start();
        JSONArray sort();
        JSONArray filter();
        JSONArray dateFilters();
        String search();
    }

    @Override
    protected String getJSON(HttpServletRequest req, TaHistoryProxy taProxy, LocationsParams params) throws Exception {

        JSONObject result = new JSONObject();
        int start;
        int limit;
        int offset;
        
        String property = "";
        String direction = "";

        List<GridProxy.Filter> filtersValuesList = new ArrayList<>();

        if (params.sort() != null) {
            property = params.sort().getJSONObject(0).getString("property");
            direction = params.sort().getJSONObject(0).getString("direction");
        }

        GridParamsUtils.setFilterParams(params.filter(), filtersValuesList);
        GridParamsUtils.setFilterParams(params.dateFilters(), filtersValuesList);

        if (params.limit() != null && params.start() != null) {
            limit = params.limit();
            start = params.start() + 1;
            offset = start + limit;
        } else {
            return FAILURE;
        }

        JSONArray arrayObj = new JSONArray();

        int totalCount = taProxy.count(start, offset, property, direction, params.search(), filtersValuesList);
        if (totalCount > 0) {
            if (offset > totalCount) {
                offset = totalCount;
            }
            List<TradeAreaHistoryEntry> locationColor = taProxy.getTradeAreaHistory(start, offset, property, direction, params.search(), filtersValuesList);
            for (TradeAreaHistoryEntry elt : locationColor) {
                JSONObject itemObj = JSONObject.fromObject(elt, TradeAreaHistoryEntry.JSON_CONFIG);
                arrayObj.add(itemObj);
            }
        }
        result.put("success", true);
        result.put("tahistory", arrayObj);
        result.put("totalCount", totalCount);

        return result.toString();
    }
}
