/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.korem.openlayers;

import java.util.Map;

/**
 *
 * @author jduchesne
 */
public interface IInfoFilter {
    boolean tableExists(String tableName);

    boolean isColumnExcluded(String tableName, String columnName);

    void alter(String tableName, Map<String, Map<String, Object>> info, String kpiColumn, Object kpiValue);

    void addPrivate(String table, String column, Object value, Map<String, Object> privateMap);

    boolean isKPI(String table, String column);

    String[] getColumns(String tableName);
}
