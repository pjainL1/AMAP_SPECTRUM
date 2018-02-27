package com.lo.hotspot;

import com.korem.config.ConfigManager;
import com.lo.config.Confs;
import com.spinn3r.log5j.Logger;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import com.lo.hotspot.HotSpotMethod.IParams;
import com.lo.util.DateType;

/**
 *
 * @author jduchesne
 */
public class HotSpotQueries {
    private static final Logger log = Logger.getLogger();
    private static final HotSpotQueries instance = new HotSpotQueries();
    
    public enum QueryType {
        SINGLE,
        COMPARE_FIRST,
        COMPARE_SECOND
    }

    static HotSpotQueries get() {
        return instance;
    }
    
    String getQuery(IParams params, String locations, List<Integer> selectedCodes) {
        if(DateType.valueOf(params.dateType())==DateType.single){
            return getBaseQuery(params, locations, selectedCodes, QueryType.SINGLE);
        }
        String baseQuery1 = getBaseQuery(params, locations, selectedCodes, QueryType.COMPARE_FIRST);
        String baseQuery2 = getBaseQuery(params, locations, selectedCodes, QueryType.COMPARE_SECOND);
        String comparisonQuery = (String)ConfigManager.get().getProperties("Hotspot").get("query.compare."+params.hotspotComparisonType().trim());
        String query = Confs.HOTSPOT_CONFIG.queryCompare().replace("%baseQuery%", baseQuery1)
                .replace("%baseQuery%", baseQuery2)
                .replace("%expression%", comparisonQuery);
        log.debug("compare basic query: "+query);
        return query;
    }
    
    private String addFilterFragments(String query, QueryType queryType, IParams params) {
        String filters = "";
        switch (queryType) {
            case SINGLE:
                if (params.minSpend() != null) {
                    filters += Confs.HOTSPOT_CONFIG.queryConditionValuesFilterFragmentSpendSingle();
                }
                if (params.minUnit()!= null) {
                    filters += Confs.HOTSPOT_CONFIG.queryConditionValuesFilterFragmentUnitSingle();
                }
                if (params.minTransactions() != null) {
                    filters += Confs.HOTSPOT_CONFIG.queryConditionValuesFilterFragmentTransactionSingle();
                }
                break;
            case COMPARE_FIRST:
                if (params.minSpend() != null) {
                    filters += Confs.HOTSPOT_CONFIG.queryConditionValuesFilterFragmentSpend();
                }
                if (params.minUnit()!= null) {
                    filters += Confs.HOTSPOT_CONFIG.queryConditionValuesFilterFragmentUnit();
                }
                if (params.minTransactions()!= null) {
                    filters += Confs.HOTSPOT_CONFIG.queryConditionValuesFilterFragmentTransaction();
                }
                break;
            case COMPARE_SECOND:
                if (params.minSpend() != null) {
                    filters += Confs.HOTSPOT_CONFIG.queryConditionValuesFilterFragmentSpend();
                }
                if (params.minUnit()!= null) {
                    filters += Confs.HOTSPOT_CONFIG.queryConditionValuesFilterFragmentUnit();
                }
                if (params.minTransactions()!= null) {
                    filters += Confs.HOTSPOT_CONFIG.queryConditionValuesFilterFragmentTransaction();
                }
                break;
        }
        
        return query.replace("%filters%", filters);
    }
    
    public static boolean ignoreMinimumValuesFilters(QueryType queryType, IParams params) {
        switch (queryType) {
            case SINGLE:
                return params.type().equals("airMiles");
            case COMPARE_FIRST:
            case COMPARE_SECOND:
                return params.dataType().equals("collector");
        }
        return false;
    }

    private String getBaseQuery(IParams params, String locations, List<Integer> selectedCodes, QueryType queryType) {
        boolean minsNeeded = null != params.minTransactions() || null != params.minSpend() || null != params.minUnit();
        String query = null;
        String conditionQuery = "";
        boolean ignoreFilters = ignoreMinimumValuesFilters(queryType, params);
        if (minsNeeded && !ignoreFilters) {
            switch (queryType) {
                case SINGLE:
                    conditionQuery = Confs.HOTSPOT_CONFIG.queryConditionValues();
                    break;
                case COMPARE_FIRST:
                case COMPARE_SECOND:
                    conditionQuery = Confs.HOTSPOT_CONFIG.queryConditionValuesSumAnalysisFragment();
                    break;
            }
            conditionQuery = addFilterFragments(conditionQuery, queryType, params);
        }
        switch (HotSpotType.valueOf(params.type())) {
            case airMiles:
                query = Confs.HOTSPOT_CONFIG.queryBaseAm();
                break;
            case sponsor:
                query = (DateType.valueOf(params.dateType())==DateType.single?Confs.HOTSPOT_CONFIG.queryBase():Confs.HOTSPOT_CONFIG.queryCompareBase())
                        .replace("%table%", "sponsor")
                        .replace("%conditionValues%", conditionQuery)
                        .replaceAll("%subCondition%", Confs.HOTSPOT_CONFIG.queryBaseSponsorKeyClause())
                        .replaceAll("%sponsors%", StringUtils.join(selectedCodes, ","));
                        
                break;
            case locations:
                if (locations != null && locations.endsWith(",")) {
                    locations = locations.substring(0, locations.length() - 1);
                }
                query = (DateType.valueOf(params.dateType())==DateType.single?Confs.HOTSPOT_CONFIG.queryBase():Confs.HOTSPOT_CONFIG.queryCompareBase())
                        .replace("%table%", "location")
                        .replace("%conditionValues%", conditionQuery)
                        .replaceAll("%subCondition%", Confs.HOTSPOT_CONFIG.queryBaseSponsorLocationClause())
                        .replaceAll("%locations%", locations);
                        
                break;
        }
        query = query.replace("%count%", params.dataType());
        log.debug("basic query: "+query);
        return query;
    }

}
