package com.lo.analysis.tradearea.builder;

import com.lo.ContextParams;
import com.lo.analysis.tradearea.TradeArea;
import com.lo.config.Confs;
import com.lo.db.helper.OraReaderWriterHelper;
import com.lo.db.om.Location;
import com.lo.db.om.SponsorGroup;
import com.spinn3r.log5j.Logger;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author Charles St-Hilaire for Korem inc.
 */
public class CustomTABuilder extends LocationBasedTABuilder {
    private static final Logger log = Logger.getLogger();
    private final GeometryFactory factory;
    
    public CustomTABuilder(Object[] params, ContextParams cp, SponsorGroup sponsorGroup, Collection<Double> locations) {
        super(cp, sponsorGroup, params);
        setLocations(locations);
        this.factory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), Confs.STATIC_CONFIG.SRID());
    }

    @Override
    protected List<TradeArea> drawTradeAreas() {
        List<TradeArea> result = new ArrayList<>();
        try{
            Geometry geometry = generateGeometry();
            if (getLocations().isEmpty()){
                result.add(new TradeArea(getDefaultLocationCode(), getDefaultLocationKey(), geometry, TradeArea.Type.custom, null, null, null));
            }else{
                List<Location> locationList = retrieveLocations();
                for (Location location : locationList) {
                    result.add(new TradeArea(location.getCode(), location.getKey(), geometry, TradeArea.Type.custom, location.getSponsorKey(), location.getCustomerLocationCode(), location.getSponsorCode()));
                }
            }
        }catch(Exception e){
            log.error("Error while creating Custom TA", e);
        }
        return result;
    }
    
    public static String getDefaultLocationCode() {
        return TradeArea.Type.custom.toString();
    }
    
    public static Double getDefaultLocationKey() {
        return null;
    }
    
    private Geometry generateGeometry() throws Exception {
        Polygon polygon = null;
        if (super.params != null){
            String allCords = (String)super.params[0];
            if (allCords.startsWith("[\"") && allCords.endsWith("\"]") && allCords.contains("\",\"")){
                String[] splitedCoords = allCords.substring(2, allCords.length() - 2).split("\",\"");
                Coordinate[] coords = new Coordinate[splitedCoords.length];
                int idx = 0;
                String[] pairCoord;
                for(String coord : splitedCoords){
                    pairCoord = coord.split(",");
                    double lng = Double.parseDouble(pairCoord[0]);
                    double lat = Double.parseDouble(pairCoord[1]);
                    coords[idx++] = new Coordinate(lng, lat, 0);
                }
                polygon = OraReaderWriterHelper.getInstance().redefineGeometry(coords);
            }
        }
        return polygon;
    }
    
    @Override
    protected List<Location> retrieveLocations() {
        return super.retrieveLocations();
    }
}
