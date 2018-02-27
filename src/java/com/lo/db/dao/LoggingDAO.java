package com.lo.db.dao;

import com.lo.console.LogHandler;
import com.lo.db.om.Log;
import com.lo.db.om.SponsorGroup;
import com.lo.db.om.User;
import com.spinn3r.log5j.Logger;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 *
 * @author ydumais
 */
public class LoggingDAO {

    private static final Logger log = Logger.getLogger();
    private static final String QUERY_INSERT = "insert into logging (datetime,user_name,sponsor_name,sponsor_key,description) values(?, ?, ?, ?, ?)";
    private static final String QUERY_SELECT = "SELECT DISTINCT l.datetime, " +
            "  l.user_name, " +
            "  c.amap_rollup_group_name sponsor, " +
            "  l.description " +
            "FROM logging l " +
            "JOIN customer c " +
            "ON c.amap_rollup_group_code = l.sponsor_name " +
            "where l.datetime >= ? and l.datetime <= ? %s " +
            "ORDER BY l.datetime DESC";
    private final AirMilesDAO dao;

    public LoggingDAO(AirMilesDAO dao) {
        this.dao = dao;
    }

    /**
     *
     * @param user
     * @param sponsor
     * @param description
     */
    public void log(User user, SponsorGroup sponsor, String description) {
        if (user != null && sponsor != null) {
        java.sql.Timestamp date = new java.sql.Timestamp(System.currentTimeMillis());
        Object[] params = new Object[]{date, user.getLogin(), sponsor.getRollupGroupCode(), sponsor.getKeys().get(0), description};
        log.debug("logging... "+Arrays.deepToString(params));
        dao.log("log", QUERY_INSERT, params);
        try {
            dao.getLoneRunner().update(QUERY_INSERT, params);
        } catch (SQLException ex) {
            dao.log("error logging user (" + user.getLogin() + ") action:" + description, ex.getMessage());
        }
    }
    }

    /**
     * Return logs from date to date. If list of sponsorKeys is empty, retreive
     * logs for all sponsors
     *
     * @param from
     * @param to
     * @param sponsorKeys
     * @return
     * @throws SQLException
     */
    public List<Log> getLogs(Date from, Date to, List<String> sponsorNames) throws SQLException {
        List<Log> result = new ArrayList<Log>();
        try {
            String inFragment = "";
            if (!sponsorNames.isEmpty()) {
                inFragment = " and l.sponsor_name " + AirMilesDAO.prepareInFragment(sponsorNames.size());
            }
            String query = String.format(QUERY_SELECT, inFragment);
            Object[] params = buildParams(from, to, sponsorNames);
            dao.log("getLogs", query, params);
            result = dao.getLoneRunner().query(query, new LogHandler(), params);
        } catch (SQLException ex) {
            log.error("Error retreiving logs.", ex);
        }
        return result;
    }

    /**
     *
     * @param from
     * @param to
     * @param sponsorNames
     * @return
     */
    private Object[] buildParams(Date from, Date to, List<String> sponsorNames) {
        List<Object> vargs = new ArrayList<Object>();
        vargs.add(new java.sql.Date(from.getTime()));
        vargs.add(new java.sql.Date(to.getTime()));
        for (String name : sponsorNames) {
            vargs.add(name);
        }
        return vargs.toArray(new Object[0]);
    }
}
