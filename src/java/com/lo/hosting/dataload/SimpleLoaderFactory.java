package com.lo.hosting.dataload;

import com.lo.hosting.om.Extract;
import com.lo.hosting.watchdog.LoaderStatus;
import com.lo.hosting.watchdog.LoadingResult;

/**
 *
 * @author ydumais
 */
public class SimpleLoaderFactory {
    private static SimpleLoaderFactory instance = new SimpleLoaderFactory();

    private SimpleLoaderFactory() {
    }

    public static SimpleLoaderFactory getInstance() {
        return instance;
    }

    public Loader get(Extract extract, LoadingResult lr) {
        LoaderStatus loaderStatus = new LoaderStatus(lr);
        switch (extract.getType()) {
            case Sponsors:{ return new FullExtractLoader(extract, lr, loaderStatus); }
            case Locations:{ return new LocationFullExtractLoader(extract, lr, loaderStatus); }
            case Collectors:{ return new FullExtractLoader(extract, lr, loaderStatus); }
            case Transactions:{ return new IncrementalExtractLoader(extract, lr, loaderStatus); }
            case PostalCode:{ return new FullExtractLoader(extract, lr, loaderStatus); }
            default:{ throw new RuntimeException("Unsuported extract type " + extract); }
        }
    }
}
