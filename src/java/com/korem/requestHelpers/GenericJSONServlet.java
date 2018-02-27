package com.korem.requestHelpers;

import static com.korem.requestHelpers.JSONServlet.FAILURE;
import java.lang.reflect.ParameterizedType;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author jduchesne
 */
public abstract class GenericJSONServlet<T> extends JSONServlet {
    
    @Override
    protected String getJSON(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return getJSON(request, response, RequestParser.<T>parse(request, getDataStoreType()));
    }

    protected Class<T> getDataStoreType() {
        return (Class<T>)((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    protected String getJSON(HttpServletRequest request, HttpServletResponse response, T params) throws Exception {
        return getJSON(request, params);
    }

    protected String getJSON(HttpServletRequest request, T params) throws Exception {
        return FAILURE;
    }
}
