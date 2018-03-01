<%@page import="org.owasp.esapi.ESAPI"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%--
    Document   : error
    Created on : 23-Jan-2013, 10:05:30 AM
    Author     : mdube
--%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<fmt:setBundle basename="loLocalString" />
<fmt:setBundle basename="build" var="build" scope="page" />
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link type="text/css" rel="stylesheet" href="../common/style.css"/>
        <title><fmt:message key='secure.login.title' /></title>
        <script type="text/javascript"  src="../js/lib/jquery/jquery-1.4.2.min.js"></script>
        <style type="text/css">
            .errorMsg {
                text-align:center;
                color: red;
            }
        </style>
    <script type="text/javascript">
            // on the page loading we check the url and also if the parameters are present
        $(document).ready(function() {
                if (jQuery.trim($('#errortype').val()) == "6001") { // ldap
                    $("#error").html("<fmt:message key='${errorMsgKey}' />");
                } else if (jQuery.trim($('#errortype').val()) == "5000") { // unknown
                    $("#error").html("<fmt:message key='secure.ldap.unknown.error' />");
                } else if (jQuery.trim($('#errortype').val()) == "6000") { // invalid access
                    $("#error").html("<fmt:message key='secure.login.valid.invalidaccess' />");
                } else if (jQuery.trim($('#errortype').val()) > 0 && jQuery.trim($('#errortype').val()) < 7000) { // sso ...
                    $("#error").html(jQuery.trim($('#errormessage').val()));
                } else {
                	 $("#error").html("<fmt:message key='secure.ldap.unknown.error' />");
            }
	                    
                var expired = '<%=ESAPI.encoder().encodeForJavaScript(request.getParameter("expired"))%>';
            if(expired == 'true'){
                $("#error").html("<fmt:message key='secure.login.valid.expired' />");
            }
        });
    </script>
    </head>
    <body>
        <div id="sso">
            <input type="hidden" name="build" value="<fmt:message bundle="${build}" key="version"/>.<fmt:message bundle="${build}" key="minor"/> b<fmt:message bundle="${build}" key="build"/>" />
            <input id="expired" name="expired" type="hidden" value="${expired}">
            <input id="errormessage" name="errormessage" type="hidden" value="${errormessage}">
        <input id="errortype" name="errortype" type="hidden" value="${errortype}">
            <div id="error" class="errorMsg"></div>
        </div>
    </body>
</html>
