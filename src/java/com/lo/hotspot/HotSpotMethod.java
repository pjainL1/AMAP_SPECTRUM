package com.lo.hotspot;

import com.korem.openlayers.parameters.IApplyParameters;
import com.korem.openlayers.parameters.IBaseParameters;
import com.korem.requestHelpers.RequestParser;
import com.lo.ContextParams;
import com.lo.util.LoggingUtil;
import com.lo.web.Apply.IProgressAware;
import com.lo.web.Apply.ProgressListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 *
 * @author jduchesne
 */
public class HotSpotMethod implements IProgressAware {

    private static final int INDEX_PARAMS = 0;
    private static final int INDEX_FACTORY = 1;
    private static final int INDEX_SESSION = 2;


    public enum ComparisonType{
        blended, growth, decline
    }
    
    public static interface IParams extends IApplyParameters {

        String dataType();

        String type();

        String hotspotComparisonType();
    }

    @Override
    public Object parseRequest(HttpServletRequest request) {
        IParams params = RequestParser.persistentParse(request, IParams.class);
        return new Object[]{params, getHotSpotFactory(request.getSession(), params), request.getSession()};
    }

    @Override
    public void execute(ProgressListener listener, Object paramsObj) {
        Object[] paramAsArray = (Object[]) paramsObj;
        IParams params = (IParams) paramAsArray[INDEX_PARAMS];
        HotSpotFactory factory = (HotSpotFactory) paramAsArray[INDEX_FACTORY];
        factory.setOptions(params);
        
        ContextParams cp = ContextParams.get((HttpSession) paramAsArray[INDEX_SESSION]);
        LoggingUtil.log(cp.getUser(), cp.getSponsor(), LoggingUtil.getHotSpotMessage(params));

        listener.update(100);
    }

    public static HotSpotFactory getHotSpotFactory(HttpSession session, IBaseParameters p) {
        String key = "hotspot" + p.mapInstanceKey();
        HotSpotFactory factory = (HotSpotFactory) session.getAttribute(key);
        if (factory == null) {
            session.setAttribute(key, factory = new HotSpotFactory(
                    ContextParams.get(session)));
        }
        return factory;
    }

}
