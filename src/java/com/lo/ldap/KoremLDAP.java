package com.lo.ldap;

import com.spinn3r.log5j.Logger;
import java.util.List;

/**
 *
 * @author mdube
 */
public class KoremLDAP extends AMAPExternalLDAP {

    private static final Logger log = Logger.getLogger(AMAPInternalLDAP.class);
    private static final String PROPERTIES_FILE_NAME = "koremLDAP";
    
    private static KoremLDAP instance;

    private KoremLDAP() { }
    /**
     * Singleton
     * @return 
     */
    public static KoremLDAP getInstance() {
        if (instance == null) {
            instance = new KoremLDAP();
            instance.init();
        }
        return instance;
    }
    
    //Load the properties bundle
    protected void init() {
        super.init(PROPERTIES_FILE_NAME);
    }
    
    /**
     * 
     * @param uid
     * @return 
     */
    public AMAPUser getAMAPUser(String uid) throws LDAPException {
        String[] attrs = getUserSID(uid);
        
        if (attrs[0] != null) {
            return new AMAPUser(uid, attrs[0], attrs[1], AMAPUser.Domain.Korem);
        }
        return null;
    }
    
    protected String getUserGroupToCompare(String ldapGroup) {
        return ldapGroup.toUpperCase().replace("\\", "");
    }

}
