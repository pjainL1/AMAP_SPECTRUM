/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lo.layer;

import com.mapinfo.midev.service.mapping.v1.FeatureLayer;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author pjain
 */
public class LocationSelectionLayer extends LocationLayer {
    
    private static volatile LocationSelectionLayer s;
    //String layerClass;
    // STYLE PARAMETERS
    String locationKeys ;
    //String query;
    

    public LocationSelectionLayer(String mapInstanceKey,HttpServletRequest req) {
        super(mapInstanceKey);
        this.layerClass = "LOCATIONSELECTIONLAYER";
        this.locationKeys = (String) req.getSession().getAttribute("SELECTED_LOCATIONS");
       // String superQuery = super.getQuery();
       // this.setQuery( superQuery + String.format("and sponsor_location_key in (%s)", locationKeys));
        this.border = null;
        this.color = "#ff0000";
        this.shape = 33;
    }
    

    public static LocationSelectionLayer getInstance(String mapInstanceKey,HttpServletRequest req) {

        if (s != null && s.getSpecMapInstanceKey().equals(mapInstanceKey)) {
            return s;
        }

        synchronized (LocationSelectionLayer.class) {

            if (s == null || !s.getSpecMapInstanceKey().equals(mapInstanceKey)) {

                s = new LocationSelectionLayer(mapInstanceKey,req);
            }
        }

        return s;

    }
   

    
    
    //getQuery fro the parent class
    //update the query using the Lcoatio Keys from the session --> super.query = query
    //
    //public com.mapinfo.midev.service.mapping.v1.Layer getLayerConfiguration(String mapInstanceKey){
        
        
        //FeatureLayer layer = (FeatureLayer) super.getLayerConfiguration(mapInstanceKey);
       // super.applyStyle(layer,fontName,size,border,color,shape);
       // return layer;
        
    //}
    
    
    
}
