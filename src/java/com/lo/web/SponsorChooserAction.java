package com.lo.web;

import com.lo.ContextParams;
import com.lo.db.dao.AirMilesDAO;
import com.lo.db.dao.SponsorDAO;
import com.lo.db.om.SponsorGroup;
import com.lo.db.om.User;
import com.spinn3r.log5j.Logger;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.lo.ContextParams;
import com.lo.db.om.SponsorGroup;
import com.lo.db.om.User;
import com.spinn3r.log5j.Logger;

/**
 *
 * @author slajoie
 */
public class SponsorChooserAction extends org.apache.struts.action.Action {

    private static final Logger log = Logger.getLogger();
    private static final String DEFAULT = "default";
    private static final String INIT = "init";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        log.debug("SponsorChooser");

        ContextParams cp = ContextParams.get(request.getSession());
        log.debug("CP");
        log.debug(cp.getClass().getName());
        log.debug(request.getHeaderNames());
        List<SponsorGroup> sponsors = null;

        User user = cp.getUser();
        log.debug("CP2");
        log.debug(user.getClass().getName());

        if (user.getSponsors() != null && !user.getSponsors().isEmpty()) {
            sponsors = user.getSponsors();
            cp.setSponsor(sponsors.get(0));
            if (user.getSponsors().size() <= 1) {
                return mapping.findForward(INIT);
            } else {
                request.setAttribute("sponsors", sponsors);
            }
        }

        return mapping.findForward(DEFAULT);
    }
}
