package com.lo.console.web;

import com.korem.openlayers.parameters.IBaseParameters;
import com.korem.requestHelpers.GenericDBBoundJSONServlet;
import com.lo.db.proxy.LayerGroupProxy;
import com.spinn3r.log5j.Logger;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author csthilaire
 */
@WebServlet("/console/AddLayerGroup.safe")
public class AddLayerGroup extends GenericDBBoundJSONServlet<LayerGroupProxy, AddLayerGroup.AddLayerGroupParams> {
    private static final Logger LOGGER = Logger.getLogger();
    protected static interface AddLayerGroupParams extends IBaseParameters {
        String sponsor();
        String groupName();
    }
    public AddLayerGroup() {
        super();
    }
    
    @Override
    protected String getJSON(HttpServletRequest request, LayerGroupProxy proxy, AddLayerGroup.AddLayerGroupParams params) throws Exception {
        proxy.addLayerGroup(params.sponsor(), params.groupName());
        LOGGER.info(params.groupName()+" LAYER_GROUP successfully added");
        return SUCCESS;
    }
}
