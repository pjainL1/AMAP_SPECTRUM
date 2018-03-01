/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lo.pdf.data;

import java.text.ParseException;
import com.lo.db.om.SponsorGroup;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import java.awt.Color;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 *
 * @author slajoie
 */
public abstract class DataWriter {

    protected final ResourceBundle conf;
    protected final static String SPACE = " ";
    protected static final String HYPHEN = "-";
    protected final static String PERCENT = "%";
    protected final static String LF="\n";
    protected final static String KM = "km";
    private static final String CONFIG = "com.lo.pdf.config";
    protected final static int DEFAULT_PADDING = 3;
    protected final static int DEFAULT_BORDER_WIDTH = 1;
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
    protected final static int DATA_ALIGNEMENT = Element.ALIGN_RIGHT;
    protected final static int TEXT_ALIGNEMENT = Element.ALIGN_LEFT;
    protected final static int TEXT_ALIGNEMENT_CENTER = Element.ALIGN_CENTER;
    protected Font fontHeader;
    protected Font fontSecondHeader;
    protected Font fontData;
    protected SponsorGroup sponsorGroup;
    protected DateFormat df;
    protected final static Color firstHeaderColor = new Color(0, 128, 192);
    protected final static Color secondHeaderColor = new Color(32, 183, 255);
    protected final static Color thirdHeaderColor = new Color(166, 209, 230);

    /**
     * Delegate header definition.
     *
     * @return
     */
    protected abstract List<PdfPCell> generateHeaders();

    /**
     * Delegate data cell generation to subclasses.
     *
     * @return
     */
    protected abstract List<PdfPCell> generateCells() throws ParseException;

    /**
     * Request column relative widths from subclasses.
     *
     * @return
     */
    protected abstract int[] generateRelativeWidths();

    protected abstract int getHeadersSize(List<PdfPCell> headers);

    protected abstract void getFirstHeaders(PdfPTable datatable, int size);

    public DataWriter(SponsorGroup sponsorGroup) {
        this.sponsorGroup = sponsorGroup;
        conf = ResourceBundle.getBundle(CONFIG);
        fontHeader = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.BOLD);
        fontSecondHeader = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.BOLD);
        fontData = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.NORMAL);
        df = new SimpleDateFormat(conf.getString("pdf.dateformat"));
    }

    public void insert(Document document, float margins) throws DocumentException {

        try {
            List<PdfPCell> headers = generateHeaders();
            PdfPTable datatable = new PdfPTable(getHeadersSize(headers));
            datatable.setWidths(generateRelativeWidths());
            datatable.setTotalWidth(document.getPageSize().getWidth() - margins);
            datatable.setLockedWidth(true);
            datatable.getDefaultCell().setPadding(DEFAULT_PADDING);
            datatable.getDefaultCell().setBorderWidth(DEFAULT_BORDER_WIDTH);
            //datatable.getDefaultCell().setHorizontalAlignment(TEXT_ALIGNEMENT);
            datatable.getDefaultCell().setBorderColor(Color.BLACK);
            datatable.getDefaultCell().setBackgroundColor(firstHeaderColor);

            // add headers
            getFirstHeaders(datatable, getHeadersSize(headers));
            addHeaderCells(headers, datatable);

            // delegate data insertion
            for (PdfPCell cell : generateCells()) {
                datatable.addCell(cell);
            }

            document.add(datatable);

        } catch (ParseException ex) {
            //log.error("Error parsing contextParams data.", ex);
        }
    }

    protected void addHeaderCells(List<PdfPCell> headers, PdfPTable datatable) {
        for (PdfPCell header : headers) {
            datatable.addCell(header);
        }
    }

    protected double parseDouble(String number) {
        return Double.parseDouble(number.replaceAll(",", ""));
    }

    protected PdfPCell generateCell(String msg, boolean odd) {
        PdfPCell cell = generateCell(msg);
        cell.setGrayFill(odd ? DEFAULT_ODD_GRAY_FILL : DEFAULT_EVEN_GRAY_FILL);
        return cell;
    }

    protected PdfPCell generateCell(double value, int type, boolean odd) {
        PdfPCell cell = generateCell(value, type);
        cell.setGrayFill(odd ? DEFAULT_ODD_GRAY_FILL : DEFAULT_EVEN_GRAY_FILL);
        return cell;
    }

    protected PdfPCell generateCell(boolean odd) {
        PdfPCell cell = generateCell();
        cell.setGrayFill(odd ? DEFAULT_ODD_GRAY_FILL : DEFAULT_EVEN_GRAY_FILL);
        return cell;
    }

    protected PdfPCell generateCell(String msg) {
        PdfPCell cell = generateCell();
        cell.addElement(generateParagraph(msg, false));
        return cell;
    }

    protected PdfPCell generateRightAlignCell(String msg) {
        PdfPCell cell = generateCell();
        cell.addElement(generateRightAlignParagraph(msg, false));
        return cell;
    }

    protected PdfPCell generateFirstHeaderCell(String msg) {
        PdfPCell cell = generateCell();
        cell.setBackgroundColor(firstHeaderColor);
        cell.addElement(generateParagraph(msg, true));
        return cell;
    }

    protected PdfPCell generateSecondHeaderCell(String msg) {
        return generateSecondHeaderCell(msg, TEXT_ALIGNEMENT);
    }

    protected PdfPCell generateSecondHeaderCell(String msg, int align) {
        Paragraph paragraph = new Paragraph(msg, fontSecondHeader);
        paragraph.setAlignment(align);
        PdfPCell cell = generateCell();
        cell.setBackgroundColor(secondHeaderColor);
        cell.addElement(paragraph);
        return cell;
    }

    protected PdfPCell generateTotalCell(String value) {
        PdfPCell cell = generateCell();
        cell.addElement(generateParagraph(value, false));
        cell.setGrayFill(DEFAULT_HEADER_GRAY_FILL);
        return cell;
    }

    protected PdfPCell generateRightAlignTotalCell(String value) {
        PdfPCell cell = generateCell();
        cell.addElement(generateRightAlignParagraph(value, false));
        cell.setGrayFill(DEFAULT_HEADER_GRAY_FILL);
        return cell;
    }

    protected PdfPCell generateTotalCell(double value, int digits) {
        PdfPCell cell = generateCell();
        cell.addElement(generateParagraph(value, digits, false));
        cell.setGrayFill(DEFAULT_HEADER_GRAY_FILL);
        return cell;
    }

    protected PdfPCell generateCell(double value, int digits) {
        PdfPCell cell = generateCell();
        cell.addElement(generateParagraph(value, digits, false));
        return cell;
    }

    protected PdfPCell generateSubheader(String msg) {
        Paragraph paragraph = new Paragraph(msg, fontSecondHeader);
        PdfPCell cell = generateCell();
        cell.addElement(paragraph);
        cell.setBackgroundColor(thirdHeaderColor);
        return cell;
    }

    protected PdfPCell generateSubheader(double value, int digits) {
        PdfPCell cell = generateCell();
        cell.addElement(generateParagraph(value, digits, true));
        return cell;
    }

    protected PdfPCell generateCell() {
        PdfPCell cell = new PdfPCell();
        cell.setPadding(DEFAULT_PADDING);
        cell.setBorderWidth(DEFAULT_BORDER_WIDTH);
        cell.setBorderColor(Color.BLACK);
        return cell;
    }

    protected PdfPCell generateNoValueCell() {
        PdfPCell cell = generateCell();
        cell.setGrayFill(DEFAULT_HEADER_GRAY_FILL);
        return cell;
    }

    protected Paragraph generateParagraph(double value, int digits, boolean header) {
        // set default number format
        NumberFormat nf = NumberFormat.getInstance(Locale.CANADA);
        nf.setMaximumFractionDigits(digits);

        Paragraph paragraph = new Paragraph(nf.format(value), header ? fontHeader : fontData);
        paragraph.setAlignment(DATA_ALIGNEMENT);
        return paragraph;
    }

    protected Paragraph generateParagraph(String msg, boolean header) {
        Paragraph paragraph = new Paragraph(msg, header ? fontHeader : fontData);
        paragraph.setAlignment(TEXT_ALIGNEMENT);
        return paragraph;
    }

    protected Paragraph generateRightAlignParagraph(String msg, boolean header) {
        Paragraph paragraph = new Paragraph(msg, header ? fontHeader : fontData);
        paragraph.setAlignment(DATA_ALIGNEMENT);
        return paragraph;
    }

    protected PdfPCell generateTotal() {
        PdfPCell cell = generateCell();
        cell.setGrayFill(DEFAULT_TOTAL_GRAY_FILL);
        return cell;
    }

    protected PdfPCell generateTotal(String msg) {
        PdfPCell cell = generateCell(msg);
        cell.setGrayFill(DEFAULT_TOTAL_GRAY_FILL);
        return cell;
    }

    protected PdfPCell generateTotal(double value, int digits) {
        PdfPCell cell = generateCell(value, digits);
        cell.setGrayFill(DEFAULT_TOTAL_GRAY_FILL);
        return cell;
    }

    public static String toPercent(double decimal, int digits) {
        NumberFormat nf = NumberFormat.getInstance(Locale.CANADA);
        nf.setMaximumFractionDigits(digits);
        nf.setMinimumFractionDigits(digits);
        return nf.format(decimal * 100) + "%";
    }

    protected String toMoney(Double decimal, int digits) {
        NumberFormat nf = NumberFormat.getInstance(Locale.CANADA);
        nf.setMaximumFractionDigits(digits);
        return decimal.equals(Double.NaN) ? "" : "$  " + nf.format(decimal);
    }
    
    protected String toString(double decimal, int digits) {
        NumberFormat nf = NumberFormat.getInstance(Locale.CANADA);
        nf.setMaximumFractionDigits(digits);
        return nf.format(decimal);
    }

}
