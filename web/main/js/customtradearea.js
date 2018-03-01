/**
 * This file is related to Custom Trade Area selection Tool.
 * 
 * @author Charles St-Hilaire for Korem inc.
 * 
 * @param {type} options
 * @returns {CustomTradeArea}
 */
am.CustomTradeArea = function(options) {
    korem.apply(this, options);
    this.init();
};
am.CustomTradeArea.prototype = {
    customTATool: null,
    mapInstanceKey: null,
    map: null,
    vectorLayer: null,
    drawFeatureControl: null,
    polygon: null,
    currentPolygon: null,
    MERCATOR: "EPSG:900913",
    WGS84: "EPSG:4326",
    
    /**
     * This method initialize this pseudo Javascript object
     * @returns {undefined}
     */
    init: function() {
        var self = this;
        self.polygon = null;
        $("input#tradeAreaChkCustom").click(function(){ self.clickCustomTA(); });
        $("input#tradeAreaBtnCustomDraw").click(function(){ self.customTATool.activate(); });
        $("input#tradeAreaBtnCustomClear").click(function(){ self.clickClear(); });
        
        self.createLayer();
        self.createCustomTATool().init();
    },
    
    /**
     * This method is about click on a Custom TA Checkbox
     * @returns {undefined}
     */
    clickCustomTA: function(){
        if ($("input#tradeAreaChkCustom").is(":checked")){
            this.customTATool.activate();
            $("input#tradeAreaBtnCustomDraw").attr("disabled",false);
            $("input#reportChkDistanceDecay").attr("checked",false);
            $("input#reportChkDistanceDecay").attr("disabled",true);
            this.setClearDisabled(false);
        }else{
            $("input#tradeAreaBtnCustomDraw").attr("disabled",true);
            this.setClearDisabled(true);
            $("input#reportChkDistanceDecay").attr("disabled",false);
            this.drawFeatureControl.deactivate();
            this.currentPolygon = null;
            this.polygon = null;
            $("td#tradeAreaCustomMsg").html("");
        }
    },
    
    /**
     * This method is about ToolBase usage.
     * @returns {unresolved}
     */
    createCustomTATool: function() {
        var self = this;
        this.customTATool = korem.apply(korem.apply({}, am.ToolBase.prototype), {
            btnId: 'tradeAreaChkCustom',
            groupId: 'tools',
            initialize: function() {},
            doActivate: function() { window.setTimeout(function(){
                self.clickDraw();
            }, 1);},
            doDeactivate: function() { window.setTimeout(function(){
                self.drawFeatureControl.deactivate();
                $("td#tradeAreaCustomMsg").html("");
            }, 1); },
            initButton: function(){ this.button = $("<div></div>"); }
        });
        return this.customTATool;
    },
    
    /**
     * When the map is toggled (duplicate or single) it call this method to refresh
     * the map.
     * @param {type} map
     * @param {type} mapInstanceKey
     * @returns {undefined}
     */
    setMap: function(map, mapInstanceKey) {
        this.vectorLayer.removeAllFeatures();
        if (this.drawFeatureControl){
            this.map.removeControl(this.drawFeatureControl);
        }
        if (this.vectorLayer){
            this.map.removeLayer(this.vectorLayer);
        }
        
        this.map = map;
        this.mapInstanceKey = mapInstanceKey;
        
        this.createLayer();
        this.customTATool.deactivate();
    },
    
    /**
     * This function is to create the OpenLayers Vector Layer
     * that will allow drawing.
     * @returns {undefined}
     */
    createLayer: function(){
        var self = this;
        self.vectorLayer = new OpenLayers.Layer.Vector('Vectors', { displayInLayerSwitcher: false });
        self.map.addLayer(self.vectorLayer);
        self.drawFeatureControl = new OpenLayers.Control.DrawFeature(self.vectorLayer, OpenLayers.Handler.Polygon);
        self.vectorLayer.events.register("featureadded", ' ' , function(polygon) {
            self.polygon = polygon;
            self.drawFeatureControl.deactivate();
            $("td#tradeAreaCustomMsg").html("");
            am.instance.tradeArea.changed();
            self.setClearDisabled(false);
        });
        this.map.addControl(this.drawFeatureControl);
    },
    
    /**
     * Behavior to perform on Draw button click
     * @returns {undefined}
     */
    clickDraw: function(){
        this.polygon = null;
        this.vectorLayer.removeAllFeatures();
        this.drawFeatureControl.activate();
        $("td#tradeAreaCustomMsg").html(am.locale.menu.taCustomMsg);
        this.setClearDisabled(false);
    },
    
    /**
     * Behavior to perform on Clear button click.
     * It remove the layer and empty the saved polgon.
     * @returns {undefined}
     */
    clickClear: function(){
        this.polygon = null;
        this.vectorLayer.removeAllFeatures();
        this.drawFeatureControl.deactivate();
        $("td#tradeAreaCustomMsg").html("");
        this.setClearDisabled(true);
        
    },
    
    setClearDisabled: function(disabled) {
        $("input#tradeAreaBtnCustomClear").attr("disabled",disabled);
    },
    
    /**
     * Simple method to clear all feature of vector layer.
     * @returns {undefined}
     */
    clearLayer: function(){
        this.vectorLayer.removeAllFeatures();
    },
    
    /**
     * This method return an array of WGS84 lon,lat pair coordinate of
     * Custom Trade Area edited polygon.
     * 
     * @returns {String}
     */
    getJSONPolygon: function(){
        var jsonPolygon = [];
        if (this.currentPolygon !== null){
            var geometry = this.currentPolygon.feature.geometry.components[0].components;
            var mercator  = new OpenLayers.Projection(this.MERCATOR);
            var wgs1984 = new OpenLayers.Projection(this.WGS84);
            var coord = null;
            for (var point in geometry){
                coord = new OpenLayers.LonLat(geometry[point].x, geometry[point].y).transform(mercator, wgs1984);
                jsonPolygon.push(coord.lon+","+coord.lat);
            }
        }
        return JSON.stringify(jsonPolygon);
    },
    applyFinished: function() {
        this.setClearDisabled(true);
    }
};