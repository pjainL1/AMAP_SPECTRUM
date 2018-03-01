/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.lo.console;

import com.lo.db.om.Log;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.dbutils.ResultSetHandler;

/**
 *
 * @author slajoie
 */
public class LogHandler implements ResultSetHandler<List<Log>> {

    @Override
    public List<Log> handle(ResultSet rs) throws SQLException {
        List<Log> logs = new ArrayList<Log>();
        while (rs.next()) {
            Log log = new Log();
            log.setDatetime(rs.getTimestamp("datetime"));
            log.setDescription(rs.getString("description"));
            log.setLogin(rs.getString("user_name"));
            log.setSponsor(rs.getString("sponsor"));
            logs.add(log);
        }
        return logs;
    }
}