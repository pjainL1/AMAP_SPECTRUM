package com.lo.hosting;

import com.korem.Proxy;
import com.lo.config.Confs;
import com.lo.db.LODataSource;
import com.spinn3r.log5j.Logger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author maitounejjar
 */
public class LoadingProxy extends Proxy {
    private static final Logger log = Logger.getLogger();
    

    public LoadingProxy(String databaseName) throws SQLException {
        super(LODataSource.getDataSource(databaseName).getConnection());
    }
    
    public long getCount( String tableName ) throws SQLException {
        
        String query = String.format(Confs.QUERIES.loadingCount(), tableName );
        log.debug(String.format("Running data loading count query: %s", query));
        PreparedStatement stmt = prepare( query );
                
        try( ResultSet rs = stmt.executeQuery() ) {
            rs.next();
            log.debug(String.format("Counted %s rows.", rs.getLong(1)));
            return rs.getLong(1);
        } 
        
    }
    
}
