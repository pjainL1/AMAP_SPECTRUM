/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lo.hosting.watchdog;

import java.io.File;

import com.lo.hosting.Config.Directory;
import com.spinn3r.log5j.Logger;

/**
 * 
 * @author YDumais
 */
public class CleanupWatchdog extends Watchdog {

    private static final Logger log = Logger.getLogger();
	// private static final BigInteger MAX_AGE_MS = BigInteger.valueOf(30 * 24*
	// 60 * 60 * 1000); 2592000000 = 30 * 24* 60 * 60 * 1000
	private static final String STR_MAX_AGE_MS = "2592000000";

    @Override
    public void run() {
        long now = System.currentTimeMillis();

		File folder = conf.getDir(Directory.inbox);
            File[] files = folder.listFiles();
		if (files!= null)		
            for (File file : files) {
                if (file.isFile()) {
                    long age = now - file.lastModified();
					if (age > Long.valueOf(STR_MAX_AGE_MS)) {
						log.debug(String.format("Deleting file %s.", file));
                        if (!file.delete()) {
							log.warn(String.format("Error deleting file %s", file));
                        }
                    }
                }
            }

		folder = null;
		files = null;
		folder = conf.getDir(Directory.processed);
		files = folder.listFiles();
		if (files!= null)
			for (File file : files) {
				if (file.isFile()) {
					long age = now - file.lastModified();
					if (age > Long.valueOf(STR_MAX_AGE_MS)) {
						log.debug(String.format("Deleting file %s.", file));
						if (!file.delete()) {
							log.warn(String.format("Error deleting file %s", file));
        }
    }
				}
			}
		
		folder = null;
		files = null;
		folder = conf.getDir(Directory.logs);
		files = folder.listFiles();
		if (files!= null)
			for (File file : files) {
				if (file.isFile()) {
					long age = now - file.lastModified();
					if (age > Long.valueOf(STR_MAX_AGE_MS)) {
						log.debug(String.format("Deleting file %s.", file));
						if (!file.delete()) {
							log.warn(String.format("Error deleting file %s", file));
						}
					}
				}
			}
	}

    @Override
    public String getName() {
        return "CleanupWatchdog[remove all file older than 30 days]";
    }

}
