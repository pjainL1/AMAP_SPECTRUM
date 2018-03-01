<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%--
    Document   : userSorter
    Created on : 2010-10-08, 15:14:53
    Author     : slajoie
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<c:forEach items="${users}" var="item" varStatus="stat">
    <tr>
        <c:set var="shortLogin" value='${fn:length(item.login) gt 15 ? fn:substring(item.login,0,15) : item.login}'/>
        <c:set var="loginDots" value='${fn:length(item.login) gt 15 ? "..." : ""}'/>
        <td title="${item.login}" >${shortLogin}${loginDots}</td>
            <c:set var="longSponsor" value=''/>
            <c:forEach items="${item.sponsors}" var="item2" varStatus="stat">
                <c:choose>
                    <c:when test="${stat.last}">
                        <c:set var="longSponsor" value='${longSponsor}${item2.rollupGroupName}'/>
                    </c:when>
                    <c:otherwise>
                        <c:set var="longSponsor" value='${longSponsor}${item2.rollupGroupName},'/>
                    </c:otherwise>
                </c:choose>
            </c:forEach>
            <c:set var="shortSponsor" value='${fn:length(longSponsor) gt 80 ? fn:substring(longSponsor,0,80) : longSponsor}'/>
            <c:set var="sponsorDots" value='${fn:length(longSponsor) gt 80 ? "..." : ""}'/>
            <c:set var="date" value='${fn:substring(item.creation,0,19)}'/>
        <td title="${longSponsor}">${shortSponsor}${sponsorDots}</td>
        <td>${date}</td>
	<td>${item.role.name}</td>
        <td><a href="#" onclick="updateUser('${item.userId}')">edit</a>&nbsp<a href="#" onclick="deleteUser('${item.userId}')">delete</a></td>
    </tr>
</c:forEach>
