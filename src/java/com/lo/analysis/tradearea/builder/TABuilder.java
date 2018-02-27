package com.lo.analysis.tradearea.builder;

import com.lo.web.Apply.ProgressListener;
import com.vividsolutions.jts.algorithm.ConvexHull;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.lo.analysis.tradearea.TradeArea;
import com.lo.config.Confs;
import com.lo.db.dao.AirMilesDAO;
import com.lo.db.dao.LocationDAO;
import com.lo.db.om.Collector;
import com.lo.db.om.Location;
import com.lo.util.Chrono;
import com.spinn3r.log5j.Logger;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import java.util.List;

/**
 *
 * @author ydumais
 */
public abstract class TABuilder {

    private static final Logger log = Logger.getLogger();
    private GeometryFactory geometryFactory;
    protected final Chrono chrono;
    private ProgressListener listener;
    protected final Object[] params;

    protected abstract List<TradeArea> drawTradeAreas();

    public TABuilder(Object[] params) {
        this.params = params;
        this.geometryFactory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), Confs.STATIC_CONFIG.SRID());
        this.chrono = new Chrono(log);
    }

    public Object[] getParams() {
        return params;
    }

    /**
     * Draw a convex hull encompassing all collectors passed as argument
     *
     * @param collectors
     * @return
     */
    public Geometry drawConvexHull(List<Collector> collectors) {
        Geometry result;
        if (collectors.size() <= 3) {
            result = drawEnvelope(collectors);
        } else {
            Coordinate[] pts = new Coordinate[collectors.size()];
            for (int i = 0; i < pts.length; i++) {
                Collector c = collectors.get(i);
                pts[i] = new Coordinate(c.getLongitude(), c.getLatitude());
            }
            ConvexHull hull = new ConvexHull(pts, geometryFactory);
            result = hull.getConvexHull();
        }
        return result;
    }

    private Geometry drawEnvelope(List<Collector> collectors) {
        Coordinate[] coords = new Coordinate[collectors.size()];
        for (int i = 0; i < coords.length; i++) {
            coords[i] = new Coordinate(collectors.get(i).getLongitude(), collectors.get(i).getLatitude());
        }
        CoordinateSequence coordinateSequence = new CoordinateArraySequence(coords);
        LinearRing shell = new LinearRing(coordinateSequence, geometryFactory);
        return new Polygon(shell, null, geometryFactory);
    }

    public List<TradeArea> drawTradeAreas(ProgressListener listener) {
        this.listener = listener;
        return drawTradeAreas();
    }

    protected void updateListener(double progress) {
        if (listener != null) {
            listener.update(progress);
        }
    }

}
