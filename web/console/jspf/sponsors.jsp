<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%-- 
    Document   : sponsors
    Created on : 2010-10-04, 15:21:16
    Author     : slajoie
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<fmt:setBundle basename="loLocalString" />

<script type="text/javascript">

    var params;

    $(document).ready(function() {
        params = {};
        params["action"] = "init";
        params["r"] = Math.random();
        $.post("UpdateSponsors.do", params, function(html) {
            $("#sponsorstable tbody").empty();
            $("#sponsorstable tbody").append(html);
        });
        
        $("#save").click(function() {
            params = {};
            params["action"] = "update";
            params["r"] = Math.random();
            $("#sponsorstable > tbody > tr").each(function() {
                params[$(this).find("select").attr('id')] = $(this).find("select").val();
            });
            $.post("UpdateSponsors.do", params, function(html) {
                $("#sponsorstable tbody").empty();
                $("#sponsorstable tbody").append(html);
                alert("<fmt:message key='mc.sponsors.save.success' />")
            });
        });

    });

</script>

<div style=" width : 100%; height : 440px; overflow-y :auto; overflow-x: hidden">
<table id="sponsorstable" class="tablesorter" cellspacing="1">
    <thead>
        <tr>
            <th><fmt:message key='mc.sponsors.sponsorname' /></th>
            <th><fmt:message key='mc.sponsors.sponsorcode' /></th>
            <th><fmt:message key='mc.sponsors.workspace' /></th>
            <th><fmt:message key='mc.sponsors.logo' /></th>
        </tr>
    </thead>
    <tbody>       
    </tbody>
</table>
</div>
<!-- <div style=" margin-top: 20px; width : 100%; text-align: right"> -->
<%--     <input type="button" id="save" value="<fmt:message key='mc.sponsors.save' />" > --%>
<!-- </div> -->



