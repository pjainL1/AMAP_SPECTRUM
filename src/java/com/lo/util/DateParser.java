/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lo.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author ydumais
 */
public class DateParser {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    private static final SimpleDateFormat sdfSpec = new SimpleDateFormat("yyyy-MM-dd");

    public static java.sql.Date parse(String val) throws ParseException {
        return new java.sql.Date(sdf.parse(val).getTime());
    }

    public static String prepareOracleWhenFragment(String value) {
        return String.format("to_date('%s','YYYYMMDD')", value);
    }

    public static String prepareOracleWhenFragmentFromPickers(String value) {
        return String.format("to_date('%s','MM/DD/YYYY')", value);
    }
    
     public static String prepareSpecWhenFragment(String value) {
        return String.format("StringToDate('%s','yyyy-mm-dd')", value);
    }

    public static String prepareSpecWhenFragmentFromPickers(String value) {
        return String.format("StringToDate('%s','mm/dd/yyyy')", value);
    }

    public static String prepareOracleWhenFragment(Date value) {
        return prepareOracleWhenFragment(sdf.format(value));
    }
    
    public static String prepareSpecWhenFragment(Date value) {
        return prepareSpecWhenFragment(sdfSpec.format(value));
    }
}
