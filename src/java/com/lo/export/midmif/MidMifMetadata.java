package com.lo.export.midmif;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author agilbert
 */
public class MidMifMetadata {
    List<String> columnNames = new ArrayList<>();
    List<String> columnTypes = new ArrayList<>();
    String coordsysClause = "TODO";//"CoordSys Earth Projection 1, 0"

    public List<String> getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(List<String> columnNames) {
        this.columnNames = columnNames;
    }

    public List<String> getColumnTypes() {
        return columnTypes;
    }

    public void setColumnTypes(List<String> columnTypes) {
        this.columnTypes = columnTypes;
    }

    public String getCoordsysClause() {
        return coordsysClause;
    }

    public void setCoordsysClause(String coordsysClause) {
        this.coordsysClause = coordsysClause;
    }
    
    
}
