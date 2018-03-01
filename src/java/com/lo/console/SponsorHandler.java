package com.lo.console;

import com.lo.db.om.SponsorGroup;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.dbutils.ResultSetHandler;

/**
 *
 * @author slajoie
 */
public class SponsorHandler implements ResultSetHandler<List<SponsorGroup>> {

    @Override
    public List<SponsorGroup> handle(ResultSet rs) throws SQLException {
        List<SponsorGroup> result = new ArrayList<>();
        SponsorGroup sponsor = null;
        while (rs.next()) {
            String sponsorGroupName = rs.getString("amap_rollup_group_name");
            String sponsorGroupCode = rs.getString("amap_rollup_group_code");
            String sponsorName = rs.getString("name");
            String sponsorCode = rs.getString("code");
            Integer sponsorKey = rs.getInt("sponsor_key");
            if (sponsor == null || !sponsorGroupName.equals(sponsor.getRollupGroupName())) {
                sponsor = new SponsorGroup();
                sponsor.setRollupGroupName(sponsorGroupName);
                sponsor.setLogo(rs.getBlob("logo"));
                sponsor.setRollupGroupCode(sponsorGroupCode);
                sponsor.setWorkspace(rs.getString("workspace"));
                sponsor.setWorkspaceKey(rs.getString("workspace_key"));
                result.add(sponsor);
            }
            sponsor.addSponsor(sponsorCode, sponsorKey, sponsorName);
        }
        return result;
    }
}


