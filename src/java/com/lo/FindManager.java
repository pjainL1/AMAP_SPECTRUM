package com.lo;

import com.lo.db.LODataSource;
import com.lo.db.dao.AirMilesDAO;
import com.spinn3r.log5j.Logger;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import org.apache.commons.dbutils.DbUtils;

/**
 *
 * @author jduchesne
 */
public class FindManager {

    private static final Logger log = Logger.getLogger();
    private static FindManager instance = new FindManager();

    public static FindManager get() {
        return instance;
    }
    private static final String P_LONGITUDE = "longitude";
    private static final String P_LATITUDE = "latitude";
    public static final String P_MINX = "minx";
    public static final String P_MAXX = "maxx";
    public static final String P_MINY = "miny";
    public static final String P_MAXY = "maxy";
    private static final String Q_LOCATION = "select longitude, latitude from sponsor_location where trim(sponsor_location_code) = ? and sponsor_code ";
    private static final String Q_FSA =
            "SELECT min(case when rownum = 1 then column_value else NULL end) as minx, "
            + "min(case when rownum = 3 then column_value else NULL end) as maxx, "
            + "max(case when rownum = 2 then column_value else NULL end) as miny, "
            + "max(case when rownum = 4 then column_value else NULL end) as maxy "
            + "FROM TABLE(SELECT sdo_aggr_mbr(geoloc).sdo_ordinates "
            + "FROM fsa "
            + "WHERE UPPER(fsa) = ?)";

    private FindManager() {
    }

    public Point2D getLocationPosition(ContextParams cp, String locationCode, List<String> sponsorCodes) {
        Connection connection = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            connection = LODataSource.getDataSource(cp).getConnection();
            String query = Q_LOCATION + AirMilesDAO.prepareInFragment(sponsorCodes.size());
            pstmt = connection.prepareStatement(query);
            int i = 1;
            pstmt.setString(i++, locationCode);
            for (String code : sponsorCodes) {
                pstmt.setString(i++, code);
            }
            rs = pstmt.executeQuery();
            return getPoint(rs);
        } catch (Exception e) {
            log.error(null, e);
        } finally {
            DbUtils.closeQuietly(connection, pstmt, rs);
        }
        return null;
    }

    public Rectangle2D getFSABounds(ContextParams cp, String fsaNumber) {
        Rectangle2D result = null;
        Connection connection = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            connection = LODataSource.getDataSource(cp).getConnection();
            pstmt = connection.prepareStatement(Q_FSA);
            pstmt.setString(1, fsaNumber.toUpperCase());
            rs = pstmt.executeQuery();
            result = getRectangle(rs);
        } catch (Exception e) {
            log.error(null, e);
        } finally {
            org.apache.commons.dbutils.DbUtils.closeQuietly(connection, pstmt, rs);
        }
        return result;
    }

    private Rectangle2D getRectangle(ResultSet rs) throws Exception {
        if (rs.next()) {
            double minX = rs.getDouble(P_MINX),
                    minY = rs.getDouble(P_MINY),
                    maxX = rs.getDouble(P_MAXX),
                    maxY = rs.getDouble(P_MAXY);
            if (minX == 0 && minY == 0 && maxX == 0 && maxY == 0) {
                return null;
            }
            return new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
        }
        return null;
    }

    private Point2D getPoint(ResultSet rs) throws Exception {
        if (rs.next()) {
            return new Point2D.Double(rs.getDouble(P_LONGITUDE), rs.getDouble(P_LATITUDE));
        }
        return null;
    }
}
