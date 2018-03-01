package com.korem.openlayers.parameters;

/**
 *
 * @author jduchesne
 */
public interface ILonLatSelectionParameters extends IBaseParameters {
    double[] getLonLatSelectionBounds();

    String[] getLayerIds();

    String getSrsName();
}
