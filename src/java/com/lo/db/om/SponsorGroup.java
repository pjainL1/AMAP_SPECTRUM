package com.lo.db.om;

import java.io.InputStream;
import java.io.Serializable;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author ydumais
 */
public class SponsorGroup implements Serializable {
    private final List<String> codes = new ArrayList<>();
    private final List<Integer> keys = new ArrayList<>();

    private String rollupGroupName;
    private String rollupGroupCode;
    private String workspace;
    private String workspaceKey;
    private Blob logo;
    private String logoURL;
    private final Map<String, Integer> codesToKeys;
    private final List<Sponsor> sponsors;
    
    public static class Sponsor implements Serializable {
        private final String code;
        private final Integer key;
        private final String name;

        public Sponsor(String code, Integer key, String name) {
            this.code = code;
            this.key = key;
            this.name = name;
        }

        public String getCode() {
            return code;
        }

        public Integer getKey() {
            return key;
        }

        public String getName() {
            return name;
        }
    }

    public SponsorGroup() {
        codesToKeys = new HashMap<>();
        sponsors = new ArrayList<>();
    }

    public String getRollupGroupName() {
        return rollupGroupName;
    }

    public void setRollupGroupName(String name) {
        this.rollupGroupName = name;
    }

    public String getRollupGroupCode() {
        return rollupGroupCode;
    }

    public void setRollupGroupCode(String rollupGroupCode) {
        this.rollupGroupCode = rollupGroupCode;
    }

    public String getWorkspace() {
        return workspace;
    }

    public String getWorkspaceKey() {
        return workspaceKey;
    }

    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }

    public void setWorkspaceKey(String workspaceKey) {
        this.workspaceKey = workspaceKey;
    }
    
    public void addSponsor(String code, Integer key, String name) {
        codes.add(code);
        keys.add(key);
        codesToKeys.put(code, key);
        sponsors.add(new Sponsor(code, key, name));
    }

    public List<String> getCodes() {
        return codes;
    }

    public List<Integer> getKeys() {
        return keys;
    }
    
    public Integer getKeyFromCode(String code) {
        return codesToKeys.get(code);
    }
    
    public String getKeysFilter() {
        StringBuilder sb = new StringBuilder();
        
        int i = 0;
        for (Integer key : keys) {
            if (i++ > 0) {
                sb.append(", ");
            }
            sb.append(key);
        }
        
        return sb.toString();
    }

    public List<Sponsor> getSponsors() {
        return sponsors;
    }
    
    public String getDisplayList() {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (Sponsor sponsor : sponsors) {
            if (i++ > 0) {
                sb.append(", ");
            }
            sb.append(sponsor.getName());
        }
        
        return sb.toString();
    }

    public Blob getLogo() {
        return logo;
    }
    
    public void setLogo(Blob logo){
        this.logo = logo;
    }

    public String getLogoURL() {
        return logoURL;
    }

    public void setLogoURL(String logoURL) {
        this.logoURL = logoURL;
    }

    
}
