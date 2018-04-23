/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lo.web;

/**
 *
 * @author pjain
 */
import com.korem.openlayers.parameters.IBoundsParameters;
import com.lo.ContextParams;
import com.lo.analysis.SpectrumLayer;
import com.lo.db.dao.AirMilesDAO;
import com.lo.db.dao.LocationDAO;
import com.lo.util.MappingUtility;
import com.mapinfo.midev.service.featurecollection.v1.IntValue;
//import com.lo.util.Envelope;
import com.mapinfo.midev.service.geometries.v1.Envelope;
import com.mapinfo.midev.service.geometries.v1.Geometry;
import com.mapinfo.midev.service.geometries.v1.Pos;
import com.mapinfo.midev.service.mapping.v1.BoundsMapView;
import com.mapinfo.midev.service.mapping.v1.FeatureLayer;
import com.mapinfo.midev.service.mapping.v1.Layer;
import com.mapinfo.midev.service.mapping.v1.MapImage;
import com.mapinfo.midev.service.mapping.v1.RenderMapRequest;
import com.mapinfo.midev.service.mapping.v1.RenderMapResponse;
import com.mapinfo.midev.service.mapping.v1.Rendering;
import com.mapinfo.midev.service.mapping.ws.v1.MappingServiceInterface;
import com.mapinfo.midev.service.mapping.ws.v1.ServiceException;
import com.mapinfo.midev.service.style.v1.MapBasicAreaStyle;
import com.mapinfo.midev.service.style.v1.MapBasicBitmapSymbol;
import com.mapinfo.midev.service.style.v1.MapBasicBrush;
import com.mapinfo.midev.service.style.v1.MapBasicFontSymbol;
import com.mapinfo.midev.service.style.v1.MapBasicPen;
import com.mapinfo.midev.service.style.v1.MapBasicPointStyle;
import com.mapinfo.midev.service.style.v1.MapBasicStyle;
import com.mapinfo.midev.service.theme.v1.Bin;
import com.mapinfo.midev.service.theme.v1.BinList;
import com.mapinfo.midev.service.theme.v1.IndividualValueTheme;
import com.mapinfo.midev.service.theme.v1.OverrideTheme;
import com.mapinfo.midev.service.theme.v1.RangeTheme;
import com.mapinfo.midev.service.theme.v1.RangeThemeProperties;
import com.mapinfo.midev.service.theme.v1.RangeThemeType;
import com.mapinfo.midev.service.theme.v1.ThemeList;
import com.mapinfo.midev.service.units.v1.PaperUnit;

import com.vividsolutions.jts.geom.Coordinate;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;

public class SpectrumRenderMap {

    MappingServiceInterface serviceInterface;
    private static volatile SpectrumRenderMap s;

    public static SpectrumRenderMap getInstance() {

        if (s != null) {
            return s;
        }

        synchronized (ListTiles.class) {

            if (s == null) {

                s = new SpectrumRenderMap();
            }
        }

        return s;

    }

    private SpectrumRenderMap() {

        try {

        } catch (Exception ex) {
            Logger.getLogger(SpectrumRenderMap.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public byte[] SpecRenderMap(List<SpectrumLayer> analysisLayers, String specMapInstanceKey, IBoundsParameters boundsParams, int width, int height) throws Exception {
        LocationDAO locationDAO = new LocationDAO(new AirMilesDAO());
        serviceInterface = MappingPreference.getServiceInterface();
        RenderMapRequest renderMapRequest = new RenderMapRequest();

        //RenderMapResponse response = new RenderMapResponse();
        com.mapinfo.midev.service.mapping.v1.Map map = new com.mapinfo.midev.service.mapping.v1.Map();
        byte[] image = null;
        String TAColorCodes = locationDAO.getSpecTAColorLocation(specMapInstanceKey);
        List<String> locationTAColorCodes = new ArrayList<String>(Arrays.asList(TAColorCodes.split(",")));

        String NWColorCodes = locationDAO.getSpecNWColorLocation(specMapInstanceKey);
        List<String> locationNWColorCodes = new ArrayList<String>(Arrays.asList(NWColorCodes.split(",")));

        ListIterator li = analysisLayers.listIterator(analysisLayers.size());
        //for (SpectrumLayer layer : analysisLayers) {

        while (li.hasPrevious()) {
            SpectrumLayer layer = (SpectrumLayer) li.previous();
            FeatureLayer layerConfig = (FeatureLayer) layer.getLayerConfiguration(specMapInstanceKey);
            if (layer.getSpecDynamicLayerClass().equals("TRADEAREA")) {
                applyStyle(layerConfig, locationTAColorCodes);
            } else if (layer.getSpecDynamicLayerClass().equals("LOCATIONSLALAYER")) {
                //applyStyleSLA(layerConfig);
            }

            map.getLayer().add(layerConfig);

        }

        renderMapRequest.setMap(map);
        renderMapRequest.setReturnImage(true);

        // build envelope (map bounds for the request)
        Envelope envelope = null;
        try {
            envelope = buildEnvelope(boundsParams.xmax(), boundsParams.ymin(), boundsParams.xmin(), boundsParams.ymax());
        } catch (Exception ex) {
            Logger.getLogger(SpectrumRenderMap.class.getName()).log(Level.SEVERE, null, ex);
        }

        BoundsMapView boundsMapView = new BoundsMapView();

        boundsMapView.setHeight(height);

        boundsMapView.setWidth(width);
        boundsMapView.setUnit(PaperUnit.PIXEL);
        boundsMapView.setRendering(Rendering.valueOf("QUALITY"));
        boundsMapView.setBounds(envelope);
        renderMapRequest.setMapView(boundsMapView);

        try {
            // this returns the map image, to be returned to client-side as
            image = serviceInterface.renderMap(renderMapRequest).getMapImage().getImage();
            System.out.println("MAP IMAGE" + image.toString());
            MappingPreference.logout(serviceInterface);
        } catch (ServiceException ex) {
            Logger.getLogger(SpectrumRenderMap.class.getName()).log(Level.SEVERE, null, ex);
        }
        return image;
    }

    public Envelope buildEnvelope(Double east, Double south, Double west, Double north) throws Exception {
        Envelope envelope = new Envelope();
        envelope.setSrsName("EPSG:3857");
        //envelope.setSrsName(SpringPropertiesUtil.getProperty("spectrum.lim.map.projection.display"));
        //set the startPoint
        Pos pos = MappingUtility.buildPos(east, south);
        pos.setMValue(54.33);
        com.mapinfo.midev.service.geometries.v1.Point p = new com.mapinfo.midev.service.geometries.v1.Point();
        p.setPos(pos);
        //p.setSrsName(SpringPropertiesUtil.getProperty("spectrum.lim.map.projection.numerical"));
        p.setSrsName("EPSG:4326");
        //set the endPoint.
        Pos pos1 = MappingUtility.buildPos(west, north);
        pos1.setMValue(54.33);
        com.mapinfo.midev.service.geometries.v1.Point p1 = new com.mapinfo.midev.service.geometries.v1.Point();
        p1.setPos(pos1);
        //p1.setSrsName(SpringPropertiesUtil.getProperty("spectrum.lim.map.projection.numerical"));
        p1.setSrsName("EPSG:4326");
        List<Geometry> points = new ArrayList<>();
        points.add(p);
        points.add(p1);
        //map.projection.display=EPSG:3857
        //List<Geometry> geoms = coordTransorms(points,
        //SpringPropertiesUtil.getProperty("spectrum.lim.map.projection.display"));
        //Coordinate[] coords = geom.getCoordinates();
        //List<Geometry> geoms = coordTransorms(points,"EPSG:3857");
        envelope.getPos().add(p.getPos());
        envelope.getPos().add(p1.getPos());
        return envelope;
    }

    private void applyStyle(FeatureLayer layer, List<String> locationColorCodes) {
        ThemeList themeList = new ThemeList();
//            OverrideTheme overrideTheme = new OverrideTheme();
//            MapBasicAreaStyle style = new MapBasicAreaStyle();
//            MapBasicBrush mapBasicBrush = new MapBasicBrush();
//            mapBasicBrush.setForegroundColor("#FF0000");
//            mapBasicBrush.setBackgroundColor("#FFFFFF");
//            mapBasicBrush.setPattern((short)1);
//            style.setMapBasicBrush(mapBasicBrush);
//            MapBasicPen pen = new MapBasicPen();
//            pen.setWidth((short)4);
//            pen.setPattern((short)2);
//            pen.setColor("#0000FF");
//            style.setMapBasicPen(pen);
//            overrideTheme.setStyle(style);
//            // or refer to this on the repository:
//            //NamedStyle namedStyle = new NamedStyle();
//            //namedStyle.setName("/PATH_TO_NAMED_STYLE");
//            //overrideTheme.setStyle(namedStyle);
//            themeList.getTheme().add(overrideTheme);

        IndividualValueTheme individualValueTheme = new IndividualValueTheme();
        individualValueTheme.setExpression("SPONSOR_LOCATION_KEY");
        BinList binList = new BinList();
        for (String locationColor : locationColorCodes) {
            Bin bin = new Bin();
            IntValue value = new IntValue();
            String[] colorWithLocation = locationColor.split("~");

            value.setValue(Integer.parseInt(colorWithLocation[0])); // The SPONSOR_LOCATION_KEY value
            bin.setValue(value);
            MapBasicAreaStyle individualStyle = new MapBasicAreaStyle();

            // The TA style with color specific to the location (define the same way as for the overrideTheme)
            MapBasicBrush mapBasicBrush2 = new MapBasicBrush();
            mapBasicBrush2.setForegroundColor(colorWithLocation[1]);
            mapBasicBrush2.setBackgroundColor("#FFFFFF");
            // mapBasicBrush2.setBackgroundColor(colorWithLocation[1]);
            mapBasicBrush2.setPattern((short) 2);
            individualStyle.setMapBasicBrush(mapBasicBrush2);
            MapBasicPen pen2 = new MapBasicPen();
            pen2.setWidth((short) 4);

            pen2.setPattern((short) 2);
            pen2.setColor(colorWithLocation[1]);
            individualStyle.setMapBasicPen(pen2);

            bin.setStyle(individualStyle);
            binList.getBin().add(bin);
        }

        individualValueTheme.setBinList(binList);
        themeList.getTheme().add(individualValueTheme);
        layer.setThemeList(themeList);

    }

//    private void applyStyleSLA(FeatureLayer layer) {
//
//        RangeThemeProperties rangeThemeProperties = MappingUtility.buildRangeThemeProperties("VALUE", 5, RangeThemeType.EQUAL_COUNT);
//
//        RangeTheme theme = new RangeTheme();
//        ThemeList themeList = new ThemeList();
//        theme.setStartStyle(getRangeStyle("#FF0000"));
//        theme.setEndStyle(getRangeStyle("#00FF00"));
//
//        theme.setRangeThemeProperties(rangeThemeProperties);
//        themeList.getTheme().add(theme);
//        layer.setThemeList(themeList);
//
//    }
//
//    private MapBasicStyle getRangeStyle(String Color) {
//        String fontName = "MapInfo Symbols";
//        short size = 14;
//        String border = "halo";
//        //String color = "#004973";
//        int shape = 35;
//        MapBasicPointStyle pStyle = new MapBasicPointStyle();
//        //System.out.println("LOGO" + logo);
////            if(logo != null){
////               MapBasicBitmapSymbol fontSymbol = new MapBasicBitmapSymbol(); 
////               fontSymbol.setURI(logo);
////               pStyle.setMapBasicSymbol(fontSymbol);
////            }
////            else{
//        MapBasicFontSymbol fontSymbol = new MapBasicFontSymbol();
//        fontSymbol.setFontName(fontName);
//        fontSymbol.setSize(size);
//        fontSymbol.setBorder(border);
//        // fontSymbol.setDropShadow(Boolean.TRUE);
//        fontSymbol.setColor(Color);
//        fontSymbol.setShape(shape);
//        pStyle.setMapBasicSymbol(fontSymbol);
//        // }
//
//        return pStyle;
//    }

}
