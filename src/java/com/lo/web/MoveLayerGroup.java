package com.lo.web;

import com.korem.openlayers.parameters.IBaseParameters;
import com.korem.requestHelpers.GenericDBBoundJSONServlet;
import com.lo.db.proxy.LayerGroupProxy;
import com.spinn3r.log5j.Logger;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;

/**
 * This servlet received a flat hierarhcy of group/layer build from layergroup
 * MVC component of ExtJS 4. It update the DB to reflect the tree view order.
 * @author Charles St-Hilaire
 */
@WebServlet(name = "moveLayerGroup", urlPatterns = {"/console/MoveLayerGroup.safe"})
public class MoveLayerGroup extends GenericDBBoundJSONServlet<LayerGroupProxy, MoveLayerGroup.MoveLayerGroupParams> {
    private static final Logger LOGGER = Logger.getLogger();
    
    protected static interface MoveLayerGroupParams extends IBaseParameters{
        String hierarchy();
    }
    
    @Override
    protected String getJSON(HttpServletRequest request, LayerGroupProxy proxy, MoveLayerGroup.MoveLayerGroupParams params) throws Exception {
        proxy.updateHierarchy(params.hierarchy());
        LOGGER.debug("Update LAYER / GROUP hierarchy: "+params.hierarchy());
        
        return SUCCESS;
    }
}
