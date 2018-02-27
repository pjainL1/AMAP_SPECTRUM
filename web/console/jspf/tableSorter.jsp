<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%-- 
    Document   : tableSorter
    Created on : 2010-10-04, 15:54:28
    Author     : slajoie
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<c:forEach items="${logs}" var="item" varStatus="stat">
<c:set var="date" value='${fn:substring(item.datetime,0,19)}'/>
    <tr>
        <td>${date}</td>
        <td>${item.login}</td>
        <td>${item.sponsor}</td>
        <td>${item.description}</td>
    </tr>
</c:forEach>

