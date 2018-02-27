package com.korem.requestHelpers;

/**
 *
 * @author jphoude
 */
public class JSONException extends Exception {
    private String code;
    
    public JSONException(String code) {
        this(code, code, null);
    }
    
    public JSONException(String code, Throwable t) {
        this(code, code, t);
    }
    
    public JSONException(String message, String code, Throwable t) {
        super(message, t);
        
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
