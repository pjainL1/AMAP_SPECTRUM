/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lo.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.lo.ContextParams;
import com.lo.config.Confs;
import com.lo.util.SessionUtils;
import com.spinn3r.log5j.Logger;

/**
 *
 * @author slajoie
 */
public class ValidateSessionFilter implements Filter {

    private static final Logger log = Logger.getLogger();
    private static final String[] exceptions = new String[]{"error.do",
        "login.do", "index.jsp", "expired.do"};

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {

        HttpServletRequest hreq = (HttpServletRequest) request;
        HttpServletResponse hres = (HttpServletResponse) response;
        HttpSession session = hreq.getSession();
        ContextParams cp = ContextParams.get(session);

        String url = hreq.getRequestURI();

        if (url.endsWith(".do") || url.endsWith(".safe")
                || url.endsWith(".jsp") || url.endsWith(".jspf")) {
            boolean filter = true;
            for (String exception : exceptions) {
                if (url.endsWith(exception)) {
                    filter = false;
                    break;
                }
            }
            if (filter) {
                boolean userLogged = isUserLogged(hreq);
                if (!SessionUtils.isValid(session) && !userLogged) {
                    log.info("Session expired, redirecting to login page");
                    if (url.contains("/console/")) {
                        hres.sendRedirect("../secure/expired.do");
                    } else if (!userLogged) {
                        // hres.sendRedirect("../secure/expired.do");
                        hres.sendRedirect(getFixedUrl(hreq.getRequestURL()
                                .toString(), "/secure/expired.do"));
						// chain.doFilter(request, new
                        // SendRedirectOverloadedResponse(hreq,hres));
                    } else {
                        hres.sendRedirect("expired.do");
                    }
                    return;
                } else if (url.contains("/console/") && !cp.getUser().isAdmin()) {
                    hres.sendRedirect("error.do");
                    return;
                }
            }
        }
        /* deliver request to next filter */
        chain.doFilter(request, response);
    }

    private static String getFixedUrl(String url, String suffix) {
        if (url.contains("analytics")) {
            String tab[] = url.split("/analytics/");
            String fixUrl = tab[0] + "/analytics/" + tab[1].split("/")[0]
                    + suffix;
            if (!Confs.CONFIG.httpsEnabled()) {
                return fixUrl;
            }
            return fixUrl.replace("http://", "https://");
        }
        if (!Confs.CONFIG.httpsEnabled()) {
            return url;
        }
        return url.replace("http://", "https://");
    }

    private synchronized boolean isUserLogged(HttpServletRequest req)
            throws FileNotFoundException {
        if (req.getParameter("uid") != null && req.getParameter("role") != null
                && req.getParameter("time") != null) {
            String code = req.getParameter("sponsorcode");
            String uid = req.getParameter("uid");
            String time = req.getParameter("time");
            
            if (Confs.CONFIG.koremInternalAccessEnabled()) {
                return true;
            }
            
            String fFileName = "/Users/pjain/NetBeansNew/FToken/FToken" + uid.toLowerCase()
                    + code.toLowerCase() + time.toLowerCase();
            File f = new File(fFileName);
            if (f.exists()) {
                StringBuilder text = new StringBuilder();
                FileInputStream fi = new FileInputStream(fFileName);
                Scanner scanner = new Scanner(fi, "UTF-8");
                try {
                    while (scanner.hasNextLine()) {
                        text.append(scanner.nextLine());
                    }
                } finally {
                    scanner.close();
                    try {
                        fi.close();
                        f.delete();
                    } catch (Exception e) {
                        log.error("Cannot delete token file");
                    }
                }
                if (text.toString().trim().split("&").length > 0) {
                    String content[] = text.toString().trim().split("&");
                    try {
                        // if time between redirect higher than 5s, reject the request
                        long current = System.currentTimeMillis();
                        long sent = Long.parseLong(content[2]);
                        if ((current - sent) > 5000) {
                            return false;
                        } else {
                            return true;
                        }
                    } catch (Exception e) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public void destroy() {
    }
}
