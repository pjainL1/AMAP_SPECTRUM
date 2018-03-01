<%-- 
    Document   : nwatch
    Created on : 21-Oct-2010, 10:03:52 AM
    Author     : ydumais
--%>

<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<fmt:setBundle basename="loLocalString" />
<table class="fullTable tradeAreaTable nwatchTable">
    <tr>
        <td>
            <input id="nwatchSpend" class="nwType" type="radio" name="nwtype" value="spend" checked />
            <label for="nwatchSpend"><fmt:message key="nw.menu.total.spend"/></label>
        </td>        
    </tr>    
    <tr>
        <td>
            <input id="nwatchUnit" class="nwType"  type="radio" name="nwtype" value="unit" />
            <label for="nwatchUnit"><fmt:message key="nw.menu.total.unit"/></label>
        </td>        
    </tr>    
</table>
<div class="menuSectionSeparator"></div>
<table class="fullTable tradeAreaTable ">
    <tr>
        <td>
            <input id="nwatchChkPrimary" value="primary" type="checkbox" /><fmt:message key="nw.menu.primary"/><br>
            <label for="nwatchChkPrimary"><fmt:message key="nw.menu.primary.nl"/></label>
        </td>        
    </tr>
    <tr>
        <td>
            <input id="nwatchChkMajority" value="majority" type="checkbox" /><fmt:message key="nw.menu.majority"/><br>
            <label for="nwatchChkMajority"><fmt:message key="nw.menu.majority.nl"/></label>
        </td>
    </tr>
</table>

<div id="nwatchDescBtn">
    <!-- stuff -->
</div>