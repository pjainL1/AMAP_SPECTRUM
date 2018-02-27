/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lo.ldap;

//import com.spinn3r.log5j.Logger;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Logger;

import com.lo.Config;
import com.lo.Pwd;
import com.lo.config.Confs;
import com.loyalty.app.marketing.security.LoyaltyEncryptor;
import java.util.List;
import org.apache.commons.lang.NotImplementedException; 

/**
 *
 * @author mdube
 */
public abstract class LDAPCredentials implements LDAPAttributes {

    private static final Logger log = ESAPI.getLogger(LDAPCredentials.class);
    protected ResourceBundle lProps;
    protected String LDAP_SERVER = "";
    protected String LDAP_USER_CN = "";
    protected String LDAP_USER_PASSWORD = "";
    protected String LDAP_ROOT_DN = "";
    protected String LDAP_ROOT_AMAP_DN = "";
    protected String LDAP_ROOT_SPONSOR_DN = "";
    protected String LDAP_PORT = "";
    protected String LDAP_USER_FILTER_STR = "";
    protected String LDAP_USER_SID_FILTER_STR = "";
    protected String AMAP_COMMON_GROUP_PREFIX = "";
    protected boolean isBundleVerified = true;

    /**
     * Load the properties bundle Default needed: LDAP_SERVER LDAP_USER_CN
     * LDAP_USER_PASSWORD LDAP_PORT LDAP_ROOT_DN LDAP_USER_FILTER_STR
     * LDAP_USER_SID_FILTER_STR
     *
     * @param propsFileName
     */
    protected void init(String propsFileName) {
        try {
// LOCAL CHANGE DO NOT CHANGE WITH KOREM CHANGES

            lProps = ResourceBundle.getBundle(propsFileName);
            if (lProps != null) {
                LDAP_SERVER = lProps.getString("LDAP_SERVER");
                LDAP_USER_CN = lProps.getString("LDAP_USER_CN");
//				LDAP_USER_PASSWORD = lProps.getString("LDAP_USER_PASSWORD");
                LDAP_PORT = lProps.getString("LDAP_PORT");
                LDAP_ROOT_DN = lProps.getString("LDAP_ROOT_DN");
                AMAP_COMMON_GROUP_PREFIX = lProps
                        .getString("AMAP_COMMON_GROUP_PREFIX");

                LDAP_USER_FILTER_STR = lProps.getString("LDAP_USER_FILTER_STR");
                LDAP_USER_SID_FILTER_STR = lProps
                        .getString("LDAP_USER_SID_FILTER_STR");
                if (!propsFileName.contains("intern")) {
// LOCAL CHANGE DO NOT CHANGE WITH KOREM CHANGES
                        LDAP_ROOT_AMAP_DN = lProps.getString("LDAP_ROOT_AMAP_DN");
                    LDAP_ROOT_SPONSOR_DN = lProps
                            .getString("LDAP_ROOT_SPONSOR_DN");
                }
            }
            if (Confs.CONFIG.koremInternalAccessEnabled()) {
                LDAP_USER_PASSWORD = Pwd.getInstance().getValue("korem.LDAP_USER_PASSWORD");
            } else if (!propsFileName.contains("intern")) {
                LDAP_USER_PASSWORD = Pwd.getInstance().getValue("external.LDAP_USER_PASSWORD");
            } else {
                LDAP_USER_PASSWORD = Pwd.getInstance().getValue("internal.LDAP_USER_PASSWORD");
            }
        } catch (MissingResourceException e) {
            log.fatal(ESAPI.log().SECURITY, false,
                    "Missing LDAP default config", e);
            isBundleVerified = false;
        }
    }

    /**
     *
     * @return
     */
    protected DirContext connectToLDAPContext() throws LDAPException {
        DirContext ctx = null;
        boolean isConnected = false;
        String[] LDAPServerURLs = LDAP_SERVER.split(";");
        int port = Integer.parseInt(LDAP_PORT);
        Hashtable<String, String> env = new Hashtable<String, String>();
        if (port == 636) {
            env.put(Context.SECURITY_PROTOCOL, "ssl");
        }
        env.put(Context.INITIAL_CONTEXT_FACTORY,
                "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, "ldap://" + LDAP_SERVER + ":" + port);
        env.put(Context.SECURITY_PRINCIPAL, LDAP_USER_CN);
        try {
            if (!Confs.CONFIG.koremInternalAccessEnabled()) {
                //password is encrypted with AES. attempting to decrypt
                LoyaltyEncryptor lencryptor = new LoyaltyEncryptor(LoyaltyEncryptor.EncryptionAlgorithms.AES);
                env.put(Context.SECURITY_CREDENTIALS, lencryptor.decryptStringsWithBracket(LDAP_USER_PASSWORD));
            } else {
                env.put(Context.SECURITY_CREDENTIALS, LDAP_USER_PASSWORD);
            }
        } catch (Exception e) {
            log.warning(ESAPI.log().SECURITY, false, "Unable to decrypt password", e);
        }
        env.put(Context.SECURITY_AUTHENTICATION, "simple");

        for (String ldapServer : LDAPServerURLs) {
            try {
                env.put("java.naming.ldap.attributes.binary", ATTR_OBJECT_SID);
                env.put(Context.PROVIDER_URL, "ldap://" + ldapServer + ":"
                        + port);
                ctx = new InitialDirContext(env);

                isConnected = true;
                break;
            } catch (NamingException e) {
                log.warning(ESAPI.log().SECURITY, false,
                        "Unable to connect to [" + ldapServer + "]", e);
            }
        }
        if (!isConnected) {
            throw new LDAPException("No LDAP available",
                    "secure.no.ldap.available");
        }
        return ctx;
    }

    /**
     * Connects to the Internal LDAP server to obtain the ObjectSID of the user
     *
     * @param uid
     * @return ObjectSID
     */
    protected String[] getUserSID(String userName) throws LDAPException {
        log.debug(ESAPI.log().SECURITY, true, "getUserSID");

        String objectSID = null;
        String cn = null;
        DirContext ctx = connectToLDAPContext();

        if (ctx != null) {
            String userFilter = LDAP_USER_FILTER_STR.replaceAll("#LDAP_LOGIN#",
                    userName);
            SearchControls ctls = new SearchControls();
            ctls.setReturningAttributes(new String[]{ATTR_TRUST_PARENT,
                ATTR_OBJECT_SID, ATTR_CN});
            ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            try {
                NamingEnumeration<?> e = ctx.search(LDAP_ROOT_DN, userFilter,
                        ctls);
                /**
                 * return the first result found
                 */
                if (e.hasMore()) {
                    SearchResult sr = (SearchResult) e.next();
                    byte[] sid = (byte[]) sr.getAttributes()
                            .get(ATTR_OBJECT_SID).get();
                    objectSID = getSIDAsString(sid);

                    cn = sr.getAttributes().get(ATTR_CN).get().toString();

                    log.info(ESAPI.log().SECURITY, true, "Found user ["
                            + userName + "]");
                    log.debug(ESAPI.log().SECURITY, true, "Found user ["
                            + userName + "] with SID [" + objectSID + "]");
                }
                ctx.close();
            } catch (NamingException e) {
                log.error(ESAPI.log().SECURITY, false,
                        "LDAPCredentials.getObjectSID", e);
            }
        } else {
            log.warning(ESAPI.log().SECURITY, false,
                    "LDAPCredentials.getObjectSID - Failed to connect to LDAP server(s)");
        }
        return new String[]{objectSID, cn};
    }

    /**
     *
     * @param SID
     * @return
     */
    protected String getSIDAsString(byte[] SID) {
        // Add the 'S' prefix
        StringBuilder strSID = new StringBuilder("S-");

		// bytes[0] : in the array is the version (must be 1 but might
        // change in the future)
        strSID.append(SID[0]).append('-');

        // bytes[2..7] : the Authority
        StringBuilder tmpBuff = new StringBuilder();
        for (int t = 2; t <= 7; t++) {
            String hexString = Integer.toHexString(SID[t] & 0xFF);
            tmpBuff.append(hexString);
        }
        strSID.append(Long.parseLong(tmpBuff.toString(), 16));

        // bytes[1] : the sub authorities count
        int count = SID[1];

		// bytes[8..end] : the sub authorities (these are Integers - notice
        // the endian)
        for (int i = 0; i < count; i++) {
            int currSubAuthOffset = i * 4;
            tmpBuff.setLength(0);
            tmpBuff.append(String.format("%02X%02X%02X%02X",
                    (SID[11 + currSubAuthOffset] & 0xFF),
                    (SID[10 + currSubAuthOffset] & 0xFF),
                    (SID[9 + currSubAuthOffset] & 0xFF),
                    (SID[8 + currSubAuthOffset] & 0xFF)));

            strSID.append('-').append(Long.parseLong(tmpBuff.toString(), 16));
        }

        // That's it - we have the SID
        return strSID.toString();
    }

    /**
     * Connects to the LDAP server to obtain a list of AMAP groups that the user
     * belongs to. Use the default ROOT_DN.
     *
     * @param userName
     * @param groupFilter
     * @return
     */
    public ArrayList<String> getUserLDAPGroup(String userCN,
            String groupFilter, int DN_TYPE) throws LDAPException {
        String dn = "";
        if (DN_TYPE == 0) {
            dn = LDAP_ROOT_AMAP_DN;
        } else {
            dn = LDAP_ROOT_SPONSOR_DN;
        }

        return getUserLDAPGroup(dn, userCN, groupFilter);
    }
    
    protected String getUserGroupToCompare(String ldapGroup) {
        return ldapGroup.toUpperCase();
    }

    /**
     * Connects to the LDAP server to obtain a list of AMAP groups that the user
     * belongs to.
     *
     * @param rootDN
     * @param userCN
     * @param groupFilter
     * @return
     * @throws LDAPException
     */
    public ArrayList<String> getUserLDAPGroup(String rootDN, String userCN,
            String groupFilter) throws LDAPException {
        log.debug(ESAPI.log().SECURITY, true, "getUserLDAPGroup(" + userCN
                + ", " + groupFilter + ")");
        DirContext ctx = connectToLDAPContext();
        ArrayList<String> groups = null;
        if (ctx != null) {
            SearchControls ctls = new SearchControls();
            ctls.setReturningAttributes(new String[]{ATTR_NAME, ATTR_MEMBER});
            ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            groups = new ArrayList<String>();
            try {
                log.debug(ESAPI.log().SECURITY, true, rootDN);
                NamingEnumeration<?> e = ctx.search(rootDN, groupFilter, ctls);
                int gcount = 0;
                while (e.hasMore()) {
                    gcount++;
                    SearchResult sr = (SearchResult) e.next();
                    Attribute attr = sr.getAttributes().get(ATTR_MEMBER);
                    log.debug(ESAPI.log().SECURITY, true, "Found group ["
                            + sr.getAttributes().get(ATTR_NAME).get()
                            .toString() + "]");
                    if (attr != null) {
                        int nbmember = attr.size();
                        for (int i = 0; i < nbmember; i++) {
                            if (getUserGroupToCompare(attr.get(i).toString())
                                    .contains(getUserGroupToCompare(userCN))) {
                                String groupName = sr.getAttributes()
                                        .get(ATTR_NAME).get().toString();
                                groups.add(groupName);
                                log.info(ESAPI.log().SECURITY, true,
                                        "User in group [" + groupName + "]");
                            }
                        }
                    }
                }
                log.info(ESAPI.log().SECURITY, true, "Filter found [" + gcount
                        + "] group");
                log.info(ESAPI.log().SECURITY, true, "User member of ["
                        + groups.size() + "] group");
                ctx.close();
            } catch (NamingException e) {
                log.warning(ESAPI.log().SECURITY, true,
                        "LDAPCredentials.getLDAPGroup - Failed to get LDAP groups");
                log.debug(ESAPI.log().SECURITY, true, "", e);
            }
        } else {
            log.warning(ESAPI.log().SECURITY, true,
                    "LDAPCredentials.getLDAPGroup - Failed to connect to LDAP server(s)");
        }
        return groups;
    }
    
    public abstract AMAPUser getAMAPUser(String uid) throws LDAPException;
}
