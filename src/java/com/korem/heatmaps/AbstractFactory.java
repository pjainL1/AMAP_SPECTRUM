package com.korem.heatmaps;

import com.spinn3r.log5j.Logger;
import java.awt.Color;

/**
 *
 * @author jduchesne
 */
public abstract class AbstractFactory<T> {

    private static final Logger log = Logger.getLogger();
    private static final double EQUATOR_KILOMETERS = 111.325;

    public DensityHeatMap create(int width, int height, float alpha,
            double xmin, double ymin, double xmax, double ymax,
            double pointRadiusInKilometers, int colorSteps, double groupByModifier,
            Color[] colors, int zoomLevel, int xOffset, int yOffset,
            LegendItem.Format format, String codesDisplayList)
            throws Exception {
        int pointRadius = getPointRadiusInPixels(width, height, xmin, ymin, xmax, ymax, pointRadiusInKilometers);
        return create(width, height, alpha, xmin, ymin, xmax, ymax, pointRadius, colorSteps,
                groupByModifier, colors, zoomLevel, xOffset, yOffset, pointRadiusInKilometers,
                format, codesDisplayList);
    }

    public DensityHeatMap create(int width, int height, float alpha,
            double xmin, double ymin, double xmax, double ymax,
            int pointRadius, int colorSteps, double groupByModifier,
            Color[] colors, int zoomLevel, int xOffset, int yOffset,
            double pointSizeInKilometers, LegendItem.Format format,
            String codesDisplayList)
            throws Exception {

        log.debug("\n\n\nwidth: %s\nheight: %s\nalpha: %s\ncolorSteps: %s\npointRadius: %s\ngroupByModifier: %s\nzoomLevel: %s\n"
                + "xOffset: %s\nyOffset: %s\n", width, height, alpha, colorSteps, pointRadius, groupByModifier, zoomLevel,
                xOffset, yOffset);
        return new DensityHeatMap(width, height, alpha,
                colorSteps, pointRadius, groupByModifier, colors, zoomLevel, xOffset, yOffset,
                pointSizeInKilometers, format, codesDisplayList);
    }

    protected void update(DensityHeatMap heatMap, T misc) {
        Point point = null;
        while ((point = nextPoint(heatMap, misc)) != null) {
            heatMap.addDotImage(point.getX(), point.getY(), point.getCount(), point.getTrueCount());
        }
    }

    /**
     * @return null if there is no more point.
     */
    protected abstract Point nextPoint(DensityHeatMap heatMap, T misc);

    protected static int getPixelsForOneDegree(double xmin, double xmax, int width) {
        return (int) (width / (xmax - xmin));
    }

    protected static double getKilometersForOneDegree(double ymin, double ymax) {
        double latitude = (ymin + ymax) / 2;
        return Math.cos(Math.toRadians(latitude)) * EQUATOR_KILOMETERS;
    }
    
    protected static int getPointRadiusInPixels(int width, int height, double xmin, double ymin, double xmax, double ymax, double pointRadiusInKilometers){
        return Math.max(2, (int) (getPixelsForOneDegree(xmin, xmax, width)
                / getKilometersForOneDegree(ymin, ymax) * pointRadiusInKilometers));
    }
}
