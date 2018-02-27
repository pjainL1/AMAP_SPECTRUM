/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

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
@WebServlet("/console/DeleteLayerGroup.safe")
public class DeleteLayerGroup extends GenericDBBoundJSONServlet<LayerGroupProxy, DeleteLayerGroup.DeleteLayerGroupParams> {
    private static final Logger LOGGER = Logger.getLogger();
    protected static interface DeleteLayerGroupParams extends IBaseParameters{
        int groupId();
    }
    
    @Override
    protected String getJSON(HttpServletRequest request, LayerGroupProxy proxy, DeleteLayerGroup.DeleteLayerGroupParams params) throws Exception {
        proxy.deleteLayerGroup(params.groupId());
        
        LOGGER.info("Delete LAYER_GROUP of id "+params.groupId());
        
        return SUCCESS;
    }
}
