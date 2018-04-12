/*
 * PDFGenerator.java
 */
package com.lo.pdf;

import com.korem.SessionlessLanguageManager;
import com.korem.heatmaps.DensityHeatMap;
import com.korem.heatmaps.HeatMapRule;
import com.korem.heatmaps.HeatMapRules;
import com.korem.heatmaps.Legend;
import com.korem.heatmaps.LegendItem;
import com.korem.heatmaps.Properties;
import com.korem.map.ws.client.MapService;
import com.korem.openlayers.kms.MapProvider;
import com.korem.spectrum.DriveTimePolygon;
import com.lo.Config;
import com.lo.ContextParams;
import com.lo.analysis.Analysis;
import com.lo.analysis.AnalysisControler;
import com.lo.analysis.tradearea.TradeArea;
import com.lo.analysis.tradearea.TradeAreaControler;
import com.lo.analysis.tradearea.TradeAreaMethod.IParams;
import com.lo.config.Confs;
import com.lo.db.dao.AirMilesDAO;
import com.lo.db.dao.LocationDAO;
import com.lo.db.dao.SummaryReportDAO;
import com.lo.db.dao.TransactionDAO;
import com.lo.db.om.Location;
import com.lo.db.om.SponsorGroup;
import com.lo.db.om.SummaryReport;
import com.lo.db.om.Transaction;
import com.lo.db.proxy.PostalCodeProxy;
import com.lo.hotspot.HotSpotFactory;
import com.lo.layer.PostalCodeLayerManager;
import com.lo.pdf.data.DataWriter;
import com.lo.pdf.data.DecayReportWriter;
import com.lo.pdf.data.SummaryReportWriter;
import com.lo.pdf.data.SummaryReportWriter.ReportType;
import com.lo.util.DateType;
import com.lo.util.Formatter;
import com.lo.util.WSClient;
import com.lo.util.WSClientLone;
import com.lo.util.WSClientUtil;
import com.lo.web.Apply.ProgressListener;
import com.lo.web.GetHeatMap;
import com.lowagie.text.BadElementException;
import com.lowagie.text.Cell;
import com.lowagie.text.Chapter;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import com.spinn3r.log5j.Logger;
import com.vividsolutions.jts.geom.Geometry;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.imageio.ImageIO;
import com.lowagie.text.Image;
import com.mapinfo.coordsys.CoordSys;
import com.mapinfo.coordsys.CoordTransform;
import com.mapinfo.util.DoublePoint;
import java.io.BufferedInputStream;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Arrays;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.servlet.http.HttpSession;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.util.JSONTokener;
import org.apache.commons.lang.StringUtils;

/**
 * @author ydumais
 */
public class PDFGenerator extends PdfPageEventHelper implements Runnable {

    public static final String PLACEMARK_TEMP_LAYER_NAME = "placemark";
    public static final String ZOOM_UNIT = "m";
    private static final int HOTSPOT_LEGEND_MIN_WIDTH = 260;

    private int mapImageWidth;
    private int mapImageHeight;
    private SessionlessLanguageManager lm;

    private double getAdjustementFactor() {
        double result = 1.0;
        double factor = (double) reportParams.width() / mapImageWidth;
        if (factor < 1) {
            result = 1.0;
        } else if (factor >= 2) {
            result = 2.0;
        } else {
            double decimals = factor - 1;
            decimals = decimals * 2;
            result = Math.max(2.0, (1 + decimals));
        }
        return result;
    }

    private static class CancelException extends Exception {
    }

    private byte[] getImage(MapService mapService) throws RemoteException {

        String[] features;
        if (reportParams.googleType().equals("grey") || reportParams.googleType().equals("night")) {
            features = getMapStyleFeatures(reportParams.googleType());
        } else {
            features = new String[]{};
        }
        byte[] bytes = WSClientLone.getMapService().getImage(
                mapInstanceKey,
                "image/png",
                mapImageWidth,
                mapImageHeight,
                reportParams.googleType(),
                "true",
                OPACITY,
                Config.getInstance().getGoogleKey(),
                Config.getInstance().getGoogleSignature(),
                features
        );

        return bytes;
    }

    public String[] getMapStyleFeatures(String style) {
        String s;
        if (style.equalsIgnoreCase("grey")) {
            s = Confs.MAP_STYLES.customStyle().toString();
        } else {
            s = Confs.MAP_STYLES.nightStyle().toString();
        }

        JSONArray jArr = JSONArray.fromObject(s);

        int size = jArr.size();
        String[] features = new String[size];

        for (int i = 0; i < jArr.size(); i++) {
            String str = "";
            int elemCount = 0;
            JSONObject jObj = jArr.getJSONObject(i);
            if (jObj.containsKey("featureType")) {
                String featureType = jObj.getString("featureType");
                if (elemCount++ > 0) {
                    str += "|";
                }
                str += "feature:" + featureType;
            }

            if (jObj.containsKey("elementType")) {
                if (elemCount++ > 0) {
                    str += "|";
                }
                String elementType = jObj.getString("elementType");
                str += "element:" + elementType;
            }

            JSONArray stylers = jObj.getJSONArray("stylers");
            for (int j = 0; j < stylers.size(); j++) {
                JSONObject rule = stylers.getJSONObject(j);
                for (Object jsonKey : rule.keySet()) {
                    String key = (String) jsonKey;
                    String value = String.valueOf(rule.get(jsonKey));

                    if (elemCount++ > 0) {
                        str += "|";
                    }
                    if (key.equals("color") || key.equals("hue")) {
                        value = value.replace("#", "0x");
                    }
                    str += String.format("%s:%s", key, value);
                }
            }

            features[i] = str;
        }

        return features;

    }

    public enum Type {

        decay, summary
    };
    private static final ResourceBundle rb = ResourceBundle.getBundle("loLocalString");
    private static final float PDF_IMAGE_DPI_RATIO = 72 * 100 / 116;
    private static final double IMAGE_DPI_RATIO = 116d / 72d;
    private static final Logger log = Logger.getLogger();
    private static ResourceBundle conf;
    private static final int OPACITY = 20;
    private static String GOOGLE_KEY = Config.getInstance().getGoogleKey();
    private static final String SPACE = " ";
    private static final String HYPHEN = "-";
    private static final String PERCENT = "%";
    private static final String KM = "km";
    private static final String TO = "to";
    public static final String CONFIG = "com.lo.pdf.config";
    private static final Integer PROGRESS_TITLE = 10;
    private static final Integer PROGRESS_MAP = 20;
    private static final Integer PROGRESS_DATA = 70;
    private static final String LOGO_RELATIVE_PATH = "/main/images/LOGO_LoyaltyOne.png";
    private static final String MARKER_RENDITION_TEMPLATE
            = "<Style><rendition><style symbol-mode=\"image\" transform=\"matrix(1.0 0.0 0.0 1.0 0.0 0.0 )\" symbol-foreground-opacity=\"1.0\" "
            + "symbol-background-opacity=\"1.0\" color-replacement-mode=\"none\"><image href=\"%s\"/></style></rendition></Style>";

    /* warnings */
    private static Integer COLLECTOR_MIN_NUMBER = 100;

    /* pdf parameters */
    private static float SPACING_MAIN = 60f;
    private static float SPACING_SECOND = 15f;
    private static float PARAGRAPH_LEADING = 55f;
    private static int MAP_LEGEND_PADDING = 5;
    private static int MAX_LEGEND_ITEM = 50;

    /* document margin definition */
    private static int MARGIN_LEFT = 50;
    private static int MARGIN_RIGHT = 50;
    private static int MARGIN_TOP = 50;
    private static int MARGIN_BOTTOM = 50;
    private static int FOOTER_LINE_SEPARATOR_OFFSET_FROM_BOTTOM = 0;
    private static int FOOTER_TEXT_OFFSET_FROM_LINE_SEPARATOR = -20;
    private static int BOTTOM_SPACING = 20;
    private static float GUTTER_WIDTH = 10f;
    private static int MAP_TITLE = 25;

    /* document fontSize */
    private static String FONT_NAME = FontFactory.HELVETICA;
    private static int TITLE_FONT_SIZE = 14;
    private static int SUBTITLE_FONT_SIZE = 12;
    private static int SUBTITLE_DATA_FONT_SIZE = 12;
    private static int DATA_FONT_SIZE = 8;
    private static int DATA_BOLD_FONT_SIZE = 8;
    private static int REPORT_TITLE_FONT_SIZE = 12;
    private static int FOOTER_FONT_SIZE = 12;

    /* font collections used in document */
    private Font fontTitle;
    private Font fontSubtitle;
    private Font fontSubtitleData;
    private BaseFont fontFooter;
    private Font fontReportTitle;

    /* PDFGenerator members */
    private String baseHREF;
    private File baseDir;
    private ContextParams contextParams;
    private PDFBean pdf;
    private com.lo.report.ReportMethod.IParams reportParams;
    private com.lo.analysis.tradearea.TradeAreaMethod.IParams taParams;
    //private KPIControler controler;

    /* utilities */
    private Calendar now;
    private DateFormat df;

    /* pdf document members */
    private Rectangle portrait;
    private Rectangle landscape;
    private Rectangle spaceRectangle = new Rectangle(200, 200);
    private Document document;
    private PdfWriter writer;
    /**
     * A template that will hold the total number of pages.
     */
    private PdfTemplate footer;

    /* counters */
    private int chapter = 0;
    private int overallProgress = 0;
    private int steps = 0;

    /* pdf table */
    protected final static int DEFAULT_PADDING = 3;
    protected final static int DEFAULT_BORDER_WIDTH = 0;
    protected final static float DEFAULT_HEADER_GRAY_FILL = 0.85f;
    protected final static float DEFAULT_TOTAL_GRAY_FILL = 0.90f;
    protected final static float DEFAULT_ODD_GRAY_FILL = 0.95f;
    protected final static float DEFAULT_EVEN_GRAY_FILL = 1.0f;
    protected final static int TEXT = 0;
    protected final static int NUMBER = 1;
    protected final static int VOLUME = 0;
    protected final static int PRICE = 2;
    protected final static int COST = 1;
    protected final static int VARIANCE = 2;
    protected final static int MARGIN = 3;
    protected final static int FRACTION_DIGITS = 2;
    protected final static int FRACTION_DIGITS_NO = 0;
    protected final static int DATA_ALIGNEMENT = Element.ALIGN_LEFT;
    protected final static int TEXT_ALIGNEMENT = Element.ALIGN_LEFT;
    protected final static int TEXT_ALIGNEMENT_CENTER = Element.ALIGN_CENTER;
    protected Font fontHeader;
    private String[] locationKeys = null;
    private String[] layers = null;
    private String[] themeLayersIds = null;
    private List<Integer> sponsorKeys;
    private String sponsorKeysList;
    private SponsorGroup selectedSponsorGroup;
    private String mapInstanceKey;
    private ProgressListener listener;
    private Integer singleLocationProgress;
    private boolean mapAlreadyAdded = false;
    private static final String COMPARISON = "comparison";
    //heatmap
    private HotSpotFactory factory;
    private static final float ALPHA = .9f;
    private static HeatMapRules rules = GetHeatMap.createRules();
    private static final String SRS = "epsg:4326";
    DoublePoint projectedPoint = null;
    private String previousLayerId = null;
    private boolean fixedImage = false;
    private boolean firstBatch = true;
    private double BATCH_ZOOM_MODIFIER;
    private HttpSession session;

    /**
     * Create PDFGenerator object, then ready to be started and forgotten. It is
     * possible to follow pdf generation progress via PDFBean.
     *
     * @param baseDir dir to temporarly store pdf file generated (to avoid
     * memory usage problem).
     * @param contextParams a copy or an immutable contextParams, valid for the
     * entire pdf generation.
     * @param baseHREF servlet baseHREF (to locate servlet ressources such as
     * entreprise logo).
     */
    public PDFGenerator(HotSpotFactory factory, ProgressListener listener, HttpSession session, com.lo.report.ReportMethod.IParams reportParams, String baseHREF) {
        PDFGenerator.conf = ResourceBundle.getBundle(CONFIG);

        readProperties();
        this.lm = new SessionlessLanguageManager("en");
        this.session = session;
        this.contextParams = ContextParams.get(session);
        this.sponsorKeysList = contextParams.getSponsorKeysList();
        selectedSponsorGroup = contextParams.getSponsor();
        this.baseDir = contextParams.getTempDir();
        this.baseHREF = baseHREF;
        this.df = new SimpleDateFormat(conf.getString("pdf.dateformat"));
        this.now = Calendar.getInstance(Locale.CANADA);
        this.portrait = PageSize.LEGAL;
        this.landscape = PageSize.LEGAL.rotate();
        this.reportParams = reportParams;
        this.locationKeys = reportParams.locationKeys().split(",");
        this.layers = reportParams.layers().split(",");
        this.themeLayersIds = reportParams.themeLayerIds().split(",");
        this.listener = listener;
        this.factory = factory;
        this.singleLocationProgress = PROGRESS_DATA / locationKeys.length / 2;
        this.BATCH_ZOOM_MODIFIER = Double.valueOf(PDFGenerator.conf.getString("pdf.map.batch.zoom.multiplier"));

        buildBean(this.baseDir);
    }

    /**
     * Start thread.
     */
    @Override
    public void run() {

        try {
            long ts1 = System.currentTimeMillis();

            document = new Document(portrait, MARGIN_LEFT, MARGIN_RIGHT, MARGIN_TOP, MARGIN_BOTTOM);
            writer = PdfWriter.getInstance(document, new FileOutputStream(pdf.getFile()));
            writer.setPageEvent(this);

            document.open();

            duplicateMapSession();

            insertTitlePage();
            updateListener(PROGRESS_TITLE);

            if (!isBatch()) {
                insertMapPage(reportParams.tradearea(), null);
            }
            updateListener(PROGRESS_MAP);

            String report = reportParams.report();
            if (report != null && !"".equals(report)) {
                insertDataPages();
            }

            long ts2 = System.currentTimeMillis();
            log.info("PDF Generation completed in: " + (ts2 - ts1) + " ms.");
        } catch (CancelException ce) {
        } catch (FileNotFoundException ex) {
            log.error("Error writting pdf to file.", ex);

        } catch (DocumentException ex) {
            log.error("Pdf error.", ex);

        } catch (IOException ex) {
            log.error("Error reading config.properties file", ex);

        } catch (Exception ex) {
            log.error("Error creating PDF.", ex);

        } finally {
            document.close();
        }
    }

    /**
     * Initiliaze fonts and document footer. Everything used globally by
     * PdfPageEventHelper overriden methods should be initialized here.
     *
     * @param writer
     * @param document
     */
    @Override
    public void onOpenDocument(PdfWriter writer, Document document) {
        try {
            fontTitle = FontFactory.getFont(FONT_NAME, TITLE_FONT_SIZE, Font.BOLD);
            fontSubtitle = FontFactory.getFont(FONT_NAME, SUBTITLE_FONT_SIZE, Font.BOLD);
            fontSubtitleData = FontFactory.getFont(FONT_NAME, SUBTITLE_DATA_FONT_SIZE, Font.NORMAL);
            fontReportTitle = FontFactory.getFont(FONT_NAME, REPORT_TITLE_FONT_SIZE, Font.BOLD);
            fontFooter = BaseFont.createFont(FONT_NAME, BaseFont.WINANSI, false);
            // initialization of the template
            footer = writer.getDirectContent().createTemplate(100, 100);
            footer.setBoundingBox(new Rectangle(-20, -20, 100, 100));

        } catch (DocumentException ex) {
            log.error("Exception caught onOpenDocument.", ex);
        } catch (IOException ex) {
            log.error("Exception caught onOpenDocument.", ex);
        }
    }

    /**
     * When document is closed, set the total number of pages in the footer
     * template. Since template are drawn once document is completed, all pages
     * will have access to total number of pages.
     *
     * @param writer
     * @param document
     */
    @Override
    public void onCloseDocument(PdfWriter writer, Document document) {
        footer.beginText();
        footer.setFontAndSize(fontFooter, FOOTER_FONT_SIZE);
        footer.setTextMatrix(0, 0);
        footer.showText(Integer.toString(writer.getPageNumber() - 1));
        footer.endText();
    }

    /**
     * Draw footer.
     *
     * @param writer
     * @param document
     */
    @Override
    public void onEndPage(PdfWriter writer, Document document) {
        PdfContentByte cb = writer.getDirectContent();

        cb.saveState();
        cb.moveTo(document.left(), document.bottom() + FOOTER_LINE_SEPARATOR_OFFSET_FROM_BOTTOM);
        cb.lineTo(document.right(), document.bottom() + FOOTER_LINE_SEPARATOR_OFFSET_FROM_BOTTOM);
        cb.setColorStroke(Color.GRAY);
        cb.stroke();
        cb.restoreState();

        cb.saveState();
        cb.setColorFill(Color.gray);
        String text = writer.getPageNumber() + "/";
        float textSize = fontFooter.getWidthPoint(text, FOOTER_FONT_SIZE);
        cb.beginText();
        cb.setFontAndSize(fontFooter, FOOTER_FONT_SIZE);

        // set current date on left of footer
        float textBase = document.bottom() + FOOTER_LINE_SEPARATOR_OFFSET_FROM_BOTTOM + FOOTER_TEXT_OFFSET_FROM_LINE_SEPARATOR;
        cb.setTextMatrix(document.left(), textBase);
        cb.showText(df.format(now.getTime()));

        // set page number
        float adjust = fontFooter.getWidthPoint("00", FOOTER_FONT_SIZE);
        cb.setTextMatrix(document.right() - textSize - adjust, textBase);
        cb.showText(text);
        cb.endText();
        cb.addTemplate(footer, document.right() - adjust, textBase);
        cb.restoreState();
    }

    /**
     * Get PDFBean helper object. Gives attributes suitable for client side
     * access.
     *
     * @return
     */
    public PDFBean getPdf() {
        return pdf;
    }

    /**
     * Build PDFBean.
     *
     * @param baseDir
     * @param contextParams
     */
    private void buildBean(File baseDir) {
        pdf = new PDFBean();
        pdf.setPdfId(String.valueOf(System.currentTimeMillis()));
        pdf.setFile(new File(baseDir, pdf.getPdfId() + ".pdf"));
        log.debug("Temp pdf file path: " + pdf.getFile().getAbsolutePath());

        StringBuilder name = new StringBuilder();

        Date date = new Date();

        name.append(contextParams.getSponsor().getRollupGroupName()).append("_").append(date.toString());
        name.append(".pdf");
        pdf.setName(name.toString());

        contextParams.setPdf(pdf);

    }

    /**
     * Draw title page.
     *
     * @throws com.lowagie.text.DocumentException
     */
    private void insertTitlePage() throws DocumentException, CancelException {

        if (this.listener.isCancel()) {
            throw new CancelException();
        }

        try {

            // sponsor name
            Paragraph title = new Paragraph(PARAGRAPH_LEADING, this.contextParams.getSponsor().getRollupGroupName(), fontTitle);
            title.setSpacingBefore(SPACING_MAIN);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(SPACING_MAIN);
            document.add(title);

            // loyalty one logo
// LOCAL CHANGE DO NOT CHANGE WITH KOREM CHANGES
            /**
             * TODO make it better (for unknown reason amap cannot load the image) 
             * the solution is to replace the alias by localhost 
             * Quickfix for logo download problem
             */
            String urlPref = this.baseHREF.substring(0,this.baseHREF.indexOf("/analytics/"));
            this.baseHREF = this.baseHREF.replace(urlPref,"http://localhost:8080");
            
// LOCAL CHANGE DO NOT CHANGE WITH KOREM CHANGES

            URL url = new URL(this.baseHREF + LOGO_RELATIVE_PATH);
            log.debug(String.format("Downloading report header image from %s", url));
            java.net.URLConnection connection = url.openConnection();
            if (this.baseHREF.contains("https://")) {
                HttpsURLConnection httpsConnection = (HttpsURLConnection) connection;
                httpsConnection.setHostnameVerifier(new HostnameVerifier() {

                    @Override
                    public boolean verify(String string, SSLSession ssls) {
                        return true;
                    }
                });
            }
            BufferedInputStream bis = new BufferedInputStream(connection.getInputStream());
            List<Byte> bin = new ArrayList<Byte>();
            try {
                byte[] buf = new byte[1024];
                int n = 0;
                while ((n = bis.read(buf)) > -1) {
                    for (byte b : buf) {
                        bin.add(b);
                    }
                }
            } finally {
                bis.close();
            }

            byte[] image = new byte[bin.size()];
            for (int i = 0; i < image.length; i++) {
                image[i] = bin.get(i);
            }
            Image logo = Image.getInstance(image);
            logo.scalePercent(PDF_IMAGE_DPI_RATIO);
//            Image logo = Image.getInstance(new URL(this.baseHREF + LOGO_RELATIVE_PATH));
            logo.setSpacingBefore(SPACING_MAIN);
            logo.setAlignment(Element.ALIGN_CENTER);
            document.add(logo);

            // air Miles
            Paragraph airMiles = new Paragraph(PARAGRAPH_LEADING, conf.getString("pdf.title.airmiles"), fontSubtitle);
            airMiles.setAlignment(Element.ALIGN_CENTER);
            document.add(airMiles);

            // date
            Paragraph date = new Paragraph(PARAGRAPH_LEADING, df.format(now.getTime()), fontTitle);
            date.setAlignment(Element.ALIGN_CENTER);
            document.add(date);

            // analysis
            Paragraph analysis = new Paragraph(PARAGRAPH_LEADING, conf.getString("pdf.title.analysis"), fontSubtitle);
            analysis.setSpacingBefore(SPACING_MAIN);
            analysis.setAlignment(Element.ALIGN_LEFT);
            document.add(analysis);
            if (reportParams.issuance() != -1.0D) {
                StringBuilder sbuilder = new StringBuilder();
                sbuilder.append(HYPHEN).append(SPACE);
                sbuilder.append(DataWriter.toPercent(reportParams.issuance(), 0)).append(SPACE);
                sbuilder.append(reportParams.tradearea().equals("units") ? conf.getString("pdf.title.airmiles.tradearea.units") : conf.getString("pdf.title.airmiles.tradearea"));
                Paragraph p = new Paragraph(PARAGRAPH_LEADING, sbuilder.toString(), fontSubtitleData);
                p.setAlignment(Element.ALIGN_LEFT);
                document.add(p);
            }
            if (reportParams.distance() != -1.0D) {
                StringBuilder sbuilder = new StringBuilder();
                sbuilder.append(HYPHEN).append(SPACE);
                sbuilder.append(reportParams.distance()).append(KM).append(SPACE);
                sbuilder.append(conf.getString("pdf.title.airmiles.tradearea.driveDistance"));
                Paragraph p = new Paragraph(PARAGRAPH_LEADING, sbuilder.toString(), fontSubtitleData);
                p.setAlignment(Element.ALIGN_LEFT);
                document.add(p);
            }
            if (hasProjected()) {
                StringBuilder sbuilder = new StringBuilder();
                sbuilder.append(HYPHEN).append(SPACE);
                sbuilder.append(conf.getString("pdf.title.airmiles.tradearea.projected"));
                Paragraph p = new Paragraph(PARAGRAPH_LEADING, sbuilder.toString(), fontSubtitleData);
                p.setAlignment(Element.ALIGN_LEFT);
                document.add(p);
            }
            if (hasCustom()) {
                StringBuilder sbuilder = new StringBuilder();
                sbuilder.append(HYPHEN).append(SPACE);
                sbuilder.append(conf.getString("pdf.title.airmiles.tradearea.custom"));
                Paragraph p = new Paragraph(PARAGRAPH_LEADING, sbuilder.toString(), fontSubtitleData);
                p.setAlignment(Element.ALIGN_LEFT);
                document.add(p);
            }
            if (null != reportParams.minTransactions()){
                StringBuilder sbuilder = new StringBuilder();
                sbuilder.append(HYPHEN).append(SPACE);
                sbuilder.append(conf.getString("pdf.title.analysis.minTransactions"));
                sbuilder.append(SPACE).append(reportParams.minTransactions());
                Paragraph p = new Paragraph(PARAGRAPH_LEADING, sbuilder.toString(), fontSubtitleData);
                p.setAlignment(Element.ALIGN_LEFT);
                document.add(p);
            }
            if (null != reportParams.minSpend()){
                Formatter formatter = new Formatter();
                StringBuilder sbuilder = new StringBuilder();
                sbuilder.append(HYPHEN).append(SPACE);
                sbuilder.append(conf.getString("pdf.title.analysis.minSpend"));
                sbuilder.append(SPACE).append(formatter.getCurrencyNumberFormat().format(reportParams.minSpend()));
                Paragraph p = new Paragraph(PARAGRAPH_LEADING, sbuilder.toString(), fontSubtitleData);
                p.setAlignment(Element.ALIGN_LEFT);
                document.add(p);
            }
            if (null != reportParams.minUnit()){
                StringBuilder sbuilder = new StringBuilder();
                sbuilder.append(HYPHEN).append(SPACE);
                sbuilder.append(conf.getString("pdf.title.analysis.minUnit"));
                sbuilder.append(SPACE).append(reportParams.minUnit());
                Paragraph p = new Paragraph(PARAGRAPH_LEADING, sbuilder.toString(), fontSubtitleData);
                p.setAlignment(Element.ALIGN_LEFT);
                document.add(p);
            }
            // store level analysis
            if (reportParams.dateType() != null && reportParams.slaTransactionValue() != null) {
                StringBuilder sbuilder = new StringBuilder();
                sbuilder.append(HYPHEN).append(SPACE);
                sbuilder.append(conf.getString("pdf.title.store.level.analysis"));
                
                String value = reportParams.slaTransactionValue();
                if (COMPARISON.toUpperCase().equals(reportParams.dateType().toUpperCase())) {
                    value += SPACE + StringUtils.capitalize(reportParams.dateType());
                }
                sbuilder.append(SPACE).append(new Chunk(MessageFormat.format(conf.getString("pdf.title.store.level.analysis.transaction.type"), value), fontSubtitleData));
                Paragraph p = new Paragraph(PARAGRAPH_LEADING, sbuilder.toString(), fontSubtitleData);
                p.setAlignment(Element.ALIGN_LEFT);
                document.add(p);
            }

            if (!"".equals(reportParams.hotspot())) {
                StringBuilder sb = new StringBuilder();
                sb.append(HYPHEN).append(SPACE).append(conf.getString("pdf.title.hotspot."+reportParams.dateType()));
                if(DateType.valueOf(reportParams.dateType())==DateType.comparison){
                    sb.append(conf.getString("pdf.title.hotspot.comparison."+reportParams.hotspotComparisonType()));
                }
                sb.append(SPACE).append(conf.getString("pdf.title.hotspot.for")).append(SPACE);
                if ("sponsor".equals(reportParams.hotspot())) {
                    sb.append(contextParams.getSponsorCodesList()).append(SPACE);
                }
                sb.append(conf.getString("pdf.title.hotspot."+reportParams.hotspot())).append(SPACE);
                sb.append(conf.getString("pdf.title.hotspot."+reportParams.dateType()+"."+reportParams.dataType()));
                Paragraph hotspot = new Paragraph(PARAGRAPH_LEADING, sb.toString(), fontSubtitleData);
                hotspot.setAlignment(Element.ALIGN_LEFT);
                document.add(hotspot);
            }

            // custom layers
            Paragraph customLayers = new Paragraph(PARAGRAPH_LEADING, conf.getString("pdf.title.customlayers"), fontSubtitle);
            customLayers.setSpacingBefore(SPACING_SECOND);
            title.setAlignment(Element.ALIGN_LEFT);
            document.add(customLayers);
            for (int i = 0; i < layers.length; i++) {
                if (!"".equals(layers[i]) && !Analysis.POSTAL_CODE.toString().equals(layers[i])) {
                    Paragraph layer = new Paragraph(PARAGRAPH_LEADING, HYPHEN + SPACE + layers[i], fontSubtitleData);
                    layer.setAlignment(Element.ALIGN_LEFT);
                    document.add(layer);
                }
            }

            // reports
            Paragraph reports = new Paragraph(PARAGRAPH_LEADING, conf.getString("pdf.title.reports"), fontSubtitle);
            reports.setSpacingBefore(SPACING_SECOND);
            title.setAlignment(Element.ALIGN_LEFT);
            document.add(reports);
            StringBuilder sb = new StringBuilder();
            if ("both".equals(reportParams.report())) {
                sb.append(HYPHEN).append(SPACE).append(conf.getString("pdf.title.reports.decay"));
                Paragraph report = new Paragraph(PARAGRAPH_LEADING, sb.toString(), fontSubtitleData);
                report.setAlignment(Element.ALIGN_LEFT);
                document.add(report);
                sb = new StringBuilder();
                sb.append(HYPHEN).append(SPACE).append(conf.getString("pdf.title.reports.summary"));
                report = new Paragraph(PARAGRAPH_LEADING, sb.toString(), fontSubtitleData);
                report.setAlignment(Element.ALIGN_LEFT);
                document.add(report);
            } else if ("decay".equals(reportParams.report())) {
                sb.append(HYPHEN).append(SPACE).append(conf.getString("pdf.title.reports.decay"));
                Paragraph report = new Paragraph(PARAGRAPH_LEADING, sb.toString(), fontSubtitleData);
                report.setAlignment(Element.ALIGN_LEFT);
                document.add(report);
            } else if ("summary".equals(reportParams.report())) {
                sb.append(HYPHEN).append(SPACE).append(conf.getString("pdf.title.reports.summary"));
                Paragraph report = new Paragraph(PARAGRAPH_LEADING, sb.toString(), fontSubtitleData);
                report.setAlignment(Element.ALIGN_LEFT);
                document.add(report);
            }

            // timeframe
            Paragraph timeframe = new Paragraph(PARAGRAPH_LEADING, conf.getString("pdf.title.timeframe") + SPACE, fontSubtitle);
            timeframe.setSpacingBefore(SPACING_SECOND);
            title.setAlignment(Element.ALIGN_LEFT);
            String timeframeTxt = reportParams.from() + SPACE + TO + SPACE + reportParams.to();
            if(DateType.valueOf(reportParams.dateType())==DateType.comparison){
                timeframeTxt += SPACE+conf.getString("pdf.title.timeframe.versus")+SPACE+reportParams.compareFrom() + SPACE + TO + SPACE + reportParams.compareTo();
            }
            timeframe.add(new Chunk(timeframeTxt, fontSubtitleData));
            document.add(timeframe);

            if (!reportParams.rollupCodesFilters().isEmpty()) {
                StringBuffer selectedollupCodes = new StringBuffer("");
                for (int i = 0; i < reportParams.rollupCodesFilters().size(); i++) {
                    selectedollupCodes.append(reportParams.rollupCodesFilters().get(i).toString());
                    if (i < reportParams.rollupCodesFilters().size() - 1) {
                        selectedollupCodes.append(" , ");
                    }
                }
                Paragraph rollupCodes = new Paragraph(PARAGRAPH_LEADING, conf.getString("pdf.title.rollup.codes") + SPACE, fontSubtitle);
                rollupCodes.setSpacingBefore(SPACING_SECOND);
                title.setAlignment(Element.ALIGN_LEFT);
                rollupCodes.add(new Chunk(selectedollupCodes.toString(), fontSubtitleData));
                document.add(rollupCodes);
            }
        } catch (Exception e) {
            log.error("error generating title", e);
        } finally {
        }
    }

    /**
     * Draw wsnav map and specialized legend.
     *
     * @throws com.lowagie.text.DocumentException
     * @throws java.rmi.RemoteException
     * @throws com.lowagie.text.BadElementException
     * @throws java.io.IOException
     */
    private void insertMapPage(final String ta, final Location location) throws DocumentException, RemoteException, BadElementException, IOException, CancelException {

        List<String> warnings = new ArrayList<String>();
        if (this.listener.isCancel()) {
            throw new CancelException();
        }

        DoublePoint dp = null;
        String locationKey = "";
        String locationCode = "";
        String title = "";

        insertNewChapter(landscape);

        if (isBatch()) {
            if (location == null) {
                title = conf.getString("pdf.map.title.batch.projected");
                dp = transformCoorditate(reportParams.longitude(), reportParams.latitude());
                locationCode = "projected";
            } else {
                title = conf.getString("pdf.map.title.batch") + SPACE + location.getCode();
                dp = new DoublePoint(location.getLongitude(), location.getLatitude());
                locationKey = String.valueOf(location.getKey());
                locationCode = location.getCode();
            }
        } else {
            title = conf.getString("pdf.map.title.screen");
        }

        PdfPTable datatable = new PdfPTable(1);
        PdfPCell cell = new PdfPCell();
        cell.setBorderWidth(0);
        Paragraph paragraph = new Paragraph(title, fontSubtitle);
        paragraph.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(paragraph);
        datatable.addCell(cell);
        document.add(datatable);

        // legends
        BufferedImage[] bufferedLegendImages = new BufferedImage[themeLayersIds.length];
        int legendWidth = 0;
        for (int i = 0; i < themeLayersIds.length; i++) {
            if (!themeLayersIds[i].isEmpty()) {
                byte[] image = WSClientLone.getLoneThematicService().getLegend(reportParams.mapInstanceKey(),
                        themeLayersIds[i],
                        Confs.STATIC_CONFIG.kmsLegendFontName(),
                        Confs.STATIC_CONFIG.kmsLegendFontStyle(),
                        Confs.STATIC_CONFIG.kmsLegendFontSize());
                InputStream in = new ByteArrayInputStream(image);
                bufferedLegendImages[i] = ImageIO.read(in);
                if (bufferedLegendImages[i].getWidth() > legendWidth) {
                    legendWidth = bufferedLegendImages[i].getWidth();
                }
            }
        }

        // image dimensions
        int imageWidth = (int) document.getPageSize().getWidth() - MARGIN_LEFT - MARGIN_RIGHT;
        int imageHeight = (int) document.getPageSize().getHeight() - MAP_TITLE - MARGIN_TOP - MARGIN_BOTTOM - BOTTOM_SPACING;

        // create buffered image
        BufferedImage bImage = new BufferedImage((int) (imageWidth * IMAGE_DPI_RATIO),
                (int) (imageHeight * IMAGE_DPI_RATIO), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = null;
        Legend legend = null;

        try {
            g2d = bImage.createGraphics();

            MapService mapService = WSClient.getMapService();

            // map
            log.debug("Retreive map image from mapInstanceKey: " + mapInstanceKey);

            if (!"".equals(reportParams.hotspot()) && this.hotSpotChecked()) {
                legendWidth = Math.max(HOTSPOT_LEGEND_MIN_WIDTH, legendWidth);
            }

            mapImageWidth = (int) ((imageWidth * IMAGE_DPI_RATIO - MAP_LEGEND_PADDING - legendWidth));
            mapImageHeight = (int) (imageHeight * IMAGE_DPI_RATIO);

            if (!fixedImage) {
                mapService.setDeviceBounds(mapInstanceKey, mapImageWidth, mapImageHeight);
                double adjustmentFactor = getAdjustementFactor();
                double previousZoom = mapService.getZoom(mapInstanceKey, ZOOM_UNIT);
                mapService.setZoom(mapInstanceKey, previousZoom / adjustmentFactor, ZOOM_UNIT);
                fixedImage = true;
            }

            // create map for specific location
            if (isBatch() && dp != null) {
                customizeMap(ta, mapService, locationKey, locationCode, dp);
                if (!"".equals(locationKey)) {
                    factory.setLocations(locationKey);
                }
            }

            byte[] bytes = null;
            try {
                long start = System.currentTimeMillis();
                bytes = getImage(mapService);
                log.info(String.format("It took %sms to retrieve image from KMS.", System.currentTimeMillis() - start));

            } catch (RemoteException ex1) {
                log.warn("First attempt to create map image failed, sometimes google api does not respond, retrying in 1s.");
                try {
                    Thread.sleep(1000);
                    bytes = getImage(mapService);
                } catch (RemoteException ex2) {
                    log.warn("Second attempt to create map image failed, final attempt in 2s.");
                    Thread.sleep(2000);
                    try {
                        bytes = getImage(mapService);
                    } catch (RemoteException ex3) {
                        log.error("Can't draw image with google background, trying without.", ex3);
                        bytes = mapService.getImage(mapInstanceKey, "image/png", mapImageWidth, mapImageHeight);
                    }
                }
            }

            double[] pdfBounds = WSClientUtil.getBounds(mapInstanceKey, SRS);

            //heat map if heat map on the map
            BufferedImage hmImage = null;
            if (!"".equals(reportParams.hotspot()) && this.hotSpotChecked()) {
                int zoom = getGoogleZoom(mapService, mapInstanceKey, mapImageWidth);
                HeatMapRule rule = rules.getRule(zoom);

                DensityHeatMap heatMap = factory.create(mapImageWidth, mapImageHeight, ALPHA,
                        pdfBounds[0], pdfBounds[1], pdfBounds[2], pdfBounds[3],
                        rule, GetHeatMap.STEPS_COLOR, Properties.get().getColors(), zoom, contextParams.getSelectedSponsorKeys());

                hmImage = heatMap.paint();

                legend = factory.getLastHeatMapLegend();
                if (legend != null) {
                    if (legend.getTotalCpt() < COLLECTOR_MIN_NUMBER) {
                        warnings.add(conf.getString("pdf.warning.mincollector.msg"));
                    }
                }
            }

            InputStream in = new ByteArrayInputStream(bytes);
            BufferedImage mapImage = ImageIO.read(in);
            g2d.drawImage(mapImage, 0, 0, mapImageWidth, mapImageHeight, null);

            //heat map if heat map on the map
            if (hmImage != null) {
                Composite oldComposite = g2d.getComposite();
                Composite newComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, reportParams.opacity().floatValue());
                g2d.setComposite(newComposite);
                g2d.drawImage(hmImage, 0, 0, mapImageWidth, mapImageHeight, null);
                g2d.setComposite(oldComposite);
            }

            // draw legend images in the image
            int top = mapImageHeight;
            if (legend != null) {
                top -= 235;
            }
            for (int i = 0; i < bufferedLegendImages.length; i++) {
                BufferedImage img = bufferedLegendImages[i];
                if (img == null) {
                    continue;
                }

                int legendSectionWidth = img.getWidth();
                int legendSectionHeight = img.getHeight();
                int left = mapImageWidth + MAP_LEGEND_PADDING + legendWidth / 2 - legendSectionWidth / 2;

                int localTop = top - legendSectionHeight;
                if (localTop < 0) {
                    break;
                    // this snippet of code would replace the break to insert a message instead of the legend
                    //legendSectionHeight = 40;
                    //g2d.setColor(Color.black);
                    //g2d.drawString(rb.getString("pdf.legendTooTall"), left + 10, top - 15);
                } else {
                    g2d.drawImage(
                            img,
                            left,
                            localTop,
                            legendSectionWidth,
                            legendSectionHeight,
                            null);
                }

                // update top for next legend section
                top -= legendSectionHeight;
            }
            if (!reportParams.isTaTextHidden()) {
                drawTAInfo(g2d, ta);
            }

            // draw map and legend contour
            g2d.setStroke(new BasicStroke(1.0f));
            g2d.setPaint(Color.BLACK);
            g2d.setBackground(Color.yellow);
            g2d.drawRect(
                    0,
                    0,
                    mapImageWidth,
                    (int) (mapImageHeight * IMAGE_DPI_RATIO) - 1);
            g2d.drawRect(
                    mapImageWidth + MAP_LEGEND_PADDING - 2,
                    0,
                    legendWidth - 1,
                    (int) (imageHeight * IMAGE_DPI_RATIO) - 1);
        } catch (Exception e) {
            log.error(null, e);
        } finally {
            if (g2d != null) {
                g2d.dispose();
            }
        }
        // Prepare image for insertion into pdf
        Image image = Image.getInstance(bImage, null);
        image.scalePercent(PDF_IMAGE_DPI_RATIO);
        log.debug("Add map image to document.");
        image.setAbsolutePosition(50, 50);
        document.add(image);
        if (legend != null) {
            addLegend(legend);
        }

        if (!warnings.isEmpty()) {
            addWarings(warnings);
        }
    }

    private String replaceStringFormatter(String str) {
        // replaces {0} used to format javascript strings with %s used to format java strings
        String newString = str.replace("{0}", "%s");
        return newString;
    }

    private List<String> getTAtext(String taType) {

        List<String> list = new ArrayList<String>();

        // split taType to mathes 
        String matchesArray[] = taType.split(",");

        // create an array list from the previous array
        ArrayList<String> matchesList = new ArrayList<String>(Arrays.asList(matchesArray));

        String taText = "";
        String taValue = "";

        if (matchesList.contains("issuance")) {
            taText = lm.get("tradeAreaInfo.amrp");
            taText = taText.replace("%", ""); // remove % char since Datawriter.toPercent function inserts it anyway
            taText = replaceStringFormatter(taText);
            taValue = String.valueOf(DataWriter.toPercent(reportParams.issuance(), 0));
            taText = String.format(taText, taValue);
            list.add(taText);
        }
        if (matchesList.contains("units")) {
            taText = lm.get("tradeAreaInfo.armpUnits");
            taText = taText.replace("%", ""); // remove % char since Datawriter.toPercent function inserts it anyway
            taText = replaceStringFormatter(taText);
            taValue = String.valueOf(DataWriter.toPercent(reportParams.issuance(), 0));
            taText = String.format(taText, taValue);
            list.add(taText);
        }

        if (matchesList.contains("distance")) {
            taText = lm.get("tradeAreaInfo.driveDistance");
            taText = replaceStringFormatter(taText);
            taText = String.format(taText, reportParams.distance());
            list.add(taText);
        }

        if (matchesList.contains("projected")) {
            taText = lm.get("tradeAreaInfo.projected");
            taText = replaceStringFormatter(taText);
            taText = String.format(taText, reportParams.projected());
            list.add(taText);
        }

        if (matchesList.contains("custom")) {
            taText = taText = lm.get("tradeAreaInfo.custom");
            list.add(taText);
        }

        return list;
    }

    private void drawTAInfo(Graphics2D g, String taType) {
        /*
         Draws TA info on the supplied graphics object
         */
        List<String> list = getTAtext(taType);

        // Graphics fill rect params
        int height = 20, width = 290;
        int baseX = (mapImageWidth - width) / 2;
        int baseY = mapImageHeight - 35;

        int xOffset = 10 + baseX, yOffset = 10 + baseY;

        // Graphics draw string params
        int xTxtOffset = 20 + baseX, yTxtOffset = 25;

        // for each value in JSONArray, fill a rectangle and draw the string inside of it
        for (int i = list.size() - 1; i >= 0; i--) {
            // get the TA info as a String
            String TAtext = list.get(i);

            g.setColor(Color.WHITE);
            g.fillRect(xOffset, yOffset, width, height);

            g.setColor(Color.BLACK);
            g.drawString(TAtext, xTxtOffset, yTxtOffset + baseY);

            g.setColor(new Color(114, 162, 229));
            g.drawRect(xOffset, yOffset, width, height);

            yOffset -= height + 2;
            yTxtOffset -= height + 2;
        }
    }

    private void addWarings(List<String> warnings) throws DocumentException {
        insertNewChapter(portrait);
        Paragraph title = new Paragraph(PARAGRAPH_LEADING, conf.getString("pdf.warning.title"), fontTitle);
        title.setAlignment(Element.ALIGN_LEFT);
        document.add(title);
        for (String warning : warnings) {
            Paragraph pWarning = new Paragraph(PARAGRAPH_LEADING, HYPHEN + SPACE + warning, fontSubtitle);
            pWarning.setAlignment(Element.ALIGN_LEFT);
            document.add(pWarning);
        }
    }

    private void addLegend(Legend legend) throws BadElementException, DocumentException, IOException {
        int cols = 3;
        PdfPTable table = new PdfPTable(cols);
        table.setWidthPercentage(18);
        table.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.setWidths(new float[]{17, 8, 150});
        table.setSpacingBefore(325);

        Font titleFont = FontFactory.getFont(Confs.STATIC_CONFIG.pdfGeneratorTitleLegendFontName(), Confs.STATIC_CONFIG.pdfGeneratorTitleLegendFontSize());
        titleFont.setStyle(Confs.STATIC_CONFIG.pdfGeneratorTitleLegendFontStyle());
        titleFont.setColor(Color.decode(String.format("0x%s", Confs.STATIC_CONFIG.pdfGeneratorColor())));
        Font font = FontFactory.getFont(Confs.STATIC_CONFIG.pdfGeneratorSubTitleLegendFontName(), Confs.STATIC_CONFIG.pdfGeneratorSubTitleLegendFontSize());
        font.setStyle(Confs.STATIC_CONFIG.pdfGeneratorSubTitleLegendFontStyle());
        font.setColor(Color.decode(String.format("0x%s", Confs.STATIC_CONFIG.pdfGeneratorColor())));

        JSONObject legendDescription = (JSONObject) new JSONTokener(legend.toString()).nextValue();

        PdfPCell cell = new PdfPCell(new Phrase(legendDescription.getString("title"), titleFont));
        cell.setColspan(cols);
        cell.setBorder(0);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        cell.setPaddingBottom(5.0f);
        table.addCell(cell);

        cell = new PdfPCell(new Phrase(legendDescription.getString("subtitle"), font));
        cell.setColspan(cols);
        cell.setBorder(0);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        table.addCell(cell);

        cell = new PdfPCell(new Phrase(MessageFormat.format(rb.getString("hotspot.legend.precision"), legend.getPrecision()), font));
        cell.setColspan(cols);
        cell.setBorder(0);
        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
        cell.setPaddingBottom(5.0f);
        table.addCell(cell);

        for (int i = legend.getItems().size() - 1; i >= 0; --i) {
            LegendItem item = legend.getItems().get(i);

            cell = new Cell(" ").createPdfPCell();
            cell.setBorder(0);
            table.addCell(cell);

            cell = new Cell().createPdfPCell();
            cell.setBackgroundColor(item.getColor());
            cell.setBorderWidthLeft(.5f);
            cell.setBorderWidthRight(.5f);
            cell.setBorderWidthBottom(.5f);
            cell.setBorderWidthTop(.5f);

            cell.setPaddingTop(1f);
            cell.setPaddingLeft(1f);
            if (i == 0) {
                cell.setPaddingBottom(1f);
            } else {
                cell.setPaddingBottom(.5f);
            }
            cell.setPaddingRight(1f);
            cell.setHorizontalAlignment(PdfPCell.ALIGN_LEFT);

            table.addCell(cell);

            String range = item.getMin() + " to " + item.getMax();
            if (item.getMin().equals("0") || item.getMin().equals("$0")) {
                range = rb.getString("hotspot.legend.under") + " " + item.getMax();
            }
            cell = new PdfPCell(new Phrase(range, font));
            cell.setBorder(0);
            cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
            table.addCell(cell);
        }

        document.add(new Paragraph(" "));
        document.add(table);
    }

    private Element getColor(Color color) throws BadElementException, IOException {
        BufferedImage img = new BufferedImage(2, 3, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = (Graphics2D) img.getGraphics();
        g2d.setColor(color);
        g2d.fillRect(0, 0, 2, 3);
        Image image = Image.getInstance(img, null);
        return image;
    }

    /**
     * Draw data pages. Replicate web format (no border, gray odd row, ...).
     * DataWriter is specialized for each KPI to adjust to implemented Data
     * object.
     *
     * @throws com.lowagie.text.DocumentException
     */
    private void insertDataPages() throws DocumentException {
        try {
            sponsorKeys = contextParams.getSelectedSponsorKeys();
            if (hasProjected()) {
                this.projectedPoint = transformCoorditate(reportParams.longitude(), reportParams.latitude());
            }
            buildReports();
        } catch (Exception ex) {
            log.debug("Error retreiving dymanic data.", ex);
        } finally {
        }
    }

    /**
     * @param longitude of sponsor location
     * @param latitude of sposor location
     * @return list of geometry
     */
    private List<Geometry> getGeom(final double longitude, final double latitude) {
        return new DriveTimePolygon(longitude, latitude, TransactionDAO.DISTANCE_BANDS).getDriveTimePolygons();
    }

    private void buildReports() throws ParseException, DocumentException, CancelException, RemoteException, BadElementException, IOException, SQLException, Exception {
        long start = System.currentTimeMillis();
        Date from = df.parse(reportParams.from());
        Date to = df.parse(reportParams.to());

        TransactionDAO tdao = new TransactionDAO(new AirMilesDAO());
        LocationDAO ldao = new LocationDAO(new AirMilesDAO());

        if (hasProjected()) {
            if ("batch".equals(reportParams.type())) {
                insertMapPage("projected"/*reportParams.tradearea()*/, null);
            }
            List<Geometry> geoms = null;
            if ("both".equals(reportParams.report()) || "decay".equals(reportParams.report())) {
                geoms = getGeom(projectedPoint.x, projectedPoint.y);
            }
            if ("both".equals(reportParams.report())) {
                // projected decay
                List<Transaction> sponsorTransactions = tdao.getProjectedSponsorTransactions(from, to, sponsorKeys, geoms, selectedSponsorGroup, sponsorKeysList, reportParams.minTransactions(), reportParams.minSpend(), reportParams.minUnit());
                List<Transaction> amTransactions = tdao.getProjectedAMTransactions(selectedSponsorGroup, geoms);
                insertDistanceDecayReport(new ArrayList<Transaction>(), sponsorTransactions, amTransactions, null);
                // projected location summary
                insertLocationSummaryReport(null, from, to, ReportType.projected, reportParams.minTransactions(), reportParams.minSpend(), reportParams.minUnit());
            } else if ("decay".equals(reportParams.report())) {
                List<Transaction> sponsorTransactions = tdao.getProjectedSponsorTransactions(from, to, sponsorKeys, geoms, selectedSponsorGroup, sponsorKeysList, reportParams.minTransactions(), reportParams.minSpend(), reportParams.minUnit());
                List<Transaction> amTransactions = tdao.getProjectedAMTransactions(selectedSponsorGroup, geoms);
                insertDistanceDecayReport(new ArrayList<Transaction>(), sponsorTransactions, amTransactions, null);
            } else {
                insertLocationSummaryReport(null, from, to, ReportType.projected, reportParams.minTransactions(), reportParams.minSpend(), reportParams.minUnit());
            }
        }

        for (int i = 0; i < locationKeys.length; i++) {
            if (!locationKeys[i].isEmpty()) {
                Location loc = ldao.getLocation(selectedSponsorGroup, Double.parseDouble(locationKeys[i]));
                String[] tas = reportParams.tradearea().split(",");
                boolean first = true;
                for (String ta : tas) {
                    if (!"projected".equals(ta)) {
                        if ("batch".equals(reportParams.type())) {
                            insertMapPage(ta/*reportParams.tradearea()*/, loc);
                        }
                        List<Geometry> geoms = null;
                        if ("both".equals(reportParams.report()) || "decay".equals(reportParams.report())) {
                            geoms = getGeom(loc.getLongitude(), loc.getLatitude());
                        }
                        if ("both".equals(reportParams.report())) {
                            // decay
                            if (first) {
                                first = false;
                                List<Transaction> locationTransactions = tdao.getLocationTransactions(from, to, sponsorKeys, Double.parseDouble(locationKeys[i]), selectedSponsorGroup, reportParams.minTransactions(), reportParams.minSpend(), reportParams.minUnit(), contextParams);
                                List<Transaction> sponsorTransactions = tdao.getProjectedSponsorTransactions(from, to, sponsorKeys, geoms, selectedSponsorGroup, sponsorKeysList, reportParams.minTransactions(), reportParams.minSpend(), reportParams.minUnit());
                                List<Transaction> amTransactions = tdao.getProjectedAMTransactions(selectedSponsorGroup, geoms);
                                insertDistanceDecayReport(locationTransactions, sponsorTransactions, amTransactions, loc);
                            }
                            updateListener(singleLocationProgress);
                            // location summary
                            try {
                                insertLocationSummaryReport(loc, from, to, ReportType.valueOf(ta), reportParams.minTransactions(), reportParams.minSpend(), reportParams.minUnit());
                            } catch (Exception e) {
                                log.error("Error generation location summary report for location: " + locationKeys[i], e);
                            }
                        } else if ("decay".equals(reportParams.report())) {
                            if (first) {
                                first = false;
                                List<Transaction> locationTransactions = tdao.getLocationTransactions(from, to, sponsorKeys, Double.parseDouble(locationKeys[i]), selectedSponsorGroup, reportParams.minTransactions(), reportParams.minSpend(), reportParams.minUnit(), contextParams);
                                List<Transaction> sponsorTransactions = tdao.getProjectedSponsorTransactions(from, to, sponsorKeys, geoms, selectedSponsorGroup, sponsorKeysList, reportParams.minTransactions(), reportParams.minSpend(), reportParams.minUnit());
                                List<Transaction> amTransactions = tdao.getProjectedAMTransactions(selectedSponsorGroup, geoms);
                                insertDistanceDecayReport(locationTransactions, sponsorTransactions, amTransactions, loc);
                            }
                        } else {
                            try {
                                insertLocationSummaryReport(loc, from, to, ReportType.valueOf(ta), reportParams.minTransactions(), reportParams.minSpend(), reportParams.minUnit());
                            } catch (Exception e) {
                                log.error("Error generation location summary report for location: " + locationKeys[i], e);
                            }
                        }
                    }
                }
            }
        }
        log.info(System.currentTimeMillis() - start + " ms spent to execute buildReports.");
    }

    private void insertDistanceDecayReport(List<Transaction> locTransactions,
            List<Transaction> spoTransactions,
            List<Transaction> amTransactions,
            Location location) throws DocumentException, CancelException, RemoteException, BadElementException, IOException {

        if (this.listener.isCancel()) {
            throw new CancelException();
        }

        try {
            insertNewChapter(landscape);

            if (location != null) {
                addReportTitle(conf.getString("pdf.title.distancedecay") + location.getCustomerLocationCode(), document);
            } else {
                addReportTitle(conf.getString("pdf.title.distancedecay.proj"), document);
            }

            DecayReportWriter drw = new DecayReportWriter(selectedSponsorGroup, locTransactions, spoTransactions, amTransactions, location);
            drw.insert(document, MARGIN_LEFT + MARGIN_RIGHT);
        } finally {
            updateListener(singleLocationProgress);
        }
    }

    private void insertLocationSummaryReport(Location loc, Date from, Date to, ReportType type, Integer minTransactions, Integer minSpend, Integer minUnit) throws CancelException, SQLException, Exception {
        if (this.listener.isCancel()) {
            throw new CancelException();
        }
        String titleTxt;
        DoublePoint dPoint;
        int lKey = 0;
        if (loc != null) {
            Double locKey = loc.getKey();
            taParams = toTradeAreaParams(locKey.toString() + ",", loc.getCode() + ",", null);

            titleTxt = conf.getString("pdf.title.locsummary") + loc.getCustomerLocationCode();
            dPoint = new DoublePoint(loc.getLongitude(), loc.getLatitude());
            if (type == ReportType.issuance) {
                List<TradeArea.Type> anInsuance = new ArrayList();
                anInsuance.add(TradeArea.Type.valueOf(type.name()));
            }
            Double val = locKey;
            lKey = val.intValue();
        } else {
            taParams = toTradeAreaParams(",", "projected,", null);
            titleTxt = conf.getString("pdf.title.locsummary.proj");
            dPoint = new DoublePoint(projectedPoint.x, projectedPoint.y);
        }

        TradeAreaControler controler = new TradeAreaControler(new String[]{type.name()}, taParams, null, contextParams, selectedSponsorGroup);
        String tempLayerId = controler.createLayer(session);
        Geometry geom = controler.getTradeAreas().get(0).getGeometry();

        insertNewChapter(landscape);
        addReportTitle(titleTxt, document);

        SummaryReportDAO sdao = new SummaryReportDAO(new AirMilesDAO());
        List<SummaryReport> reports = sdao.getSummaryReports(selectedSponsorGroup, from, to, dPoint.x, dPoint.y,
                sponsorKeys, geom, mapInstanceKey, lKey, minTransactions, minSpend, minUnit, contextParams);
        if (!reports.isEmpty()) {
            SummaryReportWriter srw = new SummaryReportWriter(selectedSponsorGroup, reports, reportParams, type, loc);
            int totalHH = -1;
            try (PostalCodeProxy pcp = new PostalCodeProxy(selectedSponsorGroup)) {
                totalHH = pcp.getTotalHouseholds(geom);
            } catch (Exception e) {
                log.error("Can't get total households", e);
            }
            srw.setTotalHouseHolds(totalHH);
            srw.insert(document, MARGIN_LEFT + MARGIN_RIGHT);
            updateListener(singleLocationProgress);
        } else {
            Paragraph title = new Paragraph(PARAGRAPH_LEADING, conf.getString("pdf.table.summary.nodata"), fontTitle);
            title.setSpacingBefore(25);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
        }
        WSClient.getMapService().removeLayer(mapInstanceKey, tempLayerId);
    }

    /**
     * Insert a new chapter (equivalent to "section on next page" in M$ World
     * lingo). Enables changing paper orientation within the same document.
     *
     * @param pageSize
     * @throws com.lowagie.text.DocumentException
     */
    private void insertNewChapter(Rectangle pageSize) throws DocumentException {
        document.newPage();
        document.setPageSize(pageSize);
        document.add(new Chapter(chapter++));
    }

    /**
     * Attemps to read properties from config file. Lazy implementation, if one
     * fails, all subsequent values are hardcoded ones. If you don't like it,
     * code it :P
     */
    private void readProperties() {
        // constant definition.
        try {
            COLLECTOR_MIN_NUMBER = Integer.parseInt(conf.getString("pdf.warning.mincollector.nbr"));

            PARAGRAPH_LEADING = Float.parseFloat(conf.getString("pdf.pragraph.leading"));

            MAP_LEGEND_PADDING = Integer.parseInt(conf.getString("pdf.legend.map.padding"));
            MAX_LEGEND_ITEM = Integer.parseInt(conf.getString("pdf.legend.item.max"));

            MARGIN_LEFT = Integer.parseInt(conf.getString("pdf.margin.left"));
            MARGIN_RIGHT = Integer.parseInt(conf.getString("pdf.margin.right"));
            MARGIN_TOP = Integer.parseInt(conf.getString("pdf.margin.top"));
            MARGIN_BOTTOM = Integer.parseInt(conf.getString("pdf.margin.bottom"));
            FOOTER_LINE_SEPARATOR_OFFSET_FROM_BOTTOM = Integer.parseInt(conf.getString("pdf.footer.lineSeparator.offset"));
            FOOTER_TEXT_OFFSET_FROM_LINE_SEPARATOR = Integer.parseInt(conf.getString("pdf.footer.text.offsetFromLineSeparator"));
            BOTTOM_SPACING = Integer.parseInt(conf.getString("pdf.image.bottomSpacing"));
            GUTTER_WIDTH = Float.parseFloat(conf.getString("pdf.column.gutterWidth"));

            FONT_NAME = conf.getString("pdf.font.name");
            TITLE_FONT_SIZE = Integer.parseInt(conf.getString("pdf.title.font.size"));
            SUBTITLE_FONT_SIZE = Integer.parseInt(conf.getString("pdf.subtitle.font.size"));
            SUBTITLE_DATA_FONT_SIZE = Integer.parseInt(conf.getString("pdf.subtitleData.font.size"));
            DATA_FONT_SIZE = Integer.parseInt(conf.getString("pdf.data.font.size"));
            DATA_BOLD_FONT_SIZE = Integer.parseInt(conf.getString("pdf.dataBold.font.size"));
            FOOTER_FONT_SIZE = Integer.parseInt(conf.getString("pdf.footer.font.size"));

        } catch (Exception ex) {
            log.error("Error parsing properties " + CONFIG + ". Default hardcoded used.", ex);
        }
    }

    private IParams toTradeAreaParams(final String strLocations, final String strLocationCodes, final String name) {
        taParams = new IParams() {

            @Override
            public String tradearea() {
                return strLocationCodes.contains("projected") ? "projected" : reportParams.tradearea();
            }

            @Override
            public String taLayerId() {
                return reportParams.taLayerId();
            }

            @Override
            public String locations() {
                return strLocations;
            }

            @Override
            public String locationsCode() {
                return strLocationCodes;
            }

            @Override
            public Double issuance() {
                return reportParams.issuance();
            }

            @Override
            public Double distance() {
                return reportParams.distance();
            }

            @Override
            public Double projected() {
                return strLocationCodes.contains("projected") ? Double.valueOf(reportParams.projected()) : null;
            }

            @Override
            public Double longitude() {
                return reportParams.longitude();
            }

            @Override
            public String polygon() {
                return reportParams.polygon();
            }

            @Override
            public Double latitude() {
                return reportParams.latitude();
            }

            @Override
            public String from() {
                return reportParams.formatFrom();
            }

            @Override
            public String to() {
                return reportParams.formatTo();
            }

            @Override
            public String mapInstanceKey() {
                return mapInstanceKey;
            }

            @Override
            public String optionalLayerName() {
                return name;
            }

            @Override
            public Integer minTransactions() {
                return reportParams.minTransactions();
            }

            @Override
            public Integer minSpend() {
                return reportParams.minSpend();
            }

            @Override
            public Integer minUnit() {
                return reportParams.minUnit();
            }

            /*@Override
             public String slaTransactionValue() {
             return reportParams.slaTransactionValue();
             }*/
            @Override
            public String dateType() {
                return reportParams.dateType();
            }

            public String compareFrom() {
                return null;
            }

            public String compareTo() {
                return null;
            }
        };

        return taParams;
    }

    private String[] getExcludedLayers() {
        return PostalCodeLayerManager.get().getLayersId(reportParams.mapInstanceKey());
    }

    private void duplicateMapSession() throws RemoteException {
        String[] excludedLayers = getExcludedLayers();
        if (isBatch()) {
            duplicateMapSessionWithNoTradeArea(excludedLayers);
            duplicateHotSpotFactory();
        } else {
            duplicateMapSessionWithTradeArea(excludedLayers);
        }
        WSClient.getFeatureSetService().removeSelection(mapInstanceKey);
        setPlacemark(mapInstanceKey);
    }

    private void duplicateMapSessionWithTradeArea(String[] excludedLayers) throws RemoteException {
        mapInstanceKey = WSClientLone.getMappingSessionService().duplicateMapSession(reportParams.mapInstanceKey(), excludedLayers);
       // MapProvider.initWorkspaceProperties(mapInstanceKey);
    }

    private void duplicateHotSpotFactory() {
        String key = "hotspot" + mapInstanceKey;
        this.factory = this.factory.createClone();
        session.setAttribute(key, factory);
    }

    private void duplicateMapSessionWithNoTradeArea(String[] excludedLayers) throws RemoteException {
        String[] layerIds = WSClient.getMapService().getLayersIdByName(reportParams.mapInstanceKey(),
                Analysis.TRADE_AREA.toString());
        for (String layerId : layerIds) {
            WSClient.getLayerService().setVisible(reportParams.mapInstanceKey(), layerId, false);
        }
        mapInstanceKey = WSClientLone.getMappingSessionService().duplicateMapSession(reportParams.mapInstanceKey(), excludedLayers);
        //MapProvider.initWorkspaceProperties(mapInstanceKey);
        for (String layerId : layerIds) {
            WSClient.getLayerService().setVisible(reportParams.mapInstanceKey(), layerId, true);
        }
    }

    private void updateListener(Integer progress) {
        overallProgress += progress;
        log.debug("Update listener to " + overallProgress);
        listener.update(Math.min(overallProgress, 99));
    }

    private void addReportTitle(String text, Document document) throws DocumentException {
        PdfPTable datatable = new PdfPTable(1);
        PdfPCell cell = new PdfPCell();
        cell.setBorderWidth(0);
        Paragraph paragraph = new Paragraph(text, fontReportTitle);
        paragraph.setAlignment(Element.ALIGN_CENTER);
        paragraph.setSpacingAfter(15f);
        cell.addElement(paragraph);
        datatable.addCell(cell);
        document.add(datatable);
    }

    private DoublePoint transformCoorditate(double longitude, double latitude) {
        DoublePoint dp = new DoublePoint(longitude, latitude);
        try {
            CoordSys mapCoordsys = CoordSys.createFromMapBasic(Confs.STATIC_CONFIG.webCoordsys());
            CoordTransform ct = new CoordTransform(mapCoordsys, CoordSys.longLatNAD27);
            ct.forward(dp);
        } catch (Exception ex) {
            log.error("Exception caught on transformCoorditate", ex);
        }
        return dp;
    }

    private boolean isBatch() {
        return "batch".equals(reportParams.type());
    }

    private boolean hotSpotChecked() {
        boolean checked = false;
        for (String layer : this.layers) {
            if (layer.equalsIgnoreCase("hotspot")) {
                checked = true;
            }
        }
        return checked;
    }

    private boolean hasProjected() {
        String projected = reportParams.projected();
        return projected != null && !"".equals(projected);
    }

    private boolean hasCustom() {
        return reportParams.polygon() != null && reportParams.polygon().trim().length() > 0;
    }

    private void customizeMap(String ta, MapService mapService, String locationKey, String locationCode, DoublePoint dp) throws RemoteException {
        mapService.setCenter(mapInstanceKey, dp.x, dp.y, SRS);
        if (!"".equals(reportParams.tradearea())) {
            if (previousLayerId != null) {
                WSClient.getMapService().removeLayer(mapInstanceKey, previousLayerId);
            }
            taParams = this.toTradeAreaParams(locationKey + ",", locationCode + ",", "" + System.currentTimeMillis());
            AnalysisControler controler = new TradeAreaControler(new String[]{ta}, taParams, null, null, selectedSponsorGroup);
            previousLayerId = controler.createLayer(session);
            if (previousLayerId != null) {
                WSClient.getLayerService().viewEntireLayer(mapInstanceKey, Integer.parseInt(previousLayerId));
                if (Math.abs(1.0 - BATCH_ZOOM_MODIFIER) > 0.001) {
                    // get/set zoom only if different from 1.0
                    double zoom = WSClient.getMapService().getZoom(mapInstanceKey, "m");
                    zoom *= BATCH_ZOOM_MODIFIER;
                    WSClient.getMapService().setZoom(mapInstanceKey, zoom, "m");
                }
            }
        }
    }
    private static final int maxZoomLevels = 23;

    private int getGoogleZoom(MapService mapService, String mapInstanceKey, int width) throws RemoteException {
        double mapJZoom = mapService.getZoom(mapInstanceKey, ZOOM_UNIT);
        log.debug("mapJZoom: " + mapJZoom);
        return getZoomLevel(mapJZoom, width);
    }

    private int getZoomLevel(double zoom, double width) {
        int closestLevel = -1;
        double closestDifference = 1.1;

        for (int i = 0; i < maxZoomLevels; i++) {
            double fTiles = Math.pow(2, i);
            double zoomTile = 40075016D / (fTiles);
            double relatedZoom = (width / 256D) * zoomTile;
            double zoomDifference = Math.abs(zoom - relatedZoom);

            if (closestLevel == -1 || (zoomDifference < closestDifference)) {
                closestLevel = i;
                closestDifference = zoomDifference;
            }
        }
        return closestLevel;
    }

    private String getMarkerRendition() {
        return String.format(MARKER_RENDITION_TEMPLATE, Confs.CONFIG.projectedMarkerPath());
    }

    private void setPlacemark(String mapInstanceKey) {
        if (hasProjected()) {
            List<String[]> labels = new ArrayList<String[]>();
            labels.add(new String[]{"projected"});
            double lon = reportParams.longitude();
            double lat = reportParams.latitude();
            DoublePoint dp = transformCoorditate(lon, lat);
            List<double[]> points = new ArrayList<double[]>();
            points.add(new double[]{dp.x, dp.y});
            try {
                WSClientLone.getMapService().createAnnotationLayer(
                        mapInstanceKey,
                        PLACEMARK_TEMP_LAYER_NAME,
                        new String[]{"mipk", "name"},
                        new int[]{1},
                        labels.toArray(new String[][]{}),
                        null,
                        points.toArray(new double[][]{}),
                        new String[]{getMarkerRendition()},
                        1);
            } catch (RemoteException ex) {
                log.error("Error adding placemark rendition.", ex);
            }
        }
    }
}
