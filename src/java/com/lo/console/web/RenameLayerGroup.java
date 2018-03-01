package com.lo.console.web;

import com.korem.openlayers.parameters.IBaseParameters;
import com.korem.requestHelpers.GenericDBBoundJSONServlet;
import com.lo.db.proxy.LayerGroupProxy;
import com.spinn3r.log5j.Logger;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Charles St-Hilaire for Korem inc.
 */
@WebServlet("/console/RenameLayerGroup.safe")
public class RenameLayerGroup extends GenericDBBoundJSONServlet<LayerGroupProxy, RenameLayerGroup.RenameLayerGroupParams> {
    private static final Logger LOGGER = Logger.getLogger();
    protected static interface RenameLayerGroupParams extends IBaseParameters{
        int groupId();
        String groupName();
    }
    
    @Override
    protected String getJSON(HttpServletRequest request, LayerGroupProxy proxy, RenameLayerGroup.RenameLayerGroupParams params) throws Exception {
        proxy.renameLayerGroup(params.groupId(), params.groupName());
        LOGGER.info("Rename LAYER_GROUP to: \""+params.groupName()+"\"");
        return SUCCESS;
    }
    
}
