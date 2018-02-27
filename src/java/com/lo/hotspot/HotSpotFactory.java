package com.lo.hotspot;

import com.korem.heatmaps.AbstractFactory;
import com.korem.heatmaps.DensityHeatMap;
import com.korem.heatmaps.HeatMapRule;
import com.korem.heatmaps.Legend;
import com.korem.heatmaps.LegendItem.Format;
import com.korem.heatmaps.Point;
import com.korem.heatmaps.Properties;
import com.korem.heatmaps.google.GmapsUtils;
import com.korem.map.util.GoogleUtils;
import com.lo.ContextParams;
import com.lo.config.Confs;
import com.lo.hotspot.HotSpotMethod.IParams;
import com.lo.util.DateType;
import com.lo.util.PreparedStatementLogger;
import com.mapinfo.coordsys.CoordSys;
import com.mapinfo.coordsys.CoordTransform;
import com.mapinfo.util.DoubleRect;
import com.spinn3r.log5j.Logger;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import org.apache.commons.dbutils.DbUtils;

/**
 *
 * @author jduchesne
 */
public class HotSpotFactory extends AbstractFactory<HotSpotFactory.OptimizeObject> implements Cloneable {

    private static final Logger log = Logger.getLogger();
    private static final CoordTransform wsg84ToGoogle = new CoordTransform(CoordSys.longLatWGS84, GoogleUtils.GOOGLE_COORDSYS);
    private static final CoordTransform googleToWsg84 = new CoordTransform(GoogleUtils.GOOGLE_COORDSYS, CoordSys.longLatWGS84);
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");

    private String locations;
    private ContextParams contextParams;
    private final Object legendMutex;
    private Legend legend;
    private int totalCpt;
    private final Object stmtMutex;
    private PreparedStatement previousStmt;

    private com.lo.hotspot.HotSpotMethod.IParams params;

    public HotSpotFactory(ContextParams params) {
        legendMutex = new Object();
        contextParams = params;
        stmtMutex = new Object();
    }

    public HotSpotFactory createClone() {
        try {
            return (HotSpotFactory) this.clone();
        } catch (CloneNotSupportedException ex) {
            log.debug("Error cloning HotSpotFactory:" + ex);
            return this;
        }
    }

    public DensityHeatMap create(int width, int height, float alpha, final double xmin,
            final double ymin, final double xmax, final double ymax, final HeatMapRule rule,
            int colorSteps, Color[] colors, int zoomLevel, List<Integer> selectedCodes)
            throws Exception {
        synchronized (legendMutex) {
            legend = null;
        }
        if (params.dataType() == null || params.type() == null) {
            return null;
        }
        totalCpt = 0;
        DensityHeatMap heatMap = null;
        try {
            String query = HotSpotQueries.get().getQuery(params, locations, selectedCodes);
            if (DateType.valueOf(params.dateType()) == DateType.single) {
                OptimizeObject opt = optimizeRequest(
                        query,
                        xmin,
                        ymin,
                        xmax,
                        ymax,
                        width,
                        height,
                        colorSteps,
                        rule.getPointRadiusInKilometers(),
                        HotSpotQueries.ignoreMinimumValuesFilters(HotSpotQueries.QueryType.SINGLE, params)
                );
                //int[] pixelOffsets = GmapsUtils.fromLngLatToPixel(xmin, ymax, zoomLevel, 0, 0);
                heatMap = super.create(width, height, alpha, xmin,
                        ymin, xmax, ymax, rule.getPointRadiusInKilometers(), colorSteps,
                        opt.optimizedGroupbyModifier, colors, zoomLevel, 0/*pixelOffsets[0]*/, 0/*pixelOffsets[1]*/,
                        getFormat(), contextParams.getSponsorCodesDisplayList());
                paintCadran(heatMap, opt);

                createLegend(heatMap);
            } else {
                boolean isCollectorBased = params.dataType() != null & params.dataType().equals("collector");
                heatMap = createGrid(query, zoomLevel, xmin, ymin, xmax, ymax, width, height, isCollectorBased);
            }
            heatMap.setTotalCpt(totalCpt);
        } catch (Exception e) {
            log.debug("", e);
        }
        return heatMap;
    }

    private Format getFormat() {
        return (HotSpotDataType.valueOf(params.dataType()) == HotSpotDataType.spend) ? Format.currency : Format.none;
    }

    private void setStmtParams(String query, PreparedStatement stmt, double xmin, double ymin, double xmax, double ymax, int width, int height, boolean ignoreFilters) throws Exception {
        List<Object> stmtparams = new ArrayList<>();
        setBaseStmtParams(true, stmtparams, stmt, xmin, ymin, xmax, ymax, width, height, ignoreFilters);
        if (DateType.valueOf(params.dateType()) == DateType.comparison) {
            setBaseStmtParams(false, stmtparams, stmt, xmin, ymin, xmax, ymax, width, height, ignoreFilters);
        }
        PreparedStatementLogger.log(log, query, stmtparams.toArray());
    }

    private void setBaseStmtParams(boolean single, List<Object> stmtparams, PreparedStatement stmt, double xmin, double ymin, double xmax, double ymax, int width, int height, boolean ignoreFilters) throws Exception {
        Date startDate = new Date(DATE_FORMAT.parse(single ? params.from() : params.compareFrom()).getTime());
        Date endDate = new Date(DATE_FORMAT.parse(single ? params.to() : params.compareTo()).getTime());
        DoubleRect dr = new DoubleRect(xmin, ymin, xmax, ymax);

        wsg84ToGoogle.forward(dr);

        if (DateType.valueOf(params.dateType()) == DateType.single) {
            stmt.setDouble(stmtparams.size() + 1, dr.xmin);
            stmtparams.add(dr.xmin);
            stmt.setDouble(stmtparams.size() + 1, dr.xmax);
            stmtparams.add(dr.xmax);
            stmt.setDouble(stmtparams.size() + 1, dr.xmin);
            stmtparams.add(dr.xmin);
            stmt.setInt(stmtparams.size() + 1, width);
            stmtparams.add(width);
            stmt.setInt(stmtparams.size() + 1, height);
            stmtparams.add(height);
            stmt.setDouble(stmtparams.size() + 1, dr.ymin);
            stmtparams.add(dr.ymin);
            stmt.setDouble(stmtparams.size() + 1, dr.ymax);
            stmtparams.add(dr.ymax);
            stmt.setDouble(stmtparams.size() + 1, dr.ymin);
            stmtparams.add(dr.ymin);
            stmt.setInt(stmtparams.size() + 1, height);
            stmtparams.add(height);
        }

        if (HotSpotType.valueOf(params.type()) != HotSpotType.airMiles) {
            stmt.setDate(stmtparams.size() + 1, startDate);
            stmtparams.add(startDate);
            stmt.setDate(stmtparams.size() + 1, endDate);
            stmtparams.add(endDate);
        }

        setBounds(stmt, stmtparams.size(), dr.xmin, dr.ymin, dr.xmax, dr.ymax, stmtparams);

        if (!ignoreFilters) {
            if (null != params.minTransactions() || null != params.minSpend() || null != params.minUnit()) {
                if (DateType.valueOf(params.dateType()) != DateType.comparison){
                    stmt.setDate(stmtparams.size() + 1, startDate);
                    stmtparams.add(startDate);
                    stmt.setDate(stmtparams.size() + 1, endDate);
                    stmtparams.add(endDate);
                    if (params.minSpend() != null) {
                        addParam(stmt, stmtparams, null != params.minSpend() ? params.minSpend() : 0);
                    }
                    
                    if (params.minUnit() != null) {
                        addParam(stmt, stmtparams, null != params.minUnit() ? params.minUnit() : 0);
                    }
                    
                    if (params.minTransactions() != null) {
                        addParam(stmt, stmtparams, null != params.minTransactions() ? params.minTransactions() : 0);
                    }
                } else {
                    addParam(stmt, stmtparams, startDate);
                    addParam(stmt, stmtparams, endDate);
                    
                    // add dates again for second query
                    addParam(stmt, stmtparams, new Date(DATE_FORMAT.parse(single ? params.compareFrom(): params.from()).getTime()));
                    addParam(stmt, stmtparams, new Date(DATE_FORMAT.parse(single ? params.compareTo(): params.to()).getTime()));
                    
                    // insert each arg 2 times
                    if (params.minSpend() != null) {
                        addParam(stmt, stmtparams, null != params.minSpend() ? params.minSpend() : 0, 2);
                    }
                    
                    if (params.minUnit() != null) {
                        addParam(stmt, stmtparams, null != params.minUnit() ? params.minUnit() : 0, 2);
                    }
                    
                    if (params.minTransactions() != null) {
                        addParam(stmt, stmtparams, null != params.minTransactions() ? params.minTransactions() : 0, 2);
                    }
                }
            }
        }
    }
    
    private void addParam(PreparedStatement stmt, List<Object> stmtparams, Date value) throws SQLException {
        stmt.setDate(stmtparams.size() + 1, value);
        stmtparams.add(value);
    }
    
    private void addParam(PreparedStatement stmt, List<Object> stmtparams, int value) throws SQLException {
        addParam(stmt, stmtparams, value, 1);
    }
    
    private void addParam(PreparedStatement stmt, List<Object> stmtparams, int value, int repeat) throws SQLException {
        for (int i = 0; i < repeat; i++) {
            stmt.setInt(stmtparams.size() + 1, value);
            stmtparams.add(value);
        }
    }

    private OptimizeObject optimizeRequest(String query, double xmin, double ymin, double xmax, double ymax, int width, int height, int colorSteps, double pointRadiusInKM, boolean ignoreFilters) throws Exception {
        long time = System.currentTimeMillis();
        int pix = 2 * getPointRadiusInPixels(width, height, xmin, ymin, xmax, ymax, pointRadiusInKM);
        double[][] radiusCount = new double[width / pix + 1][height / pix + 1];
        double min = Double.MAX_VALUE;
        OptimizeObject opt = new OptimizeObject();
        Connection connection = Properties.get().getDatasource(contextParams).getConnection();
        PreparedStatement stmt;
        synchronized (stmtMutex) {
            try {
                if (previousStmt != null) {
                    previousStmt.cancel();
                }
            } catch (SQLException e) {
            }
            stmt = previousStmt = connection.prepareStatement(query);
            stmt.setFetchSize(1000);
        }
        try {
            setStmtParams(query, stmt, xmin, ymin, xmax, ymax, width, height, ignoreFilters);
            for (double[] radiusCount1 : radiusCount) {
                for (int k = 0; k < radiusCount1.length; k++) {
                    radiusCount1[k] = 0;
                }
            }
            try (ResultSet rs = stmt.executeQuery()) {//loop and cache all results
                while (rs.next()) {
                    int px = rs.getInt("px");
                    int py = rs.getInt("py");
                    double count = rs.getDouble("count");
                    totalCpt += rs.getInt("collector");
                    min = Math.min(min, count);
                    opt.cache.add(new double[]{px, py, count});
                }
            }
            //min value will be used to offset count
            if (min < 0) {
                opt.trueCountOffset = min * -1;
            }
            for (Iterator<double[]> it = opt.cache.iterator(); it.hasNext();) {
                double[] values = it.next();
                if (min < 0) {//if we have values less than 0, need to offset all values by the min
                    values[2] += opt.trueCountOffset;
                }
                if (values[2] == 0) {//we don't want 0 values to be used in heat map algo, offset or not
                    it.remove();//safe remove while looping
                } else {//in the same loop we calculate the sum by radius zone (approx)
                    radiusCount[(int)values[0] / pix][(int)values[1] / pix] += values[2];
                }
            }
            double maxCountByRadius = 0;
            for (double[] radiusCount1 : radiusCount) {// what is the max of sum of all radius zones
                for (int k = 0; k < radiusCount1.length; k++) {
                    maxCountByRadius = Math.max(maxCountByRadius, radiusCount1[k]);
                }
            }
            opt.optimizedGroupbyModifier = (double) colorSteps / maxCountByRadius;
            log.info("Optimization (and cached raw data) done... time: " + (System.currentTimeMillis() - time));
            return opt;
        } finally {
            DbUtils.closeQuietly(stmt);
            DbUtils.closeQuietly(connection);
            synchronized (stmtMutex) {
                if (previousStmt == stmt) {
                    previousStmt = null;
                }
            }
        }
    }

    public Legend getLastHeatMapLegend() {
        synchronized (legendMutex) {
            if (legend == null) {
                try {
                    legendMutex.wait(20000);
                } catch (Exception e) {
                }
            }
            return legend;
        }
    }

    private void createLegend(final DensityHeatMap heatMap) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                synchronized (legendMutex) {
                    legend = heatMap.getLegend(params.hotspotComparisonType(), params.dateType(), params.type(), params.dataType());
                    legendMutex.notifyAll();
                }
            }
        }).start();
    }

    private void paintCadran(DensityHeatMap heatMap, OptimizeObject opt)
            throws Exception {
        long start = System.currentTimeMillis();
        log.debug("row count: " + opt.cache.size());
        update(heatMap, opt);
        log.debug("total time: " + (System.currentTimeMillis() - start));
    }

    @Override
    protected Point nextPoint(DensityHeatMap heatMap, OptimizeObject opt) {
        if (opt.cacheIndex >= opt.cache.size()) {
            opt.cache.clear();
            return null;
        }
        double[] values = opt.cache.get(opt.cacheIndex);
        opt.cacheIndex++;
        return new Point((int)values[0], (int)values[1], values[2], values[2] - opt.trueCountOffset);
    }

    private void setBounds(PreparedStatement stmt, int index, double xmin, double ymin, double xmax, double ymax, List<Object> params)
            throws SQLException {
        stmt.setDouble(++index, xmin);
        params.add(xmin);
        stmt.setDouble(++index, ymin);
        params.add(ymin);
        stmt.setDouble(++index, xmax);
        params.add(xmax);
        stmt.setDouble(++index, ymax);
        params.add(ymax);
    }

    void setOptions(IParams params) {
        this.params = params;
        setLocations(params.locations());
    }

    void setDates(SetDatesMethod.IParams params) {

    }

    public void setLocations(String locations) {
        this.locations = locations;
    }

    static class OptimizeObject {

        int cacheIndex = 0;
        List<double[]> cache = new ArrayList<>();
        double trueCountOffset = 0;
        double optimizedGroupbyModifier = 0;
    }

    private double calculateRelativeGridValue(double value) {
        if (Confs.HOTSPOT_CONFIG.queryCompareSpread().equalsIgnoreCase("sqrt")) {
            return Math.sqrt(value);
        } else if (Confs.HOTSPOT_CONFIG.queryCompareSpread().equalsIgnoreCase("log")) {
            return Math.log(value);
        } else {
            return value;
        }
    }

    private double revertRelativeGridValue(double value) {
        if (Confs.HOTSPOT_CONFIG.queryCompareSpread().equalsIgnoreCase("sqrt")) {
            return Math.pow(value, 2);
        } else if (Confs.HOTSPOT_CONFIG.queryCompareSpread().equalsIgnoreCase("log")) {
            return Math.exp(value);
        } else {
            return value;
        }
    }

    private Color buildGridColor(double max, double value, Color maxColor, Color neutralColor) {
        double ratioColor = max == 0 || value == 0 ? 0 : (calculateRelativeGridValue(Math.abs(value)) / calculateRelativeGridValue(max));
        int red = (int) Math.round(maxColor.getRed() - ((maxColor.getRed() - neutralColor.getRed()) * (1 - ratioColor)));
        int green = (int) Math.round(maxColor.getGreen() - ((maxColor.getGreen() - neutralColor.getGreen()) * (1 - ratioColor)));
        int blue = (int) Math.round(maxColor.getBlue() - ((maxColor.getBlue() - neutralColor.getBlue()) * (1 - ratioColor)));
        return new Color(red, green, blue);
    }

    private DensityHeatMap createGrid(String query, int zoomLevel, double xmin, double ymin, double xmax, double ymax, final int width, final int height, boolean isCollectorBased) throws Exception {
        //legend

        long time = System.currentTimeMillis();
        DoubleRect bounds = new DoubleRect(xmin, ymin, xmax, ymax);
        wsg84ToGoogle.forward(bounds);
        DoubleRect gutter = new DoubleRect(xmin, ymin, xmax, ymax);
        wsg84ToGoogle.forward(gutter);

        //want to allow dynamic change of the config, thus not static
        int zoomLevelThreshold = Confs.HOTSPOT_CONFIG.queryCompareZoomLevelThreshold();
        Color increaseColor = new Color(Confs.HOTSPOT_CONFIG.queryCompareIncreaseColorR(), Confs.HOTSPOT_CONFIG.queryCompareIncreaseColorG(), Confs.HOTSPOT_CONFIG.queryCompareIncreaseColorB());
        Color decreaseColor = new Color(Confs.HOTSPOT_CONFIG.queryCompareDecreaseColorR(), Confs.HOTSPOT_CONFIG.queryCompareDecreaseColorG(), Confs.HOTSPOT_CONFIG.queryCompareDecreaseColorB());
        Color neutralColor = new Color(Confs.HOTSPOT_CONFIG.queryCompareNeutralColorR(), Confs.HOTSPOT_CONFIG.queryCompareNeutralColorG(), Confs.HOTSPOT_CONFIG.queryCompareNeutralColorB());

        int tileSize = Confs.HOTSPOT_CONFIG.queryCompareTileSize();
        if (zoomLevel > zoomLevelThreshold) {
            tileSize = Confs.HOTSPOT_CONFIG.queryCompareTileSize() * (int) Math.round(Math.pow(2, zoomLevel - zoomLevelThreshold));
        }
        final double ratio = ((GmapsUtils.GREATCIRCLE_D) / ((Math.pow(2, zoomLevel)) * 256D)) * ((double) tileSize);
        double wide = ratio / 1000;
        if (wide > 1) {
            wide = Math.round(wide);
        } else {
            wide = ((double) Math.round(wide * 10)) / 10D;
        }

        log.debug("bounds... " + gutter);
        //gutter for partial tile outside bbox
        double tile0x = Math.floor((gutter.xmin + (GmapsUtils.GREATCIRCLE_D / 2D)) / ratio);
        double tile0y = Math.floor((GmapsUtils.GREATCIRCLE_D - (gutter.ymax + (GmapsUtils.GREATCIRCLE_D / 2D))) / ratio);
        double tilenx = Math.ceil((gutter.xmax + (GmapsUtils.GREATCIRCLE_D / 2D)) / ratio);
        double tileny = Math.ceil((GmapsUtils.GREATCIRCLE_D - (gutter.ymin + (GmapsUtils.GREATCIRCLE_D / 2D))) / ratio);
        log.debug("gutter..  tile0x=" + tile0x + ", tile0y=" + tile0y + ", tilenx=" + tilenx + ", tileny=" + tileny);
        gutter.xmin = (tile0x * ratio) - (GmapsUtils.GREATCIRCLE_D / 2D);
        gutter.ymax = (GmapsUtils.GREATCIRCLE_D / 2D) - (tile0y * ratio);
        gutter.xmax = ((tilenx + 1) * ratio) - (GmapsUtils.GREATCIRCLE_D / 2D);
        gutter.ymin = (GmapsUtils.GREATCIRCLE_D / 2D) - ((tileny + 1) * ratio);
        log.debug("bounds... " + gutter);
        googleToWsg84.forward(gutter);

        query = query.replaceAll("%ratio%", ratio + "");
        
        double imagex = ((bounds.xmin + (GmapsUtils.GREATCIRCLE_D / 2D)) / GmapsUtils.GREATCIRCLE_D) * ((Math.pow(2, zoomLevel)) * 256D);
        double imagey = ((GmapsUtils.GREATCIRCLE_D - (bounds.ymax + (GmapsUtils.GREATCIRCLE_D / 2D))) / GmapsUtils.GREATCIRCLE_D) * ((Math.pow(2, zoomLevel)) * 256D);
        final BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();
        double max = 0;
        totalCpt = 0;

        Connection connection = Properties.get().getDatasource(contextParams).getConnection();
        PreparedStatement stmt;
        synchronized (stmtMutex) {
            try {
                if (previousStmt != null) {
                    log.debug(String.format("Canceling statement %s. Hotspot request was canceled.", previousStmt));
                    previousStmt.cancel();
                }
            } catch (SQLException e) {
            }
            stmt = previousStmt = connection.prepareStatement(query);
            log.debug(String.format("New statement %s", stmt));
            stmt.setFetchSize(1000);
        }
        try {
            setStmtParams(query, stmt, gutter.xmin, gutter.ymin, gutter.xmax, gutter.ymax, width, height, isCollectorBased);
            try (ResultSet rs = stmt.executeQuery()) {
                int i = 0;
                while (rs.next()) {
                    if (i++ == 0) {
                        max = Math.max(Math.abs(rs.getDouble("max")), Math.abs(rs.getDouble("min")));
                    }
                    totalCpt += rs.getInt("collector");
                    double value = rs.getDouble("count");
                    double x = rs.getDouble("tilex") * tileSize;
                    double y = rs.getDouble("tiley") * tileSize;
                    Color maxColor = value < 0 ? decreaseColor : increaseColor;
                    g2d.setColor(buildGridColor(max, value, maxColor, neutralColor));
                    g2d.fillRect((int) Math.round(x - imagex), (int) Math.round(y - imagey), tileSize, tileSize);
                }
                if (max == 0) {
                    log.debug(String.format("MAX is ZERO. !! (%s)", stmt));
                }
            }
            log.debug("Grid creation done... time: " + (System.currentTimeMillis() - time) + " ms");
            legend = new Legend(wide, totalCpt, params.hotspotComparisonType(), params.dateType(), params.type(), params.dataType(), getFormat(), contextParams.getSponsorCodesDisplayList());
            if (max != 0) {//need to create range according to the spread method but we display real steps...
                double maxMet = calculateRelativeGridValue(max);
                double step = maxMet * 2 / 5;
                if (!params.hotspotComparisonType().equalsIgnoreCase("decline")) {
                    legend.push(increaseColor, (int) Math.round(revertRelativeGridValue(maxMet - step)), (int) Math.round(revertRelativeGridValue(maxMet)));
                    legend.push(buildGridColor(max, revertRelativeGridValue(maxMet - step), increaseColor, neutralColor), (int) Math.round(revertRelativeGridValue(maxMet - (step * 2))), (int) Math.round(revertRelativeGridValue(maxMet - step)));
                }
                if (params.hotspotComparisonType().equalsIgnoreCase("growth")) {
                    legend.push(neutralColor, 0, (int) Math.round(revertRelativeGridValue(step / 2)));
                } else if (params.hotspotComparisonType().equalsIgnoreCase("decline")) {
                    legend.push(neutralColor, (int) Math.round(revertRelativeGridValue(step / 2)) * -1, 0);
                } else {
                    legend.push(neutralColor, (int) Math.round(revertRelativeGridValue(step / 2)) * -1, (int) Math.round(revertRelativeGridValue(step / 2)));
                }
                if (!params.hotspotComparisonType().equalsIgnoreCase("growth")) {
                    legend.push(buildGridColor(max, revertRelativeGridValue(maxMet - step), decreaseColor, neutralColor), (int) Math.round(revertRelativeGridValue(maxMet - step)) * -1, (int) Math.round(revertRelativeGridValue(maxMet - (step * 2))) * -1);
                    legend.push(decreaseColor, (int) Math.round(revertRelativeGridValue(maxMet)) * -1, (int) Math.round(revertRelativeGridValue(maxMet - step)) * -1);
                }
            } else {
                legend.push(neutralColor, 0, 0);
            }
            return new DensityHeatMap() {
                @Override
                public BufferedImage paint() {
                    return img;
                }
            };
        } finally {
            log.debug(String.format("Closing statement %s", stmt));
            DbUtils.closeQuietly(stmt);
            DbUtils.closeQuietly(connection);
            synchronized (stmtMutex) {
                if (previousStmt == stmt) {
                    previousStmt = null;
                }
            }
        }
    }

    public static void main(String[] a) throws Exception {
        System.out.println(Math.sqrt(-5));
        System.out.println(Math.exp(Math.log(-5)));
    }
}
