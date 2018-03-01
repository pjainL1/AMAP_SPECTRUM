package com.lo.ldap;

import java.util.ArrayList;
import java.util.List;

import com.lo.Config;
import com.lo.db.om.Role;
import com.spinn3r.log5j.Logger;

/**
 *
 * @author mdube
 */
public class AMAPExternalLDAP extends LDAPCredentials {

    private static final Logger log = Logger.getLogger(AMAPExternalLDAP.class);
    private static final String PROPERTIES_FILE_NAME = "externalLDAP";
    private static AMAPExternalLDAP instance;
    private String LDAP_GROUP_FILTER_STR = "";
    private String LDAP_GROUP_CN_FILTER_STR = "";
    private String LDAP_AMAP_GROUP_FILTER_STR = "";
    private String LDAP_SPONSOR_GROUP_FILTER_STR = "";

    protected AMAPExternalLDAP() {
    }

    /**
     * Singleton
     *
     * @return
     */
    public static AMAPExternalLDAP getInstance() {
        if (instance == null) {
            instance = new AMAPExternalLDAP();
            instance.init(PROPERTIES_FILE_NAME);
        }
        return instance;
    }

    // Load the properties bundle
    protected void init(String propFile) {
        super.init(propFile);
        LDAP_GROUP_FILTER_STR = lProps.getString("LDAP_GROUP_FILTER_STR");
        LDAP_GROUP_CN_FILTER_STR = lProps.getString("LDAP_GROUP_CN_FILTER_STR");
        LDAP_AMAP_GROUP_FILTER_STR = lProps
                .getString("LDAP_AMAP_GROUP_FILTER_STR");
        LDAP_SPONSOR_GROUP_FILTER_STR = lProps
                .getString("LDAP_SPONSOR_GROUP_FILTER_STR");
    }

    /**
     *
     * @param uid
     * @return
     */
    public AMAPUser getAMAPUser(String uid) throws LDAPException {
        String[] attrs = getUserSID(uid);
        if (attrs[0] != null) {
            return new AMAPUser(uid, attrs[0], attrs[1],
                    AMAPUser.Domain.External);
        }
        return null;
    }

    /**
     *
     * @return
     */
    private String buildAMAPGroupCNFilter() {
        String groupCN = "";
        String env = Config.getInstance().getAmap_env();
        for (Role r : Role.values()) {
            if (env.equals("prd")) {
                groupCN = groupCN
                        + LDAP_GROUP_CN_FILTER_STR.replaceAll("#GROUP_CN#",
                                AMAP_COMMON_GROUP_PREFIX + r.toString());
            } else if (env.equals("uat")) {
                groupCN = groupCN
                        + LDAP_GROUP_CN_FILTER_STR.replaceAll("#GROUP_CN#",
                                AMAP_COMMON_GROUP_PREFIX + r.toString() + "*");
            } else if (env.equals("dev")) {
                groupCN = groupCN
                        + LDAP_GROUP_CN_FILTER_STR.replaceAll("#GROUP_CN#",
                                AMAP_COMMON_GROUP_PREFIX + r.toString() + "*");
            } else if (env.equals("tst")) {
                groupCN = groupCN
                        + LDAP_GROUP_CN_FILTER_STR.replaceAll("#GROUP_CN#",
                                AMAP_COMMON_GROUP_PREFIX + r.toString() + "*");
            } else if (env.equals("sqa")) {
                groupCN = groupCN
                        + LDAP_GROUP_CN_FILTER_STR.replaceAll("#GROUP_CN#",
                                AMAP_COMMON_GROUP_PREFIX + r.toString() + "*");
            } else {
                groupCN = groupCN
                        + LDAP_GROUP_CN_FILTER_STR.replaceAll("#GROUP_CN#",
                                AMAP_COMMON_GROUP_PREFIX + r.toString() + "*");
            }
        }
        return groupCN;
    }

    private static final int AMAP_DN = 0;
    private static final int SPONSOR_DN = 1;

    /**
     *
     * @param uid
     * @return
     */
    public List<AMAPGroup> getUserAMAPGroup(String userCN) throws LDAPException {
        List<AMAPGroup> result = new ArrayList<AMAPGroup>();
        String prefixe = LDAP_AMAP_GROUP_FILTER_STR.replaceFirst(
                "#AMAP_GROUP_CN#", buildAMAPGroupCNFilter());

        List<String> groups = getUserLDAPGroup(userCN, prefixe, AMAP_DN);
        for (String g : groups) {
            String role = g.substring(AMAP_COMMON_GROUP_PREFIX.length());
            log.debug("Found sponsor [" + role + "]");
            result.add(new AMAPGroup(g));
        }
        if (!result.isEmpty()) {
            return result;
        }
        return null;
    }

    /**
     *
     * @param uid
     * @return
     */
    public List<LDAPSponsorGroup> getUserSponsorGroup(String userCN)
            throws LDAPException {
        List<LDAPSponsorGroup> result = new ArrayList<>();
        String prefixe = LDAP_SPONSOR_GROUP_FILTER_STR.replaceFirst(
                "#MSTR_PROJECT#", AMAP_COMMON_GROUP_PREFIX);
        prefixe = prefixe.replaceFirst("#AMAP_GROUP_CN#",
                buildAMAPGroupCNFilter());

        List<String> groups = getUserLDAPGroup(userCN, prefixe, SPONSOR_DN);
        for (String g : groups) {
            String sponsorName = g.substring(AMAP_COMMON_GROUP_PREFIX.length())
                    .trim();
            log.debug("Found sponsor [" + sponsorName + "]");
            result.add(new LDAPSponsorGroup(g, sponsorName));
        }
        // get the All sponsor group
        List<String> allSponGroups = getUserLDAPGroup(userCN, prefixe, AMAP_DN);
        for (String g : allSponGroups) {
            String allSponsorGroupName = g.substring(
                    AMAP_COMMON_GROUP_PREFIX.length()).trim();
            log.debug("All sponsor [" + allSponsorGroupName + "]");
            result.add(new LDAPSponsorGroup(g, allSponsorGroupName));
        }

        if (!result.isEmpty()) {
            return result;
        }
        return null;
    }
}
