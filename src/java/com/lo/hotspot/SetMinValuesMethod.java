/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lo.hotspot;

import com.korem.openlayers.parameters.IApplyParameters;
import com.korem.openlayers.parameters.IBaseParameters;
import com.korem.requestHelpers.RequestParser;
import com.lo.ContextParams;
import com.lo.web.Apply;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author akriaa
 */
public class SetMinValuesMethod implements Apply.IProgressAware{

    static interface IParams extends IApplyParameters {

    }
     
    @Override
    public Object parseRequest(HttpServletRequest request) {
        IParams params = RequestParser.persistentParse(request, IParams.class);
        ContextParams cp = ContextParams.get(request.getSession());
        return new Object[]{params, HotSpotMethod.getHotSpotFactory(request.getSession(), params),
                    cp.getSponsor().getCodes()};
    }

    @Override
    public void execute(Apply.ProgressListener listener, Object paramsObj) {
        IParams params = (IParams) ((Object[]) paramsObj)[0];
        //agilbert, not needed
        //((HotSpotFactory) ((Object[]) paramsObj)[1]).setMinValues(params.minTransactions() , params.minSpend(), params.minUnit());
        listener.update(100);
    }
}
