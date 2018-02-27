<%-- 
    Document   : storeLevelAnalysis
    Created on : Oct 14, 2015, 10:30:40 AM
    Author     : smukena
--%>

<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<fmt:setBundle basename="loLocalString" />
<table class="fullTable" id="slaStf">
    <tr>
        <td>
            <h3><fmt:message key="sla.menu.single.title"/>
        </td>

    </tr>
    <tr>
        <td>
            <input id="slaStfTotalCollectors" type="checkbox" name="slaAnalysisValue" value="collectors"/>
            <label for="slaStfTotalCollectors"><fmt:message key="sla.menu.single.totalCollector"/></label>
        </td>   
        <td>
            <div class="customizeRangeBtn" style="position: relative;"></div>
        </td>        
    </tr>    
    <tr>
        <td>
            <input id="slaStfTotalSales" type="checkbox" name="slaAnalysisValue" value="spend"/>
            <label for="slaStfTotalSales"><fmt:message key="sla.menu.single.totalSales"/></label>
        </td>
        <td>
            <div class="customizeRangeBtn" style="position: relative;"></div>
        </td>         
    </tr>
    <tr>
        <td>
            <input id="slaStfTotalTransactions" type="checkbox" name="slaAnalysisValue" value="transactions"/>
            <label for="slaStfTotalTransactions"><fmt:message key="sla.menu.single.totalTransactions"/></label>
        </td>
        <td>
            <div class="customizeRangeBtn" style="position: relative;"></div>
        </td>         
    </tr>
    <tr>
        <td>
            <input id="slaStfTotalUnit" type="checkbox" name="slaAnalysisValue" value="units"/>
            <label for="slaStfTotalUnit"><fmt:message key="sla.menu.single.totalUnit"/></label>
        </td> 
        <td>
            <div class="customizeRangeBtn" style="position: relative;"></div>
        </td>         
    </tr>
</table>

<table class="fullTable hidden" id="slaCa">
    <tr>
        <td>
            <h3><fmt:message key="sla.menu.comparison.title"/></h3>
        </td>
    </tr>
    <tr>
        <td>
            <input id="slaCaTotalCollectors" class="slaAnalysisValue" type="checkbox" name="slaAnalysisValue" value="collectors">
            <label for="slaCaTotalCollectors"><fmt:message key="sla.menu.comparison.totalCollector"/></label>
        </td> 
        <td>
            <div class="customizeRangeBtn" style="position: relative;"></div>
        </td>         
    </tr>    
    <tr>
        <td>
            <input id="slaCaTotalSales" class="slaAnalysisValue"  type="checkbox" name="slaAnalysisValue" value="spend">
            <label for="slaCaTotalSales"><fmt:message key="sla.menu.comparison.totalSales"/></label>
        </td> 
        <td>
            <div class="customizeRangeBtn" style="position: relative;"></div>
        </td>         
    </tr>
    <tr>
        <td>
            <input id="slaCaTotalTransactions" class="slaAnalysisValue"  type="checkbox" name="slaAnalysisValue" value="transactions">
            <label for="slaCaTotalTransactions"><fmt:message key="sla.menu.comparison.totalTransactions"/></label>
        </td>  
        <td>
            <div class="customizeRangeBtn" style="position: relative;"></div>
        </td>         
    </tr>
    <tr>
        <td>
            <input id="slaCaTotalUnit" class="slaAnalysisValue"  type="checkbox" name="slaAnalysisValue" value="units">
            <label for="slaCaTotalUnit"><fmt:message key="sla.menu.comparison.totalUnit"/></labe>
        </td>        
        <td>
            <div class="customizeRangeBtn" style="position: relative;"></div>
        </td>         
    </tr>
</table>
<div id="slaDescBtn">
    <!-- stuff -->
</div>

<div style="display: none">
    <div id="selectedAnalysis">

    </div>
    <div id="rangeAttributesWindow">
        <p id="rangeAttributesTitle"></p>
        <div id="rangeAttributesWindowHook"></div>
    </div> 
</div>
<div id="techmaticConfigTable" style="display: none">
<table  class="fullTable colorFullTable">
    <tbody>
        <tr>
            <th>Range</th>
            <th>From</th>
            <th>To</th></tr> 
        <tr class="greyTabLine"> 
            <td><div class="colorRangeDiv rangeOneColor"></div></td>
            <td><input class="rangeInput" type="text" value="{0}"></td>
            <td class="inifinityTd">+&infin;</td>
        </tr> <tr class=""> 
            <td><div class="colorRangeDiv  rangeTwoColor"></div></td> 
            <td><input type="text" class="rangeInput" value="{2}"></td>
            <td><input type="text" class="rangeInput" value="{1}"></td>
        </tr> <tr class="greyTabLine"> 
            <td><div class="colorRangeDiv  rangeThreeColor"></div></td> 
            <td><input type="text" class="rangeInput" value="{4}"></td>
            <td><input type="text" class="rangeInput" value="{3}"></td>
        </tr> <tr class=""> 
            <td><div class="colorRangeDiv  rangeFourColor"></div></td> 
            <td><input type="text" class="rangeInput" value="{6}"></td>
            <td><input type="text" class="rangeInput" value="{5}"></td>
        </tr> <tr class="greyTabLine"> 
            <td class=""><div class="colorRangeDiv  rangeFiveColor"></div></td>
            <td class="inifinityTd">-&infin;</td>
            <td><input type="text" class="rangeInput" value="{7}"></td>
        </tr>
    </tbody>
</table>		        
</div>