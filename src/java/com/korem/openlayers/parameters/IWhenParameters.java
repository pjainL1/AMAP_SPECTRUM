/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.korem.openlayers.parameters;

import net.sf.json.JSONArray;

/**
 *
 * @author ydumais
 */
public interface IWhenParameters extends IBaseParameters {

    String from();

    String to();
    
    JSONArray filters(); 
}
