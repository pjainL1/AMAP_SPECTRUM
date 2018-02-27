package com.lo.db.proxy;

import com.lo.config.Confs;
import com.lo.db.om.TradeAreaHistoryEntry;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class TaHistoryProxy extends GridProxy {

    public TaHistoryProxy() throws SQLException {}
    
    @Override
    protected Map<String, String> getFieldsMap() {
        return Confs.CONSOLE_CONFIG.consoleTaHistoryFieldsMap();
    }
    
    @Override
    protected Set<String> getSearcheableFields() {
        return Confs.CONSOLE_CONFIG.consoleTaHistorySearcheableFieldsSet();
    }
    
    @Override
    protected String getBaseQuery() {
        return Confs.CONSOLE_QUERIES.taHistoryBaseSelect();
    }

    @Override
    protected String getIdColumn() {
        return Confs.CONSOLE_CONFIG.consoleTaHistoryIdField();
    }
    
    public List<TradeAreaHistoryEntry> getTradeAreaHistory(int start, int limit, String property, String direction, String search, List<Filter> filtersValuesList) throws SQLException, ParseException {
        List<TradeAreaHistoryEntry> result = new ArrayList<>();
        
        try (ResultSet rs = getResultSet(start, limit, property, direction, search, filtersValuesList)) {
            while (rs.next()) {
                TradeAreaHistoryEntry taHistory = new TradeAreaHistoryEntry(
                        rs.getString("SPONSOR_LOCATION_CODE"), 
                        rs.getInt("ID"), 
                        rs.getDate("CREATION_DATE"), 
                        rs.getString("TYPE"), 
                        rs.getString("TYPE_DETAIL"), 
                        rs.getDate("DATE_FROM"), 
                        rs.getDate("DATE_TO"), 
                        rs.getInt("SPONSOR_LOCATION_KEY"), 
                        rs.getString("STYLE"), 
                        null, 
                        rs.getString("LOGIN"), 
                        rs.getString("AMAP_ROLLUP_GROUP_NAME"));
                result.add(taHistory);
            }
            return result;
        }
    }
    
    public void deleteOldTradeAreaHistory() throws SQLException {
        PreparedStatement ps = prepare(Confs.CONSOLE_QUERIES.taHistoryDeleteOldRecord());
        ps.executeUpdate();
    }
}
