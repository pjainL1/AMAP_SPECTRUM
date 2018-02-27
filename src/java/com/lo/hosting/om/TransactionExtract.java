package com.lo.hosting.om;

import java.io.File;
import java.util.Date;

/**
 *
 * @author YDumais
 */
public class TransactionExtract extends Extract {
    
    private final String databaseName;

    public TransactionExtract(File file, Date time) {
        super(Type.Transactions, file, time);
        databaseName = createDatabaseName(file);
    }
    
    private String createDatabaseName(File file) {
        return file.getName().split("_", 2)[0];
    }

    @Override
    public String getTableName() {
        return "TXN";
    }

    @Override
    public Object[] interpret(String[] line) throws Exception {
        validate(line, 8);
        int i = 0;
        Object[] array = new Object[]{
            new Integer(line[i++].trim()), // COLLECTOR_KEY
            new Integer(line[i++].trim()), // SPONSOR_KEY
            new Integer(line[i++].trim()), // SPONSOR_LOCATION
            parseDate(line[i++].trim()), // TRANSACTION_DATE
            new Integer(line[i++].trim()), // COUNT
            new Float(line[i++].trim()), // SPEND
            new Float(line[i++].trim()), // BASE_MILE
            new Float(line[i++].trim()), // DISTANCE
            new java.sql.Date(time.getTime()) // EXTRACT_TIME
        };
        
//        13209146,
//        187,
//        181971,
//        20131220,
//        1,
//        27.00,
//        1,
//        1.754
        return array;
    }

    @Override
    public boolean createIndex() {
        return false;
    }

    @Override
    public String getDatasourceName() {
        return databaseName;
    }

    @Override
    public String getSchemaName() {
        return "";
    }
    
}
