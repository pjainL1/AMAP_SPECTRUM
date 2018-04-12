/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lo.web;


import com.lo.ContextParams;
import com.mapinfo.midev.service.namedresource.v1.ListNamedResourceResponse;
import com.spinn3r.log5j.Logger;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 *
 * @author pjain
 */
public class SpectrumSession {
    
    
    
        
    private static final Logger LOGGER = Logger.getLogger();
    private String SPEC_KEY = null;
    private ListNamedResourceResponse SPEC_LAYERS = null;
    private ListTiles tiledObj = ListTiles.getInstance();
    private final HttpSession SPEC_SESSION;

    public SpectrumSession(HttpServletRequest req,String sponsor, ContextParams cp) throws Exception {
        
        SPEC_KEY = getSpectrumInstanceKey( req, sponsor, cp) ;
        SPEC_LAYERS = tiledObj.getResp();
        SPEC_SESSION = req.getSession();

    }
    
  
   private String getSpectrumInstanceKey(HttpServletRequest req,String sponsor, ContextParams cp) throws Exception {
        
        String sessionId = req.getSession().getId();
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssS");
        String timeStamp = dateFormat.format(new Date());
        
        String newMapInstanceKey = sessionId + sponsor + timeStamp;
        
        return newMapInstanceKey;
        //params.setMapInstanceKey(newMapInstanceKey);
        //mapProvider.init(params);
        //initLayers(mapProvider, params);
    }
   
   public String getSpectrumKey(){
       return SPEC_KEY;
   }
   
   public HttpSession getSpectrumSession(){
       return SPEC_SESSION;
   }
    
}
