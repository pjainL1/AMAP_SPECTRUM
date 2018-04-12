/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lo.analysis;

/**
 *
 * @author pjain
 */

import com.korem.openlayers.kms.Layer;
import com.mapinfo.mapj.FeatureLayer;
import com.spinn3r.log5j.Logger;
import javax.servlet.http.HttpServletRequest;

public abstract class SpectrumLayer extends com.mapinfo.midev.service.mapping.v1.Layer{
    
    private static final Logger log = Logger.getLogger();
    
    public abstract com.mapinfo.midev.service.mapping.v1.Layer getLayerConfiguration(String mapInstanceKey);
    
    public abstract String getSpecMapInstanceKey();

    
}
