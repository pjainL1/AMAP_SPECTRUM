<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%-- 
    Document   : updateUser
    Created on : 2010-10-12, 15:10:38
    Author     : slajoie
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<fmt:setBundle basename="loLocalString" />

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title><fmt:message key='mc.user.title' /></title>
        <script type="text/javascript"  src="../../js/lib/jquery/jquery-1.4.2.min.js"></script>
        <style type="text/css">
            .inputControl {
                width: 250px;
            }
        </style>

    </head>
    <body>
    <script type="text/javascript">

    $(document).ready(function() {
        if ($('#userId').val() != "") {
            $('#username').attr("disabled", true);
        }else {
            $('#username').removeAttr("disabled");
        }
        if ("${close}" == "close") {
            window.opener.initUsers();
            window.close();
        }
    });

    submitInfo = function () {

        var sponsors = "";
        $("#sponsors option").each(function(){
            if (this.selected) {
                if (sponsors=="") {
                    sponsors = sponsors + $(this).val();
                } else {
                    sponsors = sponsors + ":" + $(this).val();
                }
            }
        });
        $('#sponsorlist').val(sponsors);

        if (!validFirstName()) {
            alert("<fmt:message key='mc.user.valid.firsname' />");
            return;
        }
        if (!validLastName()) {
            alert("<fmt:message key='mc.user.valid.lastname' />");
            return;
        }
        if (!validOneSponsor()) {
            alert("<fmt:message key='mc.user.valid.onesponsor' />");
            return;
        }
        if ($('#role').val() == 2) {
            if (!validOnlyOneSponsor()) {
                alert("<fmt:message key='mc.user.valid.onlyonesponsor' />");
                return;
            }
        }
        if (!validUsername()) {
            alert("<fmt:message key='mc.user.valid.username' />");
            return;
        }
        if ($('#userId').val() == "") {
            if (!validPassword1()) {
                alert("<fmt:message key='mc.user.valid.password' />");
                return;
            }
        }
        if (jQuery.trim($('#password').val()) != "") {
            if (!validPassword2()) {
                alert("<fmt:message key='secure.login.valid.password2' />");
                return;
            }
        }        
        if ($('#userId').val() == "") {
            $.get("ExistUser.do?r=" + Math.random() + "&login=" + $('#username').val(), function(data){
                if (data != "exist") {
                    $('#form').submit();
                } else {
                    alert("<fmt:message key='mc.user.valid.existusername' />");
                }
             });
         } else {
            $('#form').submit();
         }
    }

    cancel = function() {
        window.close();
    }

    validFirstName = function() {
        return (jQuery.trim($('#firstname').val()) != "");
    }
    validLastName = function() {
        return (jQuery.trim($('#lastname').val()) != "");
    }
    validOneSponsor = function() {
        return ($('#sponsorlist').val() != "")
    }
    validOnlyOneSponsor = function() {
        return ($('#sponsorlist').val().indexOf(":", 0) == -1)
    }
    validUsername = function() {
        return (jQuery.trim($('#username').val()) != "");
    }
    validPassword1 = function() {
        return (jQuery.trim($('#password').val()) != "");
    }
    validPassword2 = function() {
        var rege = /^[A-Za-z0-9!?#_+*%]/;
        return rege.test($('#password').val());
    }

    </script>
      
        <form id="form" action="UpdateUser.do" method="post">
            <input id="userId" name="userId" type="hidden" value="${user.userId}">
            <input id="sponsorlist" name="sponsorlist" type="hidden" value="">
            <input id="sponsorlist" name="sponsorlistcount" type="hidden" value="">
            <table style="width: 350px; margin: 20px; font-size: 14px" >
                <tr>
                    <td style="width: 25%"><fmt:message key='mc.user.firstname' />:</td>
                    <td style="width: 75%"><input id="firstname" name="firstname" type="text" value="${user.firstname}" maxlength="128" class="inputControl"></td>
                </tr>
                <tr>
                    <td><fmt:message key='mc.user.lastname' />:</td>
                    <td><input id="lastname" name="lastname" type="text" value="${user.lastname}" maxlength="128" class="inputControl"></td>
                </tr>
                <tr>
                    <td><fmt:message key='mc.user.type' />:</td>
                    <td>
                        <select id="role" name="role" class="inputControl">
                            <c:forEach items="${roles}" var="item" varStatus="stat">
                                <c:set var="selected" value='${item.roleId eq user.roleId ? "selected=\'true\'" : ""}'/>
                                <option ${selected} value="${item.roleId}" >${item.name}</option>
                            </c:forEach>
                        </select>
                    </td>
                </tr>
                <tr>
                    <td style="vertical-align:text-top;" ><fmt:message key='mc.user.sponsors' />:</td>
                    <td>
                        <select id="sponsors" name="sponsors" multiple="true" style="height:200px" class="inputControl">
                            <c:forEach items="${sponsors}" var="item" varStatus="stat">
                                <c:set var="selected" value="" />
                                <c:forEach items="${user.sponsors}" var="item2" varStatus="stat">
                                    <c:if test="${item.rollupGroupCode eq item2.rollupGroupCode}">
                                       <c:set var="selected" value="selected=\'true\'" />
                                    </c:if>
                                </c:forEach>
                                <option ${selected} value="${item.rollupGroupCode}" >${item.rollupGroupCode}: ${item.rollupGroupName}</option>
                            </c:forEach>
                        </select>
                    </td>
                </tr>
                <tr>
                    <td><fmt:message key='mc.user.username' />:</td>
                    <td><input id="username" disabled="" name="username" type="text" value="${user.login}" maxlength="64" class="inputControl" ></td>
                </tr>
                <tr>
                    <td><fmt:message key='mc.user.password' /></td>
                    <td><input id="password" name="password" type="password" value="" class="inputControl"></td>
                </tr>
                <tr>
                    <td style="text-align: left; padding-top: 20px" ></td>
                    <td style="text-align: right; padding-top: 20px; padding-right: 10px"><input type="button" value="<fmt:message key='mc.user.save' />" onclick="submitInfo()" > <input type="button" value="<fmt:message key='mc.user.cancel' />" onclick="cancel()" ></td>
                </tr>
            </table>
        </form>
    </body>
</html>
