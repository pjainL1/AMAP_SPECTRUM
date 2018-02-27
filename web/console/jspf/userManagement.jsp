<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%-- 
    Document   : userManagement
    Created on : 2010-10-04, 15:21:51
    Author     : slajoie
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<fmt:setBundle basename="loLocalString" />

<script type="text/javascript">

    $(document).ready(function() {
        initUsers();
    });

    deleteUser = function(userId) {
        if (confirm("<fmt:message key='mc.user.confirm.delete' />")) {
            $.get("DeleteUser.do?r=" + Math.random() + "&userId=" + userId, function(html) {
                $("#users tbody").empty();
                $("#users tbody").append(html);
            });
        }
    }
    
    initUsers = function() {
         $.get("InitUsers.do?r=" + Math.random(), function(html) {
            $("#users tbody").empty();
            $("#users tbody").append(html);
        });
    }

    updateUser = function(userId) {
        var winoptions;
        winoptions = "left=300,top=200,height=430,width=425,status=no,toolbar=no,menubar=no,location=no,directories=no,resizable=no,scrollbars=no";
        window.open("../console/popup/UpdateUser.do?userId="+userId+"&action=init", "updateUser", winoptions);
    }

</script>

<div style=" width : 100%; height : 440px; overflow-y :auto; overflow-x: hidden">
<table id="users" class="tablesorter" cellspacing="1">
    <thead>
        <tr>
            <th><fmt:message key='mc.user.username' /></th>
            <th><fmt:message key='mc.user.sponsor' /></th>
            <th><fmt:message key='mc.user.datecreated' /></th>
            <th><fmt:message key='mc.user.type' /></th>
            <th style="width: 65px">&nbsp</th>
        </tr>
    </thead>
    <tbody>
    </tbody>
</table>           
</div>
<div style=" width : 100%; padding-top: 20px; text-align: right">
    <input type="button" value="<fmt:message key='mc.user.createnewuser' />" onclick="updateUser('')" >
</div>

