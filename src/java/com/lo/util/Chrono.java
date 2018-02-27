/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lo.util;

import com.spinn3r.log5j.Logger;

/**
 *
 * @author ydumais
 */
public class Chrono {

    private long start;
    private String action;
    private Logger log;

    public Chrono(Logger log) {
        this.log = log;
    }

    public void start(String action) {
        if (log.isDebugEnabled()) {
            this.start = System.currentTimeMillis();
            this.action = action;
        }
    }

    public void stop() {
        if (log.isDebugEnabled()) {
            long stop = System.currentTimeMillis();
            log.debug(String.format(" ++++++++ chrono time %s ms for action %s", (stop - start), action));
        }
    }
}
