package com.korem.openlayers.kms;

import com.korem.openlayers.IMapProvider;
import com.korem.openlayers.IMapProviderFactory;

/**
 *
 * @author jduchesne
 */
public class MapProviderFactory implements IMapProviderFactory {

    private static final MapProviderFactory instance = new MapProviderFactory();

    public static MapProviderFactory instance() {
        return instance;
    }

    private MapProviderFactory() {
    }

    @Override
    public IMapProvider createMapProvider() throws Exception {
        return new MapProvider();
    }
}
