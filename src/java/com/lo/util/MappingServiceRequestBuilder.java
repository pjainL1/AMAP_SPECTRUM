package com.lo.util;

import java.util.List;

import com.mapinfo.midev.service.mapping.v1.GetMapLegendsRequest;
import com.mapinfo.midev.service.mapping.v1.GetNamedMapLegendsRequest;
import com.mapinfo.midev.service.mapping.v1.GetNamedMapWithOverlayLegendRequest;
import com.mapinfo.midev.service.mapping.v1.Layer;
import com.mapinfo.midev.service.mapping.v1.Map;
import com.mapinfo.midev.service.mapping.v1.MapView;
import com.mapinfo.midev.service.mapping.v1.OverlayList;
import com.mapinfo.midev.service.mapping.v1.RenderMapRequest;
import com.mapinfo.midev.service.mapping.v1.RenderNamedMapRequest;
import com.mapinfo.midev.service.mapping.v1.RenderNamedMapWithOverlayRequest;

public class MappingServiceRequestBuilder {

	public static RenderNamedMapRequest createRenderNamedMapRequest(String id,
			String namedMap, MapView mapView) throws Exception {
		
		RenderNamedMapRequest request = new RenderNamedMapRequest();
		request.setId(id);
		request.setMapView(mapView);
		request.setImageMimeType("image/png");
		request.setNamedMap(namedMap);
		return request;
	}

    public static RenderMapRequest createRenderMapRequest(String id, MapView mapView, List<Layer> layers) 
    	throws Exception {
    	
        RenderMapRequest request = new RenderMapRequest();
        request.setId(id);
        request.setImageMimeType("image/png");
        request.setMapView(mapView);
        Map map = new Map();
        map.getLayer().addAll(layers);
        request.setMap(map);

        return request;
    }
	
	public static RenderNamedMapWithOverlayRequest createRenderNamedMapOverlayRequest(
			String id, String namedMap, MapView mapView, Layer layer) throws Exception {
		
		RenderNamedMapWithOverlayRequest request = new RenderNamedMapWithOverlayRequest();
		request.setId(id);
		request.setMapView(mapView);
		request.setImageMimeType("image/png");
		request.setNamedMap(namedMap);
		OverlayList overlayList = new OverlayList();
		if (layer != null) {
			overlayList.getOverlay().add(layer);
		}
		request.setOverlayList(overlayList);

		return request;
	}
	
    public static GetNamedMapLegendsRequest createGetNamedMapLegendsRequest(String id,
			String namedMap, int imageHeight, int imageWidth ) throws Exception {
        GetNamedMapLegendsRequest request = new GetNamedMapLegendsRequest();
        request.setId(id);
        request.setImageMimeType("image/png");
        request.setLegendImageHeight(imageHeight);
        request.setLegendImageWidth(imageWidth);
        request.setNamedMap(namedMap);
        return request;
    }
    
    public static GetMapLegendsRequest createGetMapLegendsRequest(String id, List<Layer> layers ) throws Exception {
        GetMapLegendsRequest request = new GetMapLegendsRequest();
        request.setId(id);
        request.setImageMimeType("image/png");
        Map map = new Map();
        map.getLayer().addAll(layers);
        request.setMap(map);
        return request;
    }

	public static GetNamedMapWithOverlayLegendRequest createGetNamedMapWithOverlayLegendRequest(
														String id,
														String namedMap,Layer layer) {

		GetNamedMapWithOverlayLegendRequest request = new GetNamedMapWithOverlayLegendRequest();
		request.setId(id);
		request.setImageMimeType("image/png");
        request.setLegendImageHeight(16);
        request.setLegendImageWidth(16);
        request.setNamedMap(namedMap);
		OverlayList overlayList = new OverlayList();
		if (layer != null) {
			overlayList.getOverlay().add(layer);
		}
		request.setOverlayList(overlayList);
		return request;
	}
    
    
}
