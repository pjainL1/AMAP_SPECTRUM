package com.lo.web;

import com.lo.util.WSClientLone;
import com.spinn3r.log5j.Logger;
import java.rmi.RemoteException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * A simple ServletContextListener to patch a Legend problem.
 * @author Charles St-Hilaire for Korem inc.
 */
@WebListener
public class LoyaltyOneContextListener implements ServletContextListener{
    private static final Logger log = Logger.getLogger();
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        log.info("LoyaltyOne Context Initialized");
//        try{WSClientLone.getMappingSessionService().getSessionId();}
//        catch(RemoteException re){log.error(re.getMessage());}
    }
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        log.info("LoyaltyOne Context Destroyed");
    }
}
