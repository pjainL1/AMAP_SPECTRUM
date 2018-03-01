/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lo.ldap;

import com.lo.db.om.Role;

/**
 *
 * @author mdube
 */
public class AMAPGroup implements LDAPGroup {
    
    private String groupName;
    private Role role;

    public AMAPGroup(String groupName) {
        this.groupName = groupName;
        for (Role r : Role.values()) {
            if (groupName.contains(r.toString())) {
                this.role = r;
            }
        }
    }
    
    /**
     * @return the groupName
     */
    @Override
    public String getGroupName() {
        return groupName;
    }
    
    /**
     * @return the role
     */
    public Role getRole() {
        return role;
    }
    
}
