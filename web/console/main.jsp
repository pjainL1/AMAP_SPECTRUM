<%@page import="com.lo.config.Confs"%>
<%@page import="com.lo.ContextParams"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%-- 
    Document   : usageMonitoring
    Created on : 2010-10-04, 11:16:24
    Author     : slajoie
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">
<fmt:setBundle basename="loLocalString" />

<html>
    <head>
        <meta http-equiv="X-UA-Compatible" content="IE=8; IE=9" />
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title><fmt:message key='mc.title' /></title>
        <link type="text/css" href="../js/lib/jquery/themes/base/jquery.ui.all.css" rel="stylesheet" />
        <link type="text/css" href="../js/lib/jquery/themes/tableSorter/style.css" rel="stylesheet" />
        <script type="text/javascript" src="../js/lib/korem.js"></script>
        <script type="text/javascript" src="../js/locales/en.js"></script>
        
        <script type="text/javascript">
            colorTabShown = false;
            tradeAreaHistoryShown = false;
            groupLayerManagementShown = false;
            
            am.consoleConfig = {
                datagrids: {
                    pageSize: <%= Confs.CONSOLE_CONFIG.datagridsPageSize() %>,
                    tradeAreas: {
                        pageSize: <%= Confs.CONSOLE_CONFIG.datagridsTradeAreasPageSize()%>
                    }
                }
            };
        </script>
        
        <script type="text/javascript"  src="../js/lib/jquery/jquery-1.11.3.min.js"></script>
        <script type="text/javascript"  src="../js/lib/jquery/jquery-ui-1.10.4.custom.min.js"></script>
        <script type="text/javascript"  src="../js/lib/jquery/jquery.tablesorter.min.js"></script>
        <link href="../js/lib/ext4/resources/css/ext-all-gray.css"  rel="stylesheet" type="text/css"/>
        <script type="text/javascript" src="../js/lib/ext4/ext-all-debug.js"></script>
        <!--the color picker plugin-->        
        <link href="../js/lib/ext4/ux/grid/css/GridFilters.css"  rel="stylesheet" type="text/css"/>
        <link href="../js/lib/ext4/ux/grid/css/RangeMenu.css"  rel="stylesheet" type="text/css"/>
        <link href="../js/lib/jscolor/colorpicker.css"  rel="stylesheet" type="text/css"/>
        <script type="text/javascript" src="../js/lib/jscolor/jscolor.js"></script>  
        <script type="text/javascript" src="js/app.js"></script>  
        <script type="text/javascript" src="js/ext-overrides.js"></script>  
        <link href="main.css"  rel="stylesheet" type="text/css"/>
    </head>
    <body>

        <%
            ContextParams cp = ContextParams.get(session);
            boolean legal = cp.getUser().isAdmin();
        %>

        <script type="text/javascript">
            var colorTab = null;
            
            $(function() {
                var $tabs = $("#tabs").tabs({
                    activate: function(e, ui) {
                        var thistab = ui;
                        var active = $( "#tabs" ).tabs( "option", "active" );
                        showTab(active);
                    }
                });
                var legal = <%=legal%>;
                if (!legal) {
                    window.location = '../console';
                }
            });
            
            

            function showTab(thistab) {
                selectedtab = thistab;
                if (selectedtab != 2) {
                    colorTab && colorTab.fireEvent('tabHidden');
                }
                
                switch (thistab) {
                    case 0:
                        console.log('0');
                        break;

                    case 1:
                        console.log('01');
                        break;

                    case 2:
                        console.log('02');
                        if (colorTabShown == false) {
                            colorTabShown = true;
                            Ext.create('Ext.panel.Panel', {
                                renderTo: 'color-grid',
                                layout: 'fit',
                                width: 925,
                                items: [
                                    colorTab = Ext.create('AMAP.view.locationcolorlist')
                                ]
                            });
                        }
                        break;

                    case 3:
                        console.log('03');
                        if (tradeAreaHistoryShown == false) {
                            tradeAreaHistoryShown = true;
                            Ext.create('Ext.panel.Panel', {
                                renderTo: 'tradearea_div',
                                layout: 'fit',
                                width: 925,
                                items: [
                                    {
                                        xtype: 'tamanagementlist'
                                    }
                                ]
                            });
                            Ext.getCmp('changeDateButton').fireEvent('click');
                        }
                        break;

                    case 4:
                        console.log('04');
                        if (groupLayerManagementShown == false) {
                            groupLayerManagementShown = true;
                            Ext.create('Ext.panel.Panel', {
                                renderTo: 'layer-group',
                                layout: 'fit',
                                width: 925,
                                items: [{
                                    xtype: 'layergroup'
                                }]
                            });
                        }
                        break;
                }
            }
        </script>

        <div class="divMain" style="width: 970px" >
            <div id="tabs" style="height: 575px; font-size: 12px" >
                <ul>
                    <li><a href="#tabs-1"><fmt:message key='mc.usagemonitoring' /></a></li>
                    <li><a href="#tabs-2"><fmt:message key='mc.sponsors' /></a></li>
                    <li><a href="#tabs-3"><fmt:message key='mc.colormanagement' /></a></li>
                    <li><a href="#tabs-4"><fmt:message key='mc.tahistorymanagement' /></a></li>
                    <li><a href="#tabs-5"><fmt:message key='mc.layergroupmanagement' /></a></li>
                </ul>
                <div id="tabs-1">
                    <c:import url="/console/jspf/monitoring.jsp"></c:import>
                    </div>
                    <div id="tabs-2">
                    <c:import url="/console/jspf/sponsors.jsp"></c:import>
                    </div>
                    <div id="tabs-3" align="center" style="">
                    <c:import url="/console/jspf/colorManagement.jsp"></c:import>
                    </div>                
                    <div id="tabs-4" align="center" style="">
                    <c:import url="/console/jspf/tradeareamanagement.jsp"></c:import>
                    </div>
                    <div id="tabs-5" align="center" style="">
                    <c:import url="/console/jspf/layergroupmanagement.jsp"></c:import>
                </div>
            </div>
        </div>
    </body>

</html>
