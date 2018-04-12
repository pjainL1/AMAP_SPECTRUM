/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lo.web;

import com.mapinfo.midev.service.namedresource.v1.ListNamedResourceRequest;
import com.mapinfo.midev.service.namedresource.v1.ListNamedResourceResponse;
import com.mapinfo.midev.service.namedresource.v1.NamedResource;
import com.mapinfo.midev.service.namedresource.ws.v1.NamedResourceServiceInterface;
import com.mapinfo.midev.service.namedresource.ws.v1.ServiceException;
import com.mapinfo.midev.service.namedresource.ws.v1.NamedResourceService;
import com.mapinfo.midev.service.namedresource.v1.ResourceType;
import com.spinn3r.log5j.Logger;

/**
 *
 * @author pjain
 */
public class ListTiles {

    private static final Logger log = Logger.getLogger();
//public static void main(String[] args) {
    private ListNamedResourceResponse resp = null;
    
    private static volatile ListTiles s;

    private ListTiles() {
        
        resp = retriveTiles();

    }
    
    public ListNamedResourceResponse getResp(){
        return resp;
    }
    
   
    
     public static ListTiles getInstance(){

        if (s != null ) return s;

         synchronized(ListTiles.class){

          if (s == null ) {

           s = new ListTiles();
          }
       }

       return s;

     }
     
     
    private ListNamedResourceResponse retriveTiles(){
    
        ListNamedResourceResponse response = null;
        try {
            //create an client interface to the NamedResource service.
            NamedResourceServiceInterface serviceInterface = Preference.getServiceInterface();
            //create request
            ListNamedResourceRequest request = getTiles();
            //send request to service and get response
            response = serviceInterface.listNamedResources(request);
            //To print some basic information from response.
            
            for (NamedResource namedResource : response.getNamedResource()) {
                System.out.println(namedResource.getResourceType() + "	" + namedResource.getPath() + namedResource);
            }

            Preference.logout(serviceInterface);

        } catch (ServiceException se) {
            SpectrumUtilities.printError(se);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return response;
    }

  

    public ListNamedResourceRequest getTiles() throws Exception {
        ListNamedResourceRequest ListNamedResourceRequest = new ListNamedResourceRequest();
        ListNamedResourceRequest.setId("List123");
        
        ListNamedResourceRequest.setPath("/AMAP_DEV");
        ListNamedResourceRequest.setResourceType(ResourceType.NAMED_TILE);

        return ListNamedResourceRequest;
    }
}
