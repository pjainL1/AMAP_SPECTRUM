/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lo.db.om;

import com.lo.ldap.AMAPGroup;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author ydumais
 */
public class User implements Serializable {

    private String login;
    private Role role;
    private List<SponsorGroup> sponsors = new ArrayList<SponsorGroup>();
    private Map<String, String> attributes = new HashMap<>();
    
    public boolean isAdmin() {
        return role != null && isInRole(Role.Admin);
    }
    
    public boolean isAnalyst() {
        return role != null && isInRole(Role.Analyst);
    }
    
    public boolean isInRole(Role r) {
        return role != null && role.equals(r);
    }
    
    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public List<SponsorGroup> getSponsors() {
        return sponsors;
    }

    public void setSponsors(List<SponsorGroup> sponsors) {
        this.sponsors = sponsors;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }
    
    
}
