package com.lo.config;

import java.util.Set;

import java.util.Map;

/**
 * ConsoleConfig interface.
 * @author Auto generator
 */
public interface ConsoleConfig {

    /**
     * Return the property console.taHistory.fields.map of the consoleConfig.properties file
     * @return Map<String, String>
     */
    Map<String, String> consoleTaHistoryFieldsMap();

    /**
     * Return the property console.taHistory.searcheableFields.set of the consoleConfig.properties file
     * @return Set<String>
     */
    Set<String> consoleTaHistorySearcheableFieldsSet();

    /**
     * Return the property console.colors.searcheableFields.set of the consoleConfig.properties file
     * @return Set<String>
     */
    Set<String> consoleColorsSearcheableFieldsSet();

    /**
     * Return the property console.colors.fields.map of the consoleConfig.properties file
     * @return Map<String, String>
     */
    Map<String, String> consoleColorsFieldsMap();

    /**
     * Return the property console.taHistory.idField of the consoleConfig.properties file
     * @return String
     */
    String consoleTaHistoryIdField();

    /**
     * Return the property console.colors.idField of the consoleConfig.properties file
     * @return String
     */
    String consoleColorsIdField();

    /**
     * Return the property datagrids.pageSize of the consoleConfig.properties file
     * @return Integer
     */
    Integer datagridsPageSize();

    /**
     * Return the property datagrids.tradeAreas.pageSize of the consoleConfig.properties file
     * @return Integer
     */
    Integer datagridsTradeAreasPageSize();

}