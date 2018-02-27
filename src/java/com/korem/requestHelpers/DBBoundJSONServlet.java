package com.korem.requestHelpers;

import com.korem.Proxy;
import java.lang.reflect.ParameterizedType;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author jduchesne
 */
public abstract class DBBoundJSONServlet<T extends Proxy> extends JSONServlet {

    @Override
    protected Object doInit() throws Exception {
        return getDataStoreType().newInstance();
    }

    @Override
    protected void doFinally(Object misc) {
        ((Proxy)misc).close();
    }

    @Override
    protected String getJSONWithMisc(HttpServletRequest request, Object misc) throws Exception {
        return getJSON(request, (T)misc);
    }

    protected Class<T> getDataStoreType() {
        return (Class<T>)((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    protected abstract String getJSON(HttpServletRequest request, T proxy) throws Exception;
}
