package com.lo;

import com.lo.analysis.tradearea.TradeArea;
import com.lo.db.om.SponsorGroup;
import com.lo.db.om.User;
import com.lo.pdf.PDFBean;
import com.lo.util.SponsorFilteringManager;
import com.spinn3r.log5j.Logger;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpSession;

/**
 * Class used to encapsulate session relevant information
 *
 * @author ydumais
 */
public class ContextParams implements Serializable {

    private static final Logger LOGGER = Logger.getLogger();
    public static final String SESSION_ATTRIBUTE_NAME = "contextParams";
    private User user;
    private SponsorGroup sponsor;
//    private Map<String, EnumMap<Analysis, String>> layerIds = new HashMap<String, EnumMap<Analysis, String>>();
    private PDFBean pdf;
    private boolean pdfProcessing;
    private File tempDir = null;
    private Set<String> qualityOfDataRules = new HashSet<>();
    private List<String> selectionPKs;
    private String sponsorCodeFilter;
    private String sponsorKeyFilter;
    private String sponsorCodesList;
    private String sponsorCodesDisplayList;
    private String sponsorKeysList;
    private List<String> selectedSponsorCodes;
    private List<Integer> selectedSponsorKeys;
    private String slaTansactionValue;
    private String dateType;

    // maps location keys to trade areas.
    private Map<Double, List<TradeArea>> tradeAreas;

    public String consumerKey;
    public String token;
    public String uid;
    public String domain;
    public String languagePref;

    public ContextParams() {
        sponsorCodeFilter = "";
        tradeAreas = new LinkedHashMap<>();
    }

    public static ContextParams get(HttpSession session) {
        ContextParams cp = (ContextParams) session.getAttribute(SESSION_ATTRIBUTE_NAME);
        if (cp == null) {
            cp = new ContextParams();
        }
        return cp;
    }

    public void set(HttpSession session) {
        session.setAttribute(SESSION_ATTRIBUTE_NAME, this);
    }

    public String getSponsorCodeFilter() {
        return sponsorCodeFilter;
    }

    public void setSelectedSponsorCodes(List<String> sponsorCodes, boolean setKeys) {
        this.sponsorCodesList = SponsorFilteringManager.get().getCodesList(sponsorCodes);
        this.sponsorCodesDisplayList = SponsorFilteringManager.get().getCodesDisplayList(sponsorCodes);
        this.sponsorCodeFilter = SponsorFilteringManager.get().getCodesFilter(sponsorCodes);
        this.selectedSponsorCodes = sponsorCodes;

        if (setKeys) {
            setKeysFromCodes(sponsorCodes);
        }
    }

    private void setKeysFromCodes(List<String> codes) {
        List<Integer> selectedKeys = new ArrayList<>(codes.size());
        for (String code : codes) {
            Integer key = sponsor.getKeyFromCode(code);
            selectedKeys.add(key);
        }

        setSelectedSponsorKeys(selectedKeys);
    }

    public String getSponsorKeyFilter() {
        return sponsorKeyFilter;
    }

    public void setSelectedSponsorKeys(List<Integer> sponsorKeys) {
        this.sponsorKeysList = SponsorFilteringManager.get().getKeysList(sponsorKeys);
        this.sponsorKeyFilter = SponsorFilteringManager.get().getKeysFilter(sponsorKeys);
        this.selectedSponsorKeys = sponsorKeys;
    }

    public SponsorGroup getSponsor() {
        return sponsor;
    }

    public void setSponsor(SponsorGroup sponsor) {
        this.sponsor = sponsor;

        setSelectedSponsorCodes(sponsor.getCodes(), false);
        setSelectedSponsorKeys(sponsor.getKeys());
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getWorkspaceKey() {
        return getSponsor().getWorkspaceKey();
    }

    public PDFBean getPdf() {
        return pdf;
    }

    public void setPdf(PDFBean pdf) {
        this.pdf = pdf;
    }

    public File getTempDir() {
        return tempDir;
    }

    public void setTempDir(File tempFile) {
        this.tempDir = tempFile;
    }

    public Set<String> getQualityOfDataRules() {
        return qualityOfDataRules;
    }

    public List<String> getSelectionPKs() {
        return selectionPKs;
    }

    public void setSelectionPKs(List<String> selectionPKs) {
        this.selectionPKs = selectionPKs;
    }

    /**
     * @return the pdfProcessing
     */
    public boolean isPdfProcessing() {
        return pdfProcessing;
    }

    /**
     * @param pdfProcessing the pdfProcessing to set
     */
    public void setPdfProcessing(boolean pdfProcessing) {
        this.pdfProcessing = pdfProcessing;
    }

    public List<String> getSelectedSponsorCodes() {
        return selectedSponsorCodes;
    }

    public List<Integer> getSelectedSponsorKeys() {
        return selectedSponsorKeys;
    }

    public String getSponsorCodesList() {
        return sponsorCodesList;
    }

    public String getSponsorKeysList() {
        return sponsorKeysList;
    }

    public String getSponsorCodesDisplayList() {
        return sponsorCodesDisplayList;
    }

    public void setTradeAreas(List<TradeArea> tradeAreaList) {
        this.tradeAreas.clear();

        for (TradeArea ta : tradeAreaList) {
            List<TradeArea> locationList = this.tradeAreas.get(ta.getLocationKey());
            if (locationList == null) {
                locationList = new ArrayList<>();
                this.tradeAreas.put(ta.getLocationKey(), locationList);
            }
            locationList.add(ta);
        }

    }

    public Map<Double, List<TradeArea>> getTradeAreas() {
        return tradeAreas;
    }

    public void setSlaTansactionValue(String slaTansactionValue) {
        this.slaTansactionValue = slaTansactionValue;
    }

    public String getSlaTansactionValue() {
        return this.slaTansactionValue;
    }
}
