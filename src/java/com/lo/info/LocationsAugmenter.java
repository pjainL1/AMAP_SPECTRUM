/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lo.info;

import com.korem.openlayers.parameters.IInfoParameters;
import com.lo.ContextParams;
import java.sql.Connection;
import java.util.Map;

/**
 *
 * @author smukena
 */
public class LocationsAugmenter extends Augmenter {

    @Override
    void prepare(Connection connection, ContextParams cp, IInfoParameters params) throws Exception {
        
    }

    @Override
    void augment(Map<String, Object> info, IInfoParameters params, ContextParams cp) throws Exception {
        if (info.get("VALUE") != null && !info.get("VALUE").toString().equals("666.666")) { // 666.666 is a special value set by "location.query" (location.propeties)
            info.put(cp.getSlaTansactionValue(), info.get("VALUE"));           
        }
        info.remove("VALUE");
    }

    @Override
    void terminate() {
    }

}
