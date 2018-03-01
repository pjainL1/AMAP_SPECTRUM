package com.lo.hosting.om;

import java.io.File;
import java.util.Date;

/**
 *
 * @author ydumais
 */
public class SponsorExtract extends Extract {

    public SponsorExtract(File file, Date time) {
        super(Type.Sponsors, file, time);
    }

    @Override
    public String getTableName() {
        return "SPONSOR";
    }

    @Override
    public Object[] interpret(String[] line) {
        validate(line, 4);
        Object[] array = new Object[]{
            new Integer(line[0].trim()), // SPONSOR_KEY
            line[1].trim(), // SPONSOR_CODE
            line[2].trim(), // SPONSOR_NAME
            line[3].trim() // AMAP_ROLLUP_GROUP_CODE
        };
        return array;
    }

    @Override
    public boolean createIndex() {
        return false;
    }
}
