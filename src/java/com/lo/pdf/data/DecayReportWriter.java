/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lo.pdf.data;

import com.lo.db.om.Location;
import com.lo.db.om.SponsorGroup;
import com.lo.db.om.Transaction;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author slajoie
 */
public class DecayReportWriter extends DataWriter {

    private List<Transaction> locationTransactions;
    private List<Transaction> sponsorTransactions;
    private List<Transaction> amTransactions;
    private Location location = null;
    private final static String[] distanceBands = new String[]{"0-2", "2-5", "5-10", "10-15", "15-25", "25-30", "30-50", "50+", "N/A"};

    public DecayReportWriter(SponsorGroup sponsorGroup,
            List<Transaction> locationTransactions,
            List<Transaction> sponsorTransactions,
            List<Transaction> amTransactions,
            Location location) {
        super(sponsorGroup);
        this.locationTransactions = locationTransactions;
        this.sponsorTransactions = sponsorTransactions;
        this.amTransactions = amTransactions;
        this.location = location;

        fixNullValues(this.locationTransactions);
        fixNullValues(this.sponsorTransactions);
        fixNullValues(this.amTransactions);
        
        fontSecondHeader = FontFactory.getFont(FontFactory.HELVETICA, 8, Font.BOLD);
    }

    @Override
    protected List<PdfPCell> generateHeaders() {

        PdfPCell cell;
        List<PdfPCell> headers = new ArrayList<PdfPCell>();

        cell = generateSecondHeaderCell(conf.getString("pdf.table.decay.distance"));
        headers.add(cell);

        cell = generateSecondHeaderCell(conf.getString("pdf.table.decay.airmiles"));
        headers.add(cell);

        cell = generateSecondHeaderCell(MessageFormat.format(conf.getString("pdf.table.decay.activecollector"), sponsorGroup.getRollupGroupName()));
        headers.add(cell);

        cell = generateSecondHeaderCell(conf.getString("pdf.table.decay.penetration"));
        headers.add(cell);
        
        if (location != null) {
            
            String codeLocation = conf.getString("pdf.table.decay.loc") + SPACE + location.getCustomerLocationCode()+ SPACE;

            cell = generateSecondHeaderCell(codeLocation +  conf.getString("pdf.table.decay.collectors"));
            headers.add(cell);

            cell = generateSecondHeaderCell(conf.getString("pdf.table.decay.pourcentage") + SPACE + codeLocation+ conf.getString("pdf.table.decay.collectors"));
            headers.add(cell);

            cell = generateSecondHeaderCell(codeLocation +  conf.getString("pdf.table.decay.txns"));
            headers.add(cell);

            cell = generateSecondHeaderCell(codeLocation + conf.getString("pdf.table.decay.amspend"));
            headers.add(cell);

            cell = generateSecondHeaderCell(MessageFormat.format(conf.getString("pdf.table.decay.proportionLocationSpend"), location.getCustomerLocationCode()));
            headers.add(cell);

            cell = generateSecondHeaderCell(conf.getString("pdf.table.decay.runningtot"));
            headers.add(cell);

            cell = generateSecondHeaderCell(codeLocation + conf.getString("pdf.table.decay.spendtxn"));
            headers.add(cell);
            
            cell = generateSecondHeaderCell(codeLocation + conf.getString("pdf.table.decay.amunits"));
            headers.add(cell);
            
            cell = generateSecondHeaderCell(MessageFormat.format(conf.getString("pdf.table.decay.proportionLocationUnits"), location.getCustomerLocationCode()));
            headers.add(cell);
            
            cell = generateSecondHeaderCell(conf.getString("pdf.table.decay.runningtot"));
            headers.add(cell);  
            
            cell = generateSecondHeaderCell(codeLocation +  conf.getString("pdf.table.decay.unitstxn"));
            headers.add(cell);     
        } else {

            cell = generateSecondHeaderCell(HYPHEN);
            headers.add(cell);

            cell = generateSecondHeaderCell(HYPHEN);
            headers.add(cell);

            cell = generateSecondHeaderCell(HYPHEN);
            headers.add(cell);

            cell = generateSecondHeaderCell(HYPHEN);
            headers.add(cell);

            cell = generateSecondHeaderCell(HYPHEN);
            headers.add(cell);

            cell = generateSecondHeaderCell(HYPHEN);
            headers.add(cell);

            cell = generateSecondHeaderCell(HYPHEN);
            headers.add(cell);
            
            cell = generateSecondHeaderCell(HYPHEN);
            headers.add(cell);
            
            cell = generateSecondHeaderCell(HYPHEN);
            headers.add(cell);
            
            cell = generateSecondHeaderCell(HYPHEN);
            headers.add(cell);
            
            cell = generateSecondHeaderCell(HYPHEN);
            headers.add(cell);
        }
        //this is to apply the light blue to the second level header of the distance decay report
        for (PdfPCell pdfPCell : headers) {
            pdfPCell.setBackgroundColor(secondHeaderColor);
        }
        return headers;
    }

    @Override
    protected List<PdfPCell> generateCells() {
        List<PdfPCell> cells = new ArrayList<PdfPCell>();
        double sumLocSpend = this.getSumLocSpend();
        double sumLocUnits = this.getSumLocUnits();
        double pLocSpendCumul = 0;
        double pLocUnitsCumul = 0;
        double totAmCollectors = 0;
        double totSpCollectors = this.getSumCollectors();
        double totLocCollectors = this.getSumLocationCollectors();
        double totPLocAllColl = 0;
        double totLocTxns = 0;
        double totLocSpend = 0;
        double totPLocSpend = 0;
        double totLocUnits = 0;
        double totPLocUnits = 0;

        for (int i = 0; i < distanceBands.length; i++) {

            cells.add(generateCell(distanceBands[i]));

            if (i < 7) {
                double amCollectors = 0;
                double spCollectors = 0;
                if (i != 0) {
                    amCollectors = amTransactions.get(i).getCollectors() - amTransactions.get(i - 1).getCollectors();
                    spCollectors = sponsorTransactions.get(i).getCollectors() - sponsorTransactions.get(i - 1).getCollectors();
                } else {
                    amCollectors = amTransactions.get(i).getCollectors();
                    spCollectors = sponsorTransactions.get(i).getCollectors();
                }
                double pSpAm = (spCollectors / amCollectors);
                cells.add(generateCell(amCollectors, 0));
                cells.add(generateCell(spCollectors, 0));
                cells.add(generateRightAlignCell(toPercent(pSpAm, 1)));

                // totaux
                totAmCollectors += amCollectors;

            } else {

                cells.add(generateNoValueCell());
                cells.add(generateNoValueCell());
                cells.add(generateNoValueCell());
            }

            if (location != null) {
                double locCollectors = locationTransactions.get(i).getCollectors();
                double pLocAllColl = locCollectors / totLocCollectors;
                cells.add(generateCell(locCollectors, 0));
                cells.add(generateRightAlignCell(toPercent(pLocAllColl, 1)));

                double locTxns = locationTransactions.get(i).getTransactions();
                double locSpend = locationTransactions.get(i).getSpend();
                double pSpendTxns = locSpend / locTxns;
                double pLocSpend = locSpend / sumLocSpend;
                pLocSpendCumul += pLocSpend;
                cells.add(generateCell(locTxns, 0));
                cells.add(generateRightAlignCell(toMoney(locSpend, 0)));
                cells.add(generateRightAlignCell(toPercent(pLocSpend, 1)));
                cells.add(generateRightAlignCell(toPercent(pLocSpendCumul, 1)));
                cells.add(generateRightAlignCell(toMoney(pSpendTxns, 0)));
                
                double locUnits = locationTransactions.get(i).getUnits();
                double pUnitsTxns = locUnits / locTxns;
                double pLocUnits = locUnits / sumLocUnits;
                pLocUnitsCumul += pLocUnits;
                cells.add(generateCell(locUnits, 0));
                cells.add(generateRightAlignCell(toPercent(pLocUnits, 1)));
                cells.add(generateRightAlignCell(toPercent(pLocUnitsCumul, 1)));
                cells.add(generateCell(pUnitsTxns, 0));

//                totLocCollectors += locCollectors;
                totPLocAllColl += pLocAllColl;
                totLocTxns += locTxns;
                totLocSpend += locSpend;
                totPLocSpend += pLocSpend;
                totLocUnits += locUnits;
                totPLocUnits += pLocUnits;
            } else {
                cells.add(generateNoValueCell());
                cells.add(generateNoValueCell());
                cells.add(generateNoValueCell());
                cells.add(generateNoValueCell());
                cells.add(generateNoValueCell());
                cells.add(generateNoValueCell());
                cells.add(generateNoValueCell());
                cells.add(generateNoValueCell());
                cells.add(generateNoValueCell());
                cells.add(generateNoValueCell());
                cells.add(generateNoValueCell());
            }

        }

        // add tot cells
        cells.add(generateTotalCell(conf.getString("pdf.table.decay.summary")));
        cells.add(generateTotalCell(totAmCollectors, 0));
        cells.add(generateTotalCell(totSpCollectors, 0));
        cells.add(generateRightAlignTotalCell(toPercent(totSpCollectors / totAmCollectors, 1)));
        if (location != null) {
            cells.add(generateTotalCell(totLocCollectors, 0));
            cells.add(generateRightAlignTotalCell(toPercent(totPLocAllColl, 1)));
            cells.add(generateTotalCell(totLocTxns, 0));
            cells.add(generateRightAlignTotalCell(toMoney(totLocSpend, 2)));
            cells.add(generateRightAlignTotalCell(toPercent(totPLocSpend, 1)));
            cells.add(generateTotalCell(""));
            cells.add(generateRightAlignTotalCell(toMoney(totLocSpend / totLocTxns, 0)));
            cells.add(generateTotalCell(totLocUnits, 0));
            cells.add(generateRightAlignTotalCell(toPercent(totPLocUnits, 1)));
            cells.add(generateTotalCell(""));
            cells.add(generateTotalCell(totLocUnits / totLocTxns, 0));
        } else {
            cells.add(generateTotalCell(""));
            cells.add(generateTotalCell(""));
            cells.add(generateTotalCell(""));
            cells.add(generateTotalCell(""));
            cells.add(generateTotalCell(""));
            cells.add(generateTotalCell(""));
            cells.add(generateTotalCell(""));
            cells.add(generateTotalCell(""));
            cells.add(generateTotalCell(""));
            cells.add(generateTotalCell(""));
            cells.add(generateTotalCell(""));
        }

        return cells;
    }

    private Double getSumLocSpend() {
        Double sum = 0.0;
        if (locationTransactions != null) {
            for (Transaction transaction : locationTransactions) {
                sum += transaction.getSpend();
            }
        }
        return sum;
    }
    
    private Double getSumLocUnits() {
        Double sum = 0.0;
        if (locationTransactions != null) {
            for (Transaction transaction : locationTransactions) {
                sum += transaction.getUnits();
            }
        }
        return sum;
    }

    private Double getSumCollectors() {
        double sum = 0;
        if (sponsorTransactions != null) {
            for (int i = 0; i < sponsorTransactions.size(); i++) {
                double spCollectors;
                if (i != 0) {
                    spCollectors = sponsorTransactions.get(i).getCollectors() - sponsorTransactions.get(i - 1).getCollectors();
                } else {
                    spCollectors = sponsorTransactions.get(i).getCollectors();
                }
                sum += spCollectors;
            }
        }
        return sum;
    }

    private Double getSumLocationCollectors() {
        double sum = 0;
        if (locationTransactions != null) {
            for (int i = 0; i < locationTransactions.size(); i++) {
                sum += locationTransactions.get(i).getCollectors();
            }
        }
        return sum;
    }

    private void fixNullValues(List<Transaction> transactions) {
        for (Transaction transaction : transactions) {
            transaction.setCollectors(transaction.getCollectors() == null ? 0 : transaction.getCollectors());
            transaction.setSpend(transaction.getSpend() == null ? 0 : transaction.getSpend());
            transaction.setTransactions(transaction.getTransactions() == null ? 0 : transaction.getTransactions());
            transaction.setUnits(transaction.getUnits() == null ? 0 : transaction.getUnits());
        }
    }

    @Override
    protected int[] generateRelativeWidths() {
        return new int[]{10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10, 10};
    }

    @Override
    protected void getFirstHeaders(PdfPTable datatable, int size) {
        StringBuilder sb = new StringBuilder();

        if (location != null) {
            
            sb.append(conf.getString("pdf.table.decay.customerLocationCode")).append(SPACE);
            sb.append(":").append(SPACE);
            sb.append(location.getCustomerLocationCode()).append(SPACE);
            
            sb.append(LF).append(conf.getString("pdf.table.decay.sponsorLocationName")).append(SPACE);
            sb.append(":").append(SPACE);
            sb.append(location.getLocationName()).append(LF);
            
        } else {
            sb.append(conf.getString("pdf.table.decay.projected"));
        }

        PdfPCell currentCell = generateFirstHeaderCell(sb.toString());
        currentCell.addElement(datatable);
        currentCell.setColspan(size);
        datatable.addCell(currentCell);
    }

    @Override
    protected int getHeadersSize(List<PdfPCell> headers) {
        return headers.size();

    }
}
