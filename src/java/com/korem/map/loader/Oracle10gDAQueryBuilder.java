/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.korem.map.loader;

import com.mapinfo.coordsys.oraso.OracleSRID;
import com.mapinfo.dp.QueryParams;
import com.mapinfo.dp.SearchType;
import com.mapinfo.dp.jdbc.SpatialQueryDef;
import com.mapinfo.mapj.Layer;
import com.mapinfo.mapj.MapJ;
import com.mapinfo.util.DoublePoint;
import com.mapinfo.util.DoubleRect;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Properties;

/**
 *
 * @author ydumais
 */
public class Oracle10gDAQueryBuilder extends AbstractOracleQueryBuilder implements Serializable {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Oracle10gDAQueryBuilder.class);

    public Oracle10gDAQueryBuilder() {
    }

    public Oracle10gDAQueryBuilder(Properties props) {
        super(props);
    }

    @Override
    public SpatialQueryDef queryInRectangle(MapJ mapj, Layer layer, SpatialQueryDef queryDef, String columnNames[],
            QueryParams queryParams, DoubleRect rect) throws Exception {
        queryParams = new QueryParams(queryParams);
//        queryParams.setGeometry(true);
//        queryParams.setRendition(true);
        if (query == null) {
            query = queryDef.getQuery();
        }
//        String srid = "NULL";
//        com.mapinfo.coordsys.CoordSys coord = queryDef.getSpatialQueryMetaData().getCoordSys();
//        int id;
//        if (m_bUseSRID) {
//            coord = queryDef.get\SpatialQueryMetaData().getCoordSys();
//            srid = String.valueOf(id);
//        }
//        coord = mapj.getNumericCoordSys();
//        id = OracleSRID.getSRIDFromCS(coord);
        String spatialColumn = queryDef.getSpatialQueryMetaData().getGeometryColumn();
        String spatialCondition = "";
        SearchType sType = queryParams.getSearchType();
//        if (searchWithinRectangleWhereClause != null) {
//            spatialCondition = replaceSpatialCondition(rect, srid);
//        } else {
        spatialCondition = buildMBRCoordinates(rect);
//            if (sType == SearchType.partial) {
//                spatialCondition = buildSpatialCondition(spatialColumn, srid, rect);
//            } else {
//                spatialCondition = buildMBRSpatialCondition(spatialColumn, srid, rect);
//            }
//        }
        String topSelect = buildSelectClause(findRequiredColumns(queryDef, columnNames, queryParams));
        SpatialQueryDef sqd = null;
        if (query.indexOf("SDO_GEOMETRY") == -1) {
            String innerQuery = "";
            if (sType == SearchType.partial) {
                innerQuery = query;
            } else {
                String spatial = spatialColumn.trim().toLowerCase();
                innerQuery = query.replace(spatial, spatial + ", xmin, ymin, xmax, ymax");
            }
            sqd = buildFinalSpatialQuery(topSelect, innerQuery, spatialCondition);
        } else {
            String newQuery = topSelect + " FROM (" + query + ")";
            sqd = new SpatialQueryDef(newQuery.toString(), null);
        }
        log.debug("rectangle=" + sqd.getQuery());
        return sqd;
    }

    @Override
    public SpatialQueryDef queryAtPoint(MapJ mapj, Layer layer, SpatialQueryDef queryDef, String columnNames[], QueryParams queryParams, DoublePoint point)
            throws Exception {
        DoubleRect rect = buildRectBuffer(mapj, layer, point, queryDef);
        if (query == null) {
            query = queryDef.getQuery();
        }
        String srid = "NULL";
        if (m_bUseSRID) {
            int id = OracleSRID.getSRIDFromCS(queryDef.getSpatialQueryMetaData().getCoordSys());
            srid = String.valueOf(id);
        }
        String spatialColumn = queryDef.getSpatialQueryMetaData().getGeometryColumn();
        String spatialCondition = "";
//        if (searchWithinRectangleWhereClause != null) {
//            spatialCondition = replaceSpatialCondition(rect, srid);
//        } else {
        spatialCondition = buildContainsSpatialCondition(spatialColumn, srid, point);
//        }
        String topSelect = buildSelectClause(findRequiredColumns(queryDef, columnNames, queryParams));
        SpatialQueryDef sqd = buildFinalSpatialQuery(topSelect, query, spatialCondition);
        log.debug("point=" + sqd.getQuery());
        return sqd;
    }

    protected String buildSpatialCondition(String spatialColumn, String srid, DoubleRect rect)
            throws Exception {
        StringBuilder whereClause = new StringBuilder();
        whereClause.append(" (MDSYS.SDO_RELATE(").append(spatialColumn).append(",MDSYS.SDO_GEOMETRY(2003,").append(srid).append(",null,MDSYS.SDO_ELEM_INFO_ARRAY(1,1003,3),");
        whereClause.append("MDSYS.SDO_ORDINATE_ARRAY(");
        whereClause.append(rect.xmin).append(",").append(rect.ymin).append(",").append(rect.xmax).append(",").append(rect.ymax);
        whereClause.append(")),'mask=ANYINTERACT querytype=WINDOW') = 'TRUE')");
        return whereClause.toString();
    }

    protected String buildMBRSpatialCondition(String spatialColumn, String srid, DoubleRect rect)
            throws Exception {
        StringBuilder whereClause = new StringBuilder();
        whereClause.append(" (MDSYS.SDO_FILTER(").append(spatialColumn).append(",MDSYS.SDO_GEOMETRY(2003,").append(srid).append(",null,MDSYS.SDO_ELEM_INFO_ARRAY(1,1003,3),");
        whereClause.append("MDSYS.SDO_ORDINATE_ARRAY(");
        whereClause.append(rect.xmin).append(",").append(rect.ymin).append(",").append(rect.xmax).append(",").append(rect.ymax);
        whereClause.append("))) = 'TRUE')");
        return whereClause.toString();
    }

    protected String buildContainsSpatialCondition(String spatialColumn, String srid, DoublePoint point)
            throws Exception {
        StringBuilder whereClause = new StringBuilder();
        whereClause.append(" (MDSYS.SDO_CONTAINS(").append(spatialColumn).append(",MDSYS.SDO_GEOMETRY(2001, ").append(srid).append(", SDO_POINT_TYPE(").append(point.x).append(", ").append(point.y).append(", NULL), NULL, NULL");
        whereClause.append(")) = 'TRUE')");
        return whereClause.toString();
    }

    protected String buildSelectClause(HashMap selectCols) {
        StringBuilder selectClause = new StringBuilder();
        String ss[] = (String[]) selectCols.keySet().toArray(new String[0]);
        if (ss.length > 0) {
            for (int i = 0; i < ss.length; i++) {
                String col = m_quoteChar + ss[i] + m_quoteChar;
                if (i == 0) {
                    selectClause.append("SELECT ").append(col);
                } else {
                    selectClause.append(", ").append(col);
                }
            }
        } else {
            selectClause.append("SELECT * ");
        }
        return selectClause.toString();
    }

    protected String buildMBRCoordinates(DoubleRect rect) {
        rect.expand((rect.xmax - rect.xmin), (rect.ymax - rect.ymin));
        String whereClause = String.format(
                " (xmin >= %s and ymin >= %s and xmax <= %s and ymax <= %s) ",
                rect.xmin, rect.ymin, rect.xmax, rect.ymax);
        return whereClause;
    }

    /** @return query */
    @Override
    protected String addSpatialCondition(String query, String spatialCondition) throws Exception {
        return query.replace("__SPATIAL_PLACEHOLDER__", " and " + spatialCondition);
    }
}
