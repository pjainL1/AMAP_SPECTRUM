package com.lo.console.struts;

import com.lo.console.ConsoleControler;
import com.lo.db.om.SponsorGroup;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.lo.db.dao.AirMilesDAO;
import com.lo.db.dao.SponsorDAO;
import com.spinn3r.log5j.Logger;

/**
 * 
 * @author slajoie
 */
public class UpdateSponsorsAction extends org.apache.struts.action.Action {

    private static final Logger log = Logger.getLogger();
    private static final String DEFAULT = "default";

    @Override
	public ActionForward execute(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
        log.debug("UpdateSponsors");

		// String action = request.getParameter("action");

		// if ("update".equals(action)) {
		// Map params = request.getParameterMap();
		//
		// Map toUpsertKeys = new HashMap();
		// for (Iterator iter = params.entrySet().iterator(); iter.hasNext();) {
		// Map.Entry entry = (Map.Entry) iter.next();
		// String key = entry.getKey().toString();
		// String value = ((String[]) entry.getValue())[0].toString();
		// if (!"action".equals(key) && !"r".equals(key)) {
		// toUpsertKeys.put(key, value);
		// }
		// }
		// CustomerDAO cdao = new CustomerDAO(new AirMilesDAO());
		// cdao.deleteCustomer();
		// cdao.insertCustomer(toUpsertKeys);
		// }

//            Map toUpsertKeys = new HashMap();
//            for (Iterator iter = params.entrySet().iterator(); iter.hasNext();) {
//                Map.Entry entry = (Map.Entry) iter.next();
//                String key = entry.getKey().toString();
//                String value = ((String[]) entry.getValue())[0].toString();
//                if (!"action".equals(key) && !"r".equals(key)) {
//                    toUpsertKeys.put(key, value);
//                }
//            }
//            CustomerDAO cdao = new CustomerDAO(new AirMilesDAO());
//            cdao.updateCustomer(toUpsertKeys);

        SponsorDAO sdao = new SponsorDAO(new AirMilesDAO());
        List<SponsorGroup> sponsors = sdao.getSponsors();

        request.setAttribute("sponsors", sponsors);
        request.setAttribute("workspaces", ConsoleControler.getWorkSpaceList());

        return mapping.findForward(DEFAULT);
    }
}
