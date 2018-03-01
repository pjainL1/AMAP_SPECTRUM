/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.lo.console;

import com.lo.db.om.SponsorGroup;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.commons.dbutils.ResultSetHandler;

/**
 *
 * @author slajoie
 */
public class SingleSponsorHandler implements ResultSetHandler<SponsorGroup> {

    @Override
    public SponsorGroup handle(ResultSet rs) throws SQLException {
        SponsorGroup sponsor = new SponsorGroup();
        int i = 0;
        while (rs.next()) {
            String sponsorCode = rs.getString("code");
            Integer sponsorKey = rs.getInt("sponsor_key");        
            if (i++ == 0) {
                sponsor.setRollupGroupName(rs.getString("amap_rollup_group_name"));
                sponsor.setRollupGroupCode(rs.getString("amap_rollup_group_code"));
                sponsor.setWorkspace(rs.getString("workspace"));
                sponsor.setWorkspaceKey(rs.getString("workspace_key"));
                sponsor.setLogo(rs.getBlob("logo"));
            }
            sponsor.addSponsor(sponsorCode, sponsorKey, rs.getString("name"));
        }
        return sponsor;
    }
}

