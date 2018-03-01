package com.lo.hosting.om;

import java.io.File;
import java.util.Date;

/**
 * @author ydumais <> updated by csthilaire 2014
 */
public class SimpleExtractFactory {
    private static final SimpleExtractFactory instance = new SimpleExtractFactory();
    private SimpleExtractFactory() {}
    
    public static SimpleExtractFactory getInstance() {
        return instance;
    }
    public Extract get(Extract.Type type, File file, Date time) {
        switch (type) {
            case Sponsors:{ return new SponsorExtract(file, time); }
            case Locations:{ return new LocationExtract(file, time); }
            case Collectors:{ return new CollectorExtract(file, time); }
            case Transactions:{ return new TransactionExtract(file, time); }
            case PostalCode:{ return new PostalCodeExtract(file, time); }
            default:{ throw new RuntimeException("Unsuported type " + type); }
        }
    }
}
