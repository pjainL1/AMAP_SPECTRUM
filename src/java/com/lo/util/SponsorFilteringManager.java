package com.lo.util;

import com.lo.ContextParams;
import com.lo.config.Confs;
import java.util.List;

/**
 *
 * @author rarif
 */
public class SponsorFilteringManager {
    public static final String SPONSOR_KEYS_QUERY_PLACEHOLDER = "{sponsorKey}";

    private static final SponsorFilteringManager INSTANCE = new SponsorFilteringManager();

    private SponsorFilteringManager() {
    }

    public static final SponsorFilteringManager get() {
        return INSTANCE;
    }
    
    public String getKeysList(List<Integer> keys) {
        if (keys == null || keys.isEmpty()) {
            return "NULL";
        }
        
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
    public String getKeysFilter(List<Integer> keys) {
        String list = getKeysList(keys);
        
        return String.format(Confs.QUERIES.filtersSponsorKeys(), list);
    }
    
    public String getCodesDisplayList(List<String> codes) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        
        for (String code : codes) {
            if (i++ > 0) {
                sb.append(", ");
            }
            sb.append(code);
        }
        
        return sb.toString();
    }
    
    public String getCodesList(List<String> codes) {
        if (codes == null || codes.isEmpty()) {
            return "NULL";
        }
        
        StringBuilder sb = new StringBuilder();
        int i = 0;
        
        for (String code : codes) {
            if (i++ > 0) {
                sb.append(", ");
            }
            sb.append('\'').append(code).append('\'');
        }
        
        return sb.toString();
    }
    
    public String getCodesFilter(List<String> codes) {
        String list = getCodesList(codes);
        
        return String.format(Confs.QUERIES.filtersSponsorCodes(), list);
    }
    
    public String replaceSponsorKeysInQuery(String query, ContextParams cp) {
        return query.replace(SPONSOR_KEYS_QUERY_PLACEHOLDER, cp.getSponsorKeysList());
    }
    
    public String replaceSponsorKeysInQuery(String query, String sponsorKeysList) {
        return query.replace(SPONSOR_KEYS_QUERY_PLACEHOLDER, sponsorKeysList);
    }
}
