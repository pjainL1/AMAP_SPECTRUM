package com.korem.heatmaps;

/**
 *
 * @author jduchesne
 */
public class HeatMapRule {

    private double pointRadiusInKilometers;
    private double groupByModifier;
    private int groupByRoundModifier;

    public HeatMapRule(double pointRadiusInKilometers, double groupByModifier, int groupByRoundModifier) {
        this.pointRadiusInKilometers = pointRadiusInKilometers;
        this.groupByModifier = groupByModifier;
        this.groupByRoundModifier = groupByRoundModifier;
    }

    public double getPointRadiusInKilometers() {
        return pointRadiusInKilometers;
    }

    public double getGroupByModifier() {
        return groupByModifier;
    }

    public int getGroupByRoundModifier() {
        return groupByRoundModifier;
    }
}
