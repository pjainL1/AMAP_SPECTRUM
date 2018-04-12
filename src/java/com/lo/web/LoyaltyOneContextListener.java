package com.lo.web;

import com.lo.db.dao.AirMilesDAO;
import com.lo.db.dao.LocationDAO;
import com.lo.util.WSClientLone;
import com.spinn3r.log5j.Logger;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.logging.Level;
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
        Boolean rowsDeleted = false;
        try {
            
            LocationDAO locationDAO = new LocationDAO(new AirMilesDAO());
            //Truncate LIM_TA_POLYGON
            rowsDeleted = locationDAO.truncateTradeAreaPolygon();
        } catch (SQLException ex) {
            java.util.logging.Logger.getLogger(LoyaltyOneContextListener.class.getName()).log(Level.SEVERE, null, ex);
        }
        log.info("LIM_TA_POLYGON TRUNCATE STATUS : " + rowsDeleted);
        log.info("LoyaltyOne Context Destroyed");
    }
    
   
}
