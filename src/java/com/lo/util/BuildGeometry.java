package com.lo.util;

import java.util.List;

import com.mapinfo.midev.service.geometries.v1.Curve;
import com.mapinfo.midev.service.geometries.v1.Envelope;
import com.mapinfo.midev.service.geometries.v1.InteriorList;
import com.mapinfo.midev.service.geometries.v1.LegacyText;
import com.mapinfo.midev.service.geometries.v1.LineString;
import com.mapinfo.midev.service.geometries.v1.MultiCurve;
import com.mapinfo.midev.service.geometries.v1.MultiPoint;
import com.mapinfo.midev.service.geometries.v1.MultiPolygon;
import com.mapinfo.midev.service.geometries.v1.Point;
import com.mapinfo.midev.service.geometries.v1.Polygon;
import com.mapinfo.midev.service.geometries.v1.Pos;
import com.mapinfo.midev.service.geometries.v1.Ring;
import com.mapinfo.midev.service.units.v1.Angle;
import com.mapinfo.midev.service.units.v1.AngularUnit;

//
// This class contains functions that show how to create
// different type of geometries.
//

//
public class BuildGeometry {

    public static Point buildPoint(String srs,double x, double y,Double mValue ) throws Exception {
        Point point = new Point();
        if(srs!= null && !srs.isEmpty()){
        	point.setSrsName(srs);
        }
        point.setPos(buildPos(x, y, mValue));
        return point;
    }
    
    public static Pos buildPos(double x, double y,Double mValue) throws Exception {
        Pos pos = new Pos();
        pos.setX(x);
        pos.setY(y);
        if(mValue != null){
        	pos.setMValue(mValue);
        }
        return pos;
    }


    public static MultiPoint buildMultiPoint() throws Exception {
        MultiPoint multiPoint = new MultiPoint();
        multiPoint.setSrsName("EPSG:4326");

        Point point = new Point();
        Pos pos = new Pos();
        pos.setX(-75.44);
        pos.setY(45.66);
        pos.setMValue(new Double(54.33));
        point.setPos(pos);
        multiPoint.getPoint().add(point);

        Point point1 = new Point();
        Pos pos1 = new Pos();
        pos1.setX(-75.44);
        pos1.setY(45.66);
        pos1.setMValue(new Double(54.33));
        point1.setPos(pos1);
        multiPoint.getPoint().add(point1);

        return multiPoint;
    }


    public static Polygon buildPolygon() throws Exception {
        Polygon polygon = new Polygon();
        polygon.setSrsName("EPSG:4326");
        Ring exteriorRing = new Ring();
        LineString lineString = new LineString();

        Pos pos = new Pos();
        pos.setX(-75.66);
        pos.setY(42.33);
        pos.setMValue(new Double(-12.33));
        lineString.getPos().add(pos);

        //add the viaPoint
        Pos pos1 = new Pos();
        pos1.setX(-77.66);
        pos1.setY(43.33);
        pos1.setMValue(new Double(-17.33));
        lineString.getPos().add(pos1);
        //add the end Point.
        Pos pos2 = new Pos();
        pos2.setX(-78.66);
        pos2.setY(44.33);
        pos2.setMValue(new Double(-19.33));
        lineString.getPos().add(pos2);

        Pos pos3 = new Pos();
        pos3.setX(-75.66);
        pos3.setY(42.33);
        pos3.setMValue(new Double(-12.33));
        lineString.getPos().add(pos3);

        exteriorRing.getLineString().add(lineString);

        polygon.setExterior(exteriorRing);

        //set the interior ring.
        Ring interiorRing = new Ring();
        LineString interiorlineString = new LineString();

        Pos interiorPos = new Pos();
        interiorPos.setX(-75.66);
        interiorPos.setY(45.33);
        interiorPos.setMValue(new Double(-12.33));
        interiorlineString.getPos().add(interiorPos);
        //add the viaPoint
        Pos interiorPos1 = new Pos();
        interiorPos1.setX(-77.66);
        interiorPos1.setY(46.33);
        interiorPos1.setMValue(new Double(-17.33));
        interiorlineString.getPos().add(interiorPos1);
        //add the end Point.
        Pos interiorPos2 = new Pos();
        interiorPos2.setX(-75.66);
        interiorPos2.setY(45.33);
        interiorPos2.setMValue(new Double(-19.33));
        interiorlineString.getPos().add(interiorPos2);

        interiorRing.getLineString().add(interiorlineString);
        InteriorList interiorList = new InteriorList();
        polygon.setInteriorList(interiorList);
        //polygon.getInteriorList().getRing().add(interiorRing[0]);

        return polygon;
    }

    public static MultiPolygon buildMultiPolygon(String srs, List<Pos> points) throws Exception {
        MultiPolygon multiPolygon = new MultiPolygon();

        multiPolygon.setSrsName(srs);
        Polygon polygon = new Polygon();
        Ring exteriorRing = new Ring();
        LineString lineString = new LineString();
        lineString.getPos().addAll(points);
        exteriorRing.getLineString().add(lineString);
        polygon.setExterior(exteriorRing);

        //set the interior ring.
        Ring interiorRing = new Ring();
        LineString interiorlineString = new LineString();
        //	lineString.setDimension("2");
        //interiorlineString.setSrsName("EPSG:4326");
        Pos interiorPos = new Pos();
        interiorPos = new Pos();
        interiorPos.setX(-75.66);
        interiorPos.setY(45.33);
        interiorPos.setMValue(new Double(-12.33));
        interiorlineString.getPos().add(interiorPos);

        //add the viaPoint
        Pos interiorPos1 = new Pos();
        interiorPos1.setX(-77.66);
        interiorPos1.setY(46.33);
        interiorPos1.setMValue(new Double(-17.33));
        interiorlineString.getPos().add(interiorPos1);
        //add the end Point.
        Pos interiorPos2 = new Pos();
        interiorPos2.setX(-78.66);
        interiorPos2.setY(49.33);
        interiorPos2.setMValue(new Double(-19.33));
        interiorlineString.getPos().add(interiorPos2);
        //add the end point
        Pos interiorPos3 = new Pos();
        interiorPos3.setX(-75.66);
        interiorPos3.setY(45.33);
        interiorPos3.setMValue(new Double(-12.33));
        interiorlineString.getPos().add(interiorPos3);

        interiorRing.getLineString().add(interiorlineString);
        InteriorList interiorList = new InteriorList();
        polygon.setInteriorList(interiorList);
        //polygon.getInteriorList().getRing().add(interiorRing);
        multiPolygon.getPolygon().add(polygon);
        return multiPolygon;

    }
    
    

    public static Envelope buildWorldEnvelope() throws Exception {
        Envelope envelope = new Envelope();
        envelope.setSrsName("EPSG:4326");
        //set the lower left
        Pos pos = new Pos();
        pos.setX(-180);
        pos.setY(-90);
        pos.setMValue(new Double(54.33));
        envelope.getPos().add(pos);

        //set the upper right.
        Pos pos1 = new Pos();
        pos1.setX(180);
        pos1.setY(90);
        envelope.getPos().add(pos1);

        return envelope;
    }

    public static Envelope buildEnvelope() throws Exception {
        Envelope envelope = new Envelope();
        envelope.setSrsName("EPSG:4326");
        //set the startPoint
        Pos pos = new Pos();
        pos.setX(-80.44);
        //pos.setX(-75.44);
        pos.setY(45.66);
        pos.setMValue(new Double(54.33));
        envelope.getPos().add(pos);

        //set the endPoint.
        Pos pos1 = new Pos();
        pos1.setX(-73.44);
        //pos1.setY(43.66);
        pos1.setY(37.66);
        pos1.setMValue(54.33);
        envelope.getPos().add(pos1);

        return envelope;
    }
    
    public static Envelope buildEnvelope(String srs, List<Pos> pos ) throws Exception {
        Envelope envelope = new Envelope();
        envelope.setSrsName(srs);
        envelope.getPos().addAll(pos);
        return envelope;
    }

    public static Ring buildRing() throws Exception {
        Ring ring = new Ring();
        ring.setSrsName("EPSG:4326");
        //set the lineString.
        LineString lineString = new LineString();

        Pos pos = new Pos();
        pos = new Pos();
        pos.setX(-75.66);
        pos.setY(45.33);
        pos.setMValue(new Double(-12.33));
        lineString.getPos().add(pos);
        //add the viaPoint
        Pos pos1 = new Pos();
        pos1.setX(-77.66);
        pos1.setY(46.33);
        pos1.setMValue(new Double(-17.33));
        lineString.getPos().add(pos1);
        //add the end Point.
        Pos pos2 = new Pos();
        pos2.setX(-78.66);
        pos2.setY(49.33);
        pos2.setMValue(new Double(-19.33));
        lineString.getPos().add(pos2);
        Pos pos3 = new Pos();
        pos3.setX(-75.66);
        pos3.setY(45.33);
        pos3.setMValue(new Double(-12.33));
        lineString.getPos().add(pos3);

        ring.getLineString().add(lineString);
        return ring;
    }

    public static LineString buildLineString() throws Exception {
        LineString lineString = new LineString();
        lineString.setSrsName("EPSG:4326");
        //add the first point.
        Pos pos = new Pos();
        pos.setX(-75.44);
        pos.setY(45.66);
        pos.setMValue(new Double(54.33));
        lineString.getPos().add(pos);
        Pos pos1 = new Pos();
        pos1.setX(-73.44);
        pos1.setY(43.66);
        pos1.setMValue(new Double(54.33));
        lineString.getPos().add(pos1);
        Pos pos2 = new Pos();
        pos2.setX(-74.44);
        pos2.setY(45.66);
        pos2.setMValue(new Double(54.33));
        lineString.getPos().add(pos2);

        return lineString;

    }
    
    public static LineString buildLineString(String srs,List<Pos> pos) throws Exception {
        LineString lineString = new LineString();
        lineString.setSrsName(srs);        
        lineString.getPos().addAll(pos);
        return lineString;

    }

    public static Curve buildCurve() throws Exception {
        Curve curve = new Curve();

        curve.setSrsName("EPSG:4326");
        //set the lineString.
        LineString lineString = new LineString();
        //LineString.setSrsName("EPSG:4326");


        Pos pos = new Pos();
        pos.setX(-75.66);
        pos.setY(45.33);
        pos.setMValue(new Double(-12.33));
        lineString.getPos().add(pos);
        //add the viaPoint
        Pos pos1 = new Pos();
        pos1.setX(-77.66);
        pos1.setY(46.33);
        pos1.setMValue(new Double(-17.33));
        lineString.getPos().add(pos1);
        //add the end Point.
        Pos pos2 = new Pos();
        pos2.setX(-78.66);
        pos2.setY(49.33);
        pos2.setMValue(new Double(-19.33));
        lineString.getPos().add(pos2);

        curve.getLineString().add(lineString);

        return curve;

    }

    public static LegacyText buildLegacyText() throws Exception {
        LegacyText legacyText = new LegacyText();
        Envelope envelope = buildEnvelope();
        legacyText.setRectangle(envelope);
        legacyText.setText("This is Legacy Text");
        legacyText.setSrsName("EPSG:4326");
        Angle angle = new Angle();
        angle.setValue(90);
        angle.setUom(AngularUnit.DEGREE);
        legacyText.setAngle(angle);

        return legacyText;


    }

    public static MultiCurve buildMultiCurve() throws Exception {
        MultiCurve multiCurve = new MultiCurve();
        multiCurve.setSrsName("EPSG:4326");
        Curve curve = new Curve();
        //curve.setSrsName("EPSG:4326");
        //set the lineString.
        LineString lineString = new LineString();
        //lineString.setSrsName("EPSG:4326");

        Pos pos = new Pos();
        pos = new Pos();
        pos.setX(-75.66);
        pos.setY(45.33);
        pos.setMValue(new Double(-12.33));
        lineString.getPos().add(pos);
        //add the viaPoint
        Pos pos1 = new Pos();
        pos1.setX(-77.66);
        pos1.setY(46.33);
        pos1.setMValue(new Double(-17.33));
        lineString.getPos().add(pos1);
        //add the end Point.
        Pos pos2 = new Pos();
        pos2.setX(-78.66);
        pos2.setY(49.33);
        pos2.setMValue(new Double(-19.33));
        lineString.getPos().add(pos2);

        curve.getLineString().add(lineString);
        multiCurve.getCurve().add(curve);

        return multiCurve;

    }
}