/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lo.layer;

/**
 *
 * @author pjain
 */
import com.lo.analysis.SpectrumLayer;
import com.lo.config.Confs;
import com.mapinfo.midev.service.featurecollection.v1.IntValue;
import com.mapinfo.midev.service.mapping.v1.FeatureLayer;
import com.mapinfo.midev.service.style.v1.MapBasicAreaStyle;
import com.mapinfo.midev.service.style.v1.MapBasicBitmapSymbol;
import com.mapinfo.midev.service.style.v1.MapBasicBrush;
import com.mapinfo.midev.service.style.v1.MapBasicFontSymbol;
import com.mapinfo.midev.service.style.v1.MapBasicPen;
import com.mapinfo.midev.service.style.v1.MapBasicPointStyle;
import com.mapinfo.midev.service.style.v1.MapBasicSymbol;
import com.mapinfo.midev.service.table.v1.NamedTable;
import com.mapinfo.midev.service.table.v1.TableList;
import com.mapinfo.midev.service.table.v1.ViewTable;
import com.mapinfo.midev.service.theme.v1.Bin;
import com.mapinfo.midev.service.theme.v1.BinList;
import com.mapinfo.midev.service.theme.v1.IndividualValueTheme;
import com.mapinfo.midev.service.theme.v1.OverrideTheme;
import com.mapinfo.midev.service.theme.v1.ThemeList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringEscapeUtils;

/**
 *
 * @author pjain
 */
public class LocationLayer extends SpectrumLayer {

    String mapInstanceKey;
    String layerClass;
    protected String query;
    private static volatile LocationLayer s;
    private ViewTable sourceTable;

    // STYLE PARAMETERS
    String fontName = "MapInfo Symbols";
    short size = 14;
    String border = "halo";
    String color = "#004973";
    int shape = 35;
    String logo = null;
    
    
    public LocationLayer(){
        this.layerClass = "LOCATIONLAYER";
    }
    
    public LocationLayer(String mapInstanceKey) {
        this.mapInstanceKey = mapInstanceKey; 
        this.layerClass = "LOCATIONLAYER";
    }
    

    public static LocationLayer getInstance(String mapInstanceKey) {

        if (s != null && s.getSpecMapInstanceKey().equals(mapInstanceKey)) {
            return s;
        }

        synchronized (LocationLayer.class) {

            if (s == null || !s.getSpecMapInstanceKey().equals(mapInstanceKey)) {

                s = new LocationLayer(mapInstanceKey);
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
    public void setQuery(String query) {
        this.query = query;
    }
    
    public void setLogo(String logo) {
        this.logo = logo;
    }
    
    public String getQuery() {
        return query;
    }
    
    @Override
    public com.mapinfo.midev.service.mapping.v1.Layer getLayerConfiguration(String mapInstanceKey) {
        ViewTable viewTable = new ViewTable();
        TableList tableList = new TableList();
        NamedTable namedTable = new NamedTable();
        NamedTable namedTableSLA = new NamedTable();
      
        String xyTable = Confs.CONFIG.xyTableSPONSOR_LOCATION();
        String nameSLATable = Confs.CONFIG.namedTableLIM_SLA_RESULTS();
        namedTable.setName(xyTable);
        namedTableSLA.setName(nameSLATable);

        tableList.getTable().add(namedTable);
        tableList.getTable().add(namedTableSLA);
        
        viewTable.setTableList(tableList);
        //String query = String.format("SELECT * FROM \"/AMAP_DEV/NamedTables/LIM_TA_POLYGON\" AS LIM_TA_POLYGON WHERE LIM_TA_POLYGON.MAP_ID = %s", mapInstanceKey);
        //String query = String.format("SELECT * FROM \"%s\" AS LIM_TA_POLYGON WHERE LIM_TA_POLYGON.MAP_ID = '%s'",namePolygonTable, mapInstanceKey);
        System.out.println("QUERY : " + getQuery());
        //System.out.println("QUERY2 : " + query2);
        //String queryEscaped =  StringEscapeUtils.escapeXml(query);
        // System.out.println("AFTER ESCAPE : " + queryEscaped);
        viewTable.setName("locations");
        viewTable.setSQL(query);

        // create a FeatureLayer that refers to the above view table
        FeatureLayer layer = new FeatureLayer();
        layer.setTable(viewTable);
        this.setSourceTable(viewTable);
        
        applyStyle(layer,fontName,size,border,color,shape,logo);
        // return the layer
        return layer;
    }
    
    protected void applyStyle(FeatureLayer layer,String fontName,short size,String border,String color,int shape,String logo){
            ThemeList themeList = new ThemeList();
            
            
            OverrideTheme overrideTheme = new OverrideTheme();
            
            
            MapBasicPointStyle pStyle = new MapBasicPointStyle();
            //System.out.println("LOGO" + logo);
            if(logo != null){
               MapBasicBitmapSymbol fontSymbol = new MapBasicBitmapSymbol(); 
               fontSymbol.setURI(logo);
               pStyle.setMapBasicSymbol(fontSymbol);
            }
            else{
            MapBasicFontSymbol fontSymbol = new MapBasicFontSymbol();
            fontSymbol.setFontName(fontName);
            fontSymbol.setSize(size);
            fontSymbol.setBorder(border);
           // fontSymbol.setDropShadow(Boolean.TRUE);
            fontSymbol.setColor(color);
            fontSymbol.setShape(shape);
            pStyle.setMapBasicSymbol(fontSymbol);
            }
            
           
            overrideTheme.setStyle(pStyle);
            // or refer to this on the repository:
            //NamedStyle namedStyle = new NamedStyle();
            //namedStyle.setName("/PATH_TO_NAMED_STYLE");
            //overrideTheme.setStyle(namedStyle);
            themeList.getTheme().add(overrideTheme);

            layer.setThemeList(themeList);
        
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
