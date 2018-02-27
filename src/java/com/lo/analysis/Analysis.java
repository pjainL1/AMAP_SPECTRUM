/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lo.analysis;

import java.util.ResourceBundle;

/**
 *
 * @author ydumais
 */
public enum Analysis {

    LOCATIONS("location"), TRADE_AREA("ta"), NEIBOURHOOD_WATCH("nw"), POSTAL_CODE("po"), STORE_ANALYSIS_LEVEL_THEME("slaTheme");
    private String label;

    Analysis(String key) {
        ResourceBundle rb = ResourceBundle.getBundle("loLocalString");
        this.label = rb.getString("dynLayer.label." + key);
    }

    public static boolean isDynamicLayer(String value) {
        for (Analysis that : values()) {
            if (value.equalsIgnoreCase(that.toString())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isVisibleByDefault(String value) {
        for (Analysis that : new Analysis[]{TRADE_AREA, NEIBOURHOOD_WATCH}) {
            if (value.equalsIgnoreCase(that.toString())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return this.label;
    }
}
