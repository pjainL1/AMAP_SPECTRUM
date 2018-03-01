/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lo.util;

import com.korem.map.ws.client.AnnotationService;
import com.korem.map.ws.client.AnnotationServiceServiceLocator;
import com.korem.map.ws.client.FeatureSetService;
import com.korem.map.ws.client.FeatureSetServiceServiceLocator;
import com.korem.map.ws.client.LayerService;
import com.korem.map.ws.client.LayerServiceServiceLocator;
import com.korem.map.ws.client.MapService;
import com.korem.map.ws.client.MapServiceServiceLocator;
import com.korem.map.ws.client.MappingSessionService;
import com.korem.map.ws.client.MappingSessionServiceServiceLocator;
import com.korem.map.ws.client.SearchService;
import com.korem.map.ws.client.SearchServiceServiceLocator;
import com.korem.map.ws.client.SystemService;
import com.korem.map.ws.client.SystemServiceServiceLocator;
import com.korem.map.ws.client.ThematicService;
import com.korem.map.ws.client.ThematicServiceServiceLocator;
import com.korem.map.ws.client.ProjectService;
import com.korem.map.ws.client.ProjectServiceServiceLocator;
import com.spinn3r.log5j.Logger;
import java.net.URL;
import javax.naming.Context;
import javax.naming.InitialContext;

/**
 *
 * @author ydumais
 */
public class WSClient {
    
    private static final Logger log = Logger.getLogger();
    private static MappingSessionService mappingSessionService;
    private static MapService mapService;
    private static FeatureSetService featureSetService;
    private static LayerService layerService;
    private static SearchService searchService;
    private static ThematicService thematicService;
    private static SystemService systemService;
    private static AnnotationService annotationService;
    private static ProjectService projectService;
    
    static {
        try {
            Context initCtx = (Context) new InitialContext().lookup("java:comp/env");
            String url = (String) initCtx.lookup("kms/url");
            log.info("KMS url used [" + url + "]");
            featureSetService = new FeatureSetServiceServiceLocator().getFeatureSetService(new URL(url + "/services/FeatureSetService"));
            layerService = new LayerServiceServiceLocator().getLayerService(new URL(url + "/services/LayerService"));
            mappingSessionService = new MappingSessionServiceServiceLocator().getMappingSessionService(new URL(url + "/services/MappingSessionService"));
            mapService = new MapServiceServiceLocator().getMapService(new URL(url + "/services/MapService"));
            searchService = new SearchServiceServiceLocator().getSearchService(new URL(url + "/services/SearchService"));
            thematicService = new ThematicServiceServiceLocator().getThematicService(new URL(url + "/services/ThematicService"));
            systemService = new SystemServiceServiceLocator().getSystemService(new URL(url + "/services/SystemService"));
            annotationService = new AnnotationServiceServiceLocator().getAnnotationService(new URL(url + "/service/AnnotationService"));
            projectService = new ProjectServiceServiceLocator().getProjectService(new URL(url + "/services/ProjectService"));
        } catch (Throwable e) {
            log.fatal("Unable to initialize pushnsee web service clients", e);
        }
    }
    
    public static MapService getMapService() {
        return mapService;
    }
    
    public static LayerService getLayerService() {
        return layerService;
    }
    
    public static AnnotationService getAnnotationService() {
        return annotationService;
    }
    
    public static FeatureSetService getFeatureSetService() {
        return featureSetService;
    }
    
    public static MappingSessionService getMappingSessionService() {
        return mappingSessionService;
    }
    
    public static SearchService getSearchService() {
        return searchService;
    }
    
    public static SystemService getSystemService() {
        return systemService;
    }
    
    public static ThematicService getThematicService() {
        return thematicService;
    }
    
    public static ProjectService getProjectService() {
        return projectService;
    }
}
