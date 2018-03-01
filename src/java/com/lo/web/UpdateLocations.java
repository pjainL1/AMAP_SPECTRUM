/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lo.web;

import com.korem.openlayers.parameters.IWhenParameters;
import com.korem.requestHelpers.GenericServlet;
import com.lo.ContextParams;
import com.lo.db.om.SponsorGroup;
import com.lo.layer.LocationLayerUtils;
import com.lo.util.SponsorFilteringManager;
import com.spinn3r.log5j.Logger;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import net.sf.json.JSONArray;

/**
 *
 * @author ydumais
 */
public class UpdateLocations extends GenericServlet<IWhenParameters> {

    @Override
    protected String getJSON(HttpServletRequest req, IWhenParameters params) throws Exception {
        LocationLayerUtils llu = new LocationLayerUtils();
        ContextParams cp = ContextParams.get(req.getSession());

        List<String> selectedCodes = new ArrayList<>();
        JSONArray sponsorFilters = params.filters();
        if (sponsorFilters != null) {
            if (!sponsorFilters.isEmpty()) {
                for (int i = 0; i < sponsorFilters.size(); i++) {
                    selectedCodes.add((String) sponsorFilters.get(i));
                }
            }
            cp.setSelectedSponsorCodes(selectedCodes, true);
        }
        String logo = null;
        if (cp.getSponsor().getLogo().length() != 0){
            String baseUrl = com.lo.Config.getBaseUrl(req);
            logo = baseUrl + "console/getCustomerLogo.safe/"+cp.getSponsor().getRollupGroupCode();
        }
        
        llu.updateRange(params.mapInstanceKey(), params.from(), params.to(),
                cp.getSelectedSponsorCodes(),logo );
        
        return null;
    }
}
