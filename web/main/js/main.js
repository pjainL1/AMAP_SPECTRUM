var am = {}
am.Main = function(options) {
    am.constants = options.constants;
    delete options.constants;
    korem.apply(this, options);
    this.init();
}

am.Main.prototype = {
    workspaceKey: null,
    dashboard: null,
    layerControl: null,
    kmsLayer: null,
    applyButton: null,
    datePickers: null,
    mapsManager: null,
    selectionTool: null,
    customTradeArea: null,
    infoTool: null,
    hotSpot: null,
    tradeArea: null,
    find: null,
    nwatch: null,
    storeLevelAnalysis:null,
    loadingStateSpinner: null,
    accordion: null,
    activePanel: 0,
    listDialogs: ["tradeArea", "hotSpot", "nwatch", "report","storeLevelAnalysis"],
    openDialog:null,
    minumumValues:null,
    init: function() {
        am.OpenLayersOverrides.apply();
        this.loadingStateSpinner = new Spinner();

        var that = this;
        this.dashboard = $('#dashboard');
        this.createDashboardToggler();
        (this.accordion = $('#analysisController')).accordion({
            disabled : true
        });
        $('#reportController').accordion({
           collapsible: true,
           active: false,
           disabled: true
        });
        this.initDialog();
        var placemark;
        that.datePickers = new am.DataPickers();
        that.minumumValues = new am.MinimumValues();
        this.mapsManager = new am.MapsManager({
            mainInstance: this,
            datePickers: that.datePickers,
            workspaceKey: this.workspaceKey,
            onMapCreated: function(mapObj) {
                this.setStyledMap();

                that.streetViewControl = new am.StreetViewControl({
                    mapInstanceKey: mapObj.mapInstanceKey,
                    map: mapObj.map,
                    mapsManager: that.mapsManager
                });

                that.selectionTool = new am.SelectionTool({
                    mapInstanceKey: mapObj.mapInstanceKey,
                    map: mapObj.map,
                    mapsManager: that.mapsManager
                });
                that.datePickers.selectionTool = that.selectionTool;
                that.customTradeArea = new am.CustomTradeArea({
                    mapInstanceKey: mapObj.mapInstanceKey,
                    map: mapObj.map
                });
                that.layerControl = new am.LayerControl({
                    mapInstanceKey: mapObj.mapInstanceKey,
                    map: mapObj
                });
                that.find = new am.Find({
                    onGeocodeSelect: function (viewport) {
                        that.mapsManager.showViewport(viewport);
                        this.createMarkerFromLastAdress();
                        this.applyFinished();
                    },
                    onLocationFind: function (xy) {
                        that.mapsManager.setCenter(xy);
                    },
                    onFSAFind: function (bounds) {
                        that.mapsManager.zoomToExtent(bounds);
                    },
                    map: mapObj,
                    mapsManager: that.mapsManager,
                    layerControl: that.layerControl
                });
                that.datePickers.layerControl = that.layerControl;
                that.layerControl.addListener(that.mapsManager);
                that.layerControl.addListener(that.find);
                that.applyButton = new am.ApplyButton({
                    id: 'applyBtn',
                    mapInstanceKey: mapObj.mapInstanceKey,
                    selectionTool: that.selectionTool,
                    map: mapObj,
                    mapsManager: that.mapsManager,
                    layerControl: that.layerControl
                });
                that.datePickers.applyBtn = that.applyButton;
                that.minumumValues.applyBtn = that.applyButton;
                that.datePickers.setMap(mapObj, true);
                that.datePickers.setMapManager(that.mapsManager);
                that.applyButton.addDataHolder(that.datePickers);
                that.applyButton.addDataHolder(that.minumumValues); 
                that.applyButton.addDataHolder(that.hotSpot = new am.HotSpot({
                    applyBtn: that.applyButton,
                    map: mapObj,
                    mapsManager: that.mapsManager,
                    dates: that.datePickers.dates,
                    layerControl: that.layerControl,
                    greenDotBtn: $('#hotSpotH3 > div')
                }));
                that.datePickers.setHotspot(that.hotSpot);
                that.minumumValues.setHotspot(that.hotSpot);
                that.applyButton.addDataHolder(that.nwatch = new am.NWatch({
                    parent: that,
                    panelIdx: 1,
                    applyBtn: that.applyButton,
                    map: mapObj,
                    mapsManager: that.mapsManager,
                    dates: that.datePickers.dates,
                    layerControl: that.layerControl
                }));
                that.applyButton.addDataHolder(that.storeLevelAnalysis = new am.StoreLevelAnalysis({
                    applyBtn: that.applyButton,
                    map: mapObj,
                    mapsManager: that.mapsManager,
                    dates: that.datePickers.dates,
                    layerControl: that.layerControl, 
                    greenDotBtn: $('#storeLevelAnalysisH3 > div')
                }));
                that.datePickers.storeLevelAnalysis=that.storeLevelAnalysis;
                that.applyButton.addDataHolder(that.tradeArea = new am.TradeArea({
                    parent: that,
                    panelIdx: 1,
                    applyBtn: that.applyButton,
                    map: mapObj,
                    mapsManager: that.mapsManager,
                    dates: that.datePickers.dates,
                    layerControl: that.layerControl
                }));
                that.applyButton.addDataHolder(that.report = new am.Report({
                    parent: that,
                    panelIdx: 1,
                    applyBtn: that.applyButton,
                    map: mapObj,
                    mapsManager: that.mapsManager,
                    dates: that.datePickers.dates
                }));
                
                that.infoTool = new am.InfoTool({
                    mapInstanceKey: mapObj.mapInstanceKey,
                    map: mapObj.map,
                    dates: that.datePickers.dates,
                    minimumValues: that.minumumValues
                });
                placemark = new am.Placemark();
                placemark.setMap(mapObj);
                placemark.addListener(that.tradeArea);
                that.selectionTool.addListener(that.applyButton);
                that.selectionTool.addListener(that.tradeArea);
                that.selectionTool.addListener(that.hotSpot);
                that.selectionTool.addListener(that.nwatch);

                that.handleEnterKey();

                that.sponsorFilteringManager = new am.SponsorFilteringManager({
                    mapInstanceKey: mapObj.mapInstanceKey,
                    map: mapObj,
                    dates: that.datePickers.dates,
                    masterObj: that
                });

                reportObj.map = mapObj;

                that.fixDropdownBug();
            },
            onCompareStarted: function(masterObj) {
                
                $.ajax({
                    url: '../startCompare.safe',
                    type: 'post',
                    data: {
                        workspaceKey: this.workspaceKey
                    },
                    success: korem.callback(this, function(data) {
                        this.setStyledMap();
                        placemark.setMap(masterObj);
                        that.datePickers.updateSlaves();
                        that.datePickers.setMap(masterObj);
                        that.applyButton.setMap(masterObj);
                        that.selectionTool.setMap(masterObj.map, masterObj.mapInstanceKey);
                        that.selectionTool.updateLocationList([])
                        that.customTradeArea.setMap(masterObj.map, masterObj.mapInstanceKey);
                        that.infoTool.setMap(masterObj.map, masterObj.mapInstanceKey);
                        that.hotSpot.setMap(masterObj, true);
                        that.tradeArea.setMap(masterObj);
                        that.layerControl.setMap(masterObj, true);
                        that.sponsorFilteringManager.setMap(masterObj);
                        that.nwatch.setMap(masterObj);
                        that.streetViewControl.setMap(masterObj.map);
                        that.streetViewControl.hide();
                        that.find.setMap(masterObj);
                        reportObj.map = masterObj;
                    })
                });
            },
            resetFind: function(map) {
                that.find.resetMap(map);
            },
            onCompareStopped: function() {
                that.datePickers.hideSlaves();
            },
            onZoomChanged: function() {
                that.layerControl.zoomChanged();
            },
            setStyledMap: function() {
                // apply default style on the google map object
                var styledMap = new google.maps.StyledMapType(am.options.defaultMapStyle);
                var nightMap = new google.maps.StyledMapType(am.options.nightMapStyle);
                that.mapsManager.firstLayer.map.setOptions({
                    mapTypeControlOptions: {
                        mapTypeIds: ['grey']
                    }
                });
                that.mapsManager.firstLayer.mapObject.mapTypes.set('grey', styledMap);
                that.mapsManager.firstLayer.mapObject.mapTypes.set('night', nightMap);
            }
        });

        am.initReportDisplay(am.pdfProcessingStatus);

        /**
         * Init Inactive Interval client side
         */
//        this.miiId = window.setTimeout(this.miiProcess, am.maxInactiveInterval*1000 + 5000);
//        $('document').mousemove(this.resetMiiProcess);
    },
    fixDropdownBug: function() {
        // on IE9, the dropdown doesn't pull down unless position:relative is set. 
        // For some reason, event when setting it in the CSS, it is removed by something else, so we patch it in Javascript.
        $('#tradeAreaSelectIssuance').css('position', 'relative');
    },
    resetMiiProcess: function(evt) {
        window.clearTimeout(this.miiId);
        this.miiId = window.setTimeout(this.miiProcess, am.maxInactiveInterval * 1000);
    },
    miiProcess: function() {
        $.ajax({
            url: '../maxInactiveIntervalProcess.safe',
            context: this,
            type: 'post',
            success: function(data) {
                // nothing to do
            }
        });
    },
    openPanel: function(panel) {
        this.accordion.accordion('activate', panel.panelIdx);
    },
    createDashboardToggler: function() {
        var that = this;
        korem.apply(korem.apply({}, am.ToolBase.prototype), {
            btnId: 'dashboardToggler',
            groupId: 'dashboardToggler',
            initialize: function() {
            },
            doActivate: function() {
                that.toggleDashboard();
            },
            doDeactivate: function() {
                that.toggleDashboard();
            }
        }).init();
    },
    toggleDashboard: function() {
        var that = this;
        this.dashboard.toggle('slide', null, 1, function() {
            that.mapsManager.updateSizeIfExists();
            that.streetViewControl.recalculateDraggableContainment();
            that.closeAllDialog();
        });
    },
    enterKeyEnabled: true,
    handleEnterKey: function() {
        var that = this;
        var enterHandlers = [that.find, that.applyButton, that.applyButton, that.applyButton, {
                onEnter: function() {
                    if ($("#reportBtn").attr('disabled') == false) {
                        generatePDF('report');
                    }
                }
            }];
        document.onkeyup = function(e) {
            if (that.enterKeyEnabled) {
                korem.onEnterKey(e, function() {
                    enterHandlers[that.activePanel].onEnter();
                });
            }
        };
    },
    initDialog: function () {
        
        var that = this;
        $(document).bind("datepickerchanges", function(){
            that.closeAllDialog();
        });
        $("#dashboard").click(function (e) {
            var h3 = korem.format("{0}H3", that.openDialog);
            if (e.target.id == h3 || e.target.parentNode.id == h3) {
                that.closeAllDialog(that.openDialog);
            } else {
                that.closeAllDialog();
            }
        });

        for (var i = 0; i < this.listDialogs.length; i++) {
            var h3 = korem.format("#{0}H3", this.listDialogs[i]);
            var div = korem.format("#{0}Div", this.listDialogs[i]);
            var changeButtonDot =  function(){
                this.find(".buttonDotOff").removeClass("buttonDotOff").addClass("buttonDotOn");
            };
            $(div+ " input").change(korem.callback($(h3), changeButtonDot));
            $(div+ " select").change(korem.callback($(h3), changeButtonDot));
            var dialogOptions = korem.apply({autoOpen: false,
                position: {
                    my: "left center",
                    at: "right+20 center",
                    of: $(h3)},
                resizable: false,
                draggable: false,
                dialogClass: "arrowLeft-dialog",
                buttons: {},
                open: korem.callback(this, this.closeAllDialog, [this.listDialogs[i]]),
                close : function(){
                    $(this).trigger("closeModal");
                }
            });
            dialogOptions.buttons[am.locale.dialogs.closeButton] = function() {
                $(this).dialog('close');
                $(this).trigger("closeModal");
            };
            
            $(div).dialog(dialogOptions);
            $(h3).click(korem.callback($(div), function () {
                this.dialog("isOpen") ? this.dialog("close") : this.dialog("open");
                $(this).trigger("openModal", [this.attr('id')]);
            }));
        }
    },
    closeAllDialog : function(exept){
        this.openDialog = exept;
        for (var i = 0; i < this.listDialogs.length; i++) {
            if(exept != this.listDialogs[i]){
                $(korem.format("#{0}Div", this.listDialogs[i])).dialog('close');
            }
        }
    }
};
