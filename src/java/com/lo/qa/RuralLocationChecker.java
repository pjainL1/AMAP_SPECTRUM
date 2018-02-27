/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lo.qa;

import com.lo.ContextParams;
import com.lo.db.dao.AirMilesDAO;
import com.lo.db.dao.LocationDAO;
import com.lo.db.om.Location;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author YDumais
 */
public class RuralLocationChecker {

    public RuralLocationChecker() {
    }

    public boolean check(ContextParams contextParams, List<Double> locations) {
        if(locations.isEmpty()){
            return false;
        }
        LocationDAO ldao = new LocationDAO(new AirMilesDAO());
        StringBuilder sb = new StringBuilder(ldao.getQuery());
        sb.append(" and sponsor_location_key ").append(AirMilesDAO.prepareInFragment(locations.size()));
        ldao.setQuery(sb.toString());
        ldao.setParams(prepareLocationIn(locations));
        List<Location> locationList = ldao.retrieveLocations(contextParams.getSponsor());
        return checkForRuralPC(locationList);
    }

    private Object[] prepareLocationIn(List<Double> locations) {
        List<Object> result = new ArrayList<Object>();
        for (Double loc : locations) {
            result.add(loc);
        }
        return result.toArray(new Object[]{});
    }

    private boolean checkForRuralPC(List<Location> locations) {
        for (Location location : locations) {
            String pc = location.getPostalCode();
            if (pc != null && pc.length() >= 2 && pc.charAt(1) == '0') {
                return true;
            }
        }
        return false;
    }
}
