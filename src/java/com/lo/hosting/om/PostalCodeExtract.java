package com.lo.hosting.om;

import java.io.File;
import java.util.Date;

/**
 * This class is about Postal Code Extraction.
 * 
 * @author Charles St-Hilaire for Korem inc.
 */
public class PostalCodeExtract extends Extract{
    public PostalCodeExtract(File file, Date time) {
        super(Type.PostalCode, file, time);
    }

    @Override
    public String getTableName() {
        return "POSTAL_CODE";
    }

    @Override
    public Object[] interpret(String[] line) {
        validate(line, 4);
        Object[] array = new Object[]{
            line[0].trim(), // POSTAL_CODE
            new Double(line[1].trim()), // POSTAL_CODE_CENTROID_LATITUDE
            new Double(line[2].trim()), // POSTAL_CODE_CENTROID_LONGITUDE
            new Integer(line[3].trim()) // HOUSEHOLDS
        };
        return array;
    }

    @Override
    public boolean createIndex() {
        return true;
    }
}
