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
<link type="text/css" rel="stylesheet" href="../common/style.css" />
<title><fmt:message key='secure.login.title' /></title>
<style type="text/css">
.errorMsg {
	text-align: center;
	color: red;
}
</style>
</head>
<body>
	<div id="error" class="errorMsg">
		<fmt:message key='secure.login.loading' />
	</div>
</body>
</html>
