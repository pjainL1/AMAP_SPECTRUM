package com.lo.pdf.data;

import com.lo.db.om.Location;
import com.lo.db.om.SponsorGroup;
import com.lo.db.om.SummaryReport;
import com.lo.report.ReportMethod.IParams;
import com.lo.util.Formatter;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author slajoie
 */
public class SummaryReportWriter extends DataWriter {

    protected final static int NUM_COLS_IN_SUMMARY_REPORT = 17;
    private final static int TOTAL_SUBHEADERS = 4;

    @Override
    protected int getHeadersSize(List<PdfPCell> headers) {
        return NUM_COLS_IN_SUMMARY_REPORT;
    }

    public enum ReportType {

        units, issuance, distance, projected, custom;
    }
    private static final int TOP_REPORT = 10;
    private List<SummaryReport> summaryReports;
    private IParams reportParams;
    private final Location location;
    private ReportType reportType;
    private int totalHouseHolds;

    public SummaryReportWriter(SponsorGroup sponsorGroup, List<SummaryReport> summaryReports,
            IParams reportParams, ReportType reportType, Location location) {
        super(sponsorGroup);
        this.summaryReports = summaryReports;
        this.reportParams = reportParams;
        this.reportType = reportType;
        this.location = location;
    }

    public void setTotalHouseHolds(int totalHouseHolds) {
        this.totalHouseHolds = totalHouseHolds;
    }

    @Override
    protected List<PdfPCell> generateHeaders() {

        PdfPCell cell;
        List<PdfPCell> headers = new ArrayList<PdfPCell>();

        cell = generateSecondHeaderCell(conf.getString("pdf.table.summary.customerLocationCode"));
        cell.setRowspan(2);
        headers.add(cell);

        cell = generateSecondHeaderCell(conf.getString("pdf.table.summary.sponsorLocationName"));
        cell.setRowspan(2);
        headers.add(cell);

        if (reportType.equals(ReportType.projected)) {
            cell = generateSecondHeaderCell(conf.getString("pdf.table.summary.location.distance") + SPACE + conf.getString("pdf.table.decay.projected"));
            cell.setRowspan(2);
            headers.add(cell);
        } else {
            cell = generateSecondHeaderCell(conf.getString("pdf.table.summary.location.distance") + SPACE + conf.getString("pdf.table.summary.location.short") + SPACE + location.getCustomerLocationCode()+ SPACE + conf.getString("pdf.table.summary.location.km"));
            cell.setRowspan(2);
            headers.add(cell);
        }

        cell = generateSecondHeaderCell(conf.getString("pdf.table.summary.location.collectors"), TEXT_ALIGNEMENT_CENTER);
        cell.setColspan(3);
        headers.add(cell);

        cell = generateSecondHeaderCell(conf.getString("pdf.table.summary.location.basetrans"), TEXT_ALIGNEMENT_CENTER);
        cell.setColspan(3);
        headers.add(cell);

        cell = generateSecondHeaderCell(conf.getString("pdf.table.summary.location.baseam.spend"), TEXT_ALIGNEMENT_CENTER);
        cell.setColspan(3);
        headers.add(cell);

        cell = generateSecondHeaderCell(conf.getString("pdf.table.summary.total.spend"));
        cell.setRowspan(2);
        headers.add(cell);

        cell = generateSecondHeaderCell(conf.getString("pdf.table.summary.location.baseam.units"), TEXT_ALIGNEMENT_CENTER);
        cell.setColspan(3);
        headers.add(cell);

        cell = generateSecondHeaderCell(conf.getString("pdf.table.summary.total.units"));
        cell.setRowspan(2);
        headers.add(cell);

        return headers;
    }

    /**
     * ydumais [2011-01-04]: changed spend for base mile. Kept commented because
     * we might want both columns later
     *
     * @return
     */
    @Override
    protected List<PdfPCell> generateCells() {
        List<PdfPCell> cells = new ArrayList<PdfPCell>();
        double locSpendCumul = getLocationSpendCumul();
        double locUnitsCumul = getLocationUnitsCumul();
        double totLocCollectors = 0;
        double totLocTransactions = 0;
        double totLocSpend = 0;
        double totPlocSpendCumul = 0;
        double totLocUnits = 0;
        double totPlocUnitsCumul = 0;

        double cumLocTotCollectors = 0;
        double cumPercentOfTotCollectors = 0;
        double cumLocTotTransactions = 0;
        double cumPercentOfTotTransactions = 0;
        double cumLocTotSpend = 0;
        double cumPercentOfTotSpend = 0;
        double cumLocTotUnits = 0;
        double cumPercentOfTotUnits = 0;

        String storeTotal = conf.getString("pdf.table.summary.location.storeTotal");
        String withinTA = conf.getString("pdf.table.summary.location.withinTA");
        String percentOfTotal = conf.getString("pdf.table.summary.location.percentOfTotal");

        Collections.sort(summaryReports, new SummaryReportComparator());
        Collections.reverse(summaryReports);

        int count = Math.min(summaryReports.size(), TOP_REPORT);
        boolean addSubHeaders = true;

        for (int i = 0; i < count; i++) {

            if (addSubHeaders) {
                for (int j = 0; j < TOTAL_SUBHEADERS; j++) {
                    cells.add(generateSubheader(storeTotal));
                    cells.add(generateSubheader(withinTA));
                    cells.add(generateSubheader(percentOfTotal));
                }
                addSubHeaders = false;
            }

            cells.add(generateCell(summaryReports.get(i).getCustomerLocationCode()));
            cells.add(generateCell(summaryReports.get(i).getLocationName()));
            cells.add(generateCell(toString(summaryReports.get(i).getDistance(), 1)));

            // Collectors
            double locTotCollectors = summaryReports.get(i).getTotalCollectors();
            double locCollectors = summaryReports.get(i).getCollectors();
            double percentOfTotCollectors = locCollectors / locTotCollectors;

            // Transactions
            int locTotTransactions = summaryReports.get(i).getTotalTransactions();
            int locTransactions = summaryReports.get(i).getTransactions();
            double percentOfTotTransactions = ((double) locTransactions) / ((double) locTotTransactions);

            // Spend
            double locTotSpend = summaryReports.get(i).getTotalSpends();
            double locSpend = summaryReports.get(i).getSpends();
            double percentOfTotSpend = locSpend / locTotSpend;

            double plocSpendCumul = locSpend / locSpendCumul;

            // Units
            double locTotUnits = summaryReports.get(i).getTotalUnits();
            double locUnits = summaryReports.get(i).getUnits();
            double percentOfTotUnits = locUnits / locTotUnits;

            double plocUnitsCumul = locUnits / locUnitsCumul;

            // Add Collector cells
            cells.add(generateCell(locTotCollectors, 0));
            cells.add(generateCell(locCollectors, 0));
            cells.add(generateRightAlignCell(toPercent(percentOfTotCollectors, 1)));

            // Add Transactions cells
            cells.add(generateCell(locTotTransactions, 0));
            cells.add(generateCell(locTransactions, 0));
            cells.add(generateRightAlignCell(toPercent(percentOfTotTransactions, 1)));

            // Add Spend cells
            cells.add(generateRightAlignCell(toMoney(locTotSpend, 0)));
            cells.add(generateRightAlignCell(toMoney(locSpend, 0)));
            cells.add(generateRightAlignCell(toPercent(percentOfTotSpend, 1)));

            // Add the % total cell spend
            cells.add(generateRightAlignCell(toPercent(plocSpendCumul, 1)));

            // Add units cells
            cells.add(generateCell(locTotUnits, 0));
            cells.add(generateCell(locUnits, 0));
            cells.add(generateRightAlignCell(toPercent(percentOfTotUnits, 1)));

            // Add the % total cell units
            cells.add(generateRightAlignCell(toPercent(plocUnitsCumul, 1)));

            // Track cumulative Collectors, Transactions, and Last Cell
            cumLocTotCollectors += locTotCollectors;
            totLocCollectors += locCollectors;
            cumPercentOfTotCollectors += percentOfTotCollectors;

            cumLocTotTransactions += locTotTransactions;
            totLocTransactions += locTransactions;
            cumPercentOfTotTransactions += percentOfTotTransactions;

            cumLocTotSpend += locTotSpend;
            totLocSpend += locSpend;
            cumPercentOfTotSpend += percentOfTotSpend;

            totPlocSpendCumul += plocSpendCumul;

            cumLocTotUnits += locTotUnits;
            totLocUnits += locUnits;
            cumPercentOfTotUnits += percentOfTotUnits;

            totPlocUnitsCumul += plocUnitsCumul;

        }

        if (count == TOP_REPORT) {
            // We have already shown numbers for the first 10 location.
            // Numbers for the remaining locations will be summed and displated in a single row
            int nb = 0;

            int otherLocTotCollectors = 0;
            int otherCollectorCount = 0;
            double otherLocPercentOfCollectors = 0.0;

            int otherLocTotTransactions = 0;
            int otherTransactionCount = 0;
            double otherLocPercentOfTransactions = 0.0;

            double otherLocTotSpend = 0.0;
            double otherSpend = 0.0;
            double otherLocPercentOfSpend = 0.0;

            double otherLocTotUnits = 0.0;
            double otherUnits = 0.0;
            double otherLocPercentOfUnits = 0.0;

            for (int i = 10; i < summaryReports.size(); i++) {
                nb++;
                SummaryReport report = summaryReports.get(i);

                otherLocTotCollectors += report.getTotalCollectors();
                otherCollectorCount += report.getCollectors();

                otherLocTotTransactions += report.getTotalTransactions();
                otherTransactionCount += report.getTransactions();

                otherLocTotSpend += report.getTotalSpends();
                otherSpend += report.getSpends();

                otherLocTotUnits += report.getTotalUnits();
                otherUnits += report.getUnits();

            }
            otherLocPercentOfCollectors = ((double) otherCollectorCount / (double) otherLocTotCollectors);
            otherLocPercentOfTransactions = (double) otherTransactionCount / otherLocTotTransactions;
            otherLocPercentOfSpend = (double) otherSpend / otherLocTotSpend;
            otherLocPercentOfUnits = (double) otherUnits / otherLocTotUnits;
            double otherSpendCumul = otherSpend / locSpendCumul;

            double otherUnitsCumul = otherUnits / locUnitsCumul;

            // ADD the cumulative numbers (sums of the first 10 shown locations) TO numbers of other locations
            cumLocTotCollectors += otherLocTotCollectors;
            totLocCollectors += otherCollectorCount;

            cumLocTotTransactions += otherLocTotTransactions;
            totLocTransactions += otherTransactionCount;

            cumLocTotSpend += otherLocTotSpend;
            totLocSpend += otherSpend;

            totPlocSpendCumul += otherSpendCumul;

            cumLocTotUnits += otherLocTotUnits;
            totLocUnits += otherUnits;

            totPlocUnitsCumul += otherUnitsCumul;

            // Add second-last row, the sum of the remaining locations
            cells.add(generateCell(MessageFormat.format(conf.getString("pdf.table.summary.other"), nb)));
            cells.add(generateNoValueCell());
            cells.add(generateNoValueCell());

            cells.add(generateCell(otherLocTotCollectors, 0));
            cells.add(generateCell(otherCollectorCount, 0));
            cells.add(generateRightAlignCell(toPercent(otherLocPercentOfCollectors, 1)));

            cells.add(generateCell(otherLocTotTransactions, 0));
            cells.add(generateCell(otherTransactionCount, 0));
            cells.add(generateRightAlignCell(toPercent(otherLocPercentOfTransactions, 1)));

            cells.add(generateRightAlignCell(toMoney(otherLocTotSpend, 0)));
            cells.add(generateRightAlignCell(toMoney(otherSpend, 0)));
            cells.add(generateRightAlignCell(toPercent(otherLocPercentOfSpend, 0)));

            cells.add(generateRightAlignCell(toPercent(otherSpendCumul, 1)));

            cells.add(generateCell(otherLocTotUnits, 0));
            cells.add(generateCell(otherUnits, 0));
            cells.add(generateRightAlignCell(toPercent(otherLocPercentOfUnits, 0)));

            cells.add(generateRightAlignCell(toPercent(otherSpendCumul, 1)));
        }

        // Add The last row of the table. This shows the summary of all the locations
        cells.add(generateTotalCell(conf.getString("pdf.table.summary.summary")));
        cells.add(generateTotalCell(""));
        cells.add(generateTotalCell(""));

        cumPercentOfTotCollectors = totLocCollectors / cumLocTotCollectors;
        cumPercentOfTotTransactions = totLocTransactions / cumLocTotTransactions;
        cumPercentOfTotSpend = totLocSpend / cumLocTotSpend;
        cumPercentOfTotUnits = totLocUnits / cumLocTotUnits;

        cells.add(generateTotalCell(cumLocTotCollectors, 0));
        cells.add(generateTotalCell(totLocCollectors, 0));
        cells.add(generateRightAlignTotalCell(toPercent(cumPercentOfTotCollectors, 1)));

        cells.add(generateTotalCell(cumLocTotTransactions, 0));
        cells.add(generateTotalCell(totLocTransactions, 0));
        cells.add(generateRightAlignTotalCell(toPercent(cumPercentOfTotTransactions, 1)));

        cells.add(generateRightAlignTotalCell(toMoney(cumLocTotSpend, 0)));
        cells.add(generateRightAlignTotalCell(toMoney(totLocSpend, 0)));
        cells.add(generateRightAlignTotalCell(toPercent(cumPercentOfTotSpend, 0)));

        cells.add(generateRightAlignTotalCell(toPercent(totPlocSpendCumul, 1)));

        cells.add(generateTotalCell(cumLocTotUnits, 0));
        cells.add(generateTotalCell(totLocUnits, 0));
        cells.add(generateRightAlignTotalCell(toPercent(cumPercentOfTotUnits, 0)));

        cells.add(generateRightAlignTotalCell(toPercent(totPlocUnitsCumul, 1)));

        return cells;
    }

    @Override
    protected int[] generateRelativeWidths() {
        return new int[]{12, 15, 10, 10, 9, 7, 10, 9, 7, 11, 11, 7, 8, 11, 11, 7, 8};
    }

    @Override
    protected void getFirstHeaders(PdfPTable datatable, int size) {
        Formatter formatter = new Formatter();
        StringBuilder sb = new StringBuilder();

        if (location != null) {
            sb.append(conf.getString("pdf.table.summary.customerLocationCode"));
            sb.append(":").append(SPACE);
            sb.append(location.getCustomerLocationCode()).append(SPACE);

            sb.append(LF).append(conf.getString("pdf.table.summary.sponsorLocationName"));
            sb.append(":").append(SPACE);
            sb.append(location.getLocationName()).append(SPACE);
        } else {
            sb.append(conf.getString("pdf.table.decay.projected"));
        }
      

        if (reportType.equals(reportType.issuance)) {
            sb.append(LF).append((int) (reportParams.issuance() * 100)).append(PERCENT).append(SPACE);
            sb.append(conf.getString("pdf.table.summary.tradearea.spend"));
        } else if (reportType.equals(reportType.units)) {
            sb.append(LF).append((int) (reportParams.issuance() * 100)).append(PERCENT).append(SPACE);
            sb.append(conf.getString("pdf.table.summary.tradearea.unit"));
        } else if (reportType.equals(reportType.distance)) {
            sb.append(LF).append(conf.getString("pdf.table.summary.tradearea")).append(":").append(SPACE);
            sb.append(reportParams.distance()).append(SPACE).append(KM).append(SPACE);
        } else if (reportType.equals(reportType.custom)) {
            sb.append(LF).append(conf.getString("pdf.table.summary.location")).append(SPACE);
            sb.append(location.getCode()).append(":").append(SPACE);
            sb.append(conf.getString("pdf.table.summary.custom")).append(SPACE);
            sb.append(conf.getString("pdf.table.summary.tradearea"));
        } else {
            sb.append(LF).append(conf.getString("pdf.table.decay.projected"));
            sb.append(":").append(SPACE).append(reportParams.projected()).append(SPACE);
            sb.append(conf.getString("pdf.table.summary.projected"));
        }
        sb.append(LF).append(conf.getString("pdf.table.summary.cpcHouseholds")).append(": ").append(formatter.getNumberNumberFormat().format(totalHouseHolds)).append(LF);

        PdfPCell currentCell = generateFirstHeaderCell(sb.toString());
        currentCell.addElement(datatable);
        currentCell.setColspan(getHeadersSize(null));
        datatable.addCell(currentCell);
    }

    private double getLocationSpendCumul() {
        double sum = 0;
        for (int i = 0; i < summaryReports.size(); i++) {
            sum += summaryReports.get(i).getSpends();
        }
        return sum;
    }

    private double getLocationUnitsCumul() {
        double sum = 0;
        for (int i = 0; i < summaryReports.size(); i++) {
            sum += summaryReports.get(i).getUnits();
        }
        return sum;
    }

    protected static class SummaryReportComparator implements Comparator<SummaryReport> {

        @Override
        public int compare(SummaryReport rep1, SummaryReport rep2) {
            return Double.compare(rep1.getSpends(), rep2.getSpends());
        }
    }
}
