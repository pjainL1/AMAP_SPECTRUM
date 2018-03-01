/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.lo.util;

import com.korem.openlayers.parameters.IApplyParameters;
import com.lo.analysis.tradearea.TradeArea;
import com.lo.analysis.tradearea.TradeAreaMethod;
import com.lo.db.dao.AirMilesDAO;
import com.lo.db.dao.LoggingDAO;
import com.lo.db.om.SponsorGroup;
import com.lo.db.om.User;
import java.util.ResourceBundle;


/**
 *
 * @author slajoie
 */
public class LoggingUtil {

    private static LoggingDAO ldao = new LoggingDAO(new AirMilesDAO());
    private static ResourceBundle conf = ResourceBundle.getBundle("loLocalString");
    private static final String SPACE = " ";
    private static final String AND = "and";
    private static final String TYPE_SPONSOR = "sponsor";
    private static final String TYPE_LOCATIONS = "locations";
    private static final String DATA_COLLECTOR = "collector";
    private static final String UNITS = "unit";
    private static final String REPORT_TYPE_BATCH = "batch";
    private static final String ONE_LOCATION = "one Projected Location";

    public static void log(User user, SponsorGroup sponsor, String description) {
        ldao.log(user, sponsor, description);
    }
    
    public static String getLoginMessage() {
        return conf.getString("mc.usagemonitoring.log.login");
    }

    public static String getSessionExpiredMessage() {
        return conf.getString("mc.usagemonitoring.log.sessionexpired");
    }

    public static String getTradeAreaMessage(TradeArea.Type type, com.lo.analysis.tradearea.TradeAreaMethod.IParams params) {
        StringBuilder sb = new StringBuilder();
        sb.append(conf.getString("mc.usagemonitoring.log.tradearea"));
        switch(type){
            case custom:{
                sb.append(SPACE).append(conf.getString("mc.usagemonitoring.log.custom"));
                break;
            }
            case units:{
                sb.append(SPACE).append((int) (params.issuance() * 100)).append(conf.getString("mc.usagemonitoring.log.unit")).append(createMinimumString(params));
                break;
            }
            case issuance:{
                sb.append(SPACE).append((int) (params.issuance() * 100)).append(conf.getString("mc.usagemonitoring.log.issuance")).append(createMinimumString(params));
                break;
            }
            case distance:{
                sb.append(SPACE).append(params.distance()).append(conf.getString("mc.usagemonitoring.log.kmdistance"));
                break;
            }
            default:{break;}
        }
        if (params.projected() != null) {
            sb.append(",").append(SPACE).append(conf.getString("mc.usagemonitoring.log.tradearea"));
            sb.append(SPACE).append(params.projected()).append(conf.getString("mc.usagemonitoring.log.projected"));
        }
        return sb.toString();
    }

    public static String getHotSpotMessage(com.lo.hotspot.HotSpotMethod.IParams params) {
        StringBuilder sb = new StringBuilder();
        sb.append(conf.getString("mc.usagemonitoring.log.hotspot")).append(SPACE);
        if(DateType.valueOf(params.dateType())!=DateType.single){
            sb.append(conf.getString("mc.usagemonitoring.log.hotspot.comparison"));
            sb.append(conf.getString("mc.usagemonitoring.log.hotspot.comparison."+params.hotspotComparisonType())).append(SPACE);
        }
        sb.append(conf.getString("mc.usagemonitoring.log."+params.dataType()));
        sb.append(SPACE).append(conf.getString("mc.usagemonitoring.log.on"));
        sb.append(SPACE).append(conf.getString("mc.usagemonitoring.log."+params.type()));
        sb.append(createMinimumString(params));
        return sb.toString();
    }

    public static String getNWMessage(com.lo.analysis.nwatch.NWatchMethod.IParams params) {
        if (params.nwatchtype().equals("unit")) {
            return conf.getString("mc.usagemonitoring.log.nw.unit") + SPACE + params.nwatch() + createMinimumString(params);
        } else {
            return conf.getString("mc.usagemonitoring.log.nw.spend") + SPACE + params.nwatch() + createMinimumString(params);
        }
    }
    
    public static String getStoreLevelAnalysisMessage(com.lo.analysis.storeLevelAnalysis.StoreLevelAnalysisMethod.IParams params) {
       return conf.getString("mc.usagemonitoring.log.sla."+params.dateType()) + SPACE + params.slaTransactionValue() + createMinimumString(params);
    }

    public static String getReportMessage(com.lo.report.ReportMethod.IParams params) {
        Integer locCount = params.locationKeys().split(",").length;
        StringBuilder sb = new StringBuilder();
        if (params.type().equals(REPORT_TYPE_BATCH)) {
            sb.append(conf.getString("mc.usagemonitoring.log.batchreport"));
        } else {
            sb.append(conf.getString("mc.usagemonitoring.log.report"));
        }
        sb.append(SPACE).append(locCount).append(SPACE).append(conf.getString("mc.usagemonitoring.log.locations"));

        if ("projected".equals(params.tradearea())) {
            sb.append(SPACE).append(AND).append(SPACE).append(ONE_LOCATION);
        }
        sb.append(createMinimumString(params));
        return sb.toString();
    }

    private static String createMinimumString(IApplyParameters params) {
        String result = "";
        if (params.minTransactions() != null){
            result += ", min transactions: "+ params.minTransactions();
        }
        if (params.minSpend()!= null){
            result += ", min spend: "+ params.minSpend()+"$";
        }
        if (params.minUnit()!= null){
            result += ", min unit: "+ params.minUnit();
        }
        
        return result;
    }



}
