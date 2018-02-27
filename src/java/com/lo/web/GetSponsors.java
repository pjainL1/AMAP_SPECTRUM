package com.lo.web;

import com.korem.openlayers.parameters.IBaseParameters;
import com.korem.requestHelpers.GenericDBBoundJSONServlet;
import com.lo.db.dao.AirMilesDAO;
import com.lo.db.dao.SponsorDAO;
import com.lo.db.om.SponsorGroup;
import com.lo.db.proxy.LayerGroupProxy;
import com.spinn3r.log5j.Logger;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import net.sf.json.util.JSONBuilder;
import net.sf.json.util.JSONStringer;

/**
 * This class is to retrive sponsor for Group Layer
 * @author Charles St-Hilaire for Korem inc.
 */
@WebServlet("/GetSponsors.safe")
public class GetSponsors extends GenericDBBoundJSONServlet<LayerGroupProxy, GetSponsors.SponsorParams> {
    private static final Logger LOGGER = Logger.getLogger();
    
    protected static interface SponsorParams extends IBaseParameters {}

    @Override
    protected String getJSON(HttpServletRequest request, LayerGroupProxy proxy, SponsorParams params) throws Exception {
        SponsorDAO sdao = new SponsorDAO(new AirMilesDAO());
        List<SponsorGroup> sponsors = sdao.getSponsors();
        JSONBuilder jb = new JSONStringer().array();
        for (SponsorGroup sponsor : sponsors) {
            jb.object().
                    key("id").value(sponsor.getRollupGroupCode()).
                    key("name").value(sponsor.getRollupGroupName()).
                    endObject();
        }
        return jb.endArray().toString();
    }
    
}
