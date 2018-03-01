package com.lo.db.dao;


import com.lo.config.Confs;
import com.spinn3r.log5j.Logger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;

/**
 *
 * @author rarif
 */
public class UserAttributesDAO {

    private static final Logger log = Logger.getLogger();
    private String query;
    private final AirMilesDAO dao;

    public UserAttributesDAO(AirMilesDAO dao) {
        this.query = Confs.QUERIES.locationsDefaultQuery();
        this.dao = dao;
    }

    public Map<String, String> getAttributes(String userId) throws SQLException {
        Map<String, String> result = new HashMap<>();
        try {
            Object[] params = new Object[]{userId};
            result = dao.getLoneRunner().query(Confs.QUERIES.userGetAttributes(), new UserHandler(), userId);
        } catch (SQLException ex) {
            log.error("Error retreiving user attributes.", ex);
        }
        return result;
    }

    public void updateAttributes(String userId, Map<String, String> attributes)  {
        try {
            QueryRunner loneRunner = dao.getLoneRunner();
            Object[] params = new Object[]{userId};
            loneRunner.update(Confs.QUERIES.userDeleteAttributes(), userId);
            for (Map.Entry<String, String> entrySet : attributes.entrySet()) {
                    String key = entrySet.getKey();
                    String value = entrySet.getValue();    
                loneRunner.update(Confs.QUERIES.userInsertAttributes(), userId, key, value);
            }

        } catch (SQLException ex) {
            log.error("Error updating user attributes.", ex);
        }   
    } 
    
    
    private class UserHandler implements ResultSetHandler<Map<String, String>> {

        @Override
        public Map<String, String> handle(ResultSet rs) throws SQLException {
            Map<String, String> result = new HashMap<>();
            while (rs.next()) {
                String name = rs.getString("NAME");
                String value = rs.getString("VALUE");
                result.put(name, value);
            }
            return result;
        }
    }    
}
