/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lo.web.sso;

import java.util.PropertyResourceBundle;

import javax.naming.Context;

import com.lo.AbstractConfig;

public class Config extends AbstractConfig {
    
    private static Config instance = new Config("com.lo.web.sso.config");
    private PropertyResourceBundle prb;
   

    private Config(String path) {
        super(path);
    }

    public static Config getInstance() {
        return instance;
    }

    @Override
    protected void init(PropertyResourceBundle prb, Context context) {
        this.prb = prb;
    }

    public String getValue(String key) {
    	return super.getValue(key, prb);
    }
 
}
