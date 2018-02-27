/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.korem.openlayers.parameters;

/**
 *
 * @author jduchesne
 */
public interface IPositionParameters extends IBaseParameters {
    double getLongitude();

    double getLatitude();

    double getZoom();
}
