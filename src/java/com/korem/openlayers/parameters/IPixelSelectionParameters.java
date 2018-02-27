package com.korem.openlayers.parameters;

/**
 *
 * @author jduchesne
 */
public interface IPixelSelectionParameters extends IBaseParameters {
    int[] getPixelSelectionBounds();

    String getLayerName();

    void setLayerName(String toString);
}
