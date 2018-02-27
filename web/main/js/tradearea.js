am.TradeArea = function(options) {
    korem.apply(this, options);
    this.init();
};


am.TradeArea.prototype = {
    updateLayerWhenApplied: true,
    parent: null,
    applyBtn: null,
    map: null,
    mapsManager: null,
    dates: null,
    layerControl: null,
    window: null,
    hasChanged: false,
    placemarkInfo: null,
    projectedCheckbox: null,
    projectedSelected: false,
    projectedEls: null,
    init: function() {
        this.projectedEls = [korem.get('tradeAreaTxtProjected')];
        this.createDescWindow();
        this.handleDescBtn();
        this.handleDropDown();
        this.handleCheckboxes();
        this.handleTextInput();
        this.handleDownloadBtn();
        $(document).on("updateDataFiltering", korem.callback(this, this.needChange));
    },
    handleDownloadBtn: function() {
        $("input#tradeAreaDownloadBtn").click(function() {
            am.instance.tradeArea.getPostalCodeInfo();
        });
    },
    setMap: function(mapObj) {
        this.map = mapObj;
    },
    placemarkUpdated: function(info) {
        var disabled = (this.placemarkInfo = info) == null;
        for (var i = 0; i < this.projectedEls.length; ++i) {
            this.projectedEls[i].disabled = disabled;
        }
        if (this.projectedCheckbox.checked) {
            this.changed();
        }
        if (!disabled) {
            if (!$('[aria-describedby=tradeAreaDiv]').is(':visible')) {
                $('#tradeAreaH3 a').click();
            }
        } else {
            this.projectedCheckbox.checked = false;
        }
    },
    selectedLocationChanged: function() {
        this.hasChanged = true;
    },
    handleCheckboxes: function() {
        var that = this;
        var checkboxes = [
            korem.get('tradeAreaChkIssuance'),
            korem.get('tradeAreaChkUnits'),
            korem.get('tradeAreaChkDistance'),
            korem.get('tradeAreaChkCustom')
        ];
        this.projectedCheckbox = this.projectedEls[this.projectedEls.length] = korem.get('tradeAreaChkProjected');

        for (var i = 0; i < checkboxes.length; ++i) {
            checkboxes[i].onclick = function() {
                switch (this.value) {
                    case "issuance":
                        {
                            if (this.checked === true) {
                                $("#tradeAreaChkDistance").attr("checked", false);
                                $("#tradeAreaChkUnits").attr("checked", false);
                            }
                            break;
                        }
                    case "units":
                        {
                            if (this.checked === true) {
                                $("#tradeAreaChkIssuance").attr("checked", false);
                                $("#tradeAreaChkDistance").attr("checked", false);
                            }
                            break;
                        }
                    case "distance":
                        {
                            if (this.checked === true) {
                                $("#tradeAreaChkIssuance").attr("checked", false);
                                $("#tradeAreaChkUnits").attr("checked", false);
                            }
                            break;
                        }
                    case "projected":
                        {
                            break;
                        }
                    case "custom":
                        {
                            am.instance.customTradeArea.clickCustomTA();
                            break;
                        }
                }
                that.changed();
            };
            checkboxes[i].checked = false;
            if (checkboxes[i].value === "custom") {
                am.instance.customTradeArea.clickCustomTA();
            }
        }

        this.projectedCheckbox.onclick = function() {
            that.projectedSelected = this.checked;
            that.changed();
        };

        this.setInitialState();
    },
    setInitialState: function() {
        $('#tradeAreaChkIssuance').attr('checked', false);
        $("#tradeAreaChkUnits").attr("checked", false);
        $('#tradeAreaChkDistance').attr('checked', false);
        $('#tradeAreaChkProjected').attr('checked', false);
        $('#tradeAreaChkCustom').attr('checked', false);
        $("#tradeAreaChkProjected").attr('disabled', true);
        $("input#tradeAreaDownloadBtn").attr("disabled", true);
    },
    handleTextInput: function() {
        var that = this;
        $('#tradeAreaTxtDistance, #tradeAreaTxtProjected').keyup(function() {
            if (this == korem.get('tradeAreaTxtDistance') && korem.get('tradeAreaChkDistance').checked ||
                    this == korem.get('tradeAreaTxtProjected') && korem.get('tradeAreaChkProjected').checked) {
                that.changed();
            }
        });
    },
    handleDropDown: function() {
        var that = this;
        $('#tradeAreaSelectIssuance').change(function() {
            that.changed();
        });
        $('#tradeAreaSelectUnits').change(function() {
            that.changed();
        });
    },
    handleDescBtn: function() {
        var that = this;
        $('#tradeAreaDescBtn').click(function() {
            that.window.dialog('open');
            that.window.height(540).dialog('option', 'position', 'center');
        });
    },
    createDescWindow: function() {
        this.window = $('#tradeAreaDescWindow').dialog({
            zIndex: 7000,
            autoOpen: false,
            resizable: false,
            width: 850,
            height: 500,
            close: function() {
            },
            open: function() {
            }
        });
    },
    changed: function() {
        this.hasChanged = true;
        this.applyBtn.needed();
        EnableDisableLocSummary();
    },
    populateData: function(data, callback) {
        if (this.hasChanged) {
            if ((!data.locations || data.locations.length === 0) && $("#tradeAreaChkIssuance:checked,#tradeAreaChkUnits:checked,#tradeAreaChkDistance:checked").length > 0) {
                am.AlertDialog.show(am.locale.errors.locations);
                callback.error();
                return;
            } else if ( this.projectedCheckbox && this.projectedCheckbox.checked === true && !this.validateDistance(Number($("#tradeAreaTxtProjected").val()))) {
                am.AlertDialog.show(am.locale.tradeArea.defineLocation);
                callback.error();
                return;
            } else if ($("#tradeAreaChkDistance").is(":checked") === true && !this.validateDistance(Number($("#tradeAreaTxtDistance").val()))) {
                am.AlertDialog.show(am.locale.tradeArea.defineDrive);
                callback.error();
                return;
            }else if ($("#tradeAreaChkCustom").is(":checked") === true &&
                am.instance.customTradeArea.currentPolygon === null &&
                am.instance.customTradeArea.polygon === null){
                am.AlertDialog.show(am.locale.errors.emptycustom);
                callback.error();
                return;
            }
            data.tradearea = "";
            if ($("#tradeAreaChkIssuance").is(":checked")) {
                data.tradearea += "issuance,";
                data.issuance = Number($("#tradeAreaSelectIssuance").val());
            }
            if ($("#tradeAreaChkUnits").is(":checked")) {
                data.tradearea += "units,";
                data.issuance = Number($("#tradeAreaSelectUnits").val());
            }
            if ($("#tradeAreaChkDistance").is(":checked")) {
                data.tradearea += "distance,";
                data.distance = Number($("#tradeAreaTxtDistance").val());
            }
            if ($("#tradeAreaChkProjected").is(":checked")) {
                data.tradearea += "projected,";
                data.projected = Number($("#tradeAreaTxtProjected").val());
                data.longitude = this.placemarkInfo.lonLat.lon;
                data.latitude = this.placemarkInfo.lonLat.lat;
            }
            if ($("#tradeAreaChkCustom").is(":checked")) {
                data.tradearea += "custom,";
                if (am.instance.customTradeArea.polygon !== null) {
                    am.instance.customTradeArea.currentPolygon = am.instance.customTradeArea.polygon;
                }
                data.polygon = am.instance.customTradeArea.getJSONPolygon();

            }
            data.tradearea = data.tradearea.substring(0, data.tradearea.length);
            data.from = this.formatDate(this.dates[0]);
            data.to = this.formatDate(this.dates[1]);
            data.dateType = $(".datesFilterSelect").val();
            data.methods += 'tradearea,';
            callback.add();

        }
    },
    applyFinished: function() {
        am.instance.customTradeArea.applyFinished();

        var that = this;

        $.ajax({
            url: '../checkQARules.safe',
            context: this,
            type: 'post',
            success: function(data) {
                if (data.error == true) {
                    am.AlertDialog.show(data.qa);
                }
                that.hasChanged = true; //to force the refresh of nwatch ans hotspots
                that.mapsManager.layerVisibilityUpdated(that.map.kmsLayer);
                var taChkState = this.getTACheckboxState();
                $("input#tradeAreaDownloadBtn").attr("disabled", !(taChkState.issuance === true || taChkState.units === true || taChkState.distance === true || taChkState.projected === true || taChkState.custom === true));
            }
        });
    },
    validateDistance: function(value) {
        return (!isNaN(value)) && (value >= 1.0) && (value <= 250.0);
    },
    formatDate: function(el) {
        return $.datepicker.formatDate('yymmdd', $(el).datepicker('getDate'));
    },
    /*
     * Creates and shows TA information label based on values of input elements under the Trade Area tab
     */
    getTALabel: function() {        
        var state = this.getTACheckboxState();
        var newInnerHTML = "";
        var divStart = "<div>";
        var divEnd = "</div>";
        if (state.issuance) {
            // get the value entered in the AMRP Spend field  
            var issuance = 100 * document.getElementById("tradeAreaSelectIssuance").value;

            // get the TA info text
            var TAinfo = divStart + am.locale.tradeAreaInfo.amrp + divEnd;

            // insert issuance value in TAinfo, then assign to newInnerHTML
            newInnerHTML += korem.format(TAinfo, issuance);
        }
        if (state.units) {
            // get the value entered in the AMRP Spend field  
            var units = 100 * document.getElementById("tradeAreaSelectUnits").value;

            // get the TA info text
            var TAinfo = divStart + am.locale.tradeAreaInfo.armpUnits + divEnd;

            // insert issuance value in TAinfo, then assign to newInnerHTML
            newInnerHTML += korem.format(TAinfo, units);
        }
        if (state.distance) {
            var distance = document.getElementById("tradeAreaTxtDistance").value;
            var TAinfo = divStart + am.locale.tradeAreaInfo.driveDistance + divEnd;
            newInnerHTML += korem.format(TAinfo, distance);
        }

        if (state.custom) {
            var TAinfo = divStart + am.locale.tradeAreaInfo.custom + divEnd;
            newInnerHTML += TAinfo;
        }

        if (state.projected) {
            var projected = document.getElementById("tradeAreaTxtProjected").value;
            var TAinfo = divStart + am.locale.tradeAreaInfo.projected + divEnd;
            newInnerHTML += korem.format(TAinfo, projected);
        }
        return newInnerHTML;
    },
    /*
     * returns an object with true/false values based on the state of the checkboxes under the Trade Area tab
     */
    getTACheckboxState: function() {
        var issuance_isChecked = document.getElementById("tradeAreaChkIssuance").checked;
        var units_isChecked = document.getElementById("tradeAreaChkUnits").checked;
        var distance_isChecked = document.getElementById("tradeAreaChkDistance").checked;
        var projected_isChecked = document.getElementById("tradeAreaChkProjected").checked;
        var custom_isChecked = document.getElementById("tradeAreaChkCustom").checked;

        var TAState = {issuance: issuance_isChecked, units:units_isChecked, distance: distance_isChecked, projected: projected_isChecked, custom: custom_isChecked};
        return TAState;
    },
    /**
     * This function will download the Postal Code information as CSV
     * @returns {undefined}
     */
    getPostalCodeInfo: function() {
        var data = {
            methods: 'generatePC',
            mapInstanceKey: am.instance.customTradeArea.mapInstanceKey
        };
        if (am.instance.applyButton.appliedLocations !== null) {
            data.locations = am.instance.applyButton.appliedLocations;
        }

        am.AlertDialog.show(am.locale.download.taCsvMsg, am.locale.download.msgTitle, {width:500}, function(){
            $("input#tradeAreaDownloadBtn").attr("disabled", true);
            $.ajax({
                url: "../apply.safe",
                type: 'POST',
                data: data,
                success: function() {
                    $("#applyBtn").hide();
                    var pb = $("#progressBar");
                    pb.show();
                    pb.progressbar().progressbar('option', 'value', 0);
                    window.setTimeout(function() {
                        am.instance.tradeArea.updateDownloadProgress();
                    }, 1);
                }
            });
        });        
    },
    updateDownloadProgress: function() {
        $.ajax({
            url: '../getProgress.safe',
            data: {methods: 'generatePC'},
            type: 'post',
            success: function(returnData) {
                var progressBar;
                progressBar = $("#progressBar").progressbar();
                progressBar.progressbar('option', 'value', returnData.progress);
                if (returnData.progress < 100) {
                    window.setTimeout(function() {
                        am.instance.tradeArea.updateDownloadProgress();
                    }, 1);
                } else {
                    $("#applyBtn").show();
                    $("#progressBar").hide();
                    $("input#tradeAreaDownloadBtn").attr("disabled", false);
                    var iframe = document.createElement('iframe');
                    iframe.src = "../getPCInfo.safe";
                    iframe.className = "nosize";
                    document.body.appendChild(iframe);
                }
            }
        });
    },
    needChange:function(){
        if(this.isActive()){
            this.changed();
        }
    },    
    isActive: function(){
        return $("#tradeAreaDiv input[type=checkbox]:checked").length > 0;
    },
    hasInfoTextActive: function(){
        var state = this.getTACheckboxState();
        return state.issuance || state.units || state.distance || state.projected || state.custom;
    }
};
