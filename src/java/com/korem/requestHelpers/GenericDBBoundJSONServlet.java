package com.korem.requestHelpers;

import com.korem.Proxy;
import java.lang.reflect.ParameterizedType;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author jduchesne
 */
public abstract class GenericDBBoundJSONServlet<T extends Proxy, V> extends DBBoundJSONServlet<T> {

    protected String getJSON(HttpServletRequest request, T proxy) throws Exception {
        return getJSON(request, proxy,
                RequestParser.parse(request,
                    (Class<V>)((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[1]));
    }

    protected abstract String getJSON(HttpServletRequest request, T proxy, V params) throws Exception;
}
