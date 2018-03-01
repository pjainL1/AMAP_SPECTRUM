<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%-- 
    Document   : sponsors
    Created on : 24-Sep-2010, 3:58:30 PM
    Author     : ydumais
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<fmt:setBundle basename="loLocalString" />
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link type="text/css" rel="stylesheet" href="../common/style.css"/>
        <script type="text/javascript"  src="../js/lib/jquery/jquery-1.4.2.min.js"></script>
        <title><fmt:message key='secure.sponsor.title' /></title>
    </head>

    <script type="text/javascript">

        document.onkeypress = function(e) {
            var isNotIE = window.event == undefined;
            var keyId = (isNotIE) ? e.keyCode : window.event.keyCode;
            if (keyId == 13 || keyId == 10) {
                 $('#myForm').submit();
            }
        }

    </script>
    <body>
        <form id="myForm" name="myForm" method="post" action="init.do">
            <fieldset style="width: 400px" >
                <table style="width: 400px; margin: 20px" >
                    <tr>
                        <td style="width: 100px"><fmt:message key='secure.sponsor.sponsor' />:</td>
                        <td>
                            <select id="sponsors" name="sponsors" style="width:260px" >
                                <c:forEach items="${sponsors}" var="item" varStatus="stat">
                                    <option value="${item.rollupGroupCode}" >${item.rollupGroupCode}: ${item.rollupGroupName}</option>
                                </c:forEach>
                            </select>
                        </td>
                    </tr>
                    <tr>
                        <td style="padding-top: 38px"><input type="submit" class="btn" value="<fmt:message key='secure.sponsor.submit' />"></td>
                        <td></td>
                    </tr>
                </table>
            </fieldset>
        </form>
    </body>
</html>