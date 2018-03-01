package com.lo.ldap;

import com.lo.Config;
import com.lo.config.Confs;
import java.util.List;

/**
 *
 * @author mdube
 */
public class AMAPUser {

    private Domain domain;
    private String userName;
    private String userSID;
    private String commonName;
    private List<LDAPSponsorGroup> sg;
    private List<AMAPGroup> ag;

    public AMAPUser(String uid, String sid, String cn, Domain d) {
        this.userName = uid;
        this.userSID = sid;
        this.commonName = cn;
        this.domain = d;
    }

    /**
     * TODO remove comment BUGFIX prod control (p_minardi) cannot access AMAP Tier2 845
     * @return the userName
     */
    public String getGroupSearchCN() {
        switch (this.domain) {
            case Internal:
                return userSID;
            case External:
            	return commonName;
            default:
                return commonName;
        }
    }

    /**
     * @return the userName
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @return the userSID
     */
    public String getUserSID() {
        return userSID;
    }

    /**
     * @return the sg
     */
    public List<LDAPSponsorGroup> getSponsorGroup() {
        return sg;
    }

    /**
     * @param sg the sg to set
     */
    public void setSponsorGroup(List<LDAPSponsorGroup> SponsorGroup) {
        this.sg = SponsorGroup;
    }

    /**
     * @return the ag
     */
    public List<AMAPGroup> getAMAPGroup() {
        return ag;
    }

    /**
     * @param aMAPGroup the aMAPGroup to set
     */
    public void setAMAPGroup(List<AMAPGroup> aMAPGroup) {
        this.ag = aMAPGroup;
    }

    /**
     * @return the domain
     */
    public Domain getDomain() {
        return domain;
    }

    /**
     * @return the commonName
     */
    public String getCommonName() {
        return commonName;
    }
    
    /**
     * 
     */
    public enum Domain {

        External(Confs.CONFIG.amapExternalParamKey()),
        Internal(Confs.CONFIG.amapInternalParamKey()),
        Korem(Confs.CONFIG.amapKoremParamKey());
        
        private String key;

        Domain(String key) {
            this.key = key;
        }
        
        /**
         * 
         * @param key
         * @return 
         */
        public static Domain getByKey(String key) {
            for (Domain d : Domain.values()) {
                if (d.key.equalsIgnoreCase(key)) {
                    return d;
                }
            }
            throw new IllegalArgumentException(key);
        }
    }
}
