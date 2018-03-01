package com.lo.db.proxy;

import com.korem.Proxy;
import com.lo.ContextParams;
import com.lo.config.Confs;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author maitounejjar
 */
public class NwTaLegendProxy extends Proxy {

    public static final int MAX_QUERY_LOCATIONS = 999;

    public NwTaLegendProxy(ContextParams cp) throws SQLException {
        super(cp);
    }

    public List<LocationColor> getNwTaLegend(List<Long> locationKeys) throws SQLException {

        List<LocationColor> list = new ArrayList<>();

        if (locationKeys == null || locationKeys.isEmpty()) {
            return list;
        }

        List<String> locList = getLocationLists(locationKeys);
        StringBuilder sb = new StringBuilder();
        
        int i=0;
        // add the first partial query
        sb.append( String.format(Confs.QUERIES.legendsNwTa(), locList.get(i++)) );
        
        // for each remaining element, add " UNION " + partial query
        for( ;i<locList.size(); i++ ) {
            String partialQuery = String.format(Confs.QUERIES.legendsNwTa(), locList.get(i));
            sb.append(" UNION ").append(partialQuery);
        }

        // now that all our partial queries are in place, we can create the full query
        String query = sb.toString();

        PreparedStatement ps = prepare(query);
        ResultSet rs = null;
        rs = ps.executeQuery();

        while (rs.next()) {            
            String locationCode = rs.getString("sponsorLocationCode");            
            String taColor = rs.getString("taColor");
            String nwColor = rs.getString("nwColor");

            LocationColor lc = new LocationColor(locationCode, taColor, nwColor);
            list.add(lc);
        }

        return list;
    }

    public static List<String> getLocationLists(List<Long> locationKeys) {

        /*        
        IN  : list of location keys as longs
        OUT : list of comma separated locationKeys as Strings. Each string will contain MAX_QUERY_LOCATIONS of location Keys,
              except the last element which may contain less.
        */
        
        List<String> list = new ArrayList<>();
        int length = locationKeys.size();
        int i = 0;
        while (true) {
            int start = i;
            int finish = i + Math.min(length - i, MAX_QUERY_LOCATIONS);
            if (start >= length) {
                break; // last index inlocationKeys passed
            }
            // create a string from location keys in the index range (start,finish)
            StringBuilder locations = new StringBuilder();
            String firstLocationKey = Long.toString(locationKeys.get(i));
            locations.append(firstLocationKey);

            for (int j = start + 1; j < finish; j++) {
                locations = locations.append(", ").append(locationKeys.get(j));
            }
            
            list.add(locations.toString());

            // increase i
            i += MAX_QUERY_LOCATIONS;
        }

        return list;

    }

    public static class LocationColor {

        private String locationCode;
        private String taColor;
        private String nwColor;

        public LocationColor(String locationCode, String taColor, String nwColor) {
            this.locationCode = locationCode;
            this.taColor = taColor;
            this.nwColor = nwColor;
        }

        public String getLocationCode() {
            return locationCode;
        }

        public String getTaColor() {
            return taColor;
        }

        public String getNwColor() {
            return nwColor;
        }

        public void setLocationCode(String locationCode) {
            this.locationCode = locationCode;
        }

        public void setTaColor(String taColor) {
            this.taColor = taColor;
        }

        public void setNwColor(String nwColor) {
            this.nwColor = nwColor;
        }

    }

}
