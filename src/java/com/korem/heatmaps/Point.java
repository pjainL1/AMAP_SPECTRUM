package com.korem.heatmaps;

/**
 *
 * @author jduchesne
 */
public class Point {

    private int x;
    private int y;
    private double count;
    private double trueCount;

    public Point(int x, int y, double count) {
        this.x = x;
        this.y = y;
        this.count = count;
        this.trueCount = count;
    }
    
    public Point(int x, int y, double count, double trueCount) {
        this(x, y, count);
        this.trueCount = trueCount;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    double getCount() {
        return count;
    }
    
    double getTrueCount() {
        return trueCount;
    }
    
}
