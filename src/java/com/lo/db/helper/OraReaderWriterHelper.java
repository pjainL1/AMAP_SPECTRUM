package com.lo.db.helper;

import com.lo.config.Confs;
import com.lo.db.LODataSource;
import com.mapinfo.coordsys.CoordSys;
import com.mapinfo.unit.AngularUnit;
import com.mapinfo.unit.LinearUnit;
import com.mapinfo.util.DoublePoint;
import com.spinn3r.log5j.Logger;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.oracle.OraWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import oracle.jdbc.OracleConnection;
import oracle.sql.STRUCT;
import org.apache.tomcat.dbcp.dbcp.DelegatingConnection;

/**
 * OraWriter Helper class
 * @author Charles St-Hilaire for Korem inc.
 */
public class OraReaderWriterHelper {
    private static final Logger LOGGER = Logger.getLogger();
    
    public static final int ORACLE_GEOM_DIMENSION = 2;
    private static final int NUMBER_NODES_TO_ADD_MIN = 5;
    
    // Note that the INSERT statement in Oracle SQL has a limit of 999 arguments. 
    // Therefore, you cannot create a variable-length array of more than 999 elements using the SDO_GEOMETRY constructor inside a transactional INSERT statement
    // http://docs.oracle.com/html/A88805_01/sdo_objl.htm
    private static final int MAX_NODES = 490; // 999/2, with a little buffer to be sure it's ok.
    private static final float NUMBER_NODES_TO_ADD_PER_KM = 1/10f;
    
    private static final OraReaderWriterHelper INSTANCE  = new OraReaderWriterHelper();
    private OraReaderWriterHelper(){ super(); }
    public static OraReaderWriterHelper getInstance(){return INSTANCE ;}
    
    public STRUCT getOracleGeometry(Geometry geometry) throws SQLException{
        try(Connection con = LODataSource.getLoneDataSource().getConnection()){
            Connection aCon = ((DelegatingConnection) con).getInnermostDelegate();
            OraWriter oraWriter = new OraWriter((OracleConnection)aCon);
            oraWriter.setDimension(ORACLE_GEOM_DIMENSION);
            return oraWriter.write(geometry);
        }
    }
    
    public Polygon createGeometry(Coordinate[] coords) {
        GeometryFactory factory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), Confs.STATIC_CONFIG.SRID());
        
        return factory.createPolygon(factory.createLinearRing(coords), null);
    }
    
    public Polygon redefineGeometry(Geometry geom) throws Exception{
        Coordinate[] coordinates = geom.getCoordinates();
        return redefineGeometry(coordinates);
    }
    
    // this method add X points between each point of the polygon. 
    // See https://jira.korem.com/browse/LOTY-291
    public Polygon redefineGeometry(Coordinate[] coordinates) throws Exception{
        List<Coordinate> newPoints = new ArrayList<>(coordinates.length*10);
        int size = coordinates.length;
        int maxNodesToAdd = ((MAX_NODES - size) / (size + 1));
        for(int i=0;i<size;i++){
            double x1 = coordinates[i].x;
            double y1 = coordinates[i].y;
            double x2,y2;
            Coordinate nextPoint;
            if (i!=(size-1)) {
                nextPoint = coordinates[i+1];
            } else {
                nextPoint = coordinates[0];
            }
            x2 = nextPoint.x;
            y2 = nextPoint.y;
            double m = (y2-y1)/(x2-x1);
            double b = y1-(m*x1);
            newPoints.add(new Coordinate(x1, y1));
            double meters = CoordSys.longLatNAD27.cartesianDistance(new DoublePoint(x1, y2), new DoublePoint(x2, y2), LinearUnit.kilometer);
            int nodesToAdd = Math.max(NUMBER_NODES_TO_ADD_MIN, (int)(meters * NUMBER_NODES_TO_ADD_PER_KM));
            nodesToAdd = Math.min(maxNodesToAdd, nodesToAdd);
            LOGGER.debug(String.format("Will add %s nodes to enhance postal code search precision.", nodesToAdd));
            
            for(int j=1;j<nodesToAdd;j++){
                if(Double.isNaN(m)||Double.isInfinite(m)){
                    double y = y1+(((y2-y1)/nodesToAdd)*j);
                    newPoints.add(new Coordinate(x1,y));
                }else{
                    double x = x1+(((x2-x1)/nodesToAdd)*j);
                    double y = (m*x)+b;
                    newPoints.add(new Coordinate(x,y));
                }
            }
        }
        return createGeometry(newPoints.toArray(new Coordinate[newPoints.size()]));
    }
}
