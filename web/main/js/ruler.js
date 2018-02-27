am.Ruler = function(options) {
    korem.apply(this, options);
    korem.apply(this, {
        btnId: 'rulerButton',
        groupId: 'tools'
    });
    this.init();
}

am.Ruler.prototype = korem.apply({

    mapObj: null,
    measureEl: null,
    control: null,
    isOn: false,

    initialize: function() {
        this.listeners = [];
        this.measureEl = korem.get('measureEl');
        this.createControl();
    },

    doDeactivate: function() {
        this.isOn = false;
        this.measureEl.style.display = 'none';
        this.control.deactivate();
        this.measureEl.innerHTML = '';
    },

    doActivate: function() {
        if (this.isOn) {
            this.deactivate();
        } else {
            this.isOn = true;
            this.measureEl.style.display = 'block';
            this.control.activate();
            this.hideTAinfo();
        }
    },
    
    hideTAinfo: function() {
        /*
         * Hides the TA info label when the ruler is activated
         */        
        var inCompareMode = am.instance.mapsManager.inCompareMode;
        
        if( inCompareMode === false ) {
            korem.get('tradeAreaInfoContainer1').style.visibility = 'hidden';
        } else {
            korem.get('tradeAreaInfoContainer2').style.visibility = 'hidden';
        }
    },

    handleMeasurements: function(event) {
        if (event.measure != 0) {
            this.measureEl.innerHTML = event.measure.toFixed(3) + ' ' + event.units;
        }
    },

    setMap: function(mapObj) {
        this.mapObj = mapObj;
        this.deactivate();
        this.mapObj.map.div.appendChild(this.measureEl);
    },

    createControl: function() {
        var sketchSymbolizers = {
            Point: {
                pointRadius: 4,
                graphicName: 'square',
                fillColor: 'white',
                fillOpacity: 1,
                strokeWidth: 1,
                strokeOpacity: 1,
                strokeColor: '#333333'
            },
            Line: {
                strokeWidth: 3,
                strokeOpacity: 1,
                strokeColor: '#666666',
                strokeDashstyle: 'dash'
            }
        };
        var style = new OpenLayers.Style();
        style.addRules([
            new OpenLayers.Rule({
                symbolizer: sketchSymbolizers
            })
            ]);
        var styleMap = new OpenLayers.StyleMap({
            'default': style
        });
        var that = this;
        this.control = new OpenLayers.Control.Measure(
            OpenLayers.Handler.Path, {
                immediate: true,
                persist: true,
                handlerOptions: {
                    layerOptions: {
                        styleMap: styleMap
                    }
                }
            });
        this.control.geodesic = true;
        var handler = function(event) {
            that.handleMeasurements(event);
        }
        this.control.events.on({
            measure: handler,
            measurepartial: handler
        });
        return this.control;
    }
}, am.ToolBase.prototype);
