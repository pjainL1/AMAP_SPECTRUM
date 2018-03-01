package com.korem.requestHelpers;

import com.spinn3r.log5j.Logger;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 * @author jduchesne
 */
public class RequestParser {

    private static final Logger log = Logger.getLogger();
    private static final DateFormat FIRST_DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd");
    private static final DateFormat SECOND_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    public static <T> T persistentParse(HttpServletRequest request, Class<T> type) {
        final Map<String, Object> params = new HashMap<String, Object>();
        for (Method method : type.getMethods()) {
            if (method.getDeclaringClass() != Object.class) {
                params.put(method.getName(), parse(request.getParameter(method.getName()), method.getReturnType()));
            }
        }
        return (T) Proxy.newProxyInstance(RequestParser.class.getClassLoader(), new Class<?>[] { type },
            new InvocationHandler() {
                public Object invoke(Object proxy, Method method, Object[] args)
                            throws Throwable {
                    if (method.getDeclaringClass() != Object.class) {
                        return params.get(method.getName());
                    } else {
                        return method.invoke(proxy, args);
                    }
                }
        });
    }

    public static <T> T parse(final HttpServletRequest request, Class<T> type) {
        final Object[] mapArray = new Object[1];
        return (T) Proxy.newProxyInstance(RequestParser.class.getClassLoader(), new Class<?>[] { type },
            new InvocationHandler() {
                public Object invoke(Object proxy, Method method, Object[] args)
                            throws Throwable {
                    if (method.getName().startsWith("set")) {
                        setParameter(method.getName(), args, mapArray);
                        return null;
                    } else if ("getSession".equals(method.getName())) {
                        return request.getSession();
                    } else if (method.getDeclaringClass() != Object.class) {
                        return getParameter(method.getName(), method.getReturnType(), args, request, mapArray);
                    } else {
                        return method.invoke(proxy, args);
                    }
                }
        });
    }

    public static String format(Date date) {
        return FIRST_DATE_FORMAT.format(date);
    }

    public static Date parse(String date) {
        try {
            return FIRST_DATE_FORMAT.parse(date);
        } catch (ParseException pe) {
            try {
                return SECOND_DATE_FORMAT.parse(date);
            } catch (ParseException e) {
                log.error(null,e);
            }
        }
        return new Date();
    }

    private static Object getParameter(String methodName, Class<?> returnType, Object[] args, HttpServletRequest request,
            Object[] mapArray) {
        Object value = request.getParameter(methodName);
        if (value == null && mapArray[0] != null) {
            value = ((Map<String, Object>)mapArray[0]).get(methodName);
        } else if (value != null) {
            value = parse(value, returnType);
        } else if (args != null && args.length > 0) {
            value = args[0];
        }
        return value;
    }

    private static Object parse(Object value, Class<?> returnType) {
        if (value != null && ((String)value).length() > 0) {
            try {
                if (returnType == Double.class || returnType == double.class) {
                    value = new Double((String)value);
                } else if (returnType == Integer.class || returnType == int.class) {
                    value = new Integer((String)value);
                } else if (returnType == Long.class || returnType == long.class) {
                    value = new Long((String)value);
                } else if (returnType == Boolean.class || returnType == boolean.class) {
                    value = Boolean.parseBoolean((String)value);
                } else if (returnType == Date.class) {
                    value = parse((String)value);
                } else if (returnType == JSONObject.class) {
                    value = JSONObject.fromObject((String)value);
                } else if (returnType == JSONArray.class) {
                    value = JSONArray.fromObject((String)value);
                }
            } catch (NumberFormatException e) {
                log.debug(null,e);
            }
        } else {
            if (returnType != String.class) {
                // unless it's string, do not accept empty values.
                return null;
            }
        }
        return value;
    }

    private static void setParameter(String methodName, Object[] args, Object[] mapArray) {
        HashMap<String, Object> map = (HashMap<String, Object>)mapArray[0];
        if (map == null) {
            mapArray[0] = map = new HashMap<String, Object>();
        }
        map.put(Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4), args[0]);
    }
}
