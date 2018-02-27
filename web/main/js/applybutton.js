am.ApplyButton = function(options) {
    korem.apply(this, options);
    this.init();
};

am.ApplyButton.prototype = {
    id: null,
    mapInstanceKey: null,
    selectionTool: null,
    map: null,
    mapsManager: null,
    layerControl: null,
    button: null,
    labelEl: null,
    originalLabel: null,
    progressBar: null,
    dataHolders: null,
    isNeeded: false,
    isApplying: false,
    previousDataHolders: null,
    appliedLocations: null,
    init: function() {
        var that = this;
        this.dataHolders = [];
        this.previousDataHolders = [];
        this.button = korem.get(this.id);
        this.labelEl = korem.get(this.id + 'Center');
        this.originalLabel = this.labelEl.innerHTML;
        this.progressBar = $("#progressBar").progressbar();
        this.button.onclick = function() {
            that.apply();
        };
    },
    setMap: function(mapObj) {
        this.map = mapObj;
        this.mapInstanceKey = mapObj.mapInstanceKey;
        this.forceNeeded();
    },
    forceNeeded: function() {
        this.needed();
        this.prepareReapply();
    },
    selectedLocationChanged: function() {
        this.needed();
    },
    /**
     * DataHolders are classes that manage data that may applied. They
     * are expected to implement :
     * populateData(data) -> Must return an true if something was added to data.
     *                       The dataHolder name should be added to data.methods like this: data.methods += 'dataHolderName,'
     * applyFinished -> Will be called when the apply process is finished, if the dataHolder was part of the apply process.
     * @param {type} dataHolder
     */
    addDataHolder: function(dataHolder) {
        this.dataHolders.push(dataHolder);
    },
    needed: function() {
        this.labelEl.innerHTML = this.originalLabel + '*';
        EnableDisableBtns();
        this.isNeeded = true;
    },
    setIsApplying: function(applying) {
        this.isApplying = applying;
    },
    apply: function() {
        am.instance.customTradeArea.clearLayer();
        if (am.instance.mapsManager.inCompareMode){
           am.instance.sponsorFilteringManager.saveSponsorCodes();
        }
        if (this.isApplying) {
            return;
        }
        if (!this.isNeeded) {
            am.AlertDialog.show(am.locale.apply.nothing);
            return;
        }
            
        this.isApplying = true;
        this.mapsManager.disableCompare(true);
        var currentDataHolders = [];
        var data = {
            methods: '',
            mapInstanceKey: this.mapInstanceKey,
            locations: this.getLocations(),
            locationsCode: this.getLocationsCode()
        };
        this.appliedLocations = data.locations;
        var hasChangedCpt = 0;
        var hasError = false;
        for (var i = 0; i < this.dataHolders.length; ++i) {
            var dataHolder = this.dataHolders[i];
            dataHolder.populateData(data, {
                add: function() {
                    ++hasChangedCpt;
                    currentDataHolders.push(dataHolder);
                },
                error: function() {
                    ++hasChangedCpt;
                    hasError = true;
                }
            });
        }
        if (hasError) {
            this.setIsApplying(false);
            return;
        }
        this.previousDataHolders = currentDataHolders;
        var that = this;
        this.doApply(data, function() {
            that.updateProgress(currentDataHolders, hasChangedCpt == currentDataHolders.length);
        }, function(continuation) {
            if (currentDataHolders.length > 0) {
                continuation();
            } else {
                that.reset(hasChangedCpt == currentDataHolders.length);
            }/**/
        });
    },
    doApply: function(data, applyCallback, removeAnalysisCallback) {
        var that = this;
        var doApply = function() {
            $.ajax({
                url: '../apply.safe',
                context: this,
                type: 'post',
                data: data,
                success: function(json) {
                    if (json.invalid) {
                        window.location = 'expired.do';
                        document.location = 'expired.do';
                    }
                    if (applyCallback) {
                        applyCallback(json);
                    }
                }
            });
        };    
        var continuation = function() {
            that.button.style.display = 'none';
            $("#applyAreaSelectDiv")[0].style.display = 'none';
            that.progressBar.progressbar('option', 'value', 0);
            that.progressBar[0].style.display = 'block';
            doApply();
        };
        if (removeAnalysisCallback) {
            removeAnalysisCallback(continuation);
        } else {
            doApply();
        }
    },
    prepareReapply: function() {
        for (var i = 0; i < this.previousDataHolders.length; ++i) {
            this.previousDataHolders[i].changed();
        }
    },
    onEnter: function() {
        if (this.isNeeded) {
            this.apply();
        }
    },
    getLocations: function() {
        var locations = '';
        var data = this.selectionTool.getData();
        for (var i = 0; i < data.length; ++i) {
            locations += data[i][0] + ',';
        }
        return locations;
    },
    getLocationsCode: function() {
        var locations = '';
        var data = this.selectionTool.getData();
        for (var i = 0; i < data.length; ++i) {
            locations += data[i][1] + ',';
        }
        return locations;
    },
    updateProgress: function(listeners, allPopulated) {
        var that = this;
        $.ajax({
            url: '../getProgress.safe',
            context: this,
            type: 'post',
            success: function(data) {
                that.progressBar.progressbar('option', 'value', data.progress);
                if (data.progress < 100) {
                    that.updateProgress(listeners, allPopulated);
                } else {
                    $.ajax({
                        url: '../bubbleLocations.safe',
                        context: that,
                        type: 'post',
                        data: {
                            mapInstanceKey: this.mapInstanceKey
                        },
                        success: function() {
                            var updateLayerWhenApplied = false;
                            for (var i = 0; i < listeners.length; ++i) {
                                listeners[i].applyFinished();
                                updateLayerWhenApplied = updateLayerWhenApplied || listeners[i].updateLayerWhenApplied;
                            }
                            if (updateLayerWhenApplied) {
                                that.layerControl.updateLayers(undefined, korem.callback(this, this.updateLegends));
                            }
                            that.reset(allPopulated);
                            that.mapsManager.updateMapInfoBox(this.map);
                        }
                    });
                }
            }
        });
    },
    
    updateLegends: function() {
        this.map.legend.legendManager.getLegendColors();
    },
    reset: function(allPopulated) {
        var that = this;
        this.isNeeded = !allPopulated;
        this.isApplying = false;
        this.mapsManager.disableCompare(false);
        setTimeout(function() {
            that.progressBar[0].style.display = 'none';
            if (allPopulated) {
                that.labelEl.innerHTML = that.originalLabel;
                $(".buttonDotOn").removeClass("buttonDotOn").addClass("buttonDotOff");  
            }
            EnableDisableBtns();
            that.button.style.display = '';            
            $("#applyAreaSelectDiv")[0].style.display = '';
        }, 250);
    }
};