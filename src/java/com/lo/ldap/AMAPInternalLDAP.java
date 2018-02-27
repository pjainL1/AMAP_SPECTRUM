package com.lo.ldap;

import com.spinn3r.log5j.Logger;
import java.util.List;

/**
 *
 * @author mdube
 */
public class AMAPInternalLDAP extends LDAPCredentials {

    private static final Logger log = Logger.getLogger(AMAPInternalLDAP.class);
    private static final String PROPERTIES_FILE_NAME = "internalLDAP";
    
    private static AMAPInternalLDAP instance;
    private AMAPInternalLDAP() { }
    /**
     * Singleton
     * @return 
     */
    public static AMAPInternalLDAP getInstance() {
        if (instance == null) {
            instance = new AMAPInternalLDAP();
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
            return new AMAPUser(uid, attrs[0], attrs[1], AMAPUser.Domain.Internal);
        }
        return null;
    }

}
