package com.korem.openlayers.parameters;

/**
 *
 * @author jduchesne
 */
public interface IPixelSelectionParameters extends IBaseParameters {
    double[] getPixelSelectionBounds();

    String getLayerName();

    void setLayerName(String toString);
}
