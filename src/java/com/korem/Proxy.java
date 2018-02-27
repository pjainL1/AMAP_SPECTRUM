package com.korem;

import com.lo.ContextParams;
import com.lo.config.Confs;
import com.lo.db.LODataSource;
import com.lo.db.om.SponsorGroup;
import com.vividsolutions.jts.io.oracle.OraWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import oracle.jdbc.OracleConnection;
import org.apache.tomcat.dbcp.dbcp.DelegatingConnection;

/**
 *
 * @author jduchesne
 */
public abstract class Proxy implements AutoCloseable {

    private Connection connection;
    private boolean closeConnection;

    private Map<String, PreparedStatement> stmts;
    
    public Proxy() throws SQLException {
        this(LODataSource.getLoneDataSource().getConnection());
        closeConnection = true;
    }
    
    public Proxy(SponsorGroup sponsorGroup) throws SQLException {
        this(LODataSource.getDataSource(sponsorGroup).getConnection());
        closeConnection = true;
    }

    public Proxy(ContextParams cp) throws SQLException {
        this(LODataSource.getDataSource(cp).getConnection());
        closeConnection = true;
    }
    
    public Proxy(Connection connection) throws SQLException {
        this.setConnection(connection);
    }

    private void setConnection(Connection connection) throws SQLException {
        if (stmts == null) {
            stmts = new HashMap<String, PreparedStatement>();
        }
        setTimeZone(connection);
        this.connection = connection;
    }
    
    private void setTimeZone(Connection connection) throws SQLException {
        Connection dconn = ((DelegatingConnection) connection).getInnermostDelegate();
        OracleConnection oracleConnection = (OracleConnection) dconn;
        oracleConnection.setSessionTimeZone(Confs.CONFIG.dbTimezone());
    }

    protected Connection getConnection() {
        return connection;
    }

    public void close(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException sqle) {
                sqle.printStackTrace();
            }
        }
    }

    protected PreparedStatement prepare(final String query) {
        try {
            PreparedStatement stmt = stmts.get(query);
            if (stmt == null) {
                stmt = connection.prepareStatement(query);
                stmts.put(query, stmt);
            }
            return stmt;
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
        return null;
    }

    /**
     * Close all prepared statement created using prepare().
     * If the proxy was created using the no-argument version of the constructor,
     * this method will also close the database connection that is has opened.
     */
    @Override
    public void close() {
        for (PreparedStatement stmt : stmts.values()) {
            try {
                stmt.close();
            } catch (SQLException sqle) {
                sqle.printStackTrace();
            }
        }
        try {
            if (closeConnection && connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
    }
}
