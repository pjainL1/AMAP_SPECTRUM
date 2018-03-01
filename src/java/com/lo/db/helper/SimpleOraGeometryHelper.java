package com.lo.db.helper;

import com.lo.config.Confs;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

/**
 * @author Charles St-hilaire for Korem inc.
 */
public class SimpleOraGeometryHelper {
    private static SimpleOraGeometryHelper instance;
    
    private static final String SIMPLE_POLYGON_TEMPLATE = "SDO_UTIL.RECTIFY_GEOMETRY(SDO_GEOMETRY (2003, %d, null, SDO_ELEM_INFO_ARRAY(1,1003,1), SDO_ORDINATE_ARRAY(%s)), 0.05)";
    
    private SimpleOraGeometryHelper() {}
    
    public static SimpleOraGeometryHelper getInstance() {
        if (instance == null){
            instance = new SimpleOraGeometryHelper();
        }
        return instance;
    }
    
    public String getOracleGeometry(Geometry geom) {
        return getOracleGeometry(geom, Confs.STATIC_CONFIG.SRID());
    }
    
    public String getOracleGeometry(Geometry geom, int srid) {
        StringBuilder sb = new StringBuilder("");
        Coordinate[] coordinates = geom.getCoordinates();
        for (int i = 0; i < coordinates.length; i++) {
            Coordinate coord = coordinates[i];
            sb.append(coord.x);
            sb.append(",");
            sb.append(coord.y);
            if (i + 1 < coordinates.length) {
                sb.append(", ");
            }
        }
        return String.format(SIMPLE_POLYGON_TEMPLATE, srid, sb.toString());
    }
}
