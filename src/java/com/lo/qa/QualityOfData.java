/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lo.qa;

import com.lo.ContextParams;
import java.util.ResourceBundle;

/**
 *
 * @author YDumais
 */
public class QualityOfData {

    public enum Rule {

        insufficientForTradeArea, lowForTradeArea, insufficient, ruralPC;

        public String getKey() {
            return "qarules." + this.toString();
        }
    }
    private static final ResourceBundle rb = ResourceBundle.getBundle("loLocalString");

    public static void set(ContextParams cp, Rule rule) {
        cp.getQualityOfDataRules().add(rb.getString(rule.getKey()));
    }
}
