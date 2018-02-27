package com.korem.openlayers.parameters;

/**
 *
 * @author jduchesne
 */
public interface ILayerVisibilityParameters extends IBaseParameters {

    String id();
    String name();
    Boolean visibility();
    String parent();
    String getLabelField();
}
