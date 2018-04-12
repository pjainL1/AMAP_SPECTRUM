/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lo.analysis.tradearea;

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
public class SpectrumTradeAreaLayer extends SpectrumLayer {

    private String mapInstanceKey;
    private static volatile SpectrumTradeAreaLayer s;

    public SpectrumTradeAreaLayer(String mapInstanceKey) {
        this.mapInstanceKey = mapInstanceKey; //To change body of generated methods, choose Tools | Templates.
    }
    

    public static SpectrumTradeAreaLayer getInstance(String mapInstanceKey) {

        if (s != null && s.getSpecMapInstanceKey().equals(mapInstanceKey)) {
            return s;
        }

        synchronized (SpectrumTradeAreaLayer.class) {

            if (s == null || !s.getSpecMapInstanceKey().equals(mapInstanceKey)) {

                s = new SpectrumTradeAreaLayer(mapInstanceKey);
            }
        }

        return s;

    }
    
    @Override
    public String getSpecMapInstanceKey() {
        return mapInstanceKey;

    }
    @Override
    public com.mapinfo.midev.service.mapping.v1.Layer getLayerConfiguration(String mapInstanceKey) {
        ViewTable viewTable = new ViewTable();
        TableList tableList = new TableList();
        NamedTable namedTable = new NamedTable();
        String namePolygonTable = Confs.CONFIG.namedTableLIM_TA_PLOYGON();
        namedTable.setName(namePolygonTable);

        tableList.getTable().add(namedTable);
        viewTable.setTableList(tableList);
        //String query = String.format("SELECT * FROM \"/AMAP_DEV/NamedTables/LIM_TA_POLYGON\" AS LIM_TA_POLYGON WHERE LIM_TA_POLYGON.MAP_ID = %s", mapInstanceKey);
        String query = String.format("SELECT * FROM \"%s\" AS LIM_TA_POLYGON WHERE LIM_TA_POLYGON.MAP_ID = '%s'",namePolygonTable, mapInstanceKey);
        System.out.println("QUERY1 : " + query);
        //System.out.println("QUERY2 : " + query2);
        //String queryEscaped =  StringEscapeUtils.escapeXml(query);
        // System.out.println("AFTER ESCAPE : " + queryEscaped);
        viewTable.setName("taPolygons");
        viewTable.setSQL(query);

        // create a FeatureLayer that refers to the above view table
        FeatureLayer layer = new FeatureLayer();
        layer.setTable(viewTable);
        
        // return the layer
        return layer;
    }

}
