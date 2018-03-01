/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lo.hosting.web;

import java.io.File;
import java.io.FileFilter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import com.lo.hosting.Config;
import com.lo.hosting.watchdog.CleanupWatchdog;
import com.lo.hosting.watchdog.InboxWatchdog;
import com.lo.hosting.watchdog.Scheduler;
import com.lo.hosting.watchdog.Watchdog;
import com.spinn3r.log5j.Logger;

/**
 * 
 * @author YDumais
 */
public class ScheduleWatchdogs extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

    private static final Logger log = Logger.getLogger();

    @Override
    public void init() throws ServletException {
        super.init();
    }

	Config conf = Config.getInstance();

	File[] lookup(File dir) {
		File[] files = dir.listFiles(new FileFilter() {

    @Override
			public boolean accept(File pathname) {
				return pathname.getName().toLowerCase().endsWith(".txt");
			}
		});
		return files;
	}

	@Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
		if (com.lo.Config.getInstance().isLoadFile()) {
            schedule(new InboxWatchdog(), config.getInitParameter("loaderHour"));
			schedule(new CleanupWatchdog(),
					config.getInitParameter("cleanerHour"));
        } else {
            log.info("This instance of the application is not responsible for loading extract files.");
        }
    }

    private void schedule(Watchdog watchdog, String hour) {
        try {
            log.info("Schedule " + watchdog.toString());
            Scheduler scheduler = new Scheduler(watchdog);
			// update change the minute from 0 to 15 (fix rolling log file
			// issue)
            scheduler.schedule(Integer.valueOf(hour), 0);
		} catch (NumberFormatException nfe) {
            log.error("An error occured scheduling a watchdog.");
        }
    }
}
