package com.lo.util;

import com.mapinfo.graphics.Rendition;
import com.mapinfo.unit.Distance;
import com.mapinfo.unit.LinearUnit;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import java.awt.Color;

/**
 *
 * @author agilbert
 */
public class StyleUtils {
    public static boolean isRegion(Geometry geometry){
        if(geometry instanceof Polygon||geometry instanceof MultiPolygon){
            return true;
        }else if(geometry instanceof GeometryCollection){
            return isRegion(((GeometryCollection)geometry).getGeometryN(0));
        }
        return false;
    }
    
    public static boolean isLine(Geometry geometry){
        if(geometry instanceof LineString||geometry instanceof MultiLineString){
            return true;
        }else if(geometry instanceof GeometryCollection){
            return isLine(((GeometryCollection)geometry).getGeometryN(0));
        }
        return false;
    }
    
    public static String getRendition(Geometry geometry, Rendition rendition){
        if(isRegion(geometry)){
            return StyleUtils.getPolygonRendition(rendition);
        }else if(isLine(geometry)){
            return StyleUtils.getLineRendition(rendition);
        }else{
            return StyleUtils.getPointRendition(rendition);
        }
    }
    
    public static String getPointRendition(Rendition r) {
        try {
            int shape = r.getString(Rendition.SYMBOL_STRING).charAt(0);
            Color c = r.getColor(Rendition.SYMBOL_FOREGROUND);
            Color cb = r.getColor(Rendition.SYMBOL_BACKGROUND);
            Distance distance  = r.getDistance(Rendition.FONT_SIZE);
            long size = Math.round(distance.getScalarValue(LinearUnit.point));

            Rendition.FilterEffects filterEffects = (Rendition.FilterEffects)r.getValue(Rendition.FILTER_EFFECTS);
            String family = r.getString(Rendition.FONT_FAMILY).trim().replace("\"", "").trim();

            int effectValue = 0;
            if (filterEffects == Rendition.FilterEffects.BOX) {
                effectValue += 32;
            }
            if (filterEffects == Rendition.FilterEffects.HALO) {
                if(cb.equals(Color.BLACK)){
                    effectValue += 16;
                }else{
                    effectValue += 256;
                }
            }

            if(family.contains("MapInfo 3.0 Compatible")){
                return "Symbol (" + shape + ", " + (0x00FFFFFF & c.getRGB()) + ", " + size+")";
            }else{
                family = family.replace(" Unicode", "");
                return "Symbol (" + shape + ", " + (0x00FFFFFF & c.getRGB()) + ", " + size + ", \"" + family + "\", " + effectValue + ", 0)";
            }
        } catch (Exception e) {
            // Something went wrong, for now, we return a default Symbol
            return "SYMBOL (35,16711680,24)";
        }
    }

    public static String getLineRendition(Rendition r) {
        Color stroke = r.getColor(Rendition.STROKE);
        long width = Math.round(r.getFloat(Rendition.STROKE_WIDTH) * 10) / 10;
        if(width>7){
            width = 7;
        }
        int pattern = 2;
        if(r.getFloat(Rendition.STROKE_OPACITY) == null || r.getFloat(Rendition.STROKE_OPACITY)==0F){
            pattern = 1;
        }
        return "Pen (" + width + ", " + pattern + ", " + (0x00FFFFFF & stroke.getRGB()) + ")";
    }

    public static String getPolygonRendition(Rendition r) {
        Color stroke = r.getValue(Rendition.STROKE)!=null?r.getColor(Rendition.STROKE):Color.black;
        long width = Math.round(r.getFloat(Rendition.STROKE_WIDTH) * 10) / 10;
        if(width>7){
            width = 7;
        }
        Object fillObj = r.getValue(Rendition.FILL);
        Color foreground = null;
        Color background = null;
        if (fillObj instanceof Rendition) {
            Rendition fillRendition = (Rendition)r.getValue(Rendition.FILL);
            foreground = fillRendition.getColor(Rendition.SYMBOL_FOREGROUND);
            background = fillRendition.getColor(Rendition.SYMBOL_BACKGROUND);
        } else {
            foreground = r.getColor(Rendition.FILL);
            background = r.getColor(Rendition.FILL);
        }

        //todo process patterns
        //Object b = r.getBrush();
        //Object p = r.getPen();


        int penPattern = 2; //p.getPattern();
        int brushPattern = 2; //b.getPattern();
        
        if(r.getFloat(Rendition.FILL_OPACITY)==0F){
            brushPattern = 1;
        }
        
        if(r.getValue(Rendition.STROKE_OPACITY)!=null&&r.getFloat(Rendition.STROKE_OPACITY)==0F){
            penPattern = 1;
        }
        
        return "Pen (" + width + ", " + penPattern + ", " + (0x00FFFFFF & stroke.getRGB()) + ") Brush (" + brushPattern + ", " + (0x00FFFFFF & foreground.getRGB()) + ", " + (0x00FFFFFF & background.getRGB()) + ")";
    }
}
