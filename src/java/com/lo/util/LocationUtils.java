/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lo.util;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 *
 * @author YDumais
 */
public class LocationUtils {

    public static List<Double> parseList(String list) {
        List<Double> result = new ArrayList<Double>();
        if (list != null && list.endsWith(",")) {
            list = list.substring(0, list.length() - 1);
            StringTokenizer tokenizer = new StringTokenizer(list, ",");
            while (tokenizer.hasMoreTokens()) {
                result.add(Double.valueOf(tokenizer.nextToken()));
            }
        }
        return result;
    }

    public static List<String> parseStringList(String list) {
        List<String> result = new ArrayList<String>();
        if (list != null && list.endsWith(",")) {
            list = list.substring(0, list.length() - 1);
            StringTokenizer tokenizer = new StringTokenizer(list, ",");
            while (tokenizer.hasMoreTokens()) {
                result.add(String.valueOf(tokenizer.nextToken()));
            }
        }
        return result;
    }
}
