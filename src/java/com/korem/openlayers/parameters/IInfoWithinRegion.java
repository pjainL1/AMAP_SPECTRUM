/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.korem.openlayers.parameters;

/**
 *
 * @author jduchesne
 */
public interface IInfoWithinRegion extends IBaseParameters {
    double[] getBounds();
    String[] getLayerIds();
    String getKpiType();
}
