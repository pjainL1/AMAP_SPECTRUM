package com.lo.db.om;

/**
 *
 * @author rarif
 */
public class SponsorCode {
    private String code;
    private long key;
    private String name;

    public SponsorCode(String code, long key, String name) {
        this.code = code;
        this.key = key;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public long getKey() {
        return key;
    }

    public String getName() {
        return name;
    }
}
