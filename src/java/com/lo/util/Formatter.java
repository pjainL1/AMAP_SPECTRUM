/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lo.util;

import java.text.NumberFormat;
import java.util.Locale;

/**
 *
 * @author YDumais
 */
public class Formatter {

    protected NumberFormat percentNumberFormat;
    protected NumberFormat currencyNumberFormat;
    protected NumberFormat numberNumberFormat;

    public Formatter() {}

    public NumberFormat getCurrencyNumberFormat() {
        if (currencyNumberFormat == null) {
            currencyNumberFormat = NumberFormat.getCurrencyInstance(Locale.CANADA);
            currencyNumberFormat.setMaximumFractionDigits(0);
        }
        return currencyNumberFormat;
    }

    public NumberFormat getNumberNumberFormat() {
        if (numberNumberFormat == null) {
            numberNumberFormat = NumberFormat.getNumberInstance(Locale.CANADA);
            numberNumberFormat.setMaximumFractionDigits(2);
        }
        return numberNumberFormat;
    }

    public NumberFormat getPercentNumberFormat() {
        if (percentNumberFormat == null) {
            percentNumberFormat = NumberFormat.getPercentInstance(Locale.CANADA);
        }
        return percentNumberFormat;
    }
}
