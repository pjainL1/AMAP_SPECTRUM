/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lo.qa;

import com.korem.openlayers.parameters.IApplyParameters;
import com.korem.requestHelpers.RequestParser;
import com.lo.ContextParams;
import com.lo.util.LocationUtils;
import com.lo.web.Apply.IProgressAware;
import com.lo.web.Apply.ProgressListener;
import com.spinn3r.log5j.Logger;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 *
 * @author YDumais
 */
public class QualityOfDataMethod implements IProgressAware {

    private static final Logger log = Logger.getLogger();

    

    @Override
    public Object parseRequest(HttpServletRequest request) {
        return new Object[]{RequestParser.persistentParse(request, IApplyParameters.class),
                    request.getSession()};
    }

    @Override
    public void execute(ProgressListener listener, Object params) {
        IApplyParameters ip = (IApplyParameters) ((Object[]) params)[0];
        HttpSession session = (HttpSession) ((Object[]) params)[1];
        ContextParams contextParams = ContextParams.get(session);
        List<Double> locations = LocationUtils.parseList(ip.locations());

        log.debug("Check for sufficient transactions");
        SufficientTransactionChecker sufficientTransactionChecker =
                new SufficientTransactionChecker(ip, contextParams);
        boolean sufficientTransactionInError = sufficientTransactionChecker.check(locations);
        listener.update(50);
        if(sufficientTransactionInError){
            QualityOfData.set(contextParams, QualityOfData.Rule.insufficient);
        } else {
            log.debug("Check for rural postal code");
            RuralLocationChecker ruralLocationChecker = new RuralLocationChecker();
            if (ruralLocationChecker.check(contextParams, locations)) {
                QualityOfData.set(contextParams, QualityOfData.Rule.ruralPC);
            }
        }
        listener.update(100);
    }
}
