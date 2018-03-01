
package com.lo.analysis.tradearea.builder;

import com.lo.ContextParams;
import com.lo.db.dao.AirMilesDAO;
import com.lo.db.dao.LocationDAO;
import com.lo.db.om.Location;
import com.lo.db.om.SponsorGroup;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author jphoude
 */
public abstract class LocationBasedTABuilder extends TABuilder {
    private Collection<Double> locations;
    private ContextParams contextParams;
    private SponsorGroup sponsorGroup;

    public LocationBasedTABuilder(ContextParams cp, SponsorGroup sponsorGroup, Object[] params) {
        super(params);
        this.contextParams = cp;
        this.sponsorGroup = sponsorGroup;
    }

    public Collection<Double> getLocations() {
        return locations;
    }

    public void setLocations(Collection<Double> locations) {
        this.locations = locations;
    }
    
    protected List<Location> retrieveLocations() {
        LocationDAO ldao = new LocationDAO(new AirMilesDAO());
        StringBuilder sb = new StringBuilder(ldao.getQuery());
        sb.append(" and sponsor_location_key ").append(AirMilesDAO.prepareInFragment(locations.size()));
        ldao.setQuery(sb.toString());
        ldao.setParams(getLocationsArray());
        List<Location> result = ldao.retrieveLocations(sponsorGroup);
        return result;
    }

    private Object[] getLocationsArray() {
        return locations.toArray(new Object[]{});
    }

    public ContextParams getContextParams() {
        return contextParams;
    }

    public SponsorGroup getSponsorGroup() {
        return sponsorGroup;
    }
    
}
