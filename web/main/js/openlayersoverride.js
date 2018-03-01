am.OpenLayersOverrides = {
    apply: function() {
        OpenLayers.Control.LayerSwitcher.prototype.roundedCornerColor = '#0072cf';
        OpenLayers.Control.LayerSwitcher.prototype.roundedCorner = true;
        OpenLayers.Control.LayerSwitcher.prototype.loadContents = function() {

            // layers list div
            this.layersDiv = document.createElement("div");
            this.layersDiv.id = this.id + "_layersDiv";
            OpenLayers.Element.addClass(this.layersDiv, "layersDiv");

            this.baseLbl = document.createElement("div");
            this.baseLbl.innerHTML = OpenLayers.i18n("Base Layer");
            OpenLayers.Element.addClass(this.baseLbl, "baseLbl");

            this.baseLayersDiv = document.createElement("div");
            this.baseLayersDiv.id = "baseLayers";
            OpenLayers.Element.addClass(this.baseLayersDiv, "baseLayersDiv");

            this.dataLbl = document.createElement("div");
            this.dataLbl.innerHTML = "<hr>"
            OpenLayers.Element.addClass(this.dataLbl, "dataLbl");

            this.dataLayersDiv = document.createElement("div");
            OpenLayers.Element.addClass(this.dataLayersDiv, "dataLayersDiv");

            if (this.ascending) {
                this.layersDiv.appendChild(this.baseLbl);
                this.layersDiv.appendChild(this.baseLayersDiv);
                this.layersDiv.appendChild(this.dataLbl);
                this.layersDiv.appendChild(this.dataLayersDiv);
            } else {
                this.layersDiv.appendChild(this.dataLbl);
                this.layersDiv.appendChild(this.dataLayersDiv);
                this.layersDiv.appendChild(this.baseLbl);
                this.layersDiv.appendChild(this.baseLayersDiv);
            }

            this.div.appendChild(this.layersDiv);

            if(this.roundedCorner) {
                OpenLayers.Rico.Corner.round(this.div, {
                    corners: "tl bl",
                    bgColor: "transparent",
                    color: this.roundedCornerColor,
                    blend: false
                });
                OpenLayers.Rico.Corner.changeOpacity(this.layersDiv, 0.75);
            }

            // maximize button div
            var img = OpenLayers.Util.getImageLocation('layer-switcher-maximize.png');
            this.maximizeDiv = OpenLayers.Util.createAlphaImageDiv(
                                    "OpenLayers_Control_MaximizeDiv", 
                                    null, 

                                    null, 
                                    img, 
                                    "absolute");
            OpenLayers.Element.addClass(this.maximizeDiv, "maximizeDiv olButton");
            this.maximizeDiv.style.display = "none";

            this.div.appendChild(this.maximizeDiv);

            // minimize button div
            var img = OpenLayers.Util.getImageLocation('layer-switcher-minimize.png');
            this.minimizeDiv = OpenLayers.Util.createAlphaImageDiv(
                                    "OpenLayers_Control_MinimizeDiv", 
                                    null, 

                                    null, 
                                    img, 
                                    "absolute");
            OpenLayers.Element.addClass(this.minimizeDiv, "minimizeDiv olButton");
            this.minimizeDiv.style.display = "none";

            this.div.appendChild(this.minimizeDiv);
        }

        OpenLayers.Control.PanZoomBar.prototype.zoomStopHeight = 4;
        OpenLayers.Control.PanZoomBar.prototype.zoomBarUp = function(evt) {
            if (!OpenLayers.Event.isLeftClick(evt)) {
                return;
            }
            if (this.mouseDragStart) {
                this.div.style.cursor="";
                this.map.events.un({
                    "mouseup": this.passEventToSlider,
                    "mousemove": this.passEventToSlider,
                    scope: this
                });
                var deltaY = this.zoomStart.y - evt.xy.y;
                var zoomLevel = this.map.zoom;
                if (!this.forceFixedZoomLevel && this.map.fractionalZoom) {
                    zoomLevel += deltaY/this.zoomStopHeight;
                    zoomLevel = Math.min(Math.max(zoomLevel, 0),
                        this.map.getNumZoomLevels() - 1);
                } else {
                    zoomLevel += Math.round(deltaY/this.zoomStopHeight);
                }
                zoomLevel = Math.max(0, Math.min(zoomLevel, this.map.getNumZoomLevels() - 1));
                this.map.zoomTo(zoomLevel);
                this.mouseDragStart = null;
                this.zoomStart = null;
                OpenLayers.Event.stop(evt);
            }
        }
        OpenLayers.Control.PanZoomBar.prototype.draw = function() {
            this.buttons = [];
            this.slideFactor = 250;

            this.slider = html.div({
                props: {
                    className: 'zoomBarSlider'
                }
            });
            this.zoombarDiv = html.div({
                props: {
                    className: 'zoomBarBar'
                },
                style: {
                    height: '75px'
                }
            });
            if (!this.div) {
                this.div = html.div({
                    props: {
                        className: 'panZoomBarContainer'
                    },
                    children: [
                    html.leanTable({
                        children: [
                        html.tr({
                            children: [
                            html.td({
                                props: {
                                    className: 'compassEl compassNW',
                                    onclick: function(e) {
                                        that.map.pan(-that.slideFactor, -that.slideFactor);
                                        if (e == null) {e=window.event}
                                        OpenLayers.Event.stop(e);
                                    }
                                }
                            }),
                            html.td({
                                props: {
                                    className: 'compassCenterEl compassN',
                                    onclick: function(e) {
                                        that.map.pan(0, -that.slideFactor);
                                        if (e == null) {e=window.event}
                                        OpenLayers.Event.stop(e);
                                    }
                                }
                            }),
                            html.td({
                                props: {
                                    className: 'compassEl compassNE',
                                    onclick: function(e) {
                                        that.map.pan(that.slideFactor, -that.slideFactor);
                                        if (e == null) {e=window.event}
                                        OpenLayers.Event.stop(e);
                                    }
                                }
                            })
                            ]
                        }),
                        html.tr({
                            children: [
                            html.td({
                                props: {
                                    className: 'compassMiddleEl compassW',
                                    onclick: function(e) {
                                        that.map.pan(-that.slideFactor, 0);
                                        if (e == null) {e=window.event}
                                        OpenLayers.Event.stop(e);
                                    }
                                }
                            }),
                            html.td({
                                props: {
                                    className: 'compassCenterEl compassMiddleEl compassC',
                                    onclick: function(e) {
                                        that.map.restoreSavedPosition();
                                        if (e == null) {e=window.event}
                                        OpenLayers.Event.stop(e);
                                    }
                                }
                            }),
                            html.td({
                                props: {
                                    className: 'compassMiddleEl compassE',
                                    onclick: function(e) {
                                        that.map.pan(that.slideFactor, 0);
                                        if (e == null) {e=window.event}
                                        OpenLayers.Event.stop(e);
                                    }
                                }
                            })
                            ]
                        }),
                        html.tr({
                            children: [
                            html.td({
                                props: {
                                    className: 'compassEl compassSW',
                                    onclick: function(e) {
                                        that.map.pan(-that.slideFactor, that.slideFactor);
                                        if (e == null) {e=window.event}
                                        OpenLayers.Event.stop(e);
                                    }
                                }
                            }),
                            html.td({
                                props: {
                                    className: 'compassCenterEl compassS',
                                    onclick: function(e) {
                                        that.map.pan(0, that.slideFactor);
                                        if (e == null) {e=window.event}
                                        OpenLayers.Event.stop(e);
                                    }
                                }
                            }),
                            html.td({
                                props: {
                                    className: 'compassEl compassSE',
                                    onclick: function(e) {
                                        that.map.pan(that.slideFactor, that.slideFactor);
                                        if (e == null) {e=window.event}
                                        OpenLayers.Event.stop(e);
                                    }
                                }
                            })
                            ]
                        })
                        ]
                    }),
                    html.div({
                        props: {
                            className: 'zoomBar'
                        },
                        children: [
                        this.slider,
                        html.div({
                            props: {
                                className: 'zoomBarIn',
                                onclick: function(e) {
                                    that.map.zoomIn();
                                    if (e == null) {e=window.event}
                                    OpenLayers.Event.stop(e);
                                }
                            }
                        }),
                        this.zoombarDiv,
                        html.div({
                            props: {
                                className: 'zoomBarOut',
                                onclick: function(e) {
                                    that.map.zoomOut();
                                    if (e == null) {e=window.event}
                                    OpenLayers.Event.stop(e);
                                }
                            }
                        })
                        ]
                    })
                    ]
                });
            }
            var that = this;
            this.sliderEvents = new OpenLayers.Events(this, this.slider, null, true,
            {
                includeXY: true
            });
            this.sliderEvents.on({
                "mousedown": this.zoomBarDown,
                "mousemove": this.zoomBarDrag,
                "mouseup": this.zoomBarUp,
                "dblclick": this.doubleClick,
                "click": this.doubleClick
            });

            this.divEvents = new OpenLayers.Events(this, this.zoombarDiv, null, true,
            {
                includeXY: true
            });
            this.divEvents.on({
                "mousedown": this.divClick,
                "mousemove": this.passEventToSlider,
                "dblclick": this.doubleClick,
                "click": this.doubleClick
            });


            this.zoomStopHeight = 75 / this.map.getNumZoomLevels();
            this.startTop = parseInt(this.zoombarDiv.offsetTop + 20);
            this.moveZoomBar();
            this.map.events.register("zoomend", this, this.moveZoomBar);

            this.div.appendChild(this.zoombarDiv);
            this.div.appendChild(this.slider);
                
            return this.div;
        }

        OpenLayers.Map.prototype.savePosition = function() {
            this.savedCenter = this.getCenter();
            this.savedZoom = this.getZoom();
        }
        OpenLayers.Map.prototype.restoreSavedPosition = function() {
            this.setCenter(this.savedCenter, this.savedZoom);
        }
    }

}