package com.lo.util;

import com.lo.ContextParams;
import com.lo.db.LODataSource;
import com.lo.db.om.SponsorGroup;
import com.spinn3r.log5j.Logger;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import net.sf.json.util.JSONBuilder;
import net.sf.json.util.JSONStringer;
import org.apache.commons.dbutils.DbUtils;

/**
 *
 * @author ydumais
 */
public class InitialDatePickers {

    private static final Logger log = Logger.getLogger();
    private SimpleDateFormat dbDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
    private Date from;
    private Date to;
    private Date max;
    private Date min;

    public InitialDatePickers(SponsorGroup sponsor) {
        init(sponsor);
        adjust();
    }

    public String getInitialDates() throws Exception {
        JSONBuilder json = new JSONStringer().object().
                key("minDate").value(format(from)).
                key("maxDate").value(format(to));
        addDateLimits(json);
        return json.endObject().toString();
    }

    public Date getInitialFromDate() {
        return from;
    }

    public Date getInitialToDate() {
        return to;
    }

    private String format(Date date) {
        return dateFormat.format(date);
    }

    private Date parse(String date) throws Exception {
        return dbDateFormat.parse(date);
    }

    private void addDateLimits(JSONBuilder json) throws Exception {
        json.key("minDateLimit").value(format(min));
        json.key("maxDateLimit").value(format(max));
    }

    private void init(SponsorGroup sponsor) {
        int dateCpt = 0;
        Connection connection = null;
        try {
            connection = LODataSource.getDataSource(sponsor).getConnection();
            Statement stmt = connection.createStatement();
            try {
                ResultSet rs = stmt.executeQuery("select name, value from properties");
                try {
                    while (rs.next()) {
                        if ("date.min".equals(rs.getString(1))) {
                            min = parse(rs.getString(2));
                            ++dateCpt;
                        } else if ("date.max".equals(rs.getString(1))) {
                            max = parse(rs.getString(2));
                            ++dateCpt;
                        }
                        if (dateCpt == 2) {
                            break;
                        }
                    }
                } finally {
                    rs.close();
                }
            } finally {
                stmt.close();
            }
        } catch (Exception e) {
            log.error(null, e);
        } finally {
            DbUtils.closeQuietly(connection);
        }
    }

    private void adjust() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(max);
        cal.add(Calendar.DAY_OF_YEAR, -14);
        to = cal.getTime();
        cal.add(Calendar.YEAR, -1);
        cal.add(Calendar.DAY_OF_YEAR, 1);
        from = cal.getTime();
    }
}
