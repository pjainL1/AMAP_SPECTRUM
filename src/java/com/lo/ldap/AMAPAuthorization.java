package com.lo.ldap;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.owasp.esapi.ESAPI;

import com.lo.Config;
import com.lo.config.Confs;
import com.lo.db.dao.AirMilesDAO;
import com.lo.db.dao.SponsorDAO;
import com.lo.db.om.SponsorGroup;
import com.lo.db.om.User;
import com.spinn3r.log5j.Logger;

/**
 *
 * @author mdube
 */
public class AMAPAuthorization implements Authorization {

    private static final Logger log = Logger.getLogger(AMAPInternalLDAP.class);
    private final static String UID_PARAM_KEY = "uid";
    private final static String DOMAIN_PARAM_KEY = "domain";
    private final static String LANG_PARAM_KEY = "langPref";
    private final static String TOKEN_PARAM_KEY = "token";
    private final static String CONSUMER_PARAM_KEY = "consumerKey";
    private Map params;
    private AMAPUser user;
    private String errorMsgKey;

	// public static void main(String[] args) {
    // try {
    //
    // log.debug(ESAPI.encoder()
    // .encodeForOS(new WindowsCodec(), "ddfdsds"));
    //
    // /**
    // * TEST LOYALTY
    // */
    // AMAPUser mmockaInt = AMAPInternalLDAP.getInstance().getAMAPUser(
    // "mmocka");
    // AMAPUser mmockaExt = AMAPExternalLDAP.getInstance().getAMAPUser(
    // "mmocka");
    // List<AMAPGroup> mmAg = AMAPExternalLDAP.getInstance()
    // .getUserAMAPGroup(mmockaInt.getGroupSearchCN());
    // List<SponsorGroup> mmSg = AMAPExternalLDAP.getInstance()
    // .getUserSponsorGroup(mmockaInt.getGroupSearchCN());
    // /**
    // * TEST KOREM
    // */
    // String sid = KoremExternalLDAP.getInstance().getUserSID("mdube");
    // AMAPUser au = KoremLDAP.getInstance().getAMAPUser("mdube");
    // List<AMAPGroup> ag =
    // KoremLDAP.getInstance().getUserGroup(au.getCommonName());
    // List<AMAPGroup> apg =
    // KoremLDAP.getInstance().getPUserGroup(au.getCommonName());
    // List<SponsorGroup> sg =
    // KoremExternalLDAP.getInstance().getUserSponsorGroup(au.getCommonName());
    //
    // } catch (LDAPException ex) {
    // log.error("Error", ex);
    // }
    // }
    /**
     *
     */
    private void reset(Map params) {
        this.params = params;
        this.user = null;
        this.errorMsgKey = null;
    }

    /**
     *
     * @param params
     * @return
     */
    @Override
    public User grantAuthorization(Map params) {
        reset(params);
        try {
            doLDAPAuthorization();
        } catch (LDAPException ex) {
            errorMsgKey = ex.getKey();
            return null;
        }

        User u = new User();
        u.setLogin(user.getUserName());
        u.setRole(user.getAMAPGroup().get(0).getRole());

        /**
         * Filter All sponsor group
         */
        if (allSponsor(user.getSponsorGroup())) {
            try {
                u.getSponsors().addAll(
                        new SponsorDAO(new AirMilesDAO()).getSponsors());
            } catch (SQLException ex) {
                log.warn("Sponsor ["
                        + Config.getInstance().getString(
                                "amap.sponsor.group.all") + "] not available");
            }
        } else {
            for (LDAPSponsorGroup sg : user.getSponsorGroup()) {
                try {
					// LOCAL CHANGE DO NOT MERGE WITH KOREM CHANGES
                    SponsorGroup sponsor = new SponsorDAO(new AirMilesDAO())
                            .getSponsorByName(sg.getSponsor());
                    if (sponsor != null) {
                        u.getSponsors().add(sponsor);
                    }
                } catch (SQLException ex) {
                    log.warn("Sponsor [" + sg.getSponsor() + "] not available");
                }
            }
        }

        if (u.getSponsors() == null || u.getSponsors().isEmpty()) {
            log.warn("No available sponsors");
        }

        return u;
    }

    /**
     *
     * @param groups
     * @return
     */
    private boolean allSponsor(List<LDAPSponsorGroup> groups) {
        String allSponsorStr = Confs.CONFIG.amapSponsorGroupsAll();
        for (LDAPSponsorGroup sg : groups) {
            if (sg.getSponsor().toUpperCase()
                    .contains(allSponsorStr.toUpperCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     */
    private void doLDAPAuthorization() throws LDAPException {
        String[] uidArray = (String[]) params.get(UID_PARAM_KEY);
        String uid = ESAPI.encoder().encodeForLDAP(uidArray[0]);
        
        LDAPCredentials internalInstance = AMAPInternalLDAP.getInstance();
        AMAPExternalLDAP externalInstance = AMAPExternalLDAP.getInstance();
        if (Confs.CONFIG.koremInternalAccessEnabled()) {
            internalInstance = externalInstance = KoremLDAP.getInstance();
        }
        
        if (Confs.CONFIG.koremInternalAccessEnabled() || isInternal()) {
            user = internalInstance.getAMAPUser(uid);
        } else {
            user = externalInstance.getAMAPUser(uid);
        }

        if (user == null) {
            throw new LDAPException("Unknown user", "secure.ldap.unknown.user");
        }

        List<AMAPGroup> ag = externalInstance.getUserAMAPGroup(
                user.getGroupSearchCN());
        if (ag == null || ag.isEmpty()) {
            throw new LDAPException("No AMAPGroup", "secure.ldap.no.amap.group");
        }
        user.setAMAPGroup(ag);


        List<LDAPSponsorGroup> sg = externalInstance
                .getUserSponsorGroup(user.getGroupSearchCN());
        if (sg == null || sg.isEmpty()) {
            throw new LDAPException("No SponsorGroup",
                    "secure.ldap.no.sponsor.group");
        }
        user.setSponsorGroup(sg);
    }

    private boolean isInternal() {
        return AMAPUser.Domain.Internal.equals(findUserDomain());
    }

    /**
     *
     */
    public String getErrorMessageKey() {
        return errorMsgKey;
    }

    /**
     *
     * @param params
     * @return
     */
    private AMAPUser.Domain findUserDomain() {
        String[] domain = (String[]) params.get(DOMAIN_PARAM_KEY);
        return AMAPUser.Domain.getByKey(domain[0]);
    }
}
