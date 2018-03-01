/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lo.hosting.watchdog;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;

import com.spinn3r.log5j.Logger;

/**
 * 
 * @author ydumais
 */
public class Scheduler {

    private static final Logger log = Logger.getLogger();
    private static final long DAY = 1000 * 60 * 60 * 24;
    private final Watchdog watchdog;

    public Scheduler(Watchdog watchdog) {
        this.watchdog = watchdog;
    }

    public void schedule(int hour, int min) {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(watchdog, getFirstTime(hour, min), DAY);
    }

    private Date getFirstTime(int hour, int min) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) + 1);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, min);
        Date start = cal.getTime();
		log.debug(String.format("TimerTask %s set to run at %s.",
				this.watchdog.getName(), start));
        return start;
    }
}
