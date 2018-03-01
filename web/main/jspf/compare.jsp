<%-- 
    Document   : compare
    Created on : 2015-10-07, 11:20:02
    Author     : scavanagh
--%>

<%@page import="com.korem.LanguageManager"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<fmt:setBundle basename="loLocalString" />
<%
    LanguageManager lm = new LanguageManager(request.getServletContext(), request);
%>
<div class="filterDatesWrapper">
    <span class="dateRangeCaption"><%= lm.get("datePickers.dateRange")%></span>  
    <select name="datesFilterSelect" id="datesFilterTypesSelect" class="datesFilterSelect">
        <option value="single" selected="selected"><%= lm.get("datePickers.singleRange")%></option>
        <option value="comparison"><%= lm.get("datePickers.comparisonRange")%></option>
    </select>    
    <div class="basicDates">
        <span class="periodCaption"><%= lm.get("datePickers.periodOne")%></span>
        <input id="fromPicker" type="text"/>-
        <input id="toPicker" type="text"/>
    </div>

    <div class="comparisonDates">
        <span class="periodCaption"><%= lm.get("datePickers.periodTwo")%></span>
        <input id="comparisonFromPicker" type="text"/>-
        <input id="comparisonToPicker" type="text"/>
    </div>
    <div class="defaultBtnWrapper">    
        <button type="button" id="defaultBtnCenter"><%= lm.get("datePickers.defaultButtonText")%></button>
    </div>
</div>


<table id="fullTableDataFiltering" class="fullTable" style="" cellpadding="0" cellspacing="0" border="0">
    <tr>
        <td><%= lm.get("minimumValues.minTransactions")%></td>
        <td  align="right"><input id="minTrans" type="text" size="8"/></td>
    </tr>
    <tr>
        <td><%= lm.get("minimumValues.minSpend")%></td>
        <td  align="right" style="color:#0072cf"><b> $</b><input id="minSpend" type="text" size="8"/></td>
    </tr>
    <tr>
        <td><%= lm.get("minimumValues.minUnit")%></td>
        <td  align="right"><input id="minUnit" type="text" size="8"/></td>
    </tr>
</table>  
        
<div id="dataFilteringDescWindow" title='Data Filtering help'>
    <div id="dataFilteringWindowContent">
        <fmt:message key="filter.desc"/>
    </div>
</div>
        
