package com.korem;

import net.sf.json.JSONArray;

/**
 *
 * @author jduchesne
 */
public interface IWMSParams {

    String LAYERS();

    String BBOX();

    String FORMAT();

    Integer WIDTH();

    Integer HEIGHT();
    
     JSONArray filters(); 
}
