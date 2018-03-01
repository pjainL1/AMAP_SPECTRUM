am.Placemark = function() {
    korem.apply(this, {
        btnId: 'placemarkButton',
        groupId: 'tools'
    });
    this.init();
}

am.Placemark.prototype = korem.apply({

    listeners: null,
    mapObj: null,
    clickHandlers: null,
    icon: null,

    initialize: function() {
        this.listeners = [];
        this.clickHandlers = {};
        this.createIcon();
    },

    createIcon: function() {
        var size = new OpenLayers.Size(21,25);
        this.icon = new OpenLayers.Icon('../main/images/marker.png',
            size,
            new OpenLayers.Pixel(-(size.w/2), -size.h));
    },

    setMap: function(mapObj) {
        if (this.mapObj) {
            this.deactivate();
        }
        this.mapObj = mapObj;
        if (!this.mapObj.placemarkLayer) {
            this.mapObj.placemarkLayer = new OpenLayers.Layer.Markers('Markers', {
                displayInLayerSwitcher: false
            });
            this.mapObj.map.addLayer(this.mapObj.placemarkLayer);
            setTimeout(function() {
                // raise the placemark layer to the top.
                // this is needed to make sure the placemark's marker events are always on top.
                mapObj.map.raiseLayer(mapObj.placemarkLayer, mapObj.map.layers.length);
            }, 0);
            var that = this;
            this.mapObj.removePlacemark = function(dontInform) {
                if (mapObj.placemark) {
                    mapObj.placemarkLayer.removeMarker(mapObj.placemark);
                    mapObj.placemark.destroy();
                    mapObj.placemarkInfo = mapObj.placemark = null;
                    if (!dontInform) {
                        that.placemarkUpdated();
                    }
                }
            }
        }
        this.getClickHandler();
        this.placemarkUpdated();
    },

    getClickHandler: function() {
        var handler = this.clickHandlers[this.mapObj.mapInstanceKey];
        if (!handler) {
            this.mapObj.map.addControl(this.clickHandlers[this.mapObj.mapInstanceKey] = handler = new OpenLayers.Control.Click({
                listener: this
            }));
        }
        return handler;
    },

    trigger: function(e) {
        if (this.mapObj.removePlacemark) {
            this.mapObj.removePlacemark(true);
        }
        var lonLat = this.toLonLat(e.xy);
        this.mapObj.placemark = new OpenLayers.Marker(lonLat, this.icon.clone())
        this.mapObj.placemark.events.register('click', this, function(evt) {
            if (this.activated) {
                if(korem.get('tradeAreaChkProjected').checked){
                    $("#tradeAreaH3  > div").removeClass("buttonDotOff").addClass("buttonDotOn");
                }
                this.mapObj.removePlacemark();
                this.deactivate();
                OpenLayers.Event.stop(evt);
            }
        });
        this.mapObj.placemarkLayer.addMarker(this.mapObj.placemark);
        this.deactivate();
        this.mapObj.placemarkInfo = {
            pixel: e.xy,
            lonLat: lonLat
        };
        reportPlacemarkInfo = this.mapObj.placemarkInfo;
        this.placemarkUpdated();
        this.mapObj.map.raiseLayer(this.mapObj.placemarkLayer, this.mapObj.map.layers.length);
    },

    placemarkUpdated: function() {
        this.informListeners(function(listener) {
            listener.placemarkUpdated(this.mapObj.placemarkInfo);
        });
    },

    toLonLat: function(xy) {
        return this.mapObj.map.getLonLatFromPixel(xy);
    },

    addListener: function(listener) {
        this.listeners.push(listener);
    },

    informListeners: function(callback) {
        for (var i = 0; i < this.listeners.length; ++i) {
            callback.apply(this, [this.listeners[i]]);
        }
    },

    doDeactivate: function() {
        this.getClickHandler().deactivate();
    },

    doActivate: function() {
        this.getClickHandler().activate();
    }
}, am.ToolBase.prototype);
