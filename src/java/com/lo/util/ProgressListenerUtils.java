/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lo.util;

import com.lo.pdf.PDFProcessProgressListenerKey;
import com.lo.report.ReportMethod;
import com.lo.web.Apply.IProgressAware;
import com.lo.web.Apply.ProgressListener;
import com.spinn3r.log5j.Logger;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author ydumais
 */
public class ProgressListenerUtils {

    public static final String REPORT_PREFIX = "REPORT_";
    private static final Logger log = Logger.getLogger();
    
    private static long mapId = 0;
    private static final Map<Long, ProgressListener> instances = new HashMap<Long, ProgressListener>();

//    public static ProgressListener get(HttpServletRequest req) {
//        return get(req, "");
//    }

//    public static void set(HttpServletRequest req, ProgressListener listener) {
//        set(req, "", listener);
//    }

    public static ProgressListener get(HttpServletRequest req, String prefix) {
        return (ProgressListener) req.getSession().getAttribute(getName(prefix));
    }

    public static ProgressListener remove(long id) {
        return instances.remove(id);
    }
    
    public static ProgressListener get(long id) {
        return instances.get(id);
    }
    
//    public static ProgressListener get(HttpServletRequest req, IProgressAware[] methodsArray) {
//        return (ProgressListener) req.getSession().getAttribute(getName(methodsArray));
//    }

    /**
     * 
     * @param req
     * @param key 
     */
    public static void set(HttpServletRequest req, PDFProcessProgressListenerKey key) {
        ProgressListener listener = instances.get(key.getId());
        req.getSession().setAttribute(key.getName(), listener);
    }
    
    /**
     * 
     * @param req
     * @param listener
     * @param methodsArray
     * @return 
     */
    public static PDFProcessProgressListenerKey set(HttpServletRequest req, ProgressListener listener, IProgressAware[] methodsArray) {
        long id = mapId++;
        instances.put(id, listener);
        String name = getName(methodsArray);
        req.getSession().setAttribute(name, listener);
        return new PDFProcessProgressListenerKey(id, name);
    }

//    public static void set(HttpServletRequest req, String prefix, ProgressListener listener) {
//        req.getSession().setAttribute(getName(prefix), listener);
//    }

    public static String getName(IProgressAware[] methodsArray) {
        String prefix = "";
        for (IProgressAware method : methodsArray) {
            if (method instanceof ReportMethod) {
                log.debug("Returning report specific progress object.");
                prefix = REPORT_PREFIX;
            }
        }
        return prefix + ProgressListener.class.getSimpleName();
    }

    private static String getName(String prefix) {
        return prefix + ProgressListener.class.getSimpleName();
    }

    public static ProgressListener getWithMethodName(HttpServletRequest req, String method) {
        String prefix = "";
        if(method != null && method.contains("report")){
            prefix = REPORT_PREFIX;
        }
        return get(req, prefix);
    }
}
