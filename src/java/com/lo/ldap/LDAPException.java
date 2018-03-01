/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lo.ldap;

/**
 *
 * @author mdube
 */
public class LDAPException extends Exception {

    private String key;
    
    /**
     * Creates a new instance of
     * <code>LDAPException</code> without detail message.
     */
    public LDAPException() {
    }

    /**
     * Constructs an instance of
     * <code>LDAPException</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public LDAPException(String msg, String key) {
        super(msg);
        this.key = key;
    }

    /**
     * @return the key
     */
    public String getKey() {
        return key;
    }
}
