/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lo.db;

import com.lo.ContextParams;
import com.lo.db.om.SponsorGroup;
import com.spinn3r.log5j.Logger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.naming.InitialContext;
import javax.sql.DataSource;

/**
 *
 * @author ydumais
 */
public class LODataSource {
    public static final String LONE_DATASOURCE = "lone";

    private static final Logger log = Logger.getLogger();
    private static Map<String, DataSource> dataSources = Collections.synchronizedMap(new HashMap<String, DataSource>());
    private static Properties props;
    private static String url;
    
    public static DataSource getLoneDataSource() {
        return getDataSource(LONE_DATASOURCE);
    }

    public static DataSource getDataSource(ContextParams cp) {
        return getDataSource(cp.getSponsor().getRollupGroupCode().toLowerCase());
    }
    
    public static DataSource getDataSource(SponsorGroup sponsor) {
        return getDataSource(sponsor.getRollupGroupCode().toLowerCase());
    }
    
    public static DataSource getDataSource(String dataSourceName) {
        DataSource dataSource = dataSources.get(dataSourceName);
        if (dataSource == null) {
            try {
                InitialContext ctx = new InitialContext();
                dataSources.put(dataSourceName, dataSource = (DataSource) ctx.lookup(String.format("java:comp/env/%s", dataSourceName)));
            } catch (Exception e) {
                log.fatal("Unable to initialize datasource", e);
            }
        }

        return dataSource;
    }
}
