package com.lo.web;

import com.korem.openlayers.parameters.IBaseParameters;
import com.korem.requestHelpers.GenericJSONServlet;
import com.lo.ContextParams;
import com.lo.db.dao.AirMilesDAO;
import com.lo.db.dao.SponsorDAO;
import com.lo.db.om.SponsorGroup;
import com.spinn3r.log5j.Logger;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 * @author akriaa
 */
@WebServlet("/GetCustomerCodes.safe")
public class GetCustomerCodes extends GenericJSONServlet<GetCustomerCodes.Params>  {
    private static final Logger LOGGER = Logger.getLogger();

    protected static interface Params extends IBaseParameters {}
 
    @Override
    protected String getJSON(HttpServletRequest request, Params params) throws Exception {
        ContextParams cp = ContextParams.get(request.getSession());
        SponsorDAO sdao = new SponsorDAO(new AirMilesDAO());
        SponsorGroup sponsor = cp.getSponsor();
        List<JSONObject> x = sdao.getLocations( sponsor.getCodes());
        return JSONArray.fromObject(x).toString();
    }

}
