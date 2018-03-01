/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.korem.openlayers;

/**
 *
 * @author jduchesne
 */
public interface IMapProviderFactory {
    IMapProvider createMapProvider() throws Exception;
}
