package com.lo.db.proxy;

import com.korem.Proxy;
import com.lo.ContextParams;
import com.lo.config.Confs;
import com.spinn3r.log5j.Logger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * This class is the Proxy for LayerGroup functionality.
 * It contains all material related to DB manipulation of that functionality.
 * Its used by: LayerGroupSynchronizer.java, AddLayerGroup.java,
 * DeleteLayerGroup.java, GetLayerGroup.java, GetLayers.java,
 * GetSponsor.java, MoveLayerGroup.java, RenameLayerGroup.java
 * 
 * @author Charles St-Hilaire for Korem inc.
 */
public class LayerGroupProxy extends Proxy{
    private static final Logger LOGGER = Logger.getLogger();
    
    private static final String GROUP_ORDER_COLUMN = "GROUP_ORDER";
    private static final String LAYER_NAME_COLUMN = "LAYER_NAME";
    private static final String LAYER_ORDER_COLUMN = "LAYER_ORDER";
    private static final String GROUP_NAME_COLUMN = "GROUP_NAME";
    private static final String IS_GROUP_OTHER_COLUMN = "IS_GROUP_OTHER";
    private static final String SPONSOR_COLUMN = "AMAP_ROLLUP_GROUP_CODE";
    private static final String ID_COLUMN = "ID";
    
    public LayerGroupProxy() throws SQLException {
        super();
    }
    
    public void flushLayer(String sponsorName) throws SQLException {
        PreparedStatement ps = super.prepare(Confs.QUERIES.layerGroupDeleteAllLayer());
        if (ps != null){
            ps.setString(1, sponsorName);
            int qty = ps.executeUpdate();
            LOGGER.info(qty + " row(s) deleted from LAYER table.");
        }
    }
    public void mergeLayer(final List<String> kmsLayers, String sponsor) throws SQLException{
        List<String> excludes = new ArrayList();
        List<String> includes = new ArrayList();
        List<String> newLayers = new ArrayList();
        
        //Check is Layer group of type IS_OTHER exist
        PreparedStatement ps = super.prepare(Confs.QUERIES.layerGroupCheckOtherExist());
        int count = -1;
        if (ps != null){
            ps.setString(1, sponsor);
            try(ResultSet rs = ps.executeQuery()){
                while(rs.next()){
                    count = rs.getInt(1);
                }
            }
        }
        //if the group other doesn't exist : create it
        if (count == 0){
            ps = super.prepare(Confs.QUERIES.layerGroupInsertLayerGroupOther());
            if (ps != null){
                ps.setString(1, sponsor);
                ps.executeUpdate();
            }
        }
        
        //Determine wich DB layers are include or exclude of KMS layers.
        ps = super.prepare(Confs.QUERIES.layerGroupSelectAllLayer());
        if (ps != null){
            ps.setString(1, sponsor);
            try(ResultSet rs = ps.executeQuery()){
                String aLayer;
                while(rs.next()){
                    aLayer = rs.getString("NAME");
                    if (kmsLayers.contains(aLayer)){
                        includes.add(aLayer);
                    }else{
                        excludes.add(aLayer);
                    }
                }
            }
        }

        //If some layers have to be removed : remove them
        if (!excludes.isEmpty()){
            String query = Confs.QUERIES.layerGroupDeleteInLayer();
            StringBuilder sb = new StringBuilder();
            for (int i = 0, lg = excludes.size(); i < lg ; i++){
                sb.append("?,");
            }
            sb.delete(sb.length() - 1, sb.length());
            query = String.format(query, sb.toString());
            ps = super.prepare(query);
            if (ps != null){
                ps.setString(1, sponsor);
                for (int i = 0, lg = excludes.size(); i < lg ; i++){
                    ps.setString(i+2, excludes.get(i));
                }
                int qty = ps.executeUpdate();
                LOGGER.info(qty + " row(s) deleted from LAYER table.");
            }
        }

        //If some KMS layers are not in DB : find them
        if (includes.size() != kmsLayers.size()){
            for (String kmsLayer : kmsLayers){
                if (!includes.contains(kmsLayer)){
                    newLayers.add(kmsLayer);
                }
            }
            //If some new Layer have to be added : Add them
            if (!newLayers.isEmpty()){
                ps = super.prepare(Confs.QUERIES.layerGroupInsertLayer());
                if (ps != null){
                    for (String newLayer : newLayers){
                        ps.clearParameters();
                        ps.setString(1, sponsor);
                        ps.setString(2, sponsor);
                        ps.setString(3, newLayer);
                        ps.setString(4, sponsor);
                        
                        int qty=ps.executeUpdate();
                        LOGGER.info(qty+" row(s) inserted in LAYER table (sponsor: "+sponsor+", LayerName: "+newLayer+")");
                    }
                }
            }
        }
    }
    
    public void deleteLayerGroup(int groupId) throws SQLException{
        //Move layer of specified group to other 
        PreparedStatement ps = super.prepare(Confs.QUERIES.layerGroupUpdateLayerToOther());
        if (ps != null){
            ps.setInt(1, groupId);
            ps.setInt(2, groupId);
            int qty=ps.executeUpdate();
            LOGGER.info(qty+" LAYER moved on deleting group of id: "+groupId);
        }
        super.getConnection().commit();
        //Delete the group
        ps = super.prepare(Confs.QUERIES.layerGroupDeleteLayerGroupById());
        if (ps != null){
            ps.setInt(1, groupId);
            int qty=ps.executeUpdate();
            LOGGER.info(qty+" LAYER_GROUP deleted of id "+groupId);
        }
        super.getConnection().commit();
    }
    public void renameLayerGroup(int groupId, String groupName) throws SQLException{
        PreparedStatement ps = super.prepare(Confs.QUERIES.layerGroupRenameLayerGroup());
        if (ps != null){
            ps.setString(1, groupName);
            ps.setInt(2, groupId);
            int qty=ps.executeUpdate();
            LOGGER.info(qty+" LAYER_GROUP (id: "+groupId+") renamed to: \""+groupName+"\"");
        }
    }
    public void updateHierarchy(String hierarchy) throws SQLException{
        JSONArray ja = JSONArray.fromObject(hierarchy);
        PreparedStatement psLayer = super.prepare(Confs.QUERIES.layerGroupUpdateLayerOrder());
        PreparedStatement psLayerGroup = super.prepare(Confs.QUERIES.layerGroupUpdateLayerGroupOrder());
        if (psLayer != null && psLayerGroup != null){
            JSONObject jo; boolean isLayer; int groupId, oldGroupId, index; String name, sponsor;
            Iterator iter = ja.iterator();
            while (iter.hasNext()) {
                jo = (JSONObject) iter.next();
                isLayer = jo.getBoolean("isLayer");
                groupId = jo.getInt("groupId");
                oldGroupId = jo.getInt("oldGroupId");
                index = jo.getInt("index");
                name = jo.getString("name");
                sponsor = jo.getString("sponsor");
                if (isLayer){
                    psLayer.setInt(1, index);
                    psLayer.setInt(2, groupId);
                    psLayer.setInt(3, oldGroupId);
                    psLayer.setString(4, sponsor);
                    psLayer.setString(5, name);
                    psLayer.addBatch();
                }else{
                    psLayerGroup.setInt(1, index);
                    psLayerGroup.setInt(2, groupId);
                    psLayerGroup.addBatch();  
                }
            }
            psLayerGroup.executeBatch();
            int[] qty = psLayer.executeBatch();
            LOGGER.info("LAYER_GROUP / LAYER hierarchy refresh of "+qty.length+" element(s)");
            super.getConnection().commit();
        }
    }
    
    public List<LayerGroupDTO> getLayerGroups(String sponsor) throws SQLException{
        List<LayerGroupDTO> lgs = new ArrayList();
        PreparedStatement ps = super.prepare(Confs.QUERIES.layerGroupSelectAll());
        if (ps != null){
            ps.setString(1, sponsor);
            try(ResultSet rs = ps.executeQuery()){
                while(rs.next()){
                    lgs.add(new LayerGroupDTO(rs.getString(SPONSOR_COLUMN),
                                              rs.getInt(ID_COLUMN),
                                              "Y".equals(rs.getString(IS_GROUP_OTHER_COLUMN)),
                                              rs.getString(GROUP_NAME_COLUMN),
                                              rs.getLong(GROUP_ORDER_COLUMN),
                                              rs.getString(LAYER_NAME_COLUMN),
                                              rs.getInt(LAYER_ORDER_COLUMN)));
                }
            }
        }
        return lgs;
    }
    public Map<String, LayerGroupDTO> getLayerGroupsMap(String sponsor) throws SQLException{
        Map<String, LayerGroupDTO> layerGroups = new LinkedHashMap();
        PreparedStatement ps = super.prepare(Confs.QUERIES.layerGroupSelectAll());
        if (ps != null){
            ps.setString(1, sponsor);
            try(ResultSet rs = ps.executeQuery()){
                String aName;
                while(rs.next()){
                    layerGroups.put(aName = rs.getString(LAYER_NAME_COLUMN),
                                    new LayerGroupDTO(rs.getString(SPONSOR_COLUMN),
                                                      rs.getInt(ID_COLUMN),
                                                      "Y".equals(rs.getString(IS_GROUP_OTHER_COLUMN)),
                                                      rs.getString(GROUP_NAME_COLUMN),
                                                      rs.getLong(GROUP_ORDER_COLUMN),
                                                      aName,
                                                      rs.getInt(LAYER_ORDER_COLUMN)));
                }
            }
        }
        return layerGroups;
    }
    public void addLayerGroup(String sponsor, String groupName) throws SQLException{
        PreparedStatement ps = super.prepare(Confs.QUERIES.layerGroupInsertLayerGroup());
        if (ps != null){
            ps.setString(1, sponsor);
            ps.setString(2, groupName);
            ps.setString(3, "N");
            ps.setString(4, sponsor);
            ps.executeUpdate();
        }
    }
    
    /* A simple class to transport object data */
    public class LayerGroupDTO{
        private String sponsor;
        private int groupId;
        private boolean other;
        private String groupName;
        private long groupOrder;
        private String layerName;
        private int layerOrder;

        public LayerGroupDTO(String sponsor, int groupId, boolean other, String groupName, long groupOrder, String layerName, int layerOrder) {
            this.sponsor = sponsor;
            this.groupId = groupId;
            this.other = other;
            this.groupName = groupName;
            this.groupOrder = groupOrder;
            this.layerName = layerName;
            this.layerOrder = layerOrder;
        }
        public String getSponsor(){ return sponsor; }
        public int getGroupId(){ return groupId; }
        public boolean isOther(){ return other; }
        public String getGroupName(){ return groupName; }
        public long getGroupOrder(){ return groupOrder; }
        public String getLayerName(){ return layerName; }
        public int getLayerOrder(){ return layerOrder; }
    }
    
    /* A simple class to transport object data */
    public class SponsorDTO{
        private int key;
        private String name;

        public SponsorDTO(int key, String name) {
            this.key = key;
            this.name = name;
        }
        public int getKey(){ return key; }
        public String getName(){ return name; }
        
    }
}
