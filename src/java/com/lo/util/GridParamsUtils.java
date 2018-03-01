package com.lo.util;

import com.lo.db.proxy.GridProxy;
import java.util.List;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 * @author jphoude
 */
public class GridParamsUtils {
    public static void setFilterParams(JSONArray filters, List<GridProxy.Filter> filtersValuesList) {
        if (filters != null) {
            for (int i = 0; i < filters.size(); i++) {
                JSONObject json = filters.getJSONObject(i);
                String type = json.getString("type");
                String field = json.getString("field");
                String value = json.getString("value");
                String comparison = "";
                if (type.equalsIgnoreCase("numeric") || type.equalsIgnoreCase("date")) {
                    comparison = json.getString("comparison");
                }
                GridProxy.Filter filter = new GridProxy.Filter(type, comparison, value, field);
                filtersValuesList.add(filter);
            }
        }
    }
}
