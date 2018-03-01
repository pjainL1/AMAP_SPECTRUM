package com.lo.hosting.om;

import com.spinn3r.log5j.Logger;
import java.io.File;
import java.util.Date;

/**
 *
 * @author YDumais
 */
public class CollectorExtract extends Extract {
    private static final Logger LOGGER = Logger.getLogger();
    
    private boolean errorLogged;

    public CollectorExtract(File file, Date time) {
        super(Type.Collectors, file, time);
    }

    @Override
    public String getTableName() {
        return "COLLECTOR";
    }
    
    private Integer getCollectorKey(String value) {
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            if (!errorLogged) {
                errorLogged = true;
                LOGGER.warn("Collector key format was invalid. Trying to parse as double instead.", e);
            }
            return Double.valueOf(value).intValue();
        }
    }

    @Override
    public Object[] interpret(String[] line) throws Exception {
        int index = -1;
        Object[] array = new Object[]{
            getCollectorKey(line[++index].trim()), // 0- COLLECTOR_KEY
            new Double(line[++index].trim()), // 1- LATITUDE
            new Double(line[++index].trim()), // 2- LONGITUDE
            line[++index].trim(), // 3- POSTAL_CODE
            line[++index].trim(), // 4- DA
            line[++index].trim().equals("") ? "?" : line[index].trim(), // 5- FSA
            parseDate(line[++index].trim()), // 6- COORDINATE_CHANGE_DATE
            Integer.valueOf(line[++index].trim()), // 7- ACTIVE_LAST_YEAR
            Integer.valueOf(line[++index].trim()),// 8- PROMO_MAILABLE_FLAG
            Integer.valueOf(line[++index].trim()),// 9- EMAILABLE_FLAG
            Integer.valueOf(line[++index].trim()),// 10- WEB_ACTIVITY_FLAG
            Integer.valueOf(line[++index].trim()),// 11- MOBILE_APP_ACTIVITY_FLAG        
        };
        return array;
    }

    @Override
    public boolean createIndex() {
        return true;
    }
}
