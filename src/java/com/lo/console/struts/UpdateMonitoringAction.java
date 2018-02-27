package com.lo.console.struts;

import com.lo.db.dao.AirMilesDAO;
import com.lo.db.dao.LoggingDAO;
import com.lo.db.om.Log;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.spinn3r.log5j.Logger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

/**
 *
 * @author slajoie
 */
public class UpdateMonitoringAction extends Action {
    
    private static final Logger log = Logger.getLogger();
    private static final String DATE_FORMAT = "dd/MM/yyyy";
    private final static String DEFAULT = "default";
    
    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        log.debug("UpdateMonitoring");

        String action = request.getParameter("action");
        SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);

        Date dateBegin = df.parse(request.getParameter("datepicker1"));
        Date dateEnd = df.parse(request.getParameter("datepicker2"));
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dateEnd);
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        dateEnd = calendar.getTime();
        String sponsorId = request.getParameter("sponsorId");
        
        List<String> sponsorNames = new ArrayList<String>();
        if (!"".equals(sponsorId)) {
            sponsorNames.add(sponsorId);
        } 

        LoggingDAO ldao = new LoggingDAO(new AirMilesDAO());
        List<Log> logs = ldao.getLogs(dateBegin, dateEnd, sponsorNames);

        request.setAttribute("logs", logs);

        return mapping.findForward(DEFAULT);
    }
}
