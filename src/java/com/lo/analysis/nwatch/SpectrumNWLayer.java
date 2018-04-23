/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lo.analysis.nwatch;

import com.lo.analysis.SpectrumLayer;
import com.lo.config.Confs;
import com.mapinfo.midev.service.mapping.v1.FeatureLayer;
import com.mapinfo.midev.service.table.v1.NamedTable;
import com.mapinfo.midev.service.table.v1.TableList;
import com.mapinfo.midev.service.table.v1.ViewTable;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringEscapeUtils;

/**
 *
 * @author pjain
 */
public class SpectrumNWLayer extends SpectrumLayer {

    private String mapInstanceKey;
    private String layerClass;
    private static volatile SpectrumNWLayer s;
    private ViewTable sourceTable;

    public SpectrumNWLayer(String mapInstanceKey) {
        this.mapInstanceKey = mapInstanceKey; //To change body of generated methods, choose Tools | Templates.
        this.layerClass = "NWATCH";
    }
    

    public static SpectrumNWLayer getInstance(String mapInstanceKey) {

        if (s != null && s.getSpecMapInstanceKey().equals(mapInstanceKey)) {
            return s;
        }

        synchronized (SpectrumNWLayer.class) {

            if (s == null || !s.getSpecMapInstanceKey().equals(mapInstanceKey)) {

                s = new SpectrumNWLayer(mapInstanceKey);
            }
        }

        return s;

    }
    
    @Override
    public String getSpecMapInstanceKey() {
        return mapInstanceKey;

    }
    @Override
    public String getSpecDynamicLayerClass() {
        return layerClass;

    }
    @Override
    public com.mapinfo.midev.service.mapping.v1.Layer getLayerConfiguration(String mapInstanceKey) {
        ViewTable viewTable = new ViewTable();
        TableList tableList = new TableList();
        NamedTable namedTable = new NamedTable();
        NamedTable namedTable2 = new NamedTable();
        String nameNWResultsTable = Confs.CONFIG.namedTableLIM_NW_RESULTS();
        String nameCN06DATable = Confs.CONFIG.namedTableCN06DA();
        namedTable.setName(nameNWResultsTable);
        namedTable2.setName(nameCN06DATable);

        tableList.getTable().add(namedTable);
        tableList.getTable().add(namedTable2);
        viewTable.setTableList(tableList);
        //String query = String.format("SELECT * FROM \"/AMAP_DEV/NamedTables/LIM_TA_POLYGON\" AS LIM_TA_POLYGON WHERE LIM_TA_POLYGON.MAP_ID = %s", mapInstanceKey);
        String query = String.format("SELECT R.MAP_ID as MAP_ID, R.NW_ID as NW_ID, R.DA_ID as DA_ID, R.SPONSOR_LOCATION_KEY as SPONSOR_LOCATION_KEY, R.MAJORITY_LOCATION as MAJORITY_LOCATION, R.MAJORITY_UNITS_OR_SPEND as MAJORITY_UNITS_OR_SPEND, R.MAJORITY_LOCATION_TOTAL as MAJORITY_LOCATION_TOTAL, CN.PRCDDA06 as PRCDDA06, CN.AREA as AREA, CN.LATITUDE as LATITUDE, CN.LONGITUDE as LONGITUDE, CN.MI_PRINX as MI_PRINX, CN.GEOLOC as GEOLOC, CN.GEOSIMPLE as GEOSIMPLE, CN.XMIN as XMIN, CN.XMAX as XMAX, CN.YMIN as YMIN, CN.YMAX as YMAX FROM \"%s\" AS R ,\"%s\" AS CN WHERE CN.PRCDDA06 = R.DA_ID AND R.MAP_ID = '%s'",nameNWResultsTable,nameCN06DATable, mapInstanceKey);
        System.out.println("QUERY1 : " + query);
        //System.out.println("QUERY2 : " + query2);
        //String queryEscaped =  StringEscapeUtils.escapeXml(query);
        // System.out.println("AFTER ESCAPE : " + queryEscaped);
        viewTable.setName("taPolygons");
        viewTable.setSQL(query);

        // create a FeatureLayer that refers to the above view table
        FeatureLayer layer = new FeatureLayer();
        layer.setTable(viewTable);
        
        this.setSourceTable(viewTable);
        
        // return the layer
        return layer;
    }

    @Override
    public ViewTable getSourceTable() {
        return this.sourceTable; //To change body of generated methods, choose Tools | Templates.
    }
    
    public void setSourceTable(ViewTable sourceTable) {
        this.sourceTable = sourceTable;
        //To change body of generated methods, choose Tools | Templates.
    }
}
