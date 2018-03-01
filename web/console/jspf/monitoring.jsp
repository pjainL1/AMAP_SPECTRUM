<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%-- 
    Document   : monitoring
    Created on : 2010-10-04, 15:02:26
    Author     : slajoie
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<fmt:setBundle basename="loLocalString" />

<script type="text/javascript">

$(document).ready(function() {
    $("#tablesorter").tablesorter();
    $("#change").click(function() {
        var params = {
            datepicker1 : $("#datepicker1").val(),
            datepicker2 : $("#datepicker2").val(),
            sponsorId : $("#sponsors").val()
        }
        $.post("UpdateMonitoring.do?r=" + Math.random(),params, function(html) {
            $("#tablesorter tbody").empty();
            $("#tablesorter tbody").append(html);
            $("#tablesorter").trigger("update");
        });
        return false;
    });
});

$(function() {
    $( "#datepicker1" ).datepicker({
        changeMonth: true,
        changeYear: true,
        dateFormat: "dd/mm/yy"
    });
});

$(function() {
    $( "#datepicker2" ).datepicker({
        changeMonth: true,
        changeYear: true,
        dateFormat: "dd/mm/yy"
    });
});

</script>

<fieldset>
    <table >
        <tr>
            <td><fmt:message key='mc.usagemonitoring.timeframe' />: <input type="text" value="${datepicker1}" id="datepicker1"></td>
            <td><fmt:message key='mc.usagemonitoring.to' />: <input type="text" value="${datepicker2}" id="datepicker2"></td>
            <td style="padding-left: 20px" >
                <fmt:message key='mc.usagemonitoring.rollup' />:
                <select id="sponsors" style="width:200px" >
                    <option value ="" ><fmt:message key='mc.usagemonitoring.allrollups' /></option>
                    <c:forEach items="${sponsors}" var="item" varStatus="stat">
                        <option value="${item.rollupGroupCode}">${item.rollupGroupCode}: ${item.rollupGroupName}</option>
                    </c:forEach>
                </select>
            </td>
            <td style="text-align:right; padding-left: 220px" ><input type="button" id="change" value="<fmt:message key='mc.usagemonitoring.change' />" ></td>
        </tr>
    </table>
</fieldset>

<div style=" margin-top: 20px; width : 100%; height : 430px; overflow-y :auto; overflow-x: hidden">
<table id="tablesorter" class="tablesorter" cellspacing="1">
    <thead>
        <tr>
            <th><fmt:message key='mc.usagemonitoring.datetime' /></th>
            <th><fmt:message key='mc.usagemonitoring.user' /></th>
            <th><fmt:message key='mc.usagemonitoring.sponsor' /></th>
            <th><fmt:message key='mc.usagemonitoring.action' /></th>
        </tr>
    </thead>
    <tbody>
    </tbody>
</table>
</div>


