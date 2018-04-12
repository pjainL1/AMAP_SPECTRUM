package com.lo.util;

import com.mapinfo.midev.service.geometries.v1.Envelope;
import com.mapinfo.midev.service.geometries.v1.Point;
import com.mapinfo.midev.service.mapping.v1.BoundsMapView;
import com.mapinfo.midev.service.mapping.v1.ScaleAndCenterMapView;
import com.mapinfo.midev.service.mapping.v1.ZoomAndCenterMapView;
import com.mapinfo.midev.service.units.v1.Distance;
import com.mapinfo.midev.service.units.v1.PaperUnit;

public class MappingView {
    public static ScaleAndCenterMapView getScaleAndCenterMapView(Point mapCenter) throws Exception {
        ScaleAndCenterMapView scaleAndCenterMapView = new ScaleAndCenterMapView();
        scaleAndCenterMapView.setHeight(600);
        scaleAndCenterMapView.setWidth(800);
        scaleAndCenterMapView.setScale(20000);
        scaleAndCenterMapView.setUnit(PaperUnit.PIXEL);
        //sets the map resolution. If not specified, the default one is set to 96 DPI
        //scaleAndCenterMapView.setMapResolution(200);
        if(mapCenter != null){
        	scaleAndCenterMapView.setMapCenter(mapCenter);
        }
        
        return scaleAndCenterMapView;
    }

    public static ZoomAndCenterMapView getZoomAndCenterMapView(Point mapCenter, Distance distance) throws Exception {
        ZoomAndCenterMapView zoomAndCenterMapView = new ZoomAndCenterMapView();
        zoomAndCenterMapView.setHeight(600);
        zoomAndCenterMapView.setWidth(800);
        zoomAndCenterMapView.setUnit(PaperUnit.PIXEL);
        //sets the map resolution. If not specified, the default one is set to 96 DPI
        zoomAndCenterMapView.setZoomLevel(distance);
        if(mapCenter != null){
        	zoomAndCenterMapView.setMapCenter(mapCenter);
        }
        return zoomAndCenterMapView;
    }

    public static BoundsMapView getBoundsMapView(Envelope envelope) throws Exception {
        BoundsMapView boundsMapView = new BoundsMapView();
        boundsMapView.setHeight(600);
        boundsMapView.setWidth(800);
        boundsMapView.setUnit(PaperUnit.PIXEL);
        //sets the map resolution. If not specified, the default one is set to 96 DPI
        // REVIEW: betam: use BuildGeometry.buildWorldEnvelope() instead of buildEnvelopefull()
        if(envelope != null){
        	boundsMapView.setBounds(envelope);
        }

        return boundsMapView;
    }

    public static BoundsMapView getWorldBoundsMapView() throws Exception {
        BoundsMapView boundsMapView = new BoundsMapView();
        boundsMapView.setHeight(600);
        boundsMapView.setWidth(800);
        boundsMapView.setUnit(PaperUnit.PIXEL);
        boundsMapView.setBounds(BuildGeometry.buildWorldEnvelope());
        //sets the map resolution. If not specified, the default one is set to 96 DPI
        return boundsMapView;
    }
}
