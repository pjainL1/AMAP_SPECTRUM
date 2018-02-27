package com.lo.web;
import com.korem.requestHelpers.GenericJSONServlet;
import com.lo.ContextParams;
import com.lo.db.dao.AirMilesDAO;
import com.lo.db.dao.LocationDAO;
import com.lo.db.dao.UserAttributesDAO;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
/**
 *
 * @author rarif
 */
@WebServlet("/UpdateUserAttributes.safe")
public class UpdateUserAttributes extends GenericJSONServlet<UpdateUserAttributes.Params>{
    protected static interface Params {
        String userAttributes();
    }
    
    @Override
    protected String getJSON(HttpServletRequest req, UpdateUserAttributes.Params params) throws Exception {
        ContextParams cp = ContextParams.get(req.getSession());

        JSONObject attributes = JSONObject.fromObject(params.userAttributes());
        
        UserAttributesDAO userDAO = new UserAttributesDAO(new AirMilesDAO());
        userDAO.updateAttributes(cp.getUser().getLogin(), attributes);
        cp.getUser().setAttributes(attributes);
        return null;
    }    
}
