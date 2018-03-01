package com.lo.analysis;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.http.HttpSession;

/**
 *
 * @author maitounejjar
 */
public class NwTaLegendManager {

    private static final NwTaLegendManager INSTANCE = new NwTaLegendManager();
    private final String SESSION_MAP_KEY = "nwTaLegends";

    public static final NwTaLegendManager get() {
        return INSTANCE;
    }

    private NwTaLegendManager() {
        /*exists only to defeat instantiation*/
    }

    private ConcurrentHashMap<String, List<Long>> getSessionMap(HttpSession session, String mapInstanceKey) {
        if (session.getAttribute(SESSION_MAP_KEY) == null) {
            ConcurrentHashMap map = new ConcurrentHashMap();
            session.setAttribute(SESSION_MAP_KEY, map);
        }

        return (ConcurrentHashMap<String, List<Long>>)session.getAttribute(SESSION_MAP_KEY);
    }

    public void setSelectedLocations(HttpSession session, String mapInstanceKey, String locationCodes) {
        /*
         This method is called from "NWatchMethod.java", "StoreLevelAnalisis.java" and "TradeAreaMethod.java" to save selected locations list on the session
         It creates a ConcurrentHashMap in the session, if this map doesn't exist. Otherwise, it retrieves it and puts a new entry with key=mapInstanceKey and value=locationCodes
         */
        ConcurrentHashMap<String, List<Long>> map = getSessionMap(session, mapInstanceKey);
        map.put(mapInstanceKey, parseLocationList(locationCodes));
    }

    private List<Long> parseLocationList(String locationKeys) {
        List<Long> list = new ArrayList<>();

        String[] matches = locationKeys.split(",");

        for (String match : matches) {
            if (!match.isEmpty()) {
                double d = Double.valueOf(match);
                list.add((long) d);
            }
        }

        return list;
    }

    public List<Long> getSelectedLocations(HttpSession session, String mapInstanceKey) {

        /*
         Returns a list of locationCodes as longs
         Return an empty list of strings if the no attriute called MAP is found in the session. This happens if the method setSelectedLocations() is never called yet.
         */
        ConcurrentHashMap<String, List<Long>> map = getSessionMap(session, mapInstanceKey);
        List<Long> list = map.get(mapInstanceKey);

        if (list == null) {
            return new ArrayList<>();
        }

        return list;
    }

}
