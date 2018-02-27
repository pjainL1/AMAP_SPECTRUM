/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.lo.console.struts;

import com.lo.db.dao.AirMilesDAO;
import com.lo.db.dao.SponsorDAO;
import com.lo.db.om.SponsorGroup;
import com.spinn3r.log5j.Logger;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

/**
 *
 * @author slajoie
 */
public class InitMonitoringAction extends org.apache.struts.action.Action {

    private static final Logger log = Logger.getLogger();
    private static final String DATE_FORMAT = "dd/MM/yyyy";
    private final static String DEFAULT = "default";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        log.debug("InitMonitoring");

        SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);

        Calendar c1 = Calendar.getInstance();
        Date dateEnd = c1.getTime();
        c1.add(Calendar.DAY_OF_YEAR, -7);
        Date dateBegin = c1.getTime();

        SponsorDAO sdao = new SponsorDAO(new AirMilesDAO());
        List<SponsorGroup> sponsors = sdao.getSponsors();

        request.setAttribute("datepicker1", df.format(dateBegin));
        request.setAttribute("datepicker2", df.format(dateEnd));
        request.setAttribute("sponsors", sponsors);

        return mapping.findForward(DEFAULT);
    }
}
