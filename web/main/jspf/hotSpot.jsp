<%-- 
    Document   : hotspot
    Created on : 2015-10-02, 14:10:58
    Author     : scavanagh
--%>

<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<fmt:setBundle basename="loLocalString" />
<div class="hotspotCompare"><fmt:message key="hotspot.menu.compare.comparisonAnalysis"/></div>
<div class="hotspotSingle">
    <input id="hotSpotChkAirMiles" value="airMiles" type="checkbox"/><label for="hotSpotChkAirMiles"><fmt:message key="hotspot.menu.amb"/></label>
</div>
<div>
    <input id="hotSpotChkSponsor" value="sponsor" type="checkbox"/><label for="hotSpotChkSponsor"><fmt:message key="hotspot.menu.sponsor"/></label>
</div>
<div>
    <input id="hotSpotChkLocations" value="locations" type="checkbox"/><label for="hotSpotChkLocations"><fmt:message key="hotspot.menu.locations"/></label>
</div>
<div class="menuSectionSeparator"></div>
<div class="hotspotSingle">
    <div>
        <input id="hotSpotRadioSpend" type="radio" name="hotSpotRadioSingle" value="spend" checked/><label for="hotSpotRadioSpend"><fmt:message key="hotspot.menu.spend"/></label>
    </div>
    <div>
        <input id="hotSpotRadioUnit" type="radio" name="hotSpotRadioSingle" value="unit" checked/><label for="hotSpotRadioUnit"><fmt:message key="hotspot.menu.unit"/></label>
    </div>
    <div>
        <input id="hotSpotRadioCollector" type="radio" name="hotSpotRadioSingle" value="collector"/><label for="hotSpotRadioCollector"><fmt:message key="hotspot.menu.collector"/></label>
    </div>
</div>

<div class="hotspotCompare">
    <div>
        <select id="comparisonType">
            <option value="blended" selected><fmt:message key="hotspot.menu.blended"/></option>
            <option value="growth"><fmt:message key="hotspot.menu.growth"/></option>
            <option value="decline"><fmt:message key="hotspot.menu.decline"/></option>
        </select> 
    </div>
    <div>
        <input id="hotSpotRadioCollectorCompare" type="radio" name="hotSpotCompareRadio" value="collector"/><label for="hotSpotRadioCollectorCompare"><fmt:message key="hotspot.menu.compare.activeCollectors"/></label>
    </div>
    <div>
        <input id="hotSpotRadioTransactionsCompare" type="radio" name="hotSpotCompareRadio" value="transactions"/><label for="hotSpotRadioTransactionsCompare"><fmt:message key="hotspot.menu.compare.totalTransactions"/></label>
    </div>
    <div>
        <input id="hotSpotRadioUnitCompare" type="radio" name="hotSpotCompareRadio" value="spend" checked/><label for="hotSpotRadioUnitCompare"><fmt:message key="hotspot.menu.compare.totalSpend"/></label>
    </div>
    <div>
        <input id="hotSpotRadioSpendCompare" type="radio" name="hotSpotCompareRadio" value="unit" checked/><label for="hotSpotRadioSpendCompare"><fmt:message key="hotspot.menu.compare.totalUnits"/></label>
    </div>
</div>
<table id="hotSpotOpacityContainer" cellpadding="0" cellspacing="0" border="0">
    <tr>
        <td id="hotSpotOpacityLabel"><fmt:message key="hotspot.menu.opacity"/>:</td>
        <td>
            <div id="hotSpotOpacitySlider"></div>
        </td>
    </tr>
</table>
<div id="hotSpotDescBtn"><!-- stuff --></div>

