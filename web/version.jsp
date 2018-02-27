<%-- 
    Document   : version
    Created on : Mar 25, 2014, 12:00:55 PM
    Author     : jphoude
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<fmt:setBundle basename="build" />
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Loyalty One - version</title>
    </head>
    <body>
        <h1>Loyalty One A-Map</h1>
        Version <fmt:message key='version' />.<fmt:message key='minor' />
        b<fmt:message key='build' />r<fmt:message key='svn.revision' />
    </body>
</html>
