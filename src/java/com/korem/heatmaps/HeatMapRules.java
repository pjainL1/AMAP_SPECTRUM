package com.korem.heatmaps;

import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author jduchesne
 */
public class HeatMapRules {

    private Map<Integer, HeatMapRule> rules;

    public HeatMapRules() {
        rules = new TreeMap<Integer, HeatMapRule>();
    }

    public void addRule(int zoomLevel, double pointRadiusInKilometers, double groupByModifier, int groupByRoundModifier) {
        rules.put(zoomLevel, new HeatMapRule(pointRadiusInKilometers, groupByModifier, groupByRoundModifier));
    }

    public HeatMapRule getRule(double zoomLevel) {
        HeatMapRule rule = null;
        for (Map.Entry<Integer, HeatMapRule> entry : rules.entrySet()) {
            rule = entry.getValue();
            if (zoomLevel <= entry.getKey()) {
                break;
            }
        }
        return rule;
    }
}
