package com.lo.analysis.tradearea.builder;

import com.korem.spectrum.DriveTimePolygon;
import com.lo.ContextParams;
import com.lo.analysis.tradearea.TradeArea;
import com.lo.db.dao.AirMilesDAO;
import com.lo.db.dao.LocationDAO;
import com.lo.db.om.Location;
import com.lo.db.om.SponsorGroup;
import com.vividsolutions.jts.geom.Geometry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author ydumais
 */
public class DistanceTABuilder extends LocationBasedTABuilder {

    private List<TradeArea> tradeAreas = new ArrayList<TradeArea>();
    private int progress;
    private int steps;

    public DistanceTABuilder(Object[] params, ContextParams cp, SponsorGroup sponsorGroup, Collection<Double> locations) {
        super(cp, sponsorGroup, params);
        setLocations(locations);
        progress = 0;
        steps = (80 / (locations.size() > 0 ? locations.size() : 1));
    }

    @Override
    protected List<TradeArea> drawTradeAreas() {
        List<Location> locationList = retrieveLocations();
        for (Location location : locationList) {
            DriveTimePolygon driveTimePolygon = new DriveTimePolygon(location.getLongitude(),
                    location.getLatitude(), (Double) params[0]);
            for (Geometry geom : driveTimePolygon.getDriveTimePolygons()) {
                tradeAreas.add(new TradeArea(location.getCode(), location.getKey(), geom, TradeArea.Type.distance, location.getSponsorKey(), location.getCustomerLocationCode(), location.getSponsorCode()));
            }
            updateListener(progress += steps);
        }
        return tradeAreas;
    }
    
    @Override
    protected List<Location> retrieveLocations() {
        List<Location> retrievedLocations = super.retrieveLocations();
        updateListener(progress += 10);
        return retrievedLocations;
    }
}
