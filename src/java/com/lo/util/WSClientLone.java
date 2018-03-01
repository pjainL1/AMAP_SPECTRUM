/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lo.util;


import lone.com.korem.map.ws.client.MapService;
import lone.com.korem.map.ws.client.MappingSessionService;
import lone.com.korem.map.ws.client.LayerServiceServiceLocator;
import lone.com.korem.map.ws.client.MapServiceServiceLocator;
import lone.com.korem.map.ws.client.MappingSessionServiceServiceLocator;
import com.spinn3r.log5j.Logger;
import java.net.URL;
import javax.naming.Context;
import javax.naming.InitialContext;
import lone.com.korem.map.ws.client.FeatureSetService;
import lone.com.korem.map.ws.client.FeatureSetServiceServiceLocator;
import lone.com.korem.map.ws.client.LayerService;
import lone.com.korem.map.ws.client.ThematicService;
import lone.com.korem.map.ws.client.ThematicServiceServiceLocator;

/**
 *
 * @author ydumais
 */
public class WSClientLone {

    private static final Logger log = Logger.getLogger();
    private static MappingSessionService loneMappingSessionService;
    private static MapService loneMapService;
    private static LayerService loneLayerService;
    private static FeatureSetService loneFeatureSetService;
    private static ThematicService loneThematicService;

    static {
        try {
            Context initCtx = (Context) new InitialContext().lookup("java:comp/env");
            String url = (String) initCtx.lookup("kms/url");
            loneFeatureSetService = new FeatureSetServiceServiceLocator().getLoneFeatureSetService(new URL(url + "/services/LoneFeatureSetService"));

            loneLayerService = new LayerServiceServiceLocator().getLoneLayerService(new URL(url + "/services/LoneLayerService"));
            loneMappingSessionService = new MappingSessionServiceServiceLocator().getLoneMappingSessionService(new URL(url + "/services/LoneMappingSessionService"));
            loneMapService = new MapServiceServiceLocator().getLoneMapService(new URL(url + "/services/LoneMapService"));
            loneThematicService = new ThematicServiceServiceLocator().getLoneThematicService(new URL(url + "/services/LoneThematicService"));
        } catch (Throwable e) {
            log.fatal("Unable to initialize pushnsee web service clients", e);
        }
    }

    public static MapService getMapService() {
        return loneMapService;
    }

    public static LayerService getLayerService() {
        return loneLayerService;
    }
 
    public static MappingSessionService getMappingSessionService() {
        return loneMappingSessionService;
    }

    public static FeatureSetService getFeatureSetService() {
        return loneFeatureSetService;
    }

    public static ThematicService getLoneThematicService() {
        return loneThematicService;
    }
 }
