package com.korem.requestHelpers;

import com.korem.Proxy;
import com.lo.config.Confs;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.ParameterizedType;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author jduchesne
 */
public abstract class DBBoundServlet<T extends Proxy> extends Servlet {

    private static final String TYPE_JSON = "application/json";
    
    @Override
    public void execute(HttpServletRequest request, HttpServletResponse response) throws InstantiationException, IllegalAccessException, Exception {
        try (T proxy = getDataStoreType().newInstance()) {
            execute(proxy, request, response);
        }
    }

    protected Class<T> getDataStoreType() {
        return (Class<T>)((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    protected abstract void execute(T proxy, HttpServletRequest request, HttpServletResponse response) throws Exception;
    
    protected void printJSON(String jsonString, HttpServletResponse response) {
        response.setContentType(TYPE_JSON);
        response.setCharacterEncoding(Confs.STATIC_CONFIG.charset());

        try {
            PrintWriter writer = response.getWriter();
            writer.print(jsonString);
            writer.close();        
        } catch (IOException ex) {
            Logger.getLogger(DBBoundServlet.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
}
