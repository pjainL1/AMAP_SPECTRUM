am.LayerControl = function(options) {
    korem.apply(this, options);
    this.init();
};

am.LayerControl.prototype = {
    GROUP_SLIDE_DELAY: 333,
    VISIBILITY_CHANGE_DELAY: 250,
    
    mapInstanceKey: null,
    map: null,
    layerAdded: null,
    listeners: null,
    layerController: null,
    layers: null,
    hotspotLayer: null,
    hotspotEl: null,
    hotspotChk: null,
    geocodingLayer: null,
    geocodingEl: null,
    geocodingChk: null,
    layersById: null,
    changing: false,
    changedLayers: null,
    
    init: function() {
        this.changedLayers = [];
        this.listeners = [];
        this.layers = [];
        this.layersById = {};
        this.layerController = $('#layerController')[0];
        this.initLayers();
        reportLayerControl = this;
    },
    setMapInstanceKey: function(mapInstanceKey, reset) {
        this.mapInstanceKey = mapInstanceKey;
        this.updateLayers(reset);
    },
    setMap: function(map, reset) {
        this.mapInstanceKey = map.mapInstanceKey;
        this.map = map;
        this.updateLayers(reset);
    },
    addListener: function(listener) {
        this.listeners.push(listener);
    },
    initLayers: function(oldLayers, callback) {
        $.ajax({
            url: '../getLayers.safe',
            type: 'post',
            data: {
                mapInstanceKey: this.mapInstanceKey
            },
            success: korem.callback(this, function(data) {
                
                this.map.legend.legendManager.reset();

                for (var key in oldLayers) {
                    if (oldLayers.hasOwnProperty(key)) {
                        var found = false;
                        for (var i = 0; i < data.length; i++) {
                            if(data[i].id==key){
                                found = true;
                            }
                        }
                        if(!found&&!isNaN(key)){
                            this.cleanDeletedLayer(oldLayers[key]);
                        }
                    }
                }

                if (data.length > 0) {
                    this.newLayer(data[0], oldLayers);
                }
                this.geocodingEl = this.newLayer(this.geocodingLayer = {
                    id: 'geocodingLayer',
                    name: 'Search Result',
                    visibility: false,
                    zoomVisibility: true,
                    isGeocoding: true
                }, oldLayers);
                this.hotspotEl = this.newLayer(this.hotspotLayer = {
                    id: 'hotspot',
                    name: 'HotSpot',
                    visibility: false,
                    zoomVisibility: true,
                    isHotspot: true
                }, oldLayers);
                for (var i = 1; i < data.length; ++i) {
                    this.newLayer(data[i], oldLayers);
                }
                this.doCheckAllGroup();
                
                callback && callback();
            })
        });
    },
    cleanDeletedLayer: function(layer){
        am.instance.mapsManager.getMaster(function(mapObj) {
            layer.visibility = false;
            layer.forcedVisibility = false;
            mapObj.legend.update(layer);
        });
    },
    updateLayers: function(reset, callback) {
        this.layers.length = 0;
        var oldLayers = (reset) ? null : this.layersById;
        this.layersById = {};
        if (this.layerController.hasChildNodes()) {
            while (this.layerController.childNodes.length >= 1) {
                this.layerController.removeChild(this.layerController.firstChild);
            }
        }

        korem.timeout(function() {
            this.initLayers(oldLayers, callback);
        }, 0, this);

    },
    setHotspotElVisibility: function(visibility) {
        this.hotspotEl.style.display = (visibility) ? '' : 'none';
        this.hotspotChk.checked = true;
        if (!visibility) {
            this.hotspotLayer.visibility = false;
            this.hotspotLayer.elVisibility = false;
            this.informListeners([this.hotspotLayer]);
        } else {
            this.hotspotLayer.elVisibility = true;
            this.hotspotChk.onclick.apply(this.hotspotChk);
        }
        this.doCheckGroup(this.hotspotChk);
    },
    setGeocodingLayerElVisibility: function (visibility) {
        this.geocodingEl.style.display = (visibility) ? '' : 'none';
        this.geocodingChk.checked = true;
        if (!visibility) {
            this.geocodingLayer.visibility = false;
            this.geocodingLayer.elVisibility = false;
            this.informListeners([this.geocodingLayer]);
        } else {
            this.geocodingLayer.elVisibility = true;
            this.geocodingChk.onclick.apply(this.geocodingChk);
        }
        this.doCheckGroup(this.geocodingChk);
    },
    getGeocodingLayerElVisibility: function() {
        return this.geocodingLayer.visibility;
    },
    setVisibility: function(layer) {
        this.changedLayers.push(layer);
        var allLayers = [];
        var index;
        var i = 0;
        for (index = 0 ; index < this.layers.length ; ++index){
            if (this.layers[index].layer.id !== "hotspot" && this.layers[index].layer.id !== "geocodingLayer"){
                allLayers[i] = this.layers[index].layer;
                i++;
            }
        }
        clearTimeout(this.visibilityTimeout);
        this.visibilityTimeout = korem.timeout(function() {
            this.doSetVisibility(allLayers);
            this.changedLayers = [];
        }, this.VISIBILITY_CHANGE_DELAY, this);
    },
    getLayerVisibilityParameters: function(layers) {
        var data = [];
        for (var i = 0; i < layers.length; i++) {
            var layer = layers[i];
            var labelField = '';
            if (layer.visibility){    
                labelField = layer.selectedLabelField || layer.labelFields[0]
            }
            data.push({
                id: layer.id,
                name: layer.name, 
                visibility: layer.visibility,
                parent: layer.parent,
                labelField: labelField
            });
        }
        
        return data;
    },
    doSetVisibility: function(layers) {
        var layerParams = this.getLayerVisibilityParameters(layers);
        
        $.ajax({
            url: '../setLayerVisibility.safe',
            type: 'post',
            data: {
                mapInstanceKey: this.mapInstanceKey,
                layers: JSON.stringify(layerParams)
            },
            success: korem.callback(this, function() {
                this.informListeners(layers);
            })
        });
    },
    informListeners: function(layers) {
        for (var i = 0; i < this.listeners.length; ++i) {
            this.listeners[i].visibilityUpdated(layers);
        }
        // inform this once everything else is updated
        this.visibilityUpdated();
    },
    doCheckAllGroup: function() {
        var that = this;
        $('#layerController').find(".layergroup").each(function(index) {
            that.doCheckGroup($(this).find(".layergroupmainchk").get(0));
        });
    },
    doCheckGroup: function(chkElement) {
        var that = this;
        var doCheck = true;
        var hotspotOn = typeof that.hotspotLayer !== 'undefined' && that.hotspotLayer !== null ? that.hotspotLayer.elVisibility === true : false;
        var geocodingOn = typeof that.geocodingLayer !== 'undefined' && that.geocodingLayer !== null ? that.geocodingLayer.elVisibility === true : false;
        $(chkElement).parents(".layergroup").find(".layergroupchk").each(function(index) {
            if ((that.hotspotChk !== this || (that.hotspotChk === this && hotspotOn === true)) && this.checked === false) {
                doCheck = false;
            }
            if ((that.geocodingChk !== this || (that.geocodingChk === this && geocodingOn === true)) && this.checked === false){
                doCheck = false;
            }
        });
        $(chkElement).parents(".layergroup").find(".layergroupmainchk").get(0).checked = doCheck;
    },
    updateCheckbox: function(layer, checkbox, zoomCheckbox) {
        var checkboxVisible = (!layer.visibility || layer.zoomVisibility);
        checkbox.style.display = checkboxVisible ? 'block' : 'none';
        zoomCheckbox.style.display = checkboxVisible ? 'none' : 'block';
    },
    zoomChanged: function() {
        var that = this;
        if (!this.changing) {
            this.changing = true;
            $.ajax({
                url: '../getLayersZoomVisibility.safe',
                type: 'post',
                data: {
                    mapInstanceKey: this.mapInstanceKey
                },
                success: korem.callback(this, function(data) {
                    for (var i = 0; i < data.length; ++i) {
                        var layer = data[i];
                        var localLayer = this.layersById[layer.id];
                        if (localLayer) {
                            localLayer.zoomVisibility = layer.zoomVisibility;
                            localLayer.visibilityUpdated();
                        }
                    }
                    that.changing = false;
                    that.visibilityUpdated();
                })
            });
        }
    },
    buildGroup: function(aGroupName) {
        var that = this;
        
        var doClickOnLayers = function(groupCheck) {
            var hotspotOn = that.hotspotLayer.elVisibility === true;
            $(groupCheck).parents(".layergroup").find(".layergroupchk").each(function(index) {
                if (that.hotspotChk !== this || (that.hotspotChk === this && hotspotOn === true)) {
                    this.checked = groupCheck.checked;
                }
            });
            $(groupCheck).parents(".layergroup").find(".layergroupchk").each(function(index) {
                if (that.hotspotChk !== this || (that.hotspotChk === this && hotspotOn === true)) {
                    this.onclick();
                }
            });
        };
        var doOpenGroupList = korem.callback(this, function(arrowDiv) {
            var aList = $(arrowDiv).parents(".layergroup").find(".layergrouplist");
            if (aList.is(":visible")) {
                aList.slideUp(this.GROUP_SLIDE_DELAY, function() {
                    $(arrowDiv).removeClass("layergrouptitlearrowshow");
                    $(arrowDiv).addClass("layergrouptitlearrowhide");
                });
            } else {
                aList.slideDown(this.GROUP_SLIDE_DELAY, function() {
                    $(arrowDiv).removeClass("layergrouptitlearrowhide");
                    $(arrowDiv).addClass("layergrouptitlearrowshow");
                });
            }
        });
        
        return html.div({
            props: {
                className: 'layergroup'
            },
            children: [
                html.div({
                    props: {
                        className: 'layergrouptitle'
                    },
                    children: [
                        html.div({
                            props: {
                                className: 'layergrouptitlechk'
                            },
                            children: [
                                html.input({
                                    props: {
                                        className: 'layergroupmainchk',
                                        type: 'checkbox',
                                        onclick: function() {
                                            doClickOnLayers(this);
                                        }
                                    }
                                })
                            ]
                        }),
                        html.div({
                            props: {
                                className: 'layergrouptitlearrow layergrouptitlearrowshow',
                                innerHTML: '&nbsp;&nbsp;&nbsp;&nbsp;',
                                onclick: function() {
                                    doOpenGroupList(this);
                                }
                            }
                        }),
                        html.div({
                            props: {
                                className: 'layergrouptitlename',
                                innerHTML: aGroupName,
                                onclick: function() {
                                    doOpenGroupList($(this).parents(".layergrouptitle").find('.layergrouptitlearrow'));
                                }
                            }
                        })
                    ]
                }),
                html.div({
                    props: {
                        className: 'layergrouplist'
                    },
                    children: [
                        html.table({
                            props: {
                                className: 'layergrouplistchk'
                            }
                        })
                    ]
                })
            ]
        });
    },
    newLayer: function(layer, oldLayers) {
        // keeps a reference to the layer in legendmanager
        if (layer.name == am.Legend.prototype.TA_LAYER_NAME) {
            this.map.legend.legendManager.taLayer = layer;
        }
        
        if (layer.name == am.Legend.prototype.NW_LAYER_NAME) {
            this.map.legend.legendManager.nwLayer = layer;
        }
        
        if (layer.labelDisplays && layer.labelDisplays.length){
            var layerLabelSelect = html.select({className: 'labelNameSelect'});
            for(var ii=0; ii<layer.labelDisplays.length; ii++){
                layerLabelSelect.options.add(new Option(layer.labelDisplays[ii], layer.labelFields[ii]));
            }

            layerLabelSelect.onchange = korem.callback(this,function (evt) {
                layer.selectedLabelField = layer.labelFields[evt.target.selectedIndex]
                that.setVisibility(layer);
            });
          
        }
        
        var that = this;
        var checkbox, zoomCheckbox;
        this.layersById[layer.id] = layer;

        var oldLayer = (oldLayers) ? oldLayers[layer.id] : null;
        if (oldLayer) {
            korem.apply(layer, oldLayer);
        }else if(layer.isTheme&&layer.visibility){
            //special case when creating a theme and the theme is visible at start... amap was not used to that...
            am.instance.mapsManager.getMaster(function(mapObj) {
                mapObj.legend.update(layer);
            });
        }

        layer.visibilityUpdated = function() {
            that.updateCheckbox(layer, checkbox, zoomCheckbox);
        };
        var onchangeFnc;
        if (layer.isHotspot || layer.isGeocoding) {
            onchangeFnc = function () {
                layer.visibility = this.checked;
                that.informListeners([layer]);
                that.doCheckGroup(this);
            }
        } else {
            onchangeFnc = function () {
                layer.visibility = this.checked;
                that.updateCheckbox(layer, checkbox, zoomCheckbox);
                that.setVisibility(layer);
                that.doCheckGroup(this);
            }
        }
        /*  var onchangeFnc = (layer.isHotspot) ? function() {
            layer.visibility = this.checked;
            that.informListeners([layer]);
            that.doCheckGroup(this);
        } : function() {
            layer.visibility = this.checked;
            that.updateCheckbox(layer, checkbox, zoomCheckbox);
            that.setVisibility(layer);
            that.doCheckGroup(this);
        };*/
        checkbox = html.input({
            props: {
                type: 'checkbox',
                className: 'layergroupchk',
                //need to apply checked once it is in the dom for IE*
                //checked: layer.visibility,
                onclick: onchangeFnc
            }
        });
        if (layer.isHotspot) {
            this.hotspotChk = checkbox;
        }
        if (layer.isGeocoding){
            this.geocodingChk = checkbox;
        }
        zoomCheckbox = html.img({
            props: {
                className: 'zoomCheckbox',
                src: '../main/images/checkbox.PNG',
                onclick: function() {
                    checkbox.checked = false;
                    onchangeFnc.apply(checkbox);
                }
            }
        });
        this.layers.push({
            layer: layer,
            checkbox: checkbox,
            initialVisibility: layer.visibility
        });

        var groupDiv = null;
        var isDynamic = typeof layer.groupName === 'undefined' || layer.groupName === null;
        $(this.layerController).find(".layergroup").each(function(index) {
            var someGroupName = $(this).find(".layergrouptitlename").html();
            if (someGroupName === layer.groupName || (isDynamic === true && someGroupName === am.locale.layerGroup.dynamicLabel)) {
                groupDiv = this;
            }
        });
        if (groupDiv == null) {
            groupDiv = this.buildGroup(isDynamic ? am.locale.layerGroup.dynamicLabel :layer.groupName);
            this.layerController.appendChild(groupDiv);
        }
        var el = html.tr({
            style: {
                display: ((layer.isHotspot && !layer.elVisibility) || (layer.isGeocoding && !layer.elVisibility)) ? 'none' : ''
            },
            props: {
                className: 'layerNameLine'
            },
            children: [
                html.td({
                    props: {
                        innerHTML: this.removeUnderscore(layer.name),
                        className: 'layerNameElement'
                    }, 
                    children: (layerLabelSelect)? [
                        
                        html.div({props: {className: 'labelNameSelect'},children: [layerLabelSelect]}) ] : []
                }),
//                html.td({
//                    props: {
//                        innerHTML: layerLabelSelect
//                    }
//                }),
                html.td({
                    props: {
                        className: 'layerCheckElement'
                    },
                    children: [
                        checkbox,
                        zoomCheckbox
                    ]
                })
            ]
        });
        $(groupDiv).find(".layergrouplistchk tbody").get(0).appendChild(el);
        this.updateCheckbox(layer, checkbox, zoomCheckbox);
        $(checkbox).attr('checked', layer.visibility);
        return el;
    },
    visibilityUpdated: function() {
        this.map.legend.postUpdate();
    },
    removeUnderscore: function(value) {
        return value.replace(/_/g, ' ').replace('Ranges by', '').replace('Individual values with', '');
    }
};
