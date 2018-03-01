package com.lo.web;

import com.korem.openlayers.parameters.IBaseParameters;
import com.korem.requestHelpers.GenericJSONServlet;
import com.lo.ContextParams;
import com.lo.db.om.SponsorGroup;
import com.spinn3r.log5j.Logger;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import net.sf.json.JSONArray;

/**
 * This class is to retrieve sponsor codes
 *
 * @author rafik.
 */
@WebServlet("/GetSponsorCodes.safe")
public class GetSponsorCodes extends GenericJSONServlet<GetSponsorCodes.Params> {

    private static final Logger LOGGER = Logger.getLogger();

    protected static interface Params extends IBaseParameters {}

    @Override
    protected String getJSON(HttpServletRequest request, Params params) throws Exception {
        SponsorGroup sponsor = ContextParams.get(request.getSession()).getSponsor();
        return JSONArray.fromObject(sponsor.getSponsors()).toString();
    }
}
