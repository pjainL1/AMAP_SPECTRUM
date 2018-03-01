am.StreetViewControl = function(options) {
    korem.apply(this, options);
    this.init();
};

am.StreetViewControl.prototype = {
    pegmanButton: null,
   
    init: function() {
        var that = this;
        this.pegmanButton = $('#pegmanButton');
        this.initModal();
        that.clickPerformed = false;
        that.dragPerforming = false;
        that.dragcpt = 0;  
        this.setMap(this.map);
        this.pegmanButton.bind("click", function(event, ui) {
            
        });
        that.v_containment = [  $('#dashboard').width(), 
                                parseInt($('#'+am.instance.mapsManager.masterContainer.id).css("padding-top")),
                                $('#dashboard').width()+$('#'+am.instance.mapsManager.masterContainer.id).width() -  parseInt($('#'+am.instance.mapsManager.masterContainer.id).css("padding-right")) - 15,
                                $('#'+am.instance.mapsManager.masterContainer.id).height() -parseInt($('#'+am.instance.mapsManager.masterContainer.id).css("padding-bottom")) - 15];
        that.draggableHandler = that.getDraggable();


        this.pegmanButton.bind("mousedown", function(event, ui) {
            that.clickPerformed = false;
            that.dragcpt = 0;
            //that.getDraggable();
        });

        this.pegmanButton.bind("mouseup", function(event, ui) {
            that.clickPerformed = true;                      
        });
            
        this.pegmanButton.bind("dragstop", function(event, ui) {
                var left = Math.floor(ui.position.left + 5);
                var top = Math.floor(ui.position.top + 25);
                that.onDragStop(left, top);
            
        });

        this.pegmanButton.bind("dragstart", function(event, ui) {              
                that.onDragStart(true);
        });        
    }, 
    recalculateDraggableContainment: function(){
        var xPosition = ($('#dashboard').css('display') == "none")? 15: $('#dashboard').width()
        var bottomRightCoordunates = $('#masterLegend').position()
        this.v_containment = [  xPosition, 
                                parseInt($('#'+am.instance.mapsManager.masterContainer.id).css("padding-top")),
                                parseInt($('#'+am.instance.mapsManager.masterContainer.id).width() + (($('#dashboard').css('display') == "none")? 15: $('#dashboard').width())),
                                parseInt($('#'+am.instance.mapsManager.masterContainer.id).height())
                            ];  
        this.draggableHandler = this.getDraggable();
        
    },
    initModal: function(){
        var that = this;
        this.streetViewModal = jQuery('#streetViewModal');
       
        $('#streetViewContent').height($('#'+am.instance.mapsManager.masterContainer.id).height()*0.66);     
      
    },
    setMap: function(map) {
        this.map = map;
    },
    onReleaseMouse: function(){
        
    },
    onDragStart: function(removeDude) {
        var street = new google.maps.ImageMapType({
            getTileUrl: function(coord, zoom) {
                var X = coord.x % (1 << zoom);  // wrap
                return "http://mts1.google.com/mapslt?z=" + zoom + "&x=" + X + "&y=" + coord.y + "&s=Ga&lyrs=svv&w=256&h=256&gl=fr&hl=en&style=40,18";
            },
            tileSize: new google.maps.Size(256, 256),
            isPng: true
        });
        //this.hide();
        this.overlayStreetViewTag = this.map.baseLayer.mapObject.overlayMapTypes.push(street);
    },
    onDragStop: function(left, top) {
        var leftTop = this.fixPixelsOffest(left, top);
        left = leftTop.left;
        top  = leftTop.top;
        var pixel = new OpenLayers.Pixel(left, top);
        var lonlat = this.map.getLonLatFromPixel(pixel);
        this.pegmanDragStopPosition = lonlat;
        var params = {};
        params.srs1 = this.map.projection;
        params.srs2 = "epsg:4326";
        params.x = lonlat.lon;
        params.y = lonlat.lat;
        params.m = "convertCoord";
        var that = this;
        this.map.baseLayer.mapObject.overlayMapTypes.removeAt(this.overlayStreetViewTag - 1);  
        this.showPegman();
        this.showDialog();
        var coords = this.convertPixels(left, top);
        this.getStreetViewService(coords);
        
    },
    getDraggable : function () {
        var that = this;
        return this.pegmanButton.draggable({
            cursorAt: { top: 25, left: 5 },
            appendTo: '#'+am.instance.mapsManager.masterContainer.id, containment: that.v_containment, 
            drag: function(event, ui) {
                am.instance.streetViewControl.pegmanButton.css({top: ui.position.top});
                am.instance.streetViewControl.pegmanButton.css({left: ui.position.left});
            }
        });  
    },

    hidePegman: function() {
        $('#pegmanButton').css('display', 'none');
    },
    showPegman: function() {
        $('#pegmanButton').css('display', 'block');
    },
    
    hide: function() {
        $('#streetViewControlContainer').css('display', 'none');
    },
    show: function() {
        $('#streetViewControlContainer').css('display', 'block');
    },
    
    
    replacePegman: function(){
        am.instance.streetViewControl.pegmanButton.css({top: 0});
        am.instance.streetViewControl.pegmanButton.css({left: 0});
    },
    showDialog: function() {
        $('#streetViewContent').height($('#'+am.instance.mapsManager.masterContainer.id).height()*0.66); 
        this.streetViewModal.show(); 
        var that = this;
        this.streetViewModal.dialog({ 
                  width: $('#'+am.instance.mapsManager.masterContainer.id).width()*0.8,
                  height: $('#'+am.instance.mapsManager.masterContainer.id).height()*0.7,
                  modal: true,
                  position: { my: "center", at: "center", of: '#'+am.instance.mapsManager.masterContainer.id },
                  resizable: false,
                  autoOpen:false,
                  title:'Street View',
                  close : function(){
                      return function(){
                          that.closeDialog();
                      }
                  }()
        }); 
      
        this.streetViewModal.dialog("open");
        this.hidePegman();
        
    },
    repositionDialog: function(){
       $('#streetViewModal').css({left: this.geMapDivCenter().x});
       $('#streetViewModal').css({top : this.geMapDivCenter().y});
    },
    //function to close dialog, probably called by a button in the dialog
    closeDialog: function() {
        this.active = false;
        this.showPegman();
        this.replacePegman();
        return;
    },
    convertPixels: function(px, py){
        var xy = this.map.getLonLatFromPixel({x:px, y:py});
        var coord = xy.transform(this.map.projection, this.map.displayProjection);
        return coord;
    },
    getStreetViewService: function(coord){
        if (this.sv == null) {
            this.sv = new google.maps.StreetViewService();
        }
        var that = this;        
        that.getModalMap(coord.lat, coord.lon);

        that.active = true;
    }, 
    getModalMap: function(lat, lon){
        var mapOptions = {
            streetViewControl: false,
            mapTypeControl: false,
            mapTypeId: google.maps.MapTypeId.ROADMAP
        }

        this.modalMap = new google.maps.Map(document.getElementById('streetViewContent'), mapOptions); 
        var svCoverageLayer = new google.maps.StreetViewCoverageLayer();
        svCoverageLayer.setMap(this.modalMap);

        this.modalMap.setCenter(new google.maps.LatLng(lat, lon));
        this.modalMap.setZoom(this.map.getZoom());
        
        this.initMiniMap();
    },
    
    showPanorama: function(lat, lng){
        var panoramaOptions = {
                      position: new google.maps.LatLng(lat, lng),
                      pov: {
                        heading: 34,
                        pitch: 10,
                        zoom: 1
                      },
                      visible : true
                    };
        new google.maps.StreetViewPanorama(document.getElementById('streetViewContent'),panoramaOptions);        
    },
    
    initMiniMap: function(){
        this.extStreetViewControl = new ExtStreetviewControl({
            mainContent : "MAP",
            size:new google.maps.Size(200, 125),
            controlStatus : "MINI",
            map: this.modalMap,
            streetViewControl: false,
            service: this.sv
        });

        this.extStreetViewControl.setLocationAndPOV(this.modalMap.getCenter());  

    },
    setPegmanPositionFromLatLng: function(latLng) {
        var map = this.map;
        map.pegmanLngLat = new OpenLayers.LonLat(latLng.lng(),latLng.lat());
        map.pegmanLngLat = map.pegmanLngLat.transform(map.displayProjection, map.projection);
        var pixel = map.getPixelFromLonLat(map.pegmanLngLat);
        this.setPegmanPosition(pixel.x, pixel.y,false);

        if (!map.getExtent().containsLonLat(map.pegmanLngLat)) {
            map.setCenter(map.pegmanLngLat, map.getZoom());
        }
    }, 
    setPegmanPosition: function(x,y,setLngLat) {
        if(!this.active){
            return;
        }

        if (setLngLat) {
            if (x<0 || y<0) {
                this.map.pegmanLngLat = null;
            } else {
                this.map.pegmanLngLat = this.map.getLonLatFromPixel(new OpenLayers.Pixel(x,y));
            }
        }
    },    
    fixPixelsOffest: function(left, top){
        return {left:left+$('#streetViewControlContainer').position().left, top:top+$('#streetViewControlContainer').position().top};
    }, 
    geMapDivCenter: function(){
        var firstMapContainer = $('#firstMapContainer');
        var offset = firstMapContainer.offset();
        var width = firstMapContainer.width();
        var height = firstMapContainer.height();

        var centerX = offset.left + width / 2;
        var centerY = offset.top + height / 2;        
        return {x:centerX, y: centerY};
    }
}