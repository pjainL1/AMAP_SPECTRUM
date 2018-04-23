am.SelectionTool = function(options) {
    korem.apply(this, options);
    this.init();
};

am.SelectionTool.prototype = {

    mapInstanceKey: null,
    map: null,
    mapsManager: null,
    layerControl: null,

    layers: null,

    circularTool: null,
    polygonalTool: null,
    cancelTool: null,

    currentTool: null,

    vectorLayer: null,

    layerIds: null,

    locationList: null,

    currentData: null,
    
    locationListDefault: am.locale.selection.please,
    locationListAll: am.locale.selection.all,

    listeners: null,

    isProcessing: false,

    init: function() {
        this.layers = {};
        this.data = [];
        this.listeners = [];
        this.handleLocationList();
        this.addLayer();
        this.createCircularTool().init();
        this.createPolygonalTool().init();
        this.createCancelTool().init();
    },

    addListener: function(listener) {
        this.listeners.push(listener);
    },

    handleLocationList: function() {
        var that = this;
        this.locationList = korem.get('locationList');
        this.locationList.onchange = function() {
            for (var i = 0; i < that.listeners.length; ++i) {
                that.listeners[i].selectedLocationChanged();
            }
        };
    },

    setMap: function(map, mapInstanceKey) {
        this.map = map;
        this.mapInstanceKey = mapInstanceKey;
        this.addLayer();
    },

    //    layerAdded: function(layer) {
    //        if (layer.name.toLowerCase() == am.constants.locations.toLowerCase()) {
    //            this.layerIds = layer.id;
    //        }
    //    },

    addLayer: function() {
        var layerObj = this.layers[this.mapInstanceKey];
        if (!layerObj) {
            this.layers[this.mapInstanceKey] = layerObj = {};
            this.addOLLayer(layerObj);
        }
        this.vectorLayer = layerObj;
    },
    
    addOLLayer: function(layerObj) {
            this.map.addLayer(layerObj.layer = new OpenLayers.Layer.Vector('Vectors', {
                displayInLayerSwitcher: false
            }));
            this.map.addControl(layerObj.polygonalControl = new OpenLayers.Control.DrawFeature(layerObj.layer,
                OpenLayers.Handler.Polygon));
            this.map.addControl(layerObj.circularControl = new OpenLayers.Control.DrawFeature(layerObj.layer,
                OpenLayers.Handler.RegularPolygon,
                {
                    handlerOptions: {
                        sides: 25
                    }
                }));
            layerObj.layer.events.register('featureadded', this, this.completeSelection);
    },

    completeSelection: function(e) {
        this.isProcessing = true;
        this.currentTool.deactivate();
        $.ajax({
            url: '../setSelection.safe',
            context: this,
            type: 'post',
            data: {
                mapInstanceKey: this.mapInstanceKey,
                geometry: this.getGeometry(e.feature.geometry.getVertices()),
                append: true
            },
            success: function(data) {
                this.updateLocationList(data);
                this.clearFeature();
            }
        });
    },

    redoSelection: function(){
        $.ajax({
            url: '../reSetSelection.safe',
            context: this,
            type: 'post',
            data: {
                mapInstanceKey: this.mapInstanceKey
            },
            success: function(data) {
                this.updateLocationList(data);
            }
        });
    },

    clearClientSideSelection: function() {
        this.data = [];
        this.clearLocationList();
        this.clearFeature();
    },

    clearSelection: function() {
        this.isProcessing = true;
        var that = this;
        this.clearClientSideSelection();
        $.ajax({
            url: '../clearSelection.safe',
            context: this,
            type: 'post',
            data: {
                mapInstanceKey: this.mapInstanceKey
            },
            success: function() {
                that.mapsManager.layerUpdated();
            }
        });
    },

    clearLocationList: function() {
        this.locationList.innerHTML = '';
        this.locationList.appendChild(html.option({
            props: {
                innerHTML: this.locationListDefault
            }
        }));
    },

    updateLocationList: function(data) {
        this.data = data;
        if (data.length == 0) {
            this.clearLocationList();
        } else {
            this.locationList.innerHTML = '';
            this.locationList.appendChild(html.option({
                props: {
                    innerHTML: this.locationListAll,
                    value : ""
                }
            }));
            for (var i = 0; i < data.length; ++i) {
                this.locationList.appendChild(html.option({
                    props: {
                        innerHTML: data[i][1],
                        value: data[i][0]
                    }
                }));
            }
        }
        if(data.length > 0){
            this.locationList.onchange.apply(this.locationList);
        }
        this.mapsManager.layerUpdated();
    },

    getGeometry: function(vertices) {
        var geometry = '';
        for (var i = 0; i < vertices.length; ++i) {
            //var pixel = this.map.getViewPortPxFromLonLat(new OpenLayers.LonLat(vertices[i].x, vertices[i].y));
            //geometry += pixel.x.toFixed(0) + ',' + pixel.y.toFixed(0) + ',';
            geometry += vertices[i].x + ',' + vertices[i].y + ',';
        }
        return geometry;
    },

    createCircularTool: function() {
        var that = this;
        this.circularTool = korem.apply(korem.apply({}, am.ToolBase.prototype), {
            btnId: 'selectionToolCircular',
            groupId: 'tools',

            initialize: function() {},
            doActivate: function() {
                that.currentTool = this;
                that.vectorLayer.circularControl.activate();
            },
            doDeactivate: function() {
                that.vectorLayer.circularControl.deactivate();
            }
        });
        var originalActivate = this.circularTool.activate;
        this.circularTool.activate = function(){
            if(!that.isProcessing){
                originalActivate.apply(that.circularTool);
            }
        };
        return this.circularTool;
    },

    createPolygonalTool: function() {
        var that = this;
        this.polygonalTool = korem.apply(korem.apply({}, am.ToolBase.prototype), {
            btnId: 'selectionToolPolygonal',
            groupId: 'tools',

            initialize: function() {},
            doActivate: function() {
                that.currentTool = this;
                that.vectorLayer.polygonalControl.activate();
            },
            doDeactivate: function() {
                that.vectorLayer.polygonalControl.deactivate();
            }
        });
        var originalActivate = this.polygonalTool.activate;
        this.polygonalTool.activate = function(){
            if(!that.isProcessing){
                originalActivate.apply(that.polygonalTool);
            }
        };
        return this.polygonalTool;
    },

    createCancelTool: function() {
        var parent = this;
        this.cancelTool = korem.apply(korem.apply({}, am.ToolBase.prototype), {
            btnId: 'selectionToolCancel',
            groupId: 'tools',
            initialize: function() {},
            doActivate: function() {
                parent.clearSelection();
                this.deactivate();
                this.button.addClass('buttonActivated');
                setTimeout($.proxy(function() {
                    this.button.removeClass('buttonActivated');
                }, this), 500);
            },
            doDeactivate: function() {

            }
        });
        var originalActivate = this.cancelTool.activate;
        this.cancelTool.activate = function(){
            if(!parent.isProcessing){
                originalActivate.apply(parent.cancelTool);
            }
        };
        return this.cancelTool;
    },

    clearFeature: function() {
        setTimeout($.proxy(function() {
            this.isProcessing = false;
            this.vectorLayer.polygonalControl.destroy();
            this.vectorLayer.circularControl.destroy();
            this.vectorLayer.layer.destroy();
            this.addOLLayer(this.vectorLayer);
        }, this), 500);
    },

    getData: function() {
        if (this.locationList.selectedIndex == 0) {
            return this.data;
        } else {
            return [this.data[this.locationList.selectedIndex - 1]];
        }
    }
};
