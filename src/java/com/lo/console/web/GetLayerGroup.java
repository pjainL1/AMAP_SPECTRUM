package com.lo.console.web;

import com.korem.openlayers.kms.Layer;
import com.korem.openlayers.parameters.IBaseParameters;
import com.korem.requestHelpers.GenericDBBoundJSONServlet;
import com.lo.ContextParams;
import com.lo.db.helper.LayerGroupSynchronizer;
import com.lo.db.proxy.LayerGroupProxy;
import com.lo.db.proxy.LayerGroupProxy.LayerGroupDTO;
import com.lo.console.web.GetLayerGroup.LayerGroupParams;
import com.lo.web.GetMapInstanceKey;
import com.lo.web.GetOpenLayers;
import com.spinn3r.log5j.Logger;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import net.sf.json.util.JSONBuilder;
import net.sf.json.util.JSONStringer;

/**
 * This Servlet get the LayerGroup hierachy JSON formated for ExtJs 4 Treepanel.
 * @author Charles St-Hilaire for Korem inc.
 */
@WebServlet("/console/GetLayerGroup.safe")
public class GetLayerGroup extends GenericDBBoundJSONServlet<LayerGroupProxy, LayerGroupParams> {
    private static final Logger LOGGER = Logger.getLogger();
    
    protected static interface LayerGroupParams extends IBaseParameters{
        String sponsor();
    }

    @Override
    protected String getJSON(HttpServletRequest request, LayerGroupProxy proxy, LayerGroupParams params) throws Exception {
        
       // HttpSession session = request.getSession();
 
        
        
        if (params.sponsor() == null) {
            LOGGER.warn("GetLayerGroup.safe called with no sponsor parameter.");
            return FAILURE;
        }
        
        try {
            String mapInstanceKey = GetMapInstanceKey.getMapInstanceKey(params.sponsor(), request);
            LayerGroupSynchronizer.getInstance().doSynchronize(GetOpenLayers.getMapProvider(), mapInstanceKey, params.sponsor(), proxy, request);
        } catch (Exception e) {
            LOGGER.error("Failed to synchronize layers", e);
        }
        
        List<LayerGroupDTO> lgs = proxy.getLayerGroups(params.sponsor());
        Set<Integer> groupDone = new HashSet();
        long id = System.currentTimeMillis();
        JSONBuilder jb = new JSONStringer().object();
        jb.key("children").array();
        for (LayerGroupDTO lg : lgs){
            if (!groupDone.contains(lg.getGroupId())){
                if (groupDone.size() > 0) {
                    endGroup(jb);
                }
                groupDone.add(lg.getGroupId());
                beginGroup(jb, lg, id++);
            }
            if (lg.getLayerName() != null) {
                appendLayer(jb, lg, id++);
            }
        }
        if (groupDone.size() > 0){
            endGroup(jb);
        }
        jb.endArray().endObject();
        String result = jb.toString();
        LOGGER.debug("Retrieved GROUP / LAYERS : "+result);
        return result;
    }
    private void appendLayer(JSONBuilder jb, LayerGroupDTO lg, long id){
        jb.object().key("id").value(id).
                    key("leaf").value(true).
                    key("expanded").value(false).
                    key("groupId").value(lg.getGroupId()).
                    key("sponsor").value(lg.getSponsor()).
                    key("text").value(trimName(lg.getLayerName())).
                    key("realText").value(lg.getLayerName()).
                    key("index").value(lg.getLayerOrder()).
                    key("isOther").value(false).
                    endObject();
    }
    private void beginGroup(JSONBuilder jb, LayerGroupDTO lg, long id){
        jb.object().key("id").value(id).
                    key("leaf").value(false).
                    key("expanded").value(true).
                    key("groupId").value(lg.getGroupId()).
                    key("sponsor").value(lg.getSponsor()).
                    key("text").value(lg.getGroupName()).
                    key("realText").value(lg.getGroupName()).
                    key("index").value(lg.getGroupOrder()).
                    key("isOther").value(lg.isOther());
        jb.key("children").array();
    }
    private void endGroup(JSONBuilder jb) {
        jb.endArray().endObject();
    }
    private String trimName(String name) {
        if (name != null){
            return (name.endsWith(Layer.ACCEPTED_SUFFIX[0]) ? name.substring(0, name.indexOf(Layer.ACCEPTED_SUFFIX[0])) : (name.endsWith(Layer.ACCEPTED_SUFFIX[1]) ? name.substring(0, name.indexOf(Layer.ACCEPTED_SUFFIX[1])) : name)).replaceAll("_"," ").replaceAll("Ranges by", "").replaceAll("Individual values with", "").trim();
        }
        return "";
    }
}
