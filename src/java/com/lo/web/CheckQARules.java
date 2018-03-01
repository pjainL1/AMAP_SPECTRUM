package com.lo.web;

import com.korem.openlayers.parameters.IBaseParameters;
import com.korem.requestHelpers.GenericServlet;
import com.lo.ContextParams;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 * @author ydumais
 */
public class CheckQARules extends GenericServlet<IBaseParameters> {

    @Override
    protected String getJSON(HttpServletRequest req, IBaseParameters params) throws Exception {
        ContextParams cp = ContextParams.get(req.getSession());
        Set<String> rules = cp.getQualityOfDataRules();
        JSONObject jsonObject = new JSONObject();
        if (rules.size() > 0) {
            JSONArray jsonArray = new JSONArray();
            StringBuilder sb = new StringBuilder();
            for (String rule : rules) {
                jsonArray.add(rule);
                sb.append(rule).append("<br><br>");
            }
            rules.clear();
            jsonObject.put("error", true);
            jsonObject.put("qa", sb.toString());
        } else {
            jsonObject.put("error", false);
        }
        return jsonObject.toString();
    }
}
