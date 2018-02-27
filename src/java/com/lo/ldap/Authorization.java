/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lo.ldap;

import com.lo.db.om.User;
import java.util.Map;

/**
 *
 * @author mdube
 */
public interface Authorization {
    
    public User grantAuthorization(Map params);
    public String getErrorMessageKey();
}
