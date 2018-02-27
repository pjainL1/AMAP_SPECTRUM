am.SponsorFilteringManager = function(options) {
    korem.apply(this, options);
    this.init();
};
am.SponsorFilteringManager.prototype = {
    FILTER_CHANGE_DELAY: 2000, 
    selectedSponsorsCodes:[],
    sponsorKeyValMapping: {},
    mapInstanceKey: null,
    numberOfOptions: 0,
    init: function() {
        this.getCodes();
    },
    setMap: function(map) {
        this.mapInstanceKey = map.mapInstanceKey;
        this.map = map;
    },
    getCodes: function() {
        $.ajax({
            type: "POST",
            url: "../GetSponsorCodes.safe",
            data: "",
            context: this,
            success: function(data) {
                var cmbOption;
                var that = this;
                that.numberOfOptions = data.length;

                if (data.length > 1) {
                    $.each(data, function(i, entry) {
                        var formattedName = korem.format("{0}: {1}", entry.code, entry.name);
                        cmbOption = html.option({props: {type: 'option', value: entry.code, innerHTML: formattedName, selected: 'selected'}});
                        that.sponsorKeyValMapping[entry.code] = entry.key; 
                        $("#sponsorFilterCmb").append(cmbOption);
                    }
                    
                    );
                    
                    var multiselectOptions = {
                        //header: false,
                        minWidth: 300,
                        noneSelectedText: am.locale.sponsorFilter.noneSelectedText,
                        selectedText: am.locale.sponsorFilter.selectedText,
                        height: 'auto'
                    };
                    if (navigator.appVersion.indexOf("MSIE") > 0) {
                        multiselectOptions.open = function() {
                            // sets fixed width for IE, otherwise it will take 100% width.
                            $(".ui-multiselect-menu").width(multiselectOptions.minWidth);
                        };
                    }
                    $("#sponsorFilterCmb").multiselect(multiselectOptions);
                    $("#sponsorFilterContainer").show();
                    $('#sponsorFilterCmb').bind('change', korem.callback(this, this.onChangeSponsorFilter));
                } else {
                    //When there is no sponsor dropdown menu
                    that.sponsorKeyValMapping[data[0].code] = data[0].key; 
                }
                that.saveSponsorCodes();
            },
            error: function(XMLHttpRequest, textStatus, errorThrown) {
                alert("Status: " + textStatus);
                alert("Error: " + errorThrown);
            }
        });
    }, 
    onChangeSponsorFilter: function() {
        clearTimeout(this.changedTimeout);
        this.changedTimeout = korem.timeout(function() {
            this.doUpdateFilter();
        }, this.FILTER_CHANGE_DELAY, this);
    },
    saveSponsorCodes: function(){
        if (this.map && this.map.heatMapLayer) {
            var codes = $("#sponsorFilterCmb").val();
            if (codes == undefined) {
                // no sponsor combo.
                codes = [];
                for (var key in this.sponsorKeyValMapping) {
                    codes.push(key);
                }
            }
            this.selectedSponsorsCodes = codes;
            this.map.heatMapLayer.selectedSponsors = codes;
        }
    },
    doUpdateFilter: function() {
        var checkedVals = $("#sponsorFilterCmb").val() || ["-1"];
        var mapInstanceKey = this.mapInstanceKey;
        var from = this.dates[0].value;
        var to = this.dates[1].value;
        
        $.ajax({
            type: "POST",
            url: "../updateLocations.safe",
            data: {filters: JSON.stringify(checkedVals), mapInstanceKey: mapInstanceKey, from: from, to: to},
            success: korem.callback(this, function(data) {
                this.reapplySelection();
            }),
            error: function(XMLHttpRequest, textStatus, errorThrown) {
                alert("updateLocations failed. ");
            }
        });
        this.saveSponsorCodes();
    }, 
    reapplySelection: function() {
        $.ajax({
            type: "POST",
            url: "../reSetSelection.safe",
            data: {mapInstanceKey: this.mapInstanceKey},
            success: korem.callback(this, function(data) {
                this.masterObj.selectionTool.updateLocationList(data);
                this.masterObj.applyButton.needed();
            }),
            error: function(XMLHttpRequest, textStatus, errorThrown) {
                console.log("failure to keep the selection");
            }
        });

    }
};