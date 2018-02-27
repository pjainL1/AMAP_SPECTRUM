am.HotSpot = function(options) {
    korem.apply(this, options);
    this.init();
};

am.HotSpot.prototype = {

    applyBtn: null,
    map: null,
    mapsManager: null,
    dates: null,
    layerControl: null,

    window: null,
    oldChk: null,
    selectedCheckbox: null,
    selectedRadio: null,
    selectedCompareRadio: null,
    slider: null,
    hasChanged: false,
    radios: null,

    init: function() {
        this.handleSlider();
        this.createDescWindow();
        this.handleDescBtn();
        this.handleCheckboxes();
        this.handleSelect();
        this.handleRadios();
        this.handleCompareRadios();
        this.handleDatesFilterTypesSelect();
        this.applyOpacity();
        $(document).on("updateDataFiltering", korem.callback(this, this.needChange));
    },

    selectedLocationChanged: function() {
        this.hasChanged = true;
    },

    applyOpacity: function(value) {
        var opacity = ((value != undefined) ? value : this.slider.slider('value')) / 100;
        this.map.heatMapLayer.setOpacity(opacity);
        globalOpacity = opacity;
    },
    handleSelect:function(){
        $("#comparisonType").change(korem.callback(this, this.changed));
    },

    handleDatesFilterTypesSelect: function(){
        $("#datesFilterTypesSelect").change(korem.callback(this, this.changeUI));
    },
    
    isComparison: function(){
        return $("#datesFilterTypesSelect").val() == "comparison";
    },
    
    changeUI: function(){
        
        if(korem.get('hotSpotChkAirMiles').checked || korem.get('hotSpotChkSponsor').checked || korem.get('hotSpotChkLocations').checked){
            this.greenDotBtn.removeClass("buttonDotOff").addClass("buttonDotOn");
        }
        
        if(this.isComparison()){
            if(korem.get('hotSpotChkAirMiles').checked){
                korem.get('hotSpotChkAirMiles').checked = false;
            }
            $(".hotspotCompare").show();
            $(".hotspotSingle").hide();
        } else {
            $(".hotspotCompare").hide();
            $(".hotspotSingle").show();
        }
    },

    handleSlider: function() {
        var that = this;
        this.slider = $('#hotSpotOpacitySlider');
        var applyOpacity = korem.createSlower(100, function(obj) {
            that.applyOpacity(obj);
        });
        this.slider.slider({
            min: 0,
            max: 100,
            value: 50,
            slide: function(event, ui) {
                applyOpacity(ui.value);
            }
        });
    },

    setMap: function(mapObj, destroy) {
        this.map = mapObj;
        this.applyOpacity();
        if(destroy){
            if (this.map.hotSpotLayer) {
                this.map.hotSpotLayer.elVisibility = mapObj.hotSpotLayer.visibility = false;
                this.mapsManager.visibilityUpdated([this.map.hotSpotLayer]);
            }
        }
    },
    
    handleRadios: function() {
        var that = this;
        this.radios = [
        this.selectedRadio = korem.get('hotSpotRadioSpend'),
        korem.get('hotSpotRadioUnit'),
        korem.get('hotSpotRadioCollector')
        ];
        this.selectedRadio.checked = true;
        for (var i = 0; i < this.radios.length; ++i) {
            this.radios[i].onclick = function() {
                if (that.selectedRadio != this) {
                    that.selectedRadio = this;
                    that.changed();
                }
            };
        }
    },
    
    handleCompareRadios: function() {
        var that = this;
        this.compareRadios = [
        this.selectedCompareRadio = korem.get('hotSpotRadioCollectorCompare'),
        korem.get('hotSpotRadioSpendCompare'),
        korem.get('hotSpotRadioUnitCompare'),
        korem.get('hotSpotRadioTransactionsCompare')
        ];
        this.selectedCompareRadio.checked = true;
        for (var i = 0; i <= this.radios.length; ++i) {
            this.compareRadios[i].onclick = function() {
                if (that.selectedCompareRadio != this) {
                    that.selectedCompareRadio = this;
                    that.changed();
                }
            };
        }
    },

    setSpendDisabled: function(disabled) {
        this.radios[1].disabled = disabled;
        if ((this.radios[0].disabled = disabled)) {
            this.radios[2].checked = true;
            this.radios[2].onclick.apply(this.radios[2]);
        }
    },

    handleCheckboxes: function() {
        var that = this;
        var checkboxes = [
        korem.get('hotSpotChkAirMiles'),
        korem.get('hotSpotChkSponsor'),
        korem.get('hotSpotChkLocations')
        ];
        for (var i = 0; i < checkboxes.length; ++i) {
            checkboxes[i].checked = false;
            checkboxes[i].onclick = function() {
                if (that.selectedCheckbox == this) {
                    that.selectedCheckbox = null;
                } else {
                    if (that.selectedCheckbox != null) {
                        that.selectedCheckbox.checked = false;
                    }
                    that.selectedCheckbox = this;
                }
                that.changed();
                that.setSpendDisabled(checkboxes[0] == this && this.checked);
            };
        }
    },

    createDescWindow: function() {
        this.window = $('#hotSpotDescWindow').dialog({
            zIndex: 7000,
            autoOpen: false,
            resizable: false,
            width: 450,
            close: function() {},
            open: function() {}
        });
    },

    handleDescBtn: function() {
        var that = this;
        $('#hotSpotDescBtn').click(function() {
            that.window.dialog('open');
        });
    },

    changed: function() {
        this.hasChanged = true;
        this.applyBtn.needed();
    },

    populateData: function(data, callback) {
        if (!this.isActive()) {
            this.layerControl.setHotspotElVisibility(false);
        }
        if (this.hasChanged && this.selectedCheckbox && this.isActive()) {
            this.oldChk = null;
            if (this.selectedCheckbox.value == 'locations' && (!data.locations || data.locations.length == 0)) {
                am.AlertDialog.show(am.locale.errors.locations);
                callback.error();
                return;
            }
            this.oldChk = this.selectedCheckbox;
            data.methods += 'hotspot,';
            data.type = this.selectedCheckbox.value;
            data.dataType = this.isComparison() ? this.selectedCompareRadio.value : this.selectedRadio.value;
            data.dateType = $(".datesFilterSelect").val();
            data.hotspotComparisonType = $("#comparisonType").val();
            callback.add();
        }
    },

    valuesChanged: function() {
        if (this.oldChk) {
            this.hasChanged = true;
            this.layerControl.setHotspotElVisibility(false);
        }
    },

    applyFinished: function() {
        this.hasChanged = false;
        this.mapsManager.layerVisibilityUpdated(this.map.heatMapLayer);
        this.layerControl.setHotspotElVisibility(this.oldChk != null);
        this.map.legend.toggler.activate();
    },
    needChange:function(){
        if(this.isActive()){
            this.changed();
        }
    },    
    isActive: function(){
        if(this.isComparison()){
            return korem.get('hotSpotChkSponsor').checked || korem.get('hotSpotChkLocations').checked;
        }
        return $("#hotSpotDiv input[type=checkbox]:checked").length > 0;
    },
    hasInfoTextActive: function () {
        return this.mapsManager.mainInstance.minumumValues.hasMinCollector() &&
            (!this.isComparison() ? korem.get('hotSpotChkAirMiles').checked
            : (korem.get('hotSpotChkSponsor').checked || korem.get('hotSpotChkLocations').checked) && korem.get('hotSpotRadioCollectorCompare').checked);
    }
};