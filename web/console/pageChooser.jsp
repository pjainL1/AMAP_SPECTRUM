<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%-- 
    Document   : sponsors
    Created on : 24-Sep-2010, 3:58:30 PM
    Author     : ydumais
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<fmt:setLocale value='<%=request.getAttribute("language")%>' scope="session"/>
<fmt:setBundle basename="loLocalString" />
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link type="text/css" rel="stylesheet" href="../common/style.css"/>
        <script type="text/javascript"  src="../js/lib/jquery/jquery-1.11.3.min.js"></script>
        <title><fmt:message key='console.pagechooser.title' /></title>
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
	  <fieldset style="width: 600px;align:center;" >
           <table style="margin: 2px;" align="center">
               <tr align="center">
                <td style="padding-top: 20px">
                	<input type="button" name="admin" value="<fmt:message key='console.pagechooser.admin' />" 
                	onClick="window.location='../console/InitMonitoring.do'"> 
             	</td>
             	<td style="padding-top: 20px">
                	<input type="button" name="app" value="<fmt:message key='console.pagechooser.application' />" 
                	onClick="window.location='../secure/SponsorChooser.do'"> 
             	</td>
             	<td style="padding-top: 20px">
                	<input type="button" name="app" value="<fmt:message key='console.pagechooser.loading' />" 
                	onClick="window.location='../secure/load.jsp'">
             	</td>
          	   <td style="padding-top: 20px">
                	<input type="button" name="app" value="<fmt:message key='console.pagechooser.kms' />" 
                	onClick="window.open('../secure/kms.do','_blank')">
             	</td>	
               </tr>
           </table>
       </fieldset>
    </body>
</html>