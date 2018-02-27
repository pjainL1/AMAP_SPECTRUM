<%-- 
    Document   : load
    Created on : 2-Nov-2010, 11:34:58 AM
    Author     : ydumais
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Force data loading</title>
        <script type="text/javascript" src="../js/lib/jquery/jquery-1.4.2.min.js"></script>
        <script type="text/javascript">
            function load(){
                $.ajax({
                    url : 'forceLoad.do',
                    data : {
                        password: $('#password').val()
                    },
                    success : function(data){
                        $('#logging').html(data);
                    }});
            }
        </script>
    </head>
    <body>
        <b>Enter secret phrase and select action:</b>
        <input id="password" type="password" size="50" />
        <input type="button" value="load inbox" onclick="load()" />
        <div id="logging"></div>
    </body>
</html>
