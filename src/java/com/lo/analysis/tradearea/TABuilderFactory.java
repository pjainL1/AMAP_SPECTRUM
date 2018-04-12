package com.lo.analysis.tradearea;

import com.lo.ContextParams;
import com.lo.analysis.tradearea.TradeAreaMethod.IParams;
import com.lo.analysis.tradearea.builder.CustomTABuilder;
import com.lo.analysis.tradearea.builder.DistanceTABuilder;
import com.lo.analysis.tradearea.builder.IssuanceTABuilder;
import com.lo.analysis.tradearea.builder.ProjectedTABuilder;
import com.lo.analysis.tradearea.builder.TABuilder;
import com.lo.db.om.SponsorGroup;
import com.lo.util.DateParser;
import com.lo.util.LocationUtils;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author ydumais
 */
public class TABuilderFactory {

    /**
     * 
     * @param types
     * @param params
     * @param cp
     * @return
     * @throws Exception 
     */
    public static List<TABuilder> createTradeAreaBuilder(List<TradeArea.Type> types, IParams params, ContextParams cp, SponsorGroup sponsorGroup) throws Exception {
        List<TABuilder> aList = new ArrayList();
        for (TradeArea.Type type : types){
            switch (type) {
                case issuance:{
                    aList.add(prepareIssuanceTABuilder(params, cp, sponsorGroup , type));
                    break;
                }
                case units:{
                    aList.add(prepareIssuanceTABuilder(params, cp, sponsorGroup, type));
                    break;
                }
                case distance:{
                    aList.add(prepareDistanceTABuilder(params, cp, sponsorGroup));
                    break;
                }
                case projected:{
                    aList.add(prepareProjectedTABuilder(params, cp, sponsorGroup));
                    break;
                }
                case custom:{
                    aList.add(prepareCustomTABuilder(params, cp, sponsorGroup));
                    break;
                }
                default:{break;}
            }
        }
        return aList;
    }

    public static TABuilder prepareIssuanceTABuilder(IParams params, ContextParams cp, SponsorGroup sponsorGroup, TradeArea.Type type) throws ParseException {
        List<Object> pList = new ArrayList();
        DateParser dp = new DateParser();
        pList.add(dp.parse(params.from()));
        pList.add(dp.parse(params.to()));
        pList.add(params.issuance());
        pList.add(params.minTransactions()== null ? null : params.minTransactions());
        pList.add(params.minSpend() == null ? null : params.minSpend());
        pList.add(params.minUnit()== null ? null : params.minUnit());
        return new IssuanceTABuilder(pList.toArray(new Object[]{}), cp, sponsorGroup, 
                LocationUtils.parseList(params.locations()) , type);
        //List<Double> locationList = Arrays.asList(865.0, 160280.0, 733.0, 860.0, 990.0, 992.0, 730.0, 181971.0, 1111.0);
//         return new IssuanceTABuilder(pList.toArray(new Object[]{}), cp, sponsorGroup, 
//                locationList , type);
    }
    public static TABuilder prepareDistanceTABuilder(IParams params, ContextParams cp, SponsorGroup sponsorGroup) throws ParseException {
        List<Object> pList = new ArrayList();
        pList.add(params.distance());
        return new DistanceTABuilder(pList.toArray(new Object[]{}), cp, sponsorGroup, LocationUtils.parseList(params.locations()));
    }
    public static TABuilder prepareProjectedTABuilder(IParams params, ContextParams cp, SponsorGroup sponsorGroup) throws ParseException, Exception {
        List<Object> pList = new ArrayList();
        pList.add(params.projected());
        pList.add(params.longitude());
        pList.add(params.latitude());
        return new ProjectedTABuilder(pList.toArray(new Object[]{}));
    }
    public static TABuilder prepareCustomTABuilder(IParams params, ContextParams cp, SponsorGroup sponsorGroup) throws ParseException, Exception {
        List<Object> pList = new ArrayList();
        pList.add(params.polygon());
        TABuilder customTABuilder = new CustomTABuilder(pList.toArray(new Object[]{}), cp, sponsorGroup, LocationUtils.parseList(params.locations()));
        return customTABuilder;
    }
}
