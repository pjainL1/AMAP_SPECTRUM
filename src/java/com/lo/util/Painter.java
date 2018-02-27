package com.lo.util;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

/**
 *
 * @author ydumais
 */
public class Painter {

    private ResourceBundle rb;
    private final String tradeAreaTemplate;
    private final String tradeAreaCustomTemplate;
    private final String tradeAreaCustomTemplateHidden;
    private final String nWatchTemplate;
    private final int cycle;
    private Set<Integer> usedColors = null;
    private int minAvailable = 0;
    private boolean customSeen = false;
    
    public Painter() {
        rb = ResourceBundle.getBundle("com.lo.util.color");
        tradeAreaTemplate = rb.getString("ta.template");
        tradeAreaCustomTemplate = rb.getString("ta.template.custom");
        tradeAreaCustomTemplateHidden = rb.getString("ta.template.hidden");
        nWatchTemplate = rb.getString("nw.template");
        cycle = Integer.parseInt(rb.getString("color.cycle"));
        usedColors = new HashSet<Integer>();
    }
    public String getColor(int idx) {
        return getColor(idx, false);
    }

    public String getColor(int idx, boolean forceColor) {
        int hash = Math.abs(idx);
        int colNumber = hash % cycle;
        if ((minAvailable < cycle - 1) && (forceColor || colorAreadyUsed(colNumber))) {
            Integer nextColor = getNextAvailableColor();
            if (nextColor != -1) {
                colNumber = nextColor;
            }
        }
        if (!forceColor) {
            addColor(colNumber);
        }
        return rb.getString("color." + (colNumber));
    }

    /**
     * The rendition color attributed is related to the object hash.
     */
    public String getTradeAreaRendition(String code, String taColorFromDB) {
        if (!"".equalsIgnoreCase(taColorFromDB)) {
            return MessageFormat.format(tradeAreaTemplate, taColorFromDB);
        } else {
            return getRendition(tradeAreaTemplate, code.hashCode());
        }
    }

    /**
     * The rendition color attributed is related to the object hash.
     */
    public String getNWatchRendition(Double key, String nwatchColorFromDB) {
        if (!"".equalsIgnoreCase(nwatchColorFromDB)) {
            return MessageFormat.format(nWatchTemplate, nwatchColorFromDB);
        } else {
            return getRendition(nWatchTemplate, key.hashCode());
        }
    }

    /**
     * The rendition color attributed is related to the object index in its
     * collection. The color collection is cycled once the idx surpasses the
     * number of different colors
     *
     * @param idx
     * @return
     */
    private String getRendition(String template, int idx) {
        String color = getColor(idx, true);
        return MessageFormat.format(template, color);
    }

    public String getCustomTARendition() {
        try {
            return getRendition(customSeen ? tradeAreaCustomTemplateHidden : tradeAreaCustomTemplate, 0);
        } finally {
            customSeen = true;
        }
    }

    public String getDefaultRendition() {
        return getRendition(tradeAreaTemplate, 0);
    }

    private void addColor(Integer colNumber) {
        usedColors.add(colNumber);
    }

    private boolean colorAreadyUsed(Integer colNumber) {
        return usedColors.contains(colNumber);
    }

    private Integer getNextAvailableColor() {
        for (int i = minAvailable; i < cycle; i++) {
            if (!colorAreadyUsed(i)) {
                minAvailable = i;
                return i;
            }
        }
        return -1;
    }

    public void resetUsedColors() {
            usedColors.clear();
            minAvailable = 0;
    }
}
