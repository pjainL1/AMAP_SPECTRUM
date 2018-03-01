package com.lo.hosting.om;

import com.lo.config.Confs;
import com.lo.db.LODataSource;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.ResourceBundle;

/**
 *
 * @author ydumais
 */
public abstract class Extract {

    ResourceBundle rb = ResourceBundle.getBundle("com.lo.hosting.config");
    private SimpleDateFormat sdf;

    public enum Type {
        Sponsors,
        Locations,
        Collectors,
        Transactions,
        PostalCode;
    }
    protected final Date time;
    protected final File file;
    protected final Type type;

    public Extract(Type type, File file, Date time) {
        this.type = type;
        this.time = time;
        this.file = file;
        this.sdf = new SimpleDateFormat(rb.getString("extract.data.date.format"));
    }

    public File getFile() {
        return file;
    }

    public Date getTime() {
        return time;
    }

    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return String.format("%s: %s", type.toString(), file.toString());
    }

    /**
     * Return extract table name
     * @return
     */
    public abstract String getTableName();

    /**
     * Interpret a csv line. The goal is to cast object type into the right format.
     * @param line
     * @return
     */
    public abstract Object[] interpret(String[] line) throws Exception;

    public abstract boolean createIndex();

    void validate(String[] line, int expectedLength) {
        if (line.length != expectedLength) {
            throw new RuntimeException(String.format("Invalid input %s for %s extract.", Arrays.toString(line), this));
        }
    }

    java.sql.Date parseDate(String value) throws ParseException {
        Date date = sdf.parse(value);
        return new java.sql.Date(date.getTime());
    }
    
    public String getDatasourceName() {
        return LODataSource.LONE_DATASOURCE;
    }
    
    public String getSchemaName() {
        return Confs.CONFIG.dbMainSchema();
    }
}
