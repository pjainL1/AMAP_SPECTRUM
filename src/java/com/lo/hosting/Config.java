package com.lo.hosting;

import com.lo.AbstractConfig;
import com.lo.config.Confs;
import com.spinn3r.log5j.Logger;
import java.io.File;
import java.util.EnumMap;
import java.util.PropertyResourceBundle;

import javax.naming.Context;

import com.lo.AbstractConfig;
import com.spinn3r.log5j.Logger;

public class Config extends AbstractConfig {

    private static final Logger log = Logger.getLogger();
    private static final String KEY_MAIL_HOST = "emailHost";
    private static final String KEY_MAIL_USER = "emailUser";
    private static final String KEY_MAIL_PASS = "emailPass";
    private static final String KEY_MAIL_PORT = "emailPort";
    private static Config instance = new Config("com.lo.hosting.config");
    private EnumMap<Directory, File> directories;
    private Context ctx;
    private PropertyResourceBundle prb;
    private String mailHost;
    private String mailPort;
    private String mailUser;
    private String mailPass;
    private static final String DEFAULT_MAIL_PORT = "25";

    private Config(String path) {
        super(path);
    }

    public static Config getInstance() {
        return instance;
    }

    public enum Directory {

        inbox, error, logs, processed;

        public String key() {
            return "dir." + this;
        }
    };

    @Override
    protected void init(PropertyResourceBundle prb, Context context) {
        this.prb = prb;
        this.ctx= context;
        loadDirConfig();
        setMailInfo(context);
    }

    public String getValue(String key) {
        return prb.getString(key);
    }

    public File getDir(Directory dir) {
        File file = directories.get(dir);
        if (file == null) {
            log.error("Directory %s not found.", dir);
        }
        
        return file;
    }

    public void loadDirConfig() {
        directories = new EnumMap<Directory, File>(Directory.class);
        File home = new File( Confs.CONFIG.loadingDirHome() );
        if (home.isDirectory()) {
            for (Directory dir : Directory.values()) {
                File aDir = new File(home, prb.getString(dir.key()));
                if (aDir.isDirectory()) {
                    directories.put(dir, aDir);
                } else {
                    error(aDir);
                }
            }
        } else {
            error(home);
        }
    }

    private void error(File home) {
        log.fatal(String.format("home dir %s not found.", home.getAbsolutePath()));
    }

    private void setMailInfo(Context context) {
        mailHost = getContextString(context, KEY_MAIL_HOST);
        mailPort = getContextString(context, KEY_MAIL_PORT);
        if (mailPort == null) {
            mailPort = DEFAULT_MAIL_PORT;
        }
        mailUser = getContextString(context, KEY_MAIL_USER);
        mailPass = getContextString(context, KEY_MAIL_PASS);
    }

    public String getMailHost() {
        return mailHost;
    }

    public String getMailPass() {
        return mailPass;
    }

    public String getMailPort() {
        return mailPort;
    }

    public String getMailUser() {
        return mailUser;
    }
}
