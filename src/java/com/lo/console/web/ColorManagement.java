package com.lo.console.web;

import com.korem.requestHelpers.GenericDBBoundJSONServlet;
import com.lo.db.dao.AirMilesDAO;
import com.lo.db.dao.SponsorDAO;
import javax.servlet.http.HttpServletRequest;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import com.lo.db.om.LocationColor;
import com.lo.db.om.SponsorGroup;
import com.lo.db.proxy.ColorManagementProxy;
import com.lo.db.proxy.GridProxy;
import com.lo.util.GridParamsUtils;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;

/**
 *
 * @author rarif
 */
@WebServlet("/console/ColorManagement.safe")
public class ColorManagement extends GenericDBBoundJSONServlet<ColorManagementProxy, ColorManagement.LocationsParams> {

    protected static interface LocationsParams {
        Integer limit();
        Integer start();
        JSONArray sort();
        JSONArray filter();
        String search();
        String sponsor();
    }
    
    @Override
    protected String getJSON(HttpServletRequest request, ColorManagementProxy proxy, LocationsParams params) throws Exception {
        
        JSONObject result = new JSONObject();
        int start = 0;
        int limit = 0;
        int offSet = 0;
        String property = "";
        String direction = "";
        String search = "";

        List<GridProxy.Filter> filtersValuesList = new ArrayList<>();

        if (params.search() != null) {
            search = params.search();
        }
        
        SponsorGroup sponsor = (new SponsorDAO(new AirMilesDAO())).getSponsor(params.sponsor());
        filtersValuesList.add(new GridProxy.Filter("in", null, sponsor.getKeysFilter(), "sponsorKey"));

        if (params.sort() != null) {
            property = params.sort().getJSONObject(0).getString("property");
            direction = params.sort().getJSONObject(0).getString("direction");
        }

        GridParamsUtils.setFilterParams(params.filter(), filtersValuesList);

        if (params.limit() != null && params.start() != null) {
            limit = params.limit();
            start = params.start() + 1;
            offSet = start + limit;
        } else {
            return FAILURE;
        }

        JSONArray arrayObj = new JSONArray();
        int totalcount = proxy.count(start, limit, property, direction, search, filtersValuesList);
        if (totalcount > 0) {
            if (offSet > totalcount) {
                offSet = totalcount;
            }
            List<LocationColor> locationColor = proxy.getLocationColors(start, offSet, property, direction, search, filtersValuesList);
            for (LocationColor elt : locationColor) {
                JSONObject itemObj = JSONObject.fromObject(elt);
                arrayObj.add(itemObj);
            }
        }

        result.put("success", true);
        result.put("locationcolors", arrayObj);
        result.put("totalCount", totalcount);
            
        return result.toString();
    }
}
