/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lo.hosting.watchdog;

import com.lo.hosting.Config;
import java.io.File;
import java.io.FileFilter;
import java.util.TimerTask;

/**
 * 
 * @author ydumais
 */
public abstract class Watchdog extends TimerTask {

    Config conf = Config.getInstance();

    File[] lookup(File dir) {
        File[] files = dir.listFiles(new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                return pathname.getName().toLowerCase().endsWith(".gz");
            }
        });
        return files;
    }

    public abstract String getName();
}
