package com.lo.web;

import com.korem.requestHelpers.GenericServlet;
import com.lo.ContextParams;
import com.lo.db.dao.AirMilesDAO;
import com.lo.db.dao.LocationDAO;
import com.spinn3r.log5j.Logger;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import net.sf.json.JSONObject;

/**
 *
 * @author rarif
 */
@WebServlet("/SetColor.safe")
public class SetColor extends GenericServlet<SetColor.IParams> {
    private static final Logger LOGGER = Logger.getLogger();

    protected static interface IParams {
        String colorVal();
        String colorFlag();
        String sponsor_location_key();
    }

    @Override
    protected String getJSON(HttpServletRequest req, IParams params) throws Exception {
        ContextParams cp = ContextParams.get(req.getSession());
        JSONObject jsObj = new JSONObject();
        LocationDAO locationDAO = new LocationDAO(new AirMilesDAO());
        
        locationDAO.setColorLocation(cp, params.sponsor_location_key(), params.colorVal(), params.colorFlag());
        return null;
    }

}
