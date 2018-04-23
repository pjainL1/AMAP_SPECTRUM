/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lo.layer;

import com.lo.util.MappingUtility;
import com.mapinfo.midev.service.featurecollection.v1.AttributeValue;
import com.mapinfo.midev.service.featurecollection.v1.DoubleValue;
import com.mapinfo.midev.service.mapping.v1.FeatureLayer;
import com.mapinfo.midev.service.style.v1.MapBasicBitmapSymbol;
import com.mapinfo.midev.service.style.v1.MapBasicFontSymbol;
import com.mapinfo.midev.service.style.v1.MapBasicPointStyle;
import com.mapinfo.midev.service.style.v1.MapBasicStyle;
import com.mapinfo.midev.service.style.v1.Style;
import com.mapinfo.midev.service.theme.v1.ApplyStylePart;
import com.mapinfo.midev.service.theme.v1.CustomRangeTheme;
import com.mapinfo.midev.service.theme.v1.OverrideTheme;
import com.mapinfo.midev.service.theme.v1.RangeBin;
import com.mapinfo.midev.service.theme.v1.RangeBinList;
import com.mapinfo.midev.service.theme.v1.RangeTheme;
import com.mapinfo.midev.service.theme.v1.RangeThemeProperties;
import com.mapinfo.midev.service.theme.v1.RangeThemeType;
import com.mapinfo.midev.service.theme.v1.ThemeList;
import java.awt.Color;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 *
 * @author pjain
 */
public class LocationSLALayer extends LocationLayer {

    private static volatile LocationSLALayer s;
    //String layerClass;
    // STYLE PARAMETERS
    String locationKeys;
    Boolean attributeActive = false;
    String[] rangeClasses ;
    int[] colorInt = new int[]{16711680,12599296, 8421376, 4243456, 65280};
    
    
    //String query;

    public LocationSLALayer(String mapInstanceKey, HttpServletRequest req) {
        super(mapInstanceKey);
        this.layerClass = "LOCATIONSLALAYER";
        this.locationKeys = (String) req.getSession().getAttribute("SELECTED_LOCATIONS");
        // String superQuery = super.getQuery();
        // this.setQuery( superQuery + String.format("and sponsor_location_key in (%s)", locationKeys));
        this.border = "normal";
        this.color = "#ff0000";
        this.shape = 35;
    }

    public LocationSLALayer(String mapInstanceKey, HttpSession session) {
        super(mapInstanceKey);
        this.layerClass = "LOCATIONSLALAYER";
        this.locationKeys = (String) session.getAttribute("SELECTED_LOCATIONS");
        // String superQuery = super.getQuery();
        // this.setQuery( superQuery + String.format("and sponsor_location_key in (%s)", locationKeys));
        this.border = "normal";
        this.color = "#ff0000";
        this.shape = 35;
    }

    public static LocationSLALayer getInstance(String mapInstanceKey, HttpServletRequest req) {

        if (s != null && s.getSpecMapInstanceKey().equals(mapInstanceKey)) {
            return s;
        }

        synchronized (LocationSLALayer.class) {

            if (s == null || !s.getSpecMapInstanceKey().equals(mapInstanceKey)) {

                s = new LocationSLALayer(mapInstanceKey, req);
            }
        }

        return s;

    }

    public static LocationSLALayer getInstance(String mapInstanceKey, HttpSession session) {

        if (s != null && s.getSpecMapInstanceKey().equals(mapInstanceKey)) {
            return s;
        }

        synchronized (LocationSLALayer.class) {

            if (s == null || !s.getSpecMapInstanceKey().equals(mapInstanceKey)) {

                s = new LocationSLALayer(mapInstanceKey, session);
            }
        }

        return s;

    }
    
    public void setRangeClasses(String rangeClassesString){
        rangeClasses = rangeClassesString.split(",");
    }
    
    public void setAttributeActive(Boolean active){
        attributeActive = active;
    }

    protected CustomRangeTheme applyCustomStyle(String column,String[] rangeClasses ) {

        CustomRangeTheme customRangeTheme = new CustomRangeTheme();
        RangeBinList rangeBinList = new RangeBinList();
        int i = 0;
        
        for (String range : rangeClasses) {
            String[] ranges = range.split("~");
            rangeBinList.getRangeBin().add(buildRangeBin(
                    buildAttributeDoubleValue(Double.parseDouble(ranges[0])),
                    buildAttributeDoubleValue(Double.parseDouble(ranges[1])),
                    getStyleColors(new Color(colorInt[i++]))));
        }
        //customRangeTheme.setApplyStylePart(ApplyStylePart.COLOR);
        customRangeTheme.setRangeBinList(rangeBinList);
        customRangeTheme.setAllOthers(getStyleColors(Color.white));
        customRangeTheme.setExpression(column);
        return customRangeTheme;

}
    
 public static DoubleValue buildAttributeDoubleValue(double value) {
        DoubleValue doubleValue = new DoubleValue();
        doubleValue.setValue(value);
        return doubleValue;
    }

    public static RangeBin buildRangeBin(AttributeValue lowerAttribut, AttributeValue upperAttribut, Style style) {
        RangeBin rangeBin = new RangeBin();
        rangeBin.setLowerBound(lowerAttribut);
        rangeBin.setUpperBound(upperAttribut);
        rangeBin.setStyle(style);
        return rangeBin;
    }

    public Style getStyleColors(Color color) {
//        MapBasicFontSymbol mapBasicFontSymbol = new MapBasicFontSymbol();
//        MapBasicPointStyle mapBasicPointStyle = new MapBasicPointStyle();
//        mapBasicFontSymbol.setColor(String.format("rgb(%s,%s,%s)", color.getRed(), color.getGreen(), color.getBlue()));
//        mapBasicPointStyle.setMapBasicSymbol(mapBasicFontSymbol);
//        
        MapBasicPointStyle pStyle = new MapBasicPointStyle();
        MapBasicFontSymbol fontSymbol = new MapBasicFontSymbol();
        fontSymbol.setFontName(fontName);
        fontSymbol.setSize(size);
        fontSymbol.setBorder(border);
        fontSymbol.setColor(String.format("rgb(%s,%s,%s)", color.getRed(), color.getGreen(), color.getBlue()));
        fontSymbol.setShape(shape);
        pStyle.setMapBasicSymbol(fontSymbol);
        
        
        
        return pStyle;
    }

@Override
        protected void applyStyle(FeatureLayer layer,String fontName,short size,String border,String color,int shape,String logo){
        ThemeList themeList = new ThemeList();
       
        if(!attributeActive){
        RangeThemeProperties rangeThemeProperties = MappingUtility.buildRangeThemeProperties("VALUE", 5, RangeThemeType.EQUAL_COUNT);
        RangeTheme theme = new RangeTheme();
        theme.setStartStyle(getRangeStyle("#FF0000"));
        theme.setEndStyle(getRangeStyle("#00FF00"));
        theme.setRangeThemeProperties(rangeThemeProperties); 
        themeList.getTheme().add(theme);
        }
        else {
        CustomRangeTheme theme = applyCustomStyle("VALUE",rangeClasses);
        themeList.getTheme().add(theme);
        }
        
        
        
        
        layer.setThemeList(themeList);
    }
   

    private MapBasicStyle getRangeStyle(String Color) {

        MapBasicPointStyle pStyle = new MapBasicPointStyle();
        MapBasicFontSymbol fontSymbol = new MapBasicFontSymbol();
        fontSymbol.setFontName(fontName);
        fontSymbol.setSize(size);
        fontSymbol.setBorder(border);
        fontSymbol.setColor(Color);
        fontSymbol.setShape(shape);
        pStyle.setMapBasicSymbol(fontSymbol);


        return pStyle;
    }
    
}
