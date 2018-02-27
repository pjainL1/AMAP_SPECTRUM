package com.lo.util;

import com.lo.config.Confs;
import com.mapinfo.coordsys.CoordSys;
import com.mapinfo.coordsys.CoordTransform;
import com.mapinfo.util.DoublePoint;
import com.spinn3r.log5j.Logger;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;

/**
 *
 * @author YDumais
 */
public class PointInPolygonLocator {

    private static final Logger log = Logger.getLogger();
    private GeometryFactory factory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 8307);
    private Geometry poly;

    public PointInPolygonLocator(double[] points) throws Exception {
        init(points);
    }

    public PointInPolygonLocator(Geometry geom) {
        this.poly = geom;
    }

    public GeometryFactory getFactory() {
        return factory;
    }

    public void setFactory(GeometryFactory factory) {
        this.factory = factory;
    }

    public boolean contains(double x, double y) {
        Coordinate[] coord = new Coordinate[]{new Coordinate(x, y)};
        CoordinateSequence sequence = new CoordinateArraySequence(coord);
        Point p = new Point(sequence, factory);
        return poly.contains(p);
    }

    public Envelope getEnvelope() {
        return new Envelope(poly);
    }

    private void init(double[] points) throws Exception {
        //transformCoordinates(points);
        if (poly == null) {
            Coordinate[] coords = new Coordinate[points.length / 2];
            for (int i = 0; i < coords.length; i++) {
                coords[i] = new Coordinate(points[i * 2], points[i * 2 + 1]);
            }
            CoordinateArraySequence seq = new CoordinateArraySequence(coords);
            LinearRing shell = new LinearRing(seq, factory);
            poly = new Polygon(shell, null, factory);
        }
    }
    public Geometry getPolygon(){
        return poly;
    }
    
    private void transformCoordinates(double[] points) throws Exception {
        //CoordSys mapCoordsys = CoordSys.createFromMapBasic(Confs.STATIC_CONFIG.webCoordsys());
        CoordTransform ct = new CoordTransform(CoordSys.longLatNAD27, CoordSys.longLatWGS84);
        for (int i = 0; i < points.length; i += 2) {
            DoublePoint dp = new DoublePoint(points[i], points[i+1]);
            ct.forward(dp);
            points[i] = dp.x;
            points[i+1] = dp.y;
        }
    }
}
