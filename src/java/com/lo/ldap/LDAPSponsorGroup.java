/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lo.ldap;

/**
 *
 * @author mdube
 */
public class LDAPSponsorGroup implements LDAPGroup {
    
    private String groupName;
    private String sponsor;

    public LDAPSponsorGroup(String groupName, String sponsor) {
        this.groupName = groupName;
        this.sponsor = sponsor;
    }
    
    /**
     * @return the groupName
     */
    @Override
    public String getGroupName() {
        return groupName;
    }

    /**
     * @return the sponsor
     */
    public String getSponsor() {
        return sponsor;
    }
}
