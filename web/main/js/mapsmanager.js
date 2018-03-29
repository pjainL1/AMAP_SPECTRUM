am.MapsManager = function (options) {
    korem.apply(this, options);
    this.init();
};

am.MapsManager.prototype = {
    MAPMOVED_WAITTIME: 100,
    mainInstance: null, // am.Main instance
    datePickers: null,
    workspaceKey: null,
    onMapCreated: null,
    onCompareStarted: null,
    onCompareStopped: null,
    onZoomChanged: null,
    masterContainer: null,
    slaveContainer: null,
    relativeMasterContainer: null,
    relativeSlaveContainer: null,
    masterCell: null,
    slaveCell: null,
    slave: null,
    master: null,
    inCompareMode: false,
    lastMapMovedTime: 0,
    mapsLinked: true,
    canCompare: true,
    slaveDisabler: null,
    slaveLockToggler: null,
    mapTools: null,
    dashboardToggler: null,
    ruler: null,
    LOADEND_RELAX_DELAY: 1000,
    init: function () {
        var that = this;
        this.handleCompareBtn();
        this.createLockToggler();
        this.masterCell = korem.get('firstMapContainer');
        this.slaveCell = korem.get('secondMapContainer');
        this.relativeMasterContainer = korem.get('firstRelativeMapContainer');
        this.relativeSlaveContainer = korem.get('secondRelativeMapContainer');
        this.masterContainer = korem.get('firstMap');
        this.slaveContainer = korem.get('secondMap');
        this.slaveDisabler = korem.get('slaveDisabler');
        this.mapTools = korem.get('mapTools');
        this.findControl = korem.get('findControl');
        this.compareBtn = korem.get('compareBtn');
        this.dashboardToggler = korem.get('dashboardToggler');
        this.getMaster(function (mapObj) {
            that.onMapCreated(mapObj);
        });
        this.mapMoved = korem.createSlower(this.MAPMOVED_WAITTIME, function (obj) {
            that.doMapMoved(obj);
        });
    },
    mapMoved: null,
    tryMapMoved: function (map) {
        if (this.inCompareMode && this.mapsLinked && map == this.master.map) {
            this.mapMoved(map);
        }
    },
    disableCompare: function (disable) {
        this.canCompare = !disable;
        var cursor = (this.canCompare) ? 'pointer' : 'default';
        korem.get('compareBtn').style.cursor = cursor;
        korem.get('stopCompareBtn').style.cursor = cursor;
    },
    doMapMoved: function (map) {
        var that = this;
        this.getSlave(function (slaveObj) {
            that.masterViewChanged(map, slaveObj);
        });
    },
    masterViewChanged: function (masterMap, slaveObj) {
        if (slaveObj && slaveObj.map) {
            slaveObj.map.setCenter(masterMap.getCenter(), masterMap.getZoom());
        }
    },
    handleCompareBtn: function () {
        var compareBtn = $('#compareBtn');
        var that = this;
        compareBtn.click(function () {
            if (that.canCompare) {
                that.compare();
            }
            am.instance.sponsorFilteringManager.saveSponsorCodes();
        });
        $('#stopCompareBtn').click(function () {
            if (that.canCompare) {
                that.stopCompare();
            }
        });
    },
    toggleCompareBtn: function () {
        if (this.inCompareMode) {
            $('#compareBtn').addClass('buttonActivated');
        } else {
            $('#compareBtn').removeClass('buttonActivated');
        }
    },
    compare: function () {
        $("input#tradeAreaDownloadBtn").attr("disabled", true);
        if (this.inCompareMode) {
            this.stopCompare();
        } else {
            this.inCompareMode = true;
            this.toggleCompareBtn();
            this.startCompare();
        }
    },
    showSlaveDisabler: function () {
        this.slaveDisabler.style.display = 'block';
        this.hideControls(this.slave);
    },
    hideSlaveDisabler: function () {
        this.slaveDisabler.style.display = 'none';
        this.showControls(this.slave);
    },
    swapMaps: function () {
        this.swapLegends(this.slave, this.master);
        this.swapTAInfo(this.slave, this.master);
        this.relativeSlaveContainer.appendChild(this.masterContainer);
        this.relativeMasterContainer.appendChild(this.slaveContainer);

        var temp = this.masterContainer;
        this.masterContainer = this.slaveContainer;
        this.slaveContainer = temp;
    },
    swapMaster: function (callback) {
        this.setSlaveUpdateDisabled(false);
        var temp = this.master;
        this.master = this.slave;
        this.slave = temp;

        if (this.mapsLinked) {
            this.hideControls(this.slave);
        } else {
            this.showControls(this.slave);
        }
        this.showControls(this.master);
        this.copyBaseLayer();
        if (callback) {
            callback();
        }
    },
    copyBaseLayer: function () {
        var i = 0;
        for (; i < this.slave.map.layers.length; ++i) {
            if (this.slave.map.layers[i] == this.slave.map.baseLayer) {
                break;
            }
        }
        this.master.map.setBaseLayer(this.master.map.layers[i]);

        this.master.map.trafficLayerVector.setVisibility(false);
        this.master.map.transitLayerVector.setVisibility(false);
        this.master.map.bicyclingLayerVector.setVisibility(false);

        this.master.map.trafficLayerVector.setVisibility(this.slave.map.trafficLayerVector.getVisibility());
        this.master.map.transitLayerVector.setVisibility(this.slave.map.transitLayerVector.getVisibility());
        this.master.map.bicyclingLayerVector.setVisibility(this.slave.map.bicyclingLayerVector.getVisibility());
    },
    updateSlave: function (mapObj) {
        if (mapObj == this.master && this.inCompareMode && this.slave && this.slave.kmsLayer.updateDisabled) {
            var that = this;
            setTimeout(function () {
                that.setSlaveUpdateDisabled(false);
                that.slave.kmsLayer.refresh();
                that.slave.spectrumLayer.refresh();
                that.refreshingSlaveMap = true;
                that.slave.heatMapLayer.refresh();
            }, 200);
        }
    },
    setSlaveUpdateDisabled: function (doUpdate) {
        this.slave.kmsLayer.updateDisabled = this.slave.heatMapLayer.updateDisabled = doUpdate;
        this.slave.spectrumLayer.updateDisabled = this.slave.heatMapLayer.updateDisabled = doUpdate;
    },
    swapCompare: function () {
        this.animateSwapMap();
    },
    continueSwapMap: function () {
        var that = this;
        this.updateSize(function (slaveObj) {
            that.resetMap(slaveObj, function () {
                that.swapMaps();
                that.swapMaster(function () {
                    that.datePickers.updateLocations(that.master, function () {
                        that.onCompareStarted(that.master);
                    });
                });
            });
        });
    },
    swapLegends: function (slaveObj, masterObj) {
        slaveObj.legend.moveTo(this.relativeSlaveContainer);
        masterObj.legend.moveTo(this.relativeMasterContainer);
    },
    swapTAInfo: function (newMap, oldMap) {
        var oldContainer = $(oldMap.container).parents('td')[0];
        var newContainer = $(newMap.container).parents('td')[0];
        $('.tradeAreaInfoTxt', newContainer).html($('.tradeAreaInfoTxt', oldContainer).html());
        $('.tradeAreaInfoContainer', newContainer).css('visibility', $('.tradeAreaInfoContainer', oldContainer).css('visibility'));

        $('.tradeAreaInfoTxt', oldContainer).html('');
        $('.tradeAreaInfoContainer', oldContainer).css('visibility', 'hidden');

        $('.minInfoTxt').html('');
        $('.minInfoTxt').css('visibility', 'hidden');
    },
    stopCompare: function () {
        this.animateStopCompare();
    },
    continueStopCompare: function (callback) {
        var that = this;
        this.inCompareMode = false;
        this.toggleCompareBtn();
        this.getSlave(function (slaveObj) {
            that.resetMap(slaveObj, function () {
                slaveObj.kmsLayer.setVisibility(false);
                slaveObj.spectrumLayer.setVisibility(false);
                that.finishStopCompare(callback);
            });
        });
    },
    finishStopCompare: function (callback) {
        this.relativeMasterContainer.appendChild(this.mapTools);
        this.relativeMasterContainer.appendChild(this.findControl);
        this.relativeMasterContainer.appendChild(this.compareBtn);
        this.relativeMasterContainer.appendChild(this.dashboardToggler);
        this.slaveCell.style.width = '0%';
        this.masterCell.style.width = '100%';
        this.hideCell(this.slaveCell);
        this.showCell(this.masterCell);
        this.hideSlaveDisabler();
        this.swapMaps();
        this.slaveLockToggler.button.hide();

        this.updateSize();
        this.onCompareStopped();

        callback();
    },
    hideCell: function (cell) {
        cell.style.position = 'absolute';
        cell.style.left = '-10000px';
        cell.style.top = '-10000px';
    },
    showCell: function (cell) {
        cell.style.position = 'relative';
        cell.style.left = 'auto';
        cell.style.top = 'auto';
    },
    animateStartCompare: function () {
        var that = this;
        korem.get('newMap').style.width = '0px';
        korem.get('newMap').style.left = '5px';
        korem.get('currentMap').style.left = '5px';
        korem.get('currentMap').style.width = '185px';
        korem.get('animStartCompareCtn').style.display = 'block';
        $('#newMap').animate({
            width: 90
        }, 1000);
        $('#currentMap').animate({
            left: 105,
            width: 90
        }, 1000, function () {
            korem.get('animStartCompareCtn').style.display = 'none';
            that.continueStartCompare();
            $(".tradeAreaInfoSuperWrapperSecond").css("bottom", $("#datesFilterTypesSelect").val() == "comparison" ? 83 : 53);
        });
    },
    animateSwapMap: function () {
        var that = this;
        korem.get('newerMap').style.width = '0px';
        korem.get('newerMap').style.visibility = 'visible';
        korem.get('newMap').style.left = '5px';
        korem.get('currentMap').style.left = '105px';
        korem.get('currentMap').style.width = '90px';
        korem.get('animStartCompareCtn').style.display = 'block';
        $('#newMap').animate({
            left: 105
        }, 1000);
        $('#currentMap').animate({
            left: 200
        }, 1000);
        $('#newerMap').animate({
            width: 90
        }, 1000, function () {
            korem.get('animStartCompareCtn').style.display = 'none';
            that.continueSwapMap();
        });
    },
    animateStopCompare: function () {
        var that = this;
        korem.get('newerMap').style.width = '0px';
        korem.get('newerMap').style.visibility = 'hidden';
        korem.get('newMap').style.left = '5px';
        korem.get('newMap').style.width = '90px';
        korem.get('currentMap').style.left = '105px';
        korem.get('currentMap').style.width = '90px';
        korem.get('animStartCompareCtn').style.display = 'block';
        $('#newMap').animate({
            width: 185
        }, 1000);
        $('#currentMap').animate({
            left: 200
        }, 1000, function () {
            that.continueStopCompare(function () {
                korem.get('animStartCompareCtn').style.display = 'none';
                $(".tradeAreaInfoSuperWrapperSecond").css("bottom", 20);
            });
        });
    },
    startCompare: function () {
        this.animateStartCompare();
    },
    continueStartCompare: function () {
        var that = this;
        this.masterCell.style.width = '50%';
        this.showCell(this.slaveCell);
        this.slaveCell.style.width = '50%';
        this.showSlaveDisabler();
        this.relativeSlaveContainer.appendChild(this.mapTools);
        this.relativeSlaveContainer.appendChild(this.findControl);
        this.relativeSlaveContainer.appendChild(this.compareBtn);
        this.relativeSlaveContainer.appendChild(this.dashboardToggler);
        this.slaveLockToggler.button.show();

        this.updateSize(function (slaveObj) {
            slaveObj.kmsLayer.setVisibility(true);
            slaveObj.spectrumLayer.setVisibility(true);
            that.slaveLockToggler.deactivate();
            that.swapMaster();
            that.datePickers.updateLocations(that.master, function () {
                that.onCompareStarted(that.master);
            });
        });
    },
    hideControls: function (mapObj) {
        if (mapObj && mapObj.controls) {
            for (var i = 0; i < mapObj.controls.length; ++i) {
                var control = mapObj.controls[i];
                control.deactivate();
                mapObj.map.removeControl(control);
            }
            mapObj.controls = null;
        }
        if (mapObj && mapObj == this.slave) {
            this.setSlaveUpdateDisabled(true);
        }
    },
    showControls: function (mapObj) {
        if (!mapObj.controls) {
            if (!this.ruler) {
                this.ruler = new am.Ruler();
            }
            this.ruler.setMap(mapObj);
            mapObj.controls = [
                new OpenLayers.Control.Navigation(),
                new OpenLayers.Control.PanZoomBar(),
                new OpenLayers.Control.LayerSwitcher(),
                this.ruler.createControl()
            ];
            for (var i = 0; i < mapObj.controls.length; ++i) {
                mapObj.map.addControl(mapObj.controls[i]);
            }
        }
        if (mapObj && mapObj == this.slave) {
            this.setSlaveUpdateDisabled(false);
        }
    },
    updateSize: function (callback) {
        var that = this;
        this.getSlave(function (mapObj) {
            that.getMaster(function (masterObj) {
                mapObj.map.updateSize();
                masterObj.map.updateSize();
                if (callback) {
                    callback(mapObj, masterObj);
                }
            });
        });
    },
    updateSizeIfExists: function () {
        if (this.slave) {
            this.slave.map.updateSize();
        }
        this.master.map.updateSize();
    },
    getMap: function (callback, container, mapName, fncName) {
        var that = this;
        this.getMapInstanceKey(function (mapInstanceKey) {
            that[mapName] = that.createMap(mapInstanceKey, container, mapName);
            callback(that[mapName]);
        });
        this[fncName] = function (callback) {
            callback(that[mapName]);
        };
    },
    getMaster: function (callback) {
        this.getMap(callback, this.masterContainer, 'master', 'getMaster');
    },
    getSlave: function (callback) {
        this.getMap(callback, this.slaveContainer, 'slave', 'getSlave');
    },
    getMapInstanceKey: function (callback) {
        var that = this;
        $.ajax({
            url: '../getMapInstanceKey.safe',
            type: 'post',
            data: {
                workspaceKey: this.workspaceKey
            },
            success: function (data) {
                //that.initKeepAlive(data.mapInstanceKey);  // mdube 2013-01-24 - remove since ldap authentication
                callback(data.mapInstanceKey);
            }
        });
    },
    showViewport: function (viewport) {
        var bounds = new OpenLayers.Bounds();
        bounds.extend(this.project(viewport.getSouthWest()));
        bounds.extend(this.project(viewport.getNorthEast()));
        this.master.map.zoomToExtent(bounds);
    },
    project: function (latLng) {
        var xy = OpenLayers.Projection.transform({
            x: latLng.lng(),
            y: latLng.lat()
        }, this.master.map.displayProjection, this.master.map.projection);
        return new OpenLayers.LonLat(xy.x, xy.y);
    },
    projectXY: function (xy) {
        var projectedXY = OpenLayers.Projection.transform({
            x: xy.x,
            y: xy.y
        }, this.master.map.displayProjection, this.master.map.projection);
        return new OpenLayers.LonLat(projectedXY.x, projectedXY.y);
    },
    handleCheckboxesExclusivity: function (layer, transitLayers) {
        if (layer.object.getVisibility()) {
            for (var ii = 0; ii < transitLayers.length; ii++) {
                if (layer.object !== transitLayers[ii]) {
                    transitLayers[ii].setVisibility(false);
                }
            }
        }
    },
    createGoogleLayers: function (mapObject) {
        var that = this;
        var trafficLayer = this.trafficLayer = new google.maps.TrafficLayer();
        var transitLayer = this.transitLayer = new google.maps.TransitLayer();
        var bicyclingLayer = this.bicyclingLayer = new google.maps.BicyclingLayer();

        mapObject.trafficLayerVector = this.trafficLayerVector = new OpenLayers.Layer.Vector(
                am.locale.map.layers.traffic,
                {
                    isBaseLayer: false,
                    visibility: false,
                    eventListeners: {'visibilitychanged': function (layer) {
                            trafficLayer.setMap((layer.object.getVisibility()) ? this.map.baseLayer.mapObject : null);
                            that.handleCheckboxesExclusivity(layer, transitLayers);
                        }}
                }
        );
        mapObject.transitLayerVector = this.transitLayerVector = new OpenLayers.Layer.Vector(
                am.locale.map.layers.transit,
                {
                    isBaseLayer: false,
                    visibility: false,
                    eventListeners: {'visibilitychanged': function (layer) {
                            transitLayer.setMap((layer.object.getVisibility()) ? this.map.baseLayer.mapObject : null);
                            that.handleCheckboxesExclusivity(layer, transitLayers);
                        }}
                }
        );
        mapObject.bicyclingLayerVector = this.bicyclingLayerVector = new OpenLayers.Layer.Vector(
                am.locale.map.layers.bicycling,
                {
                    isBaseLayer: false,
                    visibility: false,
                    eventListeners: {'visibilitychanged': function (layer) {
                            bicyclingLayer.setMap((layer.object.getVisibility()) ? this.map.baseLayer.mapObject : null);
                            that.handleCheckboxesExclusivity(layer, transitLayers);
                        }}
                }
        );

        var transitLayers = [this.trafficLayerVector, this.transitLayerVector, this.bicyclingLayerVector];

        var googleLayers = [
            this.firstLayer =
                    new OpenLayers.Layer.Google(
                            am.locale.map.layers.physical,
                            {
                                type: google.maps.MapTypeId.TERRAIN
                            }),
            new OpenLayers.Layer.Google(
                    am.locale.map.layers.streets,
                    {
                        numZoomLevels: 20
                    }),
            new OpenLayers.Layer.Google(
                    am.locale.map.layers.hybrid,
                    {
                        type: google.maps.MapTypeId.HYBRID,
                        numZoomLevels: 20
                    }),
            new OpenLayers.Layer.Google(
                    am.locale.map.layers.satellite,
                    {
                        type: google.maps.MapTypeId.SATELLITE,
                        numZoomLevels: 22,
                        eventListeners: {'visibilitychanged': function (layer) {
                                if (layer.object.visibility) {
                                    var checkboxes = $(".dataLayersDiv input[type='checkbox']");
                                    checkboxes.each(function (idx, elt) {
                                        $(elt).attr('disabled', true);
                                    });
                                } else {
                                    var checkboxes = $(".dataLayersDiv input[name='checkbox']");
                                    checkboxes.each(function (idx, elt) {
                                        $(elt).removeAttr("disabled");
                                    });
                                }
                            }}
                    }
            ),
            new OpenLayers.Layer.Google(
                    am.locale.map.layers.base,
                    {
                        type: 'grey',
                        numZoomLevels: 20
                    }
            ),
            new OpenLayers.Layer.Google(
                    am.locale.map.layers.night,
                    {
                        type: 'night',
                        numZoomLevels: 20
                    }
            ),
            this.trafficLayerVector,
            this.transitLayerVector,
            this.bicyclingLayerVector
        ];

        return googleLayers;
    },
    createSpecialLayers: function (mapInstanceKey, callback) {
        var kmsLayer = new OpenLayers.Layer.KMS(
                'KMS', '../getOpenLayers.safe', {
                    format: 'image/png',
                    layers: [mapInstanceKey]
                }, {
            singleTile: true,
            ratio: 1,
            transitionEffect: 'null',
            units: 'm',
            displayInLayerSwitcher: false
        });
        reportKmsLayer = kmsLayer;
        

        var spectrumLayer = new OpenLayers.Layer.XYZ(
                'spectrumLayer','../getTileSpectrum.safe' , {
                    format: 'image/png',
                    isBaseLayer: false,
                    getURL: function (bounds) {
                        var refreshTimeStamp = new Date().getTime();
                        var res = this.map.getResolution();
                        var x = Math.round((bounds.left - this.maxExtent.left) / (res * this.tileSize.w)) + 1;
                        var y = Math.round((this.maxExtent.top - bounds.top) / (res * this.tileSize.h)) + 1;
                        var z = this.map.getZoom() + this.zoomOffset + 1; // matchopenlayers zoom level to spectrum's
                        //var mapPath = "COVERAGE/Tiles/_3_HSPA_";
                        return this.url+"?" + "z" + "=" + z + "&" + "x" + "=" + x + "&" + "y" + "=" + y + "&refreshTimeStamp=" + refreshTimeStamp;
                    },
                    refresh: function(){
                        this.refreshTimeStamp = new Date().getTime();
                        this.setVisibility(false);
                        this.setVisibility(true);
                        
                    }
                
        }
        );


        var heatMap = new OpenLayers.Layer.HeatMap(
                'HeatMap', '../getHeatMap.safe', {
                    format: 'image/png',
                    layers: [mapInstanceKey]
                }, {
            visibility: false,
            singleTile: true,
            ratio: 1,
            transitionEffect: 'null',
            displayInLayerSwitcher: false
        });

        var geocodingLayer = new OpenLayers.Layer.Markers("Geocoding Layer",
                {
                    isBaseLayer: false,
                    visibility: false,
                    displayInLayerSwitcher: false
                }
        );
        callback.apply(this, [kmsLayer, heatMap, geocodingLayer,spectrumLayer]);
    },

    createOOMap: function (container) {
        var style = [{featureType: "all", elementType: "all", stylers: [{visibility: "simplified"}, {hue: "#4d00ff"}, {saturation: -64}]}];
        var map = new OpenLayers.Map(container,
                {
                    projection: new OpenLayers.Projection("EPSG:900913"),
                    displayProjection: new OpenLayers.Projection("EPSG:4326"),
                    units: "m",
                    numZoomLevels: 16,
                    maxResolution: 156543.0339,
                    maxExtent: new OpenLayers.Bounds(-20037508, -20037508, 20037508, 20037508.34),
                    controls: [],
                    styles: style

                }
        );



        return map;
    },
    initCenter: function (map) {
        var center, zoom;
        if (this.master) {
            center = this.master.map.getCenter();
            zoom = this.master.map.getZoom();
        } else {
            center = new OpenLayers.LonLat(-9949043.0001309, 6410925.9245239);
            zoom = 4;
        }
        setTimeout(function () {
            map.setCenter(center, zoom);
            map.savePosition();
        }, 500);
    },
    initEvents: function (map, mapObj, kmsLayer, heatMap,spectrumLayer) {
        var layerLoadCpt = 0;
        map.events.register('move', this, function () {
            this.tryMapMoved(map);
        });
        kmsLayer.events.register('loadstart', this, function () {
            ++layerLoadCpt;
            map.div.style.cursor = 'wait';
        });
        spectrumLayer.events.register('loadstart', this, function () {
            ++layerLoadCpt;
            map.div.style.cursor = 'wait';
        });
        heatMap.events.register('loadstart', this, function () {
            ++layerLoadCpt;
            map.div.style.cursor = 'wait';
        });
        heatMap.events.register('loadend', this, function () {
            if (--layerLoadCpt == 0) {
                map.div.style.cursor = '';
            }
            if (mapObj.hotSpotLayer) {
                mapObj.legend.update(mapObj.hotSpotLayer, mapObj.hotSpotLayerVisibility);
            }
        });
        var zoomChanged = false;
        map.events.register('zoomend', this, function () {
            zoomChanged = true;
        });
        kmsLayer.events.register('loadend', this, function () {
            
            if (--layerLoadCpt == 0) {
                map.div.style.cursor = '';
            }
            if (zoomChanged) {
                zoomChanged = false;
                this.onZoomChanged();
            }
            this.updateSlave(mapObj);
        });
        spectrumLayer.events.register('loadend', this, function () {
            //console.log('loadend : ' + new Date().getTime());
            if (--layerLoadCpt == 0) {
                map.div.style.cursor = '';
            }
            if (zoomChanged) {
                zoomChanged = false;
                this.onZoomChanged();
            }
            this.updateSlave(mapObj);
        });
    },
    createMap: function (mapInstanceKey, container, legendId) {
        var mapObj;
        this.createSpecialLayers(mapInstanceKey, function (kmsLayer, heatMap, geocodingLayer,spectrumLayer) {
            var map = this.createOOMap(container);
            map.addLayers(this.createGoogleLayers(map));
            
            map.addLayers([kmsLayer, heatMap, geocodingLayer,spectrumLayer]);
            
            this.initCenter(map);
            //map.addLayers(this.createSpectrumLayer(map));
            mapObj = {
                container: container,
                map: map,
                kmsLayer: kmsLayer,
                heatMapLayer: heatMap,
                geocodingLayer: geocodingLayer,
                spectrumLayer:spectrumLayer,
                mapInstanceKey: mapInstanceKey,
                legend: new am.Legend({
                    id: legendId,
                    map: map,
                    mapInstanceKey: mapInstanceKey
                }),
                trafficLayer: this.trafficLayer,
                transitLayer: this.transitLayer,
                bicyclingLayer: this.bicyclingLayer
            };
            this.initEvents(map, mapObj, kmsLayer, heatMap, geocodingLayer,spectrumLayer);

            this.showControls(mapObj);
        });
        return mapObj;
    },
    visibilityUpdated: function (layers) {
        var that = this;
        this.getMaster(function (mapObj) {
            var refreshKms = false;
            for (var i = 0; i < layers.length; i++) {
                var layer = layers[i];
                if (layer.isHotspot) {
                    mapObj.hotSpotLayer = layer;
                    mapObj.hotSpotLayerVisibility = layer.visibility;
                    mapObj.heatMapLayer.setVisibility(layer.visibility);
                    if (!layer.visibility) {
                        mapObj.legend.update(layer, layer.visibility);
                    }
                } else if (layer.isGeocoding)
                {
                    mapObj.geocodingLayer.setVisibility(layer.visibility);
                    
                } else {
                    refreshKms = true;
                    mapObj.legend.update(layer);
                }
            }
            if (refreshKms) {
                that.layerVisibilityUpdated(mapObj.kmsLayer);
                that.layerVisibilityUpdated(mapObj.spectrumLayer);
            }
        });
    },
    layerUpdated: function () {
        var that = this;
        this.getMaster(function (mapObj) {
            that.layerVisibilityUpdated(mapObj.kmsLayer);
            that.layerVisibilityUpdated(mapObj.spectrumLayer);
        });
    },
    layerVisibilityUpdated: function (layer) {
        layer.refresh(true);
    },
    createLockToggler: function () {
        var that = this;
        this.slaveLockToggler = korem.apply(korem.apply({}, am.ToolBase.prototype), {
            btnId: 'slaveLockToggler',
            groupId: 'slaveLockToggler',
            control: null,
            initialize: function () {
            },
            doActivate: function () {
                that.mapsLinked = false;
                that.hideSlaveDisabler();
            },
            doDeactivate: function () {
                that.setSlaveUpdateDisabled(that.mapsLinked = true);
                that.showSlaveDisabler();
                that.getMaster(function (masterObj) {
                    that.getSlave(function (slaveObj) {
                        that.masterViewChanged(masterObj.map, slaveObj);
                        that.updateSlave(masterObj);
                    });
                });
            }
        });
        this.slaveLockToggler.init();
    },
    resetMap: function (mapObj, callback) {
        var that = this;
        if (mapObj.removePlacemark) {
            mapObj.removePlacemark();
        }
        $.ajax({
            url: '../resetMap.safe',
            type: 'post',
            data: {
                mapInstanceKey: mapObj.mapInstanceKey
            },
            success: function () {
                mapObj.trafficLayer.setMap(null);
                mapObj.transitLayer.setMap(null);
                mapObj.bicyclingLayer.setMap(null);

                that.layerVisibilityUpdated(mapObj.kmsLayer);
                that.layerVisibilityUpdated(mapObj.spectrumLayer);
                that.mainInstance.streetViewControl.show();
                that.resetFind(mapObj);
                if (mapObj.hotSpotLayer && !callback) {
                    mapObj.hotSpotLayer.elVisibility = mapObj.hotSpotLayer.visibility = false;
                    that.visibilityUpdated(mapObj.hotSpotLayer);
                }
                mapObj.legend.reset();
                if (callback) {
                    callback();
                }
            }
        });
    },
    setCenter: function (xy) {
        this.master.map.setCenter(this.projectXY(xy), 13);
    },
    zoomToExtent: function (boundsXY) {
        var bounds = new OpenLayers.Bounds();
        bounds.extend(this.projectXY({
            x: boundsXY.minx,
            y: boundsXY.miny
        }));
        bounds.extend(this.projectXY({
            x: boundsXY.maxx,
            y: boundsXY.maxy
        }));
        this.master.map.zoomToExtent(bounds);
    },
    initKeepAlive: function (mapInstanceKey) {
        setInterval(function () {
            $.ajax({
                url: 'keepAlive.do',
                type: 'get',
                data: {
                    mapInstanceKey: mapInstanceKey,
                    r: Math.random()
                }
            });
        }, 1000 * 60 * 5);
    },
    updateMapInfoBox: function (map) {
        var container = $(map.container).parents('td')[0];
        var html = "";
        if (this.mainInstance.hotSpot.hasInfoTextActive()) {
            if (this.mainInstance.hotSpot.isComparison()) {
                html += am.locale.hotspot.filterInvalid.comparison;
            } else {
                html += am.locale.hotspot.filterInvalid.single;
            }
        } else if (this.mainInstance.storeLevelAnalysis.hasInfoTextActive() && this.mainInstance.hotSpot.isComparison()) {
            html += am.locale.hotspot.filterInvalid.comparison;
        }

        if (this.mainInstance.tradeArea.hasInfoTextActive()) {
            html += this.mainInstance.tradeArea.getTALabel();
        }
        html == "" ? this.hideMapInfoBox(container) : this.showMapInfoBox(container, html);
    },
    hideMapInfoBox: function (container) {
        $('.minInfoTxt', container).html('');
        $('.minInfoTxt', container).css('visibility', 'hidden');
        $('.tradeAreaInfoTxt', container).html('');
        $('.tradeAreaInfoContainer', container).css('visibility', 'hidden');
    },
    showMapInfoBox: function (container, html) {
        $('.tradeAreaInfoTxt', container).html(html);
        $('.tradeAreaInfoContainer', container).css('visibility', 'visible');
    }
};