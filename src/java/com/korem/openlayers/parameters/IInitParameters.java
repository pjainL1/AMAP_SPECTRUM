package com.korem.openlayers.parameters;

import java.util.Date;
import java.util.List;

/**
 *
 * @author jduchesne
 */
public interface IInitParameters extends IBaseParameters {
    String workspaceKey();

    List<String> sponsorCodes();

    void setMapInstanceKey(String mapInstanceKey);
    //void setSpecMapInstanceKey(String specMapInstanceKey);

    Date from();

    Date to();
    
    String baseUrl();
    
    String logo();
    

    
}
