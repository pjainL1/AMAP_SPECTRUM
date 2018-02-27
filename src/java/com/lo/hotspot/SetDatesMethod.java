package com.lo.hotspot;

import com.korem.openlayers.parameters.IBaseParameters;
import com.korem.requestHelpers.RequestParser;
import com.lo.ContextParams;
import com.lo.web.Apply.IProgressAware;
import com.lo.web.Apply.ProgressListener;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author jduchesne
 */
public class SetDatesMethod implements IProgressAware {

    static interface IParams extends IBaseParameters {

        String startDate();

        String endDate();
    }

    @Override
    public Object parseRequest(HttpServletRequest request) {
        IParams params = RequestParser.persistentParse(request, IParams.class);
        ContextParams cp = ContextParams.get(request.getSession());
        return new Object[]{params, HotSpotMethod.getHotSpotFactory(request.getSession(), params),
                    cp.getSponsor().getCodes()};
    }

    @Override
    public void execute(ProgressListener listener, Object paramsObj) {
        IParams params = (IParams) ((Object[]) paramsObj)[0];
        ((HotSpotFactory) ((Object[]) paramsObj)[1]).setDates(params);
        listener.update(100);
    }
}
