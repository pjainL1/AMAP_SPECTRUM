package com.lo.analysis.tradearea.builder;

import com.korem.spectrum.DriveTimePolygon;
import com.lo.analysis.tradearea.TradeArea;
import com.lo.config.Confs;
import com.mapinfo.coordsys.CoordSys;
import com.mapinfo.coordsys.CoordTransform;
import com.mapinfo.util.DoublePoint;
import com.spinn3r.log5j.Logger;
import com.vividsolutions.jts.geom.Geometry;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ydumais
 */
public class ProjectedTABuilder extends TABuilder {

    private static final Logger log = Logger.getLogger();
    private Double lon;
    private Double lat;
    private Double distance;
    private List<TradeArea> tradeAreas = new ArrayList<TradeArea>();

    public ProjectedTABuilder(Object[] params) throws Exception {
        super(params);
        distance = ((Double) params[0]);
        transformCoordinates();
    }

    private void transformCoordinates() throws Exception {
        DoublePoint dp = new DoublePoint((Double) params[1], (Double) params[2]);
        CoordSys mapCoordsys = CoordSys.createFromMapBasic(Confs.STATIC_CONFIG.webCoordsys());
        CoordTransform ct = new CoordTransform(mapCoordsys, CoordSys.longLatWGS84);
        ct.forward(dp);
        lon = dp.x;
        lat = dp.y;
        log.debug(String.format("drive distance trade area on coordinates %s, %s.", lon, lat));
    }

    @Override
    protected List<TradeArea> drawTradeAreas() {
        DriveTimePolygon driveTimePolygon = new DriveTimePolygon(lon, lat, distance);
        for (Geometry geom : driveTimePolygon.getDriveTimePolygons()) {
            TradeArea ta = new TradeArea(TradeArea.Type.projected.toString(), null, geom, TradeArea.Type.projected, null, null, null);
            ta.setProjectedLatitude(lat);
            ta.setProjectedLongitude(lon);
            tradeAreas.add(ta);
        }
        return tradeAreas;
    }
}
