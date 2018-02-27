package com.lo;

import com.spinn3r.log5j.Logger;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 *
 * @author jduchesne
 */
public abstract class AbstractConfig {

    private static Context context;

    protected static Logger log = Logger.getLogger();
    
    protected AbstractConfig(String configPath) {
        init((configPath != null) ? (PropertyResourceBundle) ResourceBundle.getBundle(configPath) : null,
                getContext());
    }

    protected abstract void init(PropertyResourceBundle prb, Context context);

    private static Context getContext() {
        if (context == null) {
            try {
                context = (Context)new InitialContext().lookup("java:comp/env");
            } catch (NamingException ex) {
                log.fatal(null, ex);
            }
        }
        return context;
    }

    protected String getContextString(Context context, String key) {
        try {
            return (String)context.lookup(key);
        } catch (Exception e) {
            log.debug(e);
            return null;
        }
    }

    protected Double getContextDouble(Context context, String key) {
        try {
            return (Double)context.lookup(key);
        } catch (Exception e) {
            log.debug(e);
            return null;
        }
    }
    
	public String getValue(String key,ResourceBundle prb) {
		String value = null;
		value = prb.getString(key);
		return value;
	}
}
