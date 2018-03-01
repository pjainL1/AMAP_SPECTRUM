package com.korem.heatmaps;


import com.spinn3r.log5j.Logger;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.MultipleGradientPaint;
import java.awt.RadialGradientPaint;
import java.awt.image.BufferedImage;
import java.awt.image.ByteLookupTable;
import java.awt.image.LookupOp;
import java.awt.image.LookupTable;
import java.awt.image.Raster;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class DensityHeatMap {
    private static final Logger log = Logger.getLogger();
    public static final int TABLE_SIZE = 256;

    private static Map<Integer, BufferedImage> dots;
    
    private BufferedImage colorImage;
    private LookupTable colorTable;
    private LookupOp colorOp;

    private BufferedImage monochromeImage;

    private double baseCount;
    private double baseTrueCount;
    private int baseX;
    private int baseY;
    private double[][] valueMatrix;
    private double[][] trueValueMatrix;
    private int[][] colorMatrix;
    private BlendComposite blendComposite;
    private double modifier;
    private int zoomLevel;
    private int xOffset;
    private int yOffset;
    private double pointRadiusInKM;

    private Color[] colors;
    private LegendItem.Format format;
    private int totalCpt;
    private int pointRadius;
    private int steps;
    private String sponsorCodesDisplayList;

    public DensityHeatMap(){
        
    }
    
    public DensityHeatMap(int width, int height, float alpha, int steps,
            int pointRadius, double modifier, Color[] colors, int zoomLevel,
            int xOffset, int yOffset, double pointRadiusInKM, LegendItem.Format format,
            String codesDisplayList) {
        this.modifier = modifier;
        this.colors = colors;
        this.zoomLevel = zoomLevel;
        this.pointRadiusInKM = pointRadiusInKM;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.format = format;
        this.pointRadius = pointRadius;
        this.steps = steps;
        this.sponsorCodesDisplayList = codesDisplayList;

        if (dots == null) {
            dots = new HashMap<Integer, BufferedImage>();
        }
        
        colorImage = createEvenlyDistributedGradientImage(new Dimension(
            256, 1), colors);
        colorTable = createColorLookupTable(colorImage, alpha);
        colorOp = new LookupOp(colorTable, null);

        monochromeImage = createImage(width, height);
        makeTransparent(monochromeImage);

        valueMatrix = new double[width + pointRadius * 4][height + pointRadius * 4];
        trueValueMatrix = new double[width + pointRadius * 4][height + pointRadius * 4];
        colorMatrix = new int[width + pointRadius * 4][height + pointRadius * 4];
        blendComposite = new BlendComposite(new IBlendListener() {
            @Override
            public void blended(int x, int y, int pixel, int resultPixel) {
                x += baseX;
                y += baseY;
                valueMatrix[x][y] += baseCount;
                trueValueMatrix[x][y] += baseTrueCount;
                colorMatrix[x][y] = resultPixel;
            }
            @Override
            public void value(int x, int y) {
                x += baseX;
                y += baseY;
                if (x >= 0 && y >= 0) {
                    valueMatrix[x][y] += baseCount;
                    trueValueMatrix[x][y] += baseTrueCount;
                }
            }
        }, steps);
    }

    public void setTotalCpt(int totalCpt) {
        this.totalCpt = totalCpt;
    }

    public int getZoomLevel() {
        return zoomLevel;
    }

    public int getXOffset() {
        return xOffset;
    }

    public int getYOffset() {
        return yOffset;
    }

    private BufferedImage createImage(int width, int height) {
        return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    }

    private void makeTransparent(BufferedImage img) {
        Graphics2D g = (Graphics2D)img.createGraphics();
        g.setColor(new Color(255, 255, 255, 255));
        g.setBackground(new Color(255, 255, 255, 255));
        g.clearRect(0, 0, img.getWidth(), img.getHeight());
    }

    /**
     * Creates the color lookup table from an image
     *
     * @param im
     * @return
     */
    private LookupTable createColorLookupTable(BufferedImage im,
            float alpha) {
        Raster imageRaster = im.getData();
        double sampleStep = 1D * im.getWidth() / TABLE_SIZE;
        
        byte[][] byteColorTable = new byte[4][TABLE_SIZE];
        int[] pixel = new int[1];
        Color c;

        for (int i = 0; i < TABLE_SIZE; ++i) {
            imageRaster.getDataElements((int) (i * sampleStep), 0, pixel);

            c = new Color(pixel[0]);

            byteColorTable[0][i] = (byte) c.getRed();
            byteColorTable[1][i] = (byte) c.getGreen();
            byteColorTable[2][i] = (byte) c.getBlue();
            if (i >= 250) {
                byteColorTable[3][i] = (byte) 0;
            } else {
                byteColorTable[3][i] = (byte) (alpha * 0xff);
            }
        }

        LookupTable lookupTable = new ByteLookupTable(0, byteColorTable);

        return lookupTable;
    }

    private BufferedImage createEvenlyDistributedGradientImage(
            Dimension size, Color[] colors) {
        BufferedImage im = createImage(size.width, size.height);
        Graphics2D g = im.createGraphics();

        float[] fractions = new float[colors.length];
        float step = .95f / (colors.length - 2f);

        for (int i = 0; i < colors.length - 1; ++i) {
            fractions[i] = i * step;
        }
        fractions[fractions.length - 1] = 1f;

        LinearGradientPaint gradient = new LinearGradientPaint(
            0, 0, size.width, 1, fractions, colors,
            MultipleGradientPaint.CycleMethod.REPEAT);

        g.setPaint(gradient);
        g.fillRect(0, 0, size.width, size.height);

        g.dispose();
        return im;
    }

    private BufferedImage colorize(LookupOp colorOp) {
        return colorOp.filter(monochromeImage, null);
    }

    private BufferedImage createPlainCircleImage(int size, Color color) {
        BufferedImage im = createImage(size, size);

        Graphics2D g = (Graphics2D) im.getGraphics();

        float center = (float)size / 2;
        RadialGradientPaint paint = new RadialGradientPaint(center,
                center, center, new float[] {0f, 1f}, new Color[] {color, Color.WHITE});
        g.setPaint(paint);

        g.fillOval(0, 0, size, size);

        g.dispose();

        return im;
    }

    public BufferedImage paint() {
        return colorize(colorOp);
    }

    public void addDotImage(int x, int y, double count, double trueCount) {
        if (x >= 0 && y >= 0) {
            baseX = x;
            baseY = y;
            baseCount = count;
            baseTrueCount = trueCount;
            drawDotImage(x, y, count, (Graphics2D) monochromeImage.getGraphics());
        }
    }

    private void drawDotImage(int x, int y, double count, Graphics2D g) {
        double weight = count * modifier;
        int localPointRadius = this.pointRadius;
        if (weight < 1) {
            localPointRadius *= weight;
            if (localPointRadius < 1) {
                localPointRadius = 1;
            }
            weight = 1;
        }
        --weight;
        BufferedImage dot = getDot(localPointRadius * 2, new Color(steps, steps, steps));
        //log.debug(x+";"+y+" : "+weight);
        blendComposite.setWeight((int)weight);
        g.setComposite(blendComposite);
        g.drawImage(dot, null, x - localPointRadius, y - localPointRadius);
    }

    private BufferedImage getDot(int pointRadius, Color color) {
        BufferedImage dot = dots.get(pointRadius);
        if (dot == null) {
            dots.put(pointRadius, dot = createPlainCircleImage(pointRadius, color));
        }
        return dot;
    }

    public Legend getLegend(String comparisonType, String dateType, String type, String dataType) {
        Legend legend = new Legend(pointRadiusInKM, totalCpt, comparisonType,  dateType, type, dataType, format, sponsorCodesDisplayList);
        int greyLevel;
        int intColor;
        int[] dest = new int[4];
        double value, trueValue;
        double max = Double.MAX_VALUE*-1;
        double min = Double.MAX_VALUE;

        // Grey levels associated with colors.
        double[][] localSteps = new double[colors.length][3];
        int step = TABLE_SIZE / colors.length;
        for (int i = localSteps.length - 1; i >= 0; --i) {
            localSteps[i][0] = step * (localSteps.length - i);
            localSteps[i][1] = Double.MAX_VALUE*-1;
            localSteps[i][2] = 0;
        }

        // For each point, find which colors it belongs to.
        for (int i = 0; i < colorMatrix.length; ++i) {
            for (int j = 0; j < colorMatrix[i].length; ++j) {
                intColor = colorMatrix[i][j];
                value = valueMatrix[i][j];
                if (value != 0 && BlendComposite.isValid(intColor)) {
                    BlendComposite.toArray(intColor, dest);
                    greyLevel = dest[0];

                    trueValue = trueValueMatrix[i][j];
                    max = Math.max(max, trueValue);
                    min = Math.min(min, trueValue);

                    for (int k = localSteps.length - 1; k >= 0; --k) {
                        if (greyLevel <= localSteps[k][0]) {
                            localSteps[k][1] = Math.max(localSteps[k][1], trueValue);
                            localSteps[k][2]++;
                            break;
                        }
                    }
                }
            }
        }
        
        log.debug("max...="+max);
        log.debug("min...="+min);
        log.debug(Arrays.deepToString(localSteps));       
        
        int i = 6;
        int validSteps = 0;
        for (int j = i - 1; j >= 1; --j) {
            if (legend.getItems().isEmpty() && localSteps[j][1] == Double.MAX_VALUE*-1) {
                continue;
            }
            validSteps++;
        }
        
        double legendStep = (max - min) / validSteps;
        log.debug("legendStep...="+legendStep);
        for (int j = i - 1; j >= 1; --j) {
            if (legend.getItems().isEmpty() && localSteps[j][1] == Double.MAX_VALUE*-1) {
                continue;
            }
            double nextMax = max - legendStep;
            legend.push(colors[colors.length - 1 - j], (j == 1) ? 0: (int)nextMax, (int)max);
            max = nextMax;
        }
        
        if (legend.getItems().isEmpty()) {
            if (localSteps[0][1] > 0) {
                legend.push(colors[colors.length - 2], 0, (int)localSteps[0][1]);
            }
        }
        return legend;
    }
}

