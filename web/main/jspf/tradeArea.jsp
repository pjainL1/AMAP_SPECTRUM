<%-- 
    Document   : tradeArea
    Created on : 7-Oct-2010, 11:07:13 AM
    Author     : ydumais
--%>

<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<fmt:setBundle basename="loLocalString" />

<table class="fullTable tradeAreaTable">
    <tr>
        <td class="taLeft">
            <input id="tradeAreaChkIssuance" value="issuance" type="checkbox"/><label for="tradeAreaChkIssuance"><fmt:message key="ta.menu.issuance"/></label>
        </td>
        <td>
            <select name="tradeAreaSelectIssuance" id="tradeAreaSelectIssuance" >
                <option value="0.10">10%</option>
                <option value="0.20">20%</option>
                <option value="0.30">30%</option>
                <option value="0.40">40%</option>
                <option value="0.50">50%</option>
                <option value="0.60" selected="selected">60%</option>
                <option value="0.65">65%</option>
                <option value="0.70">70%</option>
                <option value="0.75">75%</option>
                <option value="0.80">80%</option>
                <option value="0.85">85%</option>
                <option value="0.90">90%</option>
            </select>
        </td>
    </tr>
    <tr>
        <td class="taLeft">
            <input id="tradeAreaChkUnits" value="units" type="checkbox"/><label for="tradeAreaChkUnits"><fmt:message key="ta.menu.units"/></label>
        </td>
        <td>
            <select name="tradeAreaSelectUnits" id="tradeAreaSelectUnits" >
                <option value="0.10">10%</option>
                <option value="0.20">20%</option>
                <option value="0.30">30%</option>
                <option value="0.40">40%</option>
                <option value="0.50">50%</option>
                <option value="0.60" selected="selected">60%</option>
                <option value="0.65">65%</option>
                <option value="0.70">70%</option>
                <option value="0.75">75%</option>
                <option value="0.80">80%</option>
                <option value="0.85">85%</option>
                <option value="0.90">90%</option>
            </select>
        </td>
    </tr>
    <tr>
        <td class="taLeft">
            <input id="tradeAreaChkDistance" value="distance" type="checkbox" /><label for="tradeAreaChkDistance"><fmt:message key="ta.menu.distance"/></label>
        </td>
        <td>
            <input type="text" id="tradeAreaTxtDistance" class="tradeAreaTxt" value="10" />&nbsp;<fmt:message key="label.km"/>
        </td>
    </tr>
    <tr>
        <td class="taLeft">
            <input id="tradeAreaChkProjected" value="projected" type="checkbox" disabled/><label for="tradeAreaChkProjected"><fmt:message key="ta.menu.projected"/></label>
        </td>
        <td>
            <input type="text" id="tradeAreaTxtProjected" class="tradeAreaTxt" value="10" disabled/>&nbsp;<fmt:message key="label.km"/>
        </td>
    </tr>
    <tr>
        <td colspan="2">
            <input id="tradeAreaChkCustom"  value="custom" type="checkbox"/><label for="tradeAreaChkCustom"><fmt:message key="ta.menu.custom"/></label>
        </td>
    </tr>
    <tr>
        <td colspan="2">
            <input type="button" id="tradeAreaBtnCustomDraw" class="tradeAreaBtn" value="<fmt:message key="label.draw"/>" />
            <input type="button" id="tradeAreaBtnCustomClear" class="tradeAreaBtn" value="<fmt:message key="label.clear"/>" />
        </td>
    </tr>
    <tr>
        <td id="tradeAreaCustomMsg" class="smaller" colspan="2"></td>
    </tr>
    <tr class="fullheight" >
        <td class="center bottom" colspan="2"><input id="tradeAreaDownloadBtn" type="button" value="<fmt:message key="label.download.pc.info"/>" /></td>
    </tr>
</table>
<div id="tradeAreaDescBtn"><!-- stuff --></div>

