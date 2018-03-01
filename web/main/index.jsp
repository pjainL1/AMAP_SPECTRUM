<%@page import="org.owasp.esapi.ESAPI"%>
<%@page import="org.apache.commons.lang.StringEscapeUtils"%>
<%@page import="net.sf.json.JSONArray"%>
<%@page import="net.sf.json.JSONObject"%>
<%@page import="com.lo.config.Confs"%>
<%@page import="com.korem.LanguageManager"%>
<%@page import="com.lo.ContextParams"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<fmt:setBundle basename="loLocalString" />

<%
    LanguageManager lm = new LanguageManager(request.getServletContext(), request);
    ContextParams cp = ContextParams.get(session);
%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.lo.Config"%>
<html>
    <head>
        <meta http-equiv="X-UA-Compatible" content="IE=8; IE=9" />
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title><fmt:message key="browser.title"/></title>

        <link type="text/css" rel="stylesheet" href="../js/lib/openlayers/theme/default/style.css"/>
        <link type="text/css" rel="stylesheet" href="../js/lib/jquery/themes/base/jquery.ui.all.css"/>
        <link type="text/css" rel="stylesheet" href="../main/css/main.css?<%= Confs.get().getBuildId()%>"/>
        <link type="text/css" rel="stylesheet" href="../main/css/jquery.multiselect.css?<%= Confs.get().getBuildId()%>"/>
        <!--[if lt IE 9]>
            <link type="text/css" rel="stylesheet" href="../main/css/main-ie.css"/>
        <![endif]-->

        <script type="text/javascript" src="https://maps.googleapis.com/maps/api/js?v=3.23&client=<%=Config.getInstance().getGoogleKey()%>&region=ca&language=<%= lm.getLanguageName()%>"></script>

        <script type="text/javascript" src="../js/lib/json2.js"></script>
        <script type="text/javascript" src="../js/lib/openlayers/OpenLayers-2.12.js"></script>

        <script type="text/javascript" src="../js/lib/openlayers/googlev3fix.js?<%= Confs.get().getBuildId()%>"></script>
        <script type="text/javascript" src="../js/lib/openlayers/KMS.js?<%= Confs.get().getBuildId()%>"></script>
        <script type="text/javascript" src="../js/lib/openlayers/heatmap.js?<%= Confs.get().getBuildId()%>"></script>

        <script type="text/javascript" src="../js/lib/jquery/jquery-1.11.3.min.js"></script>
        <script type="text/javascript" src="../js/lib/jquery/jquery-ui-1.10.4.custom.js"></script>
        <script type="text/javascript" src="../js/lib/jquery/jquery.ui.datepicker.js"></script>

        <script type="text/javascript" src="../js/lib/korem.js?<%= Confs.get().getBuildId()%>"></script>
        <script type="text/javascript" src="../js/lib/htmlHelper.js?<%= Confs.get().getBuildId()%>"></script>
        <script type="text/javascript" src="../js/lib/spin.min.js?<%= Confs.get().getBuildId()%>"></script>
        <script type="text/javascript" src="../main/js/main.js?<%= Confs.get().getBuildId()%>"></script>

        <script type="text/javascript" src="../js/locales/en.js?<%= Confs.get().getBuildId()%>"></script>

        <script type="text/javascript" src="../main/js/openlayersoverride.js?<%= Confs.get().getBuildId()%>"></script>

        <script type="text/javascript" src="../main/js/toolbase.js?<%= Confs.get().getBuildId()%>"></script>
        <script type="text/javascript" src="../main/js/layercontrol.js?<%= Confs.get().getBuildId()%>"></script>
        <script type="text/javascript" src="../main/js/applybutton.js?<%= Confs.get().getBuildId()%>"></script>
        <script type="text/javascript" src="../main/js/datepickers.js?<%= Confs.get().getBuildId()%>"></script>
        <script type="text/javascript" src="../main/js/infotool.js?<%= Confs.get().getBuildId()%>"></script>
        <script type="text/javascript" src="../main/js/selectiontool.js?<%= Confs.get().getBuildId()%>"></script>
        <script type="text/javascript" src="../main/js/customtradearea.js?<%= Confs.get().getBuildId()%>"></script>
        <script type="text/javascript" src="../main/js/mapsmanager.js?<%= Confs.get().getBuildId()%>"></script>
        <script type="text/javascript" src="../main/js/legend.js?<%= Confs.get().getBuildId()%>"></script>
        <script type="text/javascript" src="../main/js/hotspot.js?<%= Confs.get().getBuildId()%>"></script>
        <script type="text/javascript" src="../main/js/tradearea.js?<%= Confs.get().getBuildId()%>"></script>
        <script type="text/javascript" src="../main/js/ruler.js?<%= Confs.get().getBuildId()%>"></script>
        <script type="text/javascript" src="../main/js/placemark.js?<%= Confs.get().getBuildId()%>"></script>
        <script type="text/javascript" src="../main/js/find.js?<%= Confs.get().getBuildId()%>"></script>
        <script type="text/javascript" src="../main/js/nwatch.js?<%= Confs.get().getBuildId()%>"></script>
        <script type="text/javascript" src="../main/js/report.js?<%= Confs.get().getBuildId()%>"></script>
        <script type="text/javascript" src="../main/js/storeLevelAnalysis.js?<%= Confs.get().getBuildId()%>"></script>
        <script type="text/javascript" src="../main/js/alertdialog.js?<%= Confs.get().getBuildId()%>"></script>
        <script type="text/javascript" src="../main/js/jquery.multiselect.js?<%= Confs.get().getBuildId()%>"></script>
        <script type="text/javascript" src="../main/js/sponsorfilteringmanager.js?<%= Confs.get().getBuildId()%>"></script>
        <script type="text/javascript" src="../main/js/minimumvalues.js?<%= Confs.get().getBuildId()%>"></script>

        <script type="text/javascript" src="../main/js/nwtalegendmanager.js?<%= Confs.get().getBuildId()%>"></script>
        <script type="text/javascript" src="../main/js/ExtStreetViewControl.js"></script>         
        <script type="text/javascript" src="../main/js/streetviewcontrol.js?<%= Confs.get().getBuildId()%>"></script>
        <script type="text/javascript">
            am.options = {
                defaultMapStyle: <%= JSONArray.fromObject(Confs.MAP_STYLES.customStyle()).toString()%>,
                nightMapStyle: <%= JSONArray.fromObject(Confs.MAP_STYLES.nightStyle()).toString()%>
            };
            am.user = {
                attributes: <%= JSONArray.fromObject(cp.getUser().getAttributes()).toString()%>
            };
        </script>

        <script type="text/javascript" >
            initOnLoad = function () {
                am.maxInactiveInterval = ${maxInactiveInterval};
                am.pdfProcessingStatus = '${pdfProcessingStatus}';
                am.instance = new am.Main({
                    workspaceKey: '<%=ESAPI.encoder().encodeForJavaScript(request.getParameter("param.workspaceKey"))%>',
                    constants: {
                        legendUrl: '../getLegend.safe?mapInstanceKey=%s0&layerID=%s1',
                        locations: '<fmt:message key="dynLayer.label.location"/>'
                    }
                });
            }
        </script>

    </head>
    <body onload="initOnLoad();">
        <div id="containerIndex">
            <table class="fullTable" border="0">
                <tr>
                    <td class="fullTable">
                        <table class="fullTable" cellpadding="0" cellspacing="0" border="0">
                            <tr>
                                <td valign="top">
                                    <table id="dashboard" cellpadding="0" cellspacing="0" border="0">
                                        <tr>
                                            <td id="menuContainer">
                                                <div id="loyaltyOneLogo"></div>
                                                <div id="userGuide"><a target="_blank" href="../main/pdf/AMAP_User_Guide.pdf"><fmt:message key="label.userGuide"/></a></div>
                                                <div id="sponsorFilterContainer" style="display:none">
                                                    <select id="sponsorFilterCmb" multiple="multiple"></select>
                                                </div>
                                                <table id="menuInnerContainer" cellpadding="0" cellspacing="0" border="0">
                                                    <tr>
                                                        <td id="analysisController">

                                                            <h3 id="filterH3">
                                                                <div class="buttonDotOff"></div>
                                                                <a href="#"><fmt:message key="filter.menu.title"/></a>
                                                                <span id="dataFilteringBtn"><!-- stuff --></span>
                                                            </h3>
                                                            <div id="filterDiv">
                                                                <c:import url="jspf/compare.jsp" />
                                                            </div>

                                                            <h3 id="tradeAreaH3">
                                                                <div class="buttonDotOff"></div>
                                                                <a href="#"><fmt:message key="ta.menu.title"/></a>
                                                            </h3>
                                                            <div id="tradeAreaDiv" title="<fmt:message key="ta.menu.title"/>">
                                                                <c:import url="jspf/tradeArea.jsp" />
                                                            </div>

                                                            <h3 id="hotSpotH3">
                                                                <div class="buttonDotOff"></div>
                                                                <a href="#"><fmt:message key="hotspot.menu.title"/></a>
                                                            </h3>
                                                            <div id="hotSpotDiv" title="<fmt:message key="hotspot.menu.title"/>" class="accordionContent">
                                                                <c:import url="jspf/hotSpot.jsp" />
                                                            </div>

                                                            <h3 id="nwatchH3">
                                                                <div class="buttonDotOff"></div>
                                                                <a href="#"><fmt:message key="nw.menu.title"/></a>
                                                            </h3>
                                                            <div id="nwatchDiv" title="<fmt:message key="nw.menu.title"/>">
                                                                <c:import url="jspf/nwatch.jsp" />
                                                            </div>

                                                            <h3 id="storeLevelAnalysisH3">
                                                                <div class="buttonDotOff"></div>
                                                                <a href="#"><fmt:message key="sla.menu.title"/></a>
                                                            </h3>
                                                            <div id="storeLevelAnalysisDiv" title="<fmt:message key="sla.menu.title"/>">
                                                                <c:import url="jspf/storeLevelAnalysis.jsp" />
                                                            </div>
                                                        </td>
                                                    </tr>
                                                </table>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td id="applyArea">
                                                <div id="applyAreaInnerDiv">
                                                    <div id="applyAreaSelectDiv">
                                                        <select id="locationList">
                                                            <option><fmt:message key="apply.locations.makeSelection"/></option>
                                                        </select>
                                                    </div>
                                                    <table id="applyBtn" class="btn" cellpadding="0" cellspacing="0" border="0">
                                                        <tr>
                                                            <td id="applyBtnLeft" class="btnLeft"></td>
                                                            <td id="applyBtnCenter" class="btnCenter">
                                                                Apply
                                                            </td>
                                                            <td id="applyBtnRight" class="btnRight"></td>
                                                        </tr>
                                                    </table>
                                                    <div id="progressBar"></div>
                                                </div>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td id="cellReporting">
                                                <div id="reportController">
                                                    <h3 id="reportH3">
                                                        <div class="buttonDotOff"></div>
                                                        <a href="#"><fmt:message key="report.menu.title"/></a>
                                                    </h3>
                                                    <div id="reportDiv" class="accordionRemove" title="<fmt:message key="report.menu.title"/>">
                                                        <c:import url="jspf/report.jsp" />
                                                    </div>
                                                </div>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td id="layerControllerContainer">
                                                <div id="layerControllerInnerContainer">
                                                    <table class="fullTable" cellpadding="0" cellspacing="0" border="0">
                                                        <tr>
                                                            <td id="layerContainerTitleTD">
                                                                <table id="layerContainerTitle" cellpadding="0" cellspacing="0" border="0">
                                                                    <tr>
                                                                        <td class="menuLeftOn"></td>
                                                                        <td class="menuCenterOn"><fmt:message key="layerControl.title"/></td>
                                                                        <td class="menuRightOn"></td>
                                                                    </tr>
                                                                </table>
                                                            </td>
                                                        </tr>
                                                        <tr>
                                                            <td>
                                                                <div id="layerContainer">
                                                                    <div id="layerController"></div>
                                                                </div>
                                                            </td>
                                                        </tr>
                                                    </table>
                                                </div>
                                            </td>
                                        </tr>
                                    </table>
                                </td>
                                <td id="secondMapContainer">
                                    <div id="secondRelativeMapContainer">
                                        <div id="slaveLegend" class="legend">
                                            <div id="slaveLegendList" class="legendList">
                                                <div id="slaveLegendCanvas" class="clearfix">
                                                    <div id="slaveLegendTitle" class="legendTitle"><fmt:message key="legend.title"/></div>
                                                    <div id="slaveLegendEmpty" class="legendEmpty"><fmt:message key="legend.empty"/></div>
                                                </div>
                                            </div>
                                            <div id="slaveLegendToggler" class="legendToggler"></div>
                                        </div>

                                        <div class="tradeAreaInfoSuperWrapper">
                                            <div class="tradeAreaInfoContainer mapInfoBox" id="tradeAreaInfoContainer2">
                                                <table>
                                                    <tr>
                                                        <td><div class="tradeAreaInfoTxt" id="tradeAreaInfoTxt2"></div></td>
                                                        <td class="tradeAreaInfoCloseBtnTD"><img id="tradeAreaCloseBtn2" class="tradeAreaInfoCloseBtn" src="../main/images/close_amap.png" onclick="document.getElementById('tradeAreaInfoContainer2').style.visibility = 'hidden'"></img></td>
                                                    </tr>
                                                </table>
                                            </div>
                                        </div>

                                        <div id="stopCompareBtn"></div>
                                        <div id="secondMap"></div>
                                    </div>
                                </td>
                                <td id="firstMapContainer">

                                    <div id="firstRelativeMapContainer">
                                        <div id="mapTools">
                                            <div id="selectionToolCancel"></div>

                                            <div id="selectionToolPolygonal"></div>
                                            <div id="selectionToolCircular"></div>
                                            <div id="placemarkButton"></div>
                                            <div id="rulerButton"></div>
                                            <div id="infoToolButton"></div>
                                        </div>
                                        <div id="streetViewModal" style="display:none" class="ui-dialog ui-widget ui-widget-content ui-corner-all undefined ui-draggable ui-resizable">
                                            <div id="streetViewContent"></div>
                                        </div>                                
                                        <div id="streetViewControlContainer" class="transparentDiv">
                                            <div id="pegmanButton"></div>
                                        </div>            
                                        <div id="findControl"><input id="findInput" placeholder="<fmt:message key="search.placeholder" />" class="ui-corner-all" type="text" value=""><div id="findBtn"></div></div>  
                                        <div id="compareBtn"></div>
                                        <div id="slaveDisabler"></div>
                                        <div id="slaveLockToggler"></div>
                                        <div id="dashboardToggler"></div>

                                        <div id="slaveDatepickers">
                                            <div class="datepickerDiv">
                                                <span class="datepickerLabel"><fmt:message key="date.from"/></span>
                                                <input id="fromSlavePicker" class="slaveHasDatepicker" type="text"/>
                                            </div>
                                            <div class="slaveDatepickerDiv">
                                                <span class="datepickerLabel"><fmt:message key="date.to"/></span>
                                                <input id="toSlavePicker" class="slaveHasDatepicker" type="text"/>
                                            </div>
                                        </div>

                                        <div id="comparisonSlaveDatepickers">
                                            <div class="datepickerDiv">
                                                <span class="datepickerLabel"><fmt:message key="date.from"/></span>
                                                <input id="comparisonFromSlavePicker" class="slaveHasDatepicker" type="text"/>
                                            </div>
                                            <div class="slaveDatepickerDiv">
                                                <span class="datepickerLabel"><fmt:message key="date.to"/></span>
                                                <input id="comparisonToSlavePicker" class="slaveHasDatepicker" type="text"/>
                                            </div>
                                        </div>

                                        <div class="tradeAreaInfoSuperWrapperSecond">
                                            <div class="tradeAreaInfoContainer mapInfoBox" id="tradeAreaInfoContainer1">
                                                <table>
                                                    <tr>
                                                        <td>
                                                            <div class="minInfoTxt" id="hotSpotInfoTxt1" ></div>
                                                            <div class="tradeAreaInfoTxt" id="tradeAreaInfoTxt1" ></div>
                                                        </td> 
                                                        <td class="tradeAreaInfoCloseBtnTD"><img id="tradeAreaCloseBtn1" class="tradeAreaInfoCloseBtn" src="../main/images/close_amap.png" onclick="document.getElementById('tradeAreaInfoContainer1').style.visibility = 'hidden'"></img></td>
                                                    </tr>
                                                </table>
                                            </div>
                                        </div>

                                        <div id="masterLegend" class="legend">
                                            <div id="masterLegendList" class="legendList">
                                                <div id="masterLegendCanvas" class="clearfix">
                                                    <div id="masterLegendTitle" class="legendTitle"><fmt:message key="legend.title"/></div>
                                                    <div id="masterLegendEmpty" class="legendEmpty"><fmt:message key="legend.empty"/></div>
                                                </div>
                                            </div>
                                            <div id="masterLegendToggler" class="legendToggler"></div>
                                        </div>
                                        <div id="firstMap"></div>
                                    </div>
                                </td>
                            </tr>
                        </table>
                    </td>
                </tr>
            </table>

            <div style="display: none">
                <div id="tradeAreaDescWindow" title="<fmt:message key="ta.desc.title"/>">
                    <div id="tradeAreaDescWindowContent">
                        <fmt:message key="ta.desc"/>
                    </div>
                </div>
                <div id="nWatchDescWindow" title="<fmt:message key="nw.desc.title"/>">
                    <div id="nWatchDescWindowContent">
                        <fmt:message key="nw.desc"/>
                    </div>
                </div>
                <div id="reportDescWindow" title="<fmt:message key="report.desc.title"/>">
                    <div id="reportDescWindowContent">
                        <fmt:message key="report.desc"/>
                    </div>
                </div>
                <div id="infoToolWindow" title="Info-tool Window">
                    <div id="infoToolWindowContent"></div>
                    <table id="infoToolWindowLoading"><tr><td><img src="../main/images/LOADING_anim.gif"/></td></tr></table>
                    <table id="infoToolWindowNoResult"><tr><td><fmt:message key="infotool.noresult"/></td></tr></table>
                    <table id="infoToolWindowError"><tr><td><fmt:message key="infotool.error"/></td></tr></table>
                </div>
                <div id="hotSpotDescWindow" title="HotSpot help">
                    <div><fmt:message key="hotspot.help.text"/></div>
                </div>
                <div id="slaDescWindow" title="<fmt:message key="sla.desc.title"/>">
                    <div id="slaDescWindowContent">
                        <fmt:message key="sla.desc"/>
                    </div>
                </div>
                <div id="findDescWindow" title="Find help">
                    <div><fmt:message key="find.help.text"/></div>
                </div>
            </div>
            <div id="animStartCompareCtn" class="pageDisabler">
                <div class="pageDisablerBg"></div>
                <div id="animStartCompare">
                    <div id="newerMap"></div>
                    <div id="newMap"></div>
                    <div id="currentMap"></div>
                </div>
            </div>
            <div id="measureEl" class="mapInfoBox"></div>
        </div>
    </body>
</html>
