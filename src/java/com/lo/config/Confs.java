
package com.lo.config;

import com.korem.config.ConfigManager;

/**
 *
 * @author jphoude
 */
public class Confs {
    private static final Confs INSTANCE = new Confs();
    
    public static final Config CONFIG = ConfigManager.get().getConfig("Config", Config.class);
    public static final StaticConfig STATIC_CONFIG = ConfigManager.get().getConfig("StaticConfig", StaticConfig.class);
    public static final MapStyles MAP_STYLES = ConfigManager.get().getConfig("MapStyles", MapStyles.class);
    public static final Hotspot HOTSPOT_CONFIG = ConfigManager.get().getConfig("Hotspot", Hotspot.class);
    
    public static final Queries QUERIES = ConfigManager.get().getConfig("Queries", Queries.class);
    public static final ConsoleQueries CONSOLE_QUERIES = ConfigManager.get().getConfig("ConsoleQueries", ConsoleQueries.class);
    public static final ConsoleConfig CONSOLE_CONFIG = ConfigManager.get().getConfig("ConsoleConfig", ConsoleConfig.class);
    
    public static final Build BUILD = ConfigManager.get().getConfig("Build", Build.class);
    
    private final long startupTime;
    
    private Confs() {
        startupTime = System.currentTimeMillis();
    }
    
    public static final Confs get() {
        return INSTANCE;
    }
    
    public String getBuildId() {
        return String.format("%s.%sb%sr%s-%s", 
                BUILD.version(), BUILD.minor(), BUILD.build(), BUILD.svnRevision(), startupTime);
    }
}
