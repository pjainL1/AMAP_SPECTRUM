<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%-- 
    Document   : sponsorSorter
    Created on : 2010-10-06, 14:09:43
    Author     : slajoie
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<c:forEach items="${sponsors}" var="item" varStatus="stat">
    <tr>
        <td>${item.rollupGroupName}</td>
        <td>
            <c:forEach items="${item.codes}" var="item2" varStatus="stat">
                <c:choose>
                    <c:when test="${stat.last}">
                        ${item2}
                    </c:when>
                    <c:otherwise>
                        ${item2},
                    </c:otherwise>
                </c:choose>
            </c:forEach>
        </td>
        <td>
            <select id="${item.rollupGroupCode}" style="width:200px" >
                <option value="NOTAWORKSPACE" >...</option>
                <c:forEach items="${workspaces}" var="item3" varStatus="stat">
                    <c:set var="selected" value='${item.workspace eq item3 ? "selected=\'true\'" : ""}'/>
                    <option ${selected} value="${item3}" >${item3}</option>
                </c:forEach>
            </select>
        </td>
        <td>
            <table>
                <tr>
                    <td><img src="getCustomerLogo.safe/${item.rollupGroupCode}" width="25"></td>
                    <td>
                        <form method="POST" action="uploadCustomerLogo.safe" enctype="multipart/form-data" >
                            <table><tr> <td>
                                File:
                                <input type="file" name="file" id="file" accept="image/png" /> <br/>              
                            </td> 
                            <td>
                                <input type="hidden" name="id" value="${item.rollupGroupCode}" />
                             </td> 
                             <td>
                                 <input type="submit" class="jqueryBlueButton" value="Upload" name="upload" id="upload" />
                             </td></tr></table>
                        </form>
                    </td>
                    <c:if test="${item.logo.length() > 0}">
                    <td>
                        <form method="POST" action="deleteCustomerLogo.safe">
                            <table><tr> <td>
                                <input type="hidden" name="id" value="${item.rollupGroupCode}" />
                                <input type="submit" class="jqueryBlueButton" value="Delete" name="delete" id="delete" />
                            </td></tr></table>
                        </form>     
                    </td>    
                    </c:if>
                    
                </tr>
            </table>
        </td>
    </tr>
</c:forEach>
