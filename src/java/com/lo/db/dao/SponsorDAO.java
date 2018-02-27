/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lo.db.dao;

import com.lo.console.SingleSponsorForUpdate;
import com.lo.console.SingleSponsorHandler;
import com.lo.console.SponsorHandler;
import com.lo.db.om.SponsorGroup;
import java.sql.SQLException;
import java.util.List;
import com.spinn3r.log5j.Logger;
import java.io.InputStream;
import java.sql.ResultSet;
import java.util.ArrayList;
import net.sf.json.JSONObject;
import org.apache.commons.dbutils.ResultSetHandler;

/**
 *
 * @author slajoie
 */
public class SponsorDAO {

    private static final Logger log = Logger.getLogger();
    private static final String QUERY_SPONSOR_LIST
            = " select s.sponsor_key, s.sponsor_code code, s.sponsor_name name,"
            + " c.workspace, c.workspace_key, c.amap_rollup_group_name, c.amap_rollup_group_code, c.logo"
            + " from sponsor s"
            + " join customer c on c.amap_rollup_group_code = s.amap_rollup_group_code order by amap_rollup_group_code";
    private static final String QUERY_SPONSOR_ANALYST
            = " select s.sponsor_key, s.sponsor_code code, s.sponsor_name name,"
            + " c.workspace, c.workspace_key, c.amap_rollup_group_name, c.amap_rollup_group_code"
            + " from sponsor s"
            + " join customer c on c.sponsor_name = s.sponsor_name"
            + " join users_customer uc on uc.amap_rollup_group_code = c.amap_rollup_group_code"
            + " where uc.user_id=? order by amap_rollup_group_code";
    private static final String QUERY_SPONSOR
            = " select s.sponsor_key, s.sponsor_code code, s.sponsor_name name,"
            + " c.workspace, c.workspace_key, c.amap_rollup_group_name, c.amap_rollup_group_code, c.logo"
            + " from sponsor s"
            + " join customer c on c.amap_rollup_group_code = s.amap_rollup_group_code "
            + " where c.amap_rollup_group_code=?";
	
	private static final String QUERY_SPONSOR_LDAP
   			= " select s.sponsor_key, s.sponsor_code code, s.sponsor_name name,"
            + " c.workspace, c.workspace_key, c.amap_rollup_group_name, c.amap_rollup_group_code, c.logo"
            + " from sponsor s"
    		+ " join customer c on c.amap_rollup_group_code = s.amap_rollup_group_code "
    		+ " where upper(c.amap_rollup_group_name)=upper(?)";

    private static final String QUERY_SPONSOR_FOR_UPDATE
            = " select logo"
            + " from customer "
            + " where amap_rollup_group_code = ? "
            + " for update ";
    private static final String QUERY_DELETE_LOGO
            = "update customer set logo = empty_blob() where customer.amap_rollup_group_code = ?";
    private static final String QUERY_SELECT_LOCATIONS
            = "SELECT CUSTOMER_LOCATION_CODE, SPONSOR_LOCATION_CODE, LONGITUDE, LATITUDE from SPONSOR_LOCATION WHERE SPONSOR_CODE ";



    private AirMilesDAO dao;

    public SponsorDAO(AirMilesDAO dao) {
        this.dao = dao;
    }

    public List<SponsorGroup> getSponsors() throws SQLException {
        List<SponsorGroup> result = new ArrayList<SponsorGroup>();
        try {
            dao.log("getSponsors", QUERY_SPONSOR_LIST);
            result = dao.getLoneRunner().query(QUERY_SPONSOR_LIST, new SponsorHandler());
        } catch (SQLException ex) {
            log.error("Error retreiving sponsors.", ex);
        }
        return result;
    }

    public List<SponsorGroup> getSponsors(Integer userId) throws SQLException {
        List<SponsorGroup> result = new ArrayList<>();
        try {
            Object[] params = new Object[]{userId};
            dao.log("getAnalystSponsors", QUERY_SPONSOR_ANALYST);
            result = dao.getLoneRunner().query(QUERY_SPONSOR_ANALYST, new SponsorHandler(), params);
        } catch (SQLException ex) {
            log.error("Error retreiving analyst sponsors.", ex);
        }
        return result;
    }

    public SponsorGroup getSponsor(String groupCode) throws SQLException {
        Object[] params = new Object[]{groupCode};
        dao.log("login", QUERY_SPONSOR, params);
        SponsorGroup group = dao.getLoneRunner().query(QUERY_SPONSOR, new SingleSponsorHandler(), params);
        if (group.getRollupGroupCode() != null) {
            return group;
        }
        
        return null;
    }

    public SponsorGroup getSponsorByName(String groupName) throws SQLException {
    	Object[] params = new Object[]{groupName};
    	dao.log("login", QUERY_SPONSOR, params);
    	SponsorGroup group = dao.getLoneRunner().query(QUERY_SPONSOR_LDAP, new SingleSponsorHandler(), params);
    	if (group.getRollupGroupCode() != null) {
    		return group;
    	}
    	
    	return null;
    }
    
    public SponsorGroup getSponsorByCode(String sponsorCode) throws SQLException {
        return getSponsor(sponsorCode);
//        Object[] params = new Object[]{sponsorCode};
//        dao.log("login", QUERY_SPONSOR_CODE, params);
//        return dao.getRunner().query(QUERY_SPONSOR_CODE, new SingleSponsorHandler(), params);
    }

//       public Integer getSponsorKey(Double locationKey) throws SQLException {
//        ResultSetHandler<SponsorGroup> handler = new BeanHandler(SponsorGroup.class);
//        Object[] params = new Object[]{locationKey};
//        dao.log("getSponsorKey", QUERY_SPONSOR_KEY);
//        SponsorGroup sponsor = dao.getRunner().query(QUERY_SPONSOR_KEY, handler, params);
//        return Integer.parseInt(sponsor.getName()); // in this special case we put sponsor_key in the name fiel
//    }
    public void setCustomerLogo(String groupCode, InputStream logo) {
        try {
            Object[] params = new Object[]{groupCode};
            dao.getLoneRunner().query(QUERY_SPONSOR_FOR_UPDATE,params ,new SingleSponsorForUpdate(logo));
            dao.getLoneRunner().getDataSource().getConnection().commit();
        } catch (SQLException ex) {
            log.error("Error setting customer logo", ex);
        }
    }

    public void deleteCustomerLogo(String groupCode) throws Exception {
        dao.getLoneRunner().update(QUERY_DELETE_LOGO, groupCode);
    }

    public List<JSONObject> getLocations(List<String> codes) throws SQLException {
        String inFragment = AirMilesDAO.prepareInFragment(codes.size());
   
        
        ResultSetHandler<List<JSONObject>> handler = new ResultSetHandler<List<JSONObject>>() {
            @Override
            public List<JSONObject> handle(ResultSet rs) throws SQLException {
            
                List<JSONObject> list = new ArrayList<>();
                while (rs.next()) {
                    JSONObject result = new JSONObject();
                    String clc = rs.getString("CUSTOMER_LOCATION_CODE");
                    String slc = rs.getString("SPONSOR_LOCATION_CODE");
                    Double lon = rs.getDouble("LONGITUDE");
                    Double lat = rs.getDouble("LATITUDE");
                    result.accumulate("CUSTOMER_LOCATION_CODE", clc).accumulate("SPONSOR_LOCATION_CODE", slc).accumulate("LONGITUDE", lon).accumulate("LATITUDE", lat);
                    list.add(result);
                }

                return list;
            }
        };
        return dao.getLoneRunner().query(QUERY_SELECT_LOCATIONS + inFragment, handler, codes.toArray());
    }
    
    
}
