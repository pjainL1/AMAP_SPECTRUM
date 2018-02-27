package com.korem.servlets;

import com.lo.ContextParams;
import com.lo.util.LoggingUtil;
import javax.servlet.http.*;
import org.apache.commons.logging.*;

/**
 * HTTP session lifecycle listener. Used by the events logger.
 * @author ydumais
 */
public class WebSessionListener implements HttpSessionListener {

    private static final Log log = LogFactory.getLog(WebSessionListener.class);

    @Override
    public void sessionCreated(HttpSessionEvent se) {
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        HttpSession session = se.getSession();
        ContextParams cp = ContextParams.get(session);
        LoggingUtil.log(cp.getUser(), cp.getSponsor(), LoggingUtil.getSessionExpiredMessage());
        log.debug("Destroying session: " + session.getId());
    }
}
