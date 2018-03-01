
package com.lo.db.proxy;

import com.lo.config.Confs;
import com.lo.db.om.LocationColor;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class ColorManagementProxy extends GridProxy {

    public ColorManagementProxy() throws SQLException {}

    @Override
    protected Map<String, String> getFieldsMap() {
        return Confs.CONSOLE_CONFIG.consoleColorsFieldsMap();
    }

    @Override
    protected Set<String> getSearcheableFields() {
        return Confs.CONSOLE_CONFIG.consoleColorsSearcheableFieldsSet();
    }
    
    @Override
    protected String getBaseQuery() {
        return Confs.CONSOLE_QUERIES.tradeAreaColorBaseSelect();
    }

    @Override
    protected String getIdColumn() {
        return Confs.CONSOLE_CONFIG.consoleColorsIdField();
    }
    
    public List<LocationColor> getLocationColors(int start, int limit, String property, String direction, String search, List<Filter> filtersValuesList) throws SQLException, ParseException {
        List<LocationColor> result = new ArrayList<>();
        
        try (ResultSet rs = getResultSet(start, limit, property, direction, search, filtersValuesList)) {
            while (rs.next()) {
                LocationColor lc = new LocationColor(
                        Integer.parseInt(rs.getString("SPONSOR_LOCATION_KEY")), 
                        rs.getString("sponsor_location_code"), 
                        rs.getString("sponsor_location_name"), 
                        rs.getString("CITY"), 
                        
                        rs.getString("POSTAL_CODE"), 
                        rs.getString("NWATCH_COLOR"), 
                        rs.getString("TA_COLOR"));
                result.add(lc);
            }
        }
        return result;
    }
    
}
