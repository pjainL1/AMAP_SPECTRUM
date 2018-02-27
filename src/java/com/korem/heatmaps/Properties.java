package com.korem.heatmaps;

import com.lo.ContextParams;
import com.lo.db.LODataSource;
import java.awt.Color;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

/**
 *
 * @author jduchesne
 */
public class Properties {
    
    private static Properties instance = new Properties();
    public static Properties get() {
        return instance;
    }

    private static final String IMAGE_FORMAT_HTTP = "image/png";
    private static final String IMAGE_FORMAT = "png";

    private Properties() {
        try {
            Context initCtx = new InitialContext();
            Context envCtx = (Context) initCtx.lookup("java:comp/env");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getHttpImageFormat() {
        return IMAGE_FORMAT_HTTP;
    }

    public String getImageFormat() {
        return IMAGE_FORMAT;
    }

    public DataSource getDatasource(ContextParams cp) {
        return LODataSource.getDataSource(cp);
    }

    public Color[] getColors() {
        return new Color[] { new Color(153, 0, 153), Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN, new Color(255, 255, 255, 1) };
    }
}
