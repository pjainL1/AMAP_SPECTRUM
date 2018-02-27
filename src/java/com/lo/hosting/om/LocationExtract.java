/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lo.hosting.om;

import java.io.File;
import java.util.Date;

/**
 *
 * @author YDumais
 */
public class LocationExtract extends Extract {

    public LocationExtract(File file, Date time) {
        super(Type.Locations, file, time);
    }

    @Override
    public String getTableName() {
        return "SPONSOR_LOCATION";
    }

    @Override
    public Object[] interpret(String[] line) {
        validate(line, 12);
        int i = 0;
        Object[] array = new Object[]{
            new Integer(line[i++].trim()), // 0- SPONSOR_KEY
            new Integer(line[i++].trim()), // 1- SPONSOR_LOCATION_KEY
            line[i++].trim(), // 2- SPONSOR_CODE
            line[i++].trim(), // 3- SPONSOR_LOCATION_CODE (AMCODE)
            line[i++].trim(), // 4- CUSTOMER_LOCATION_CODE
            line[i++].trim(), // 5- SPONSOR_LOCATION_NAME
            new Double(line[i++].trim()), // 6- LONGITUDE
            new Double(line[i++].trim()), // 7- LATITUDE
            line[i++].trim(), // 8- CITY
            line[i++].trim(), // 9- PROVINCE_CODE
            line[i++].trim(), // 10- POSTAL_CODE
            line[i++].trim(), // 11- FSA
        };
        return array;
    }

    @Override
    public boolean createIndex() {
        return true;
    }
}
