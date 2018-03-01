package com.lo.util;

import com.lo.db.helper.SimpleOraGeometryHelper;
import com.spinn3r.log5j.Logger;
import com.vividsolutions.jts.geom.Geometry;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 *
 * @author ydumais
 */
public class PreparedStatementLogger {
    public static void log(Logger log, String query, Object[] args) {
        log(null, log, query, args);
    }

    public static void log(String queryName, Logger log, String query, Object[] args) {
        StringBuilder sb = new StringBuilder(queryName != null ? queryName : "query").append(": ").append(query);
        for (Object o : args) {
            int idx = sb.indexOf("?");
            String value = o.toString();
            if (o instanceof Date) {
                value = formatSqlDate((Date)o);
            } else if (o instanceof Geometry) {
                value = SimpleOraGeometryHelper.getInstance().getOracleGeometry((Geometry) o);
            }
            sb.replace(idx, idx + 1, value);
        }
        log.debug(sb.toString());
    }
    
    private static String formatSqlDate(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("YYYY-MM-dd");
        return String.format("TO_DATE('%s', 'YYYY-MM-DD')", format.format(date));
    }
}
