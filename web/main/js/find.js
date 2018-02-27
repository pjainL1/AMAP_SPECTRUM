am.Find = function (options) {
    korem.apply(this, options);
    this.init();
}

am.Find.prototype = {
    FSA_INDEX: 2,
    map: null,
    onGeocodeSelect: null,
    onLocationFind: null,
    onFSAFind: null,
    mapsManager: null,
    layerControl: null,
    geocoder: null,
    findInput: null,
    currentViewport: null,
    findField: null,
    findFieldFSA: null,
    currentFindFnc: null,
    geocodingPin: null,
    geocodeBounds: null,
    changed: false,
    MERCATOR: "EPSG:900913",
    WGS84: "EPSG:4326",
    init: function () {
        this.geocodeBounds = new google.maps.LatLngBounds(new google.maps.LatLng(41.459191940254, -146.85424265404),
                new google.maps.LatLng(68.015796246337, -51.053461407864));
        this.geocoder = new google.maps.Geocoder();
        this.handleFindInput();
        this.handleGoButton();
        this.handleDescBtn();
        this.initializeLocations();
    },
    visibilityUpdated: function (layers) {
    },
    setMap: function(map) {
        this.map = map;
    },
    handleDescBtn: function () {
        var window = $('#findDescWindow').dialog({
            zIndex: 7000,
            autoOpen: false,
            resizable: false,
            width: 450,
            close: function () {
            },
            open: function () {
            }
        });
        $('#findDescBtn').click(function () {
            window.dialog('open');
        });
    },
    handleGoButton: function () {
        var that = this;
        korem.get('findBtn').onclick = function () {
            that.onEnter();
        };
    },
    onEnter: function () {
        this.changed = true;
        var searchVal = $.trim(this.findInput[0].value.toLowerCase());
        if (this.customerLocationCodes[searchVal] != undefined ){
            this.onLocationFind(this.customerLocationCodes[searchVal]);
        }else if (this.sponsorLocationCodes[searchVal] != undefined){
            this.onLocationFind(this.sponsorLocationCodes[searchVal]);
        }else {
            this.geocode(this.findInput[0].value);
        }
    },
    handleFindInput: function () {
        var that = this;
        var cache = {};
        this.findInput = $('#findInput');
        this.findInput.autocomplete({
            minLength: 2,
            source: function (request, response) {
                that.currentViewport = null;
                var term = request.term;
                if (term in cache) {
                    response(cache[term]);
                    return;
                }
                if (that.customerLocationCodes[term] == undefined && that.sponsorLocationCodes[term] == undefined) {
                    // location not found: do google geocode for autocomplete.
                    that.doGeocode(term, function (results) {
                        response(cache[term] = results);
                    });
                } else {
                    // location found: clear previous autocomplete results.
                    response(null);
                }
                
            },
            select: function (event, ui) {
                if (ui.item) {
                    that.changed = true;
                    that.onGeocodeSelect(that.currentViewport = ui.item.viewport);
                }
            }
        });
    },
    find: function (term, url, callback) {
        $.ajax({
            url: url,
            context: this,
            type: 'post',
            data: {
                term: term
            },
            success: function (data) {
                callback.apply(this, [data]);
            }
        });
    },
    noResult: function () {
        am.AlertDialog.show(am.locale.find.noResult);
    },    
    findFSA: function (term) {
        this.find(term, '../findFSA.safe', function (data) {
            if (data != null) {
                this.onFSAFind(data);
            } else {
                this.noResult();
            }
        });
    },
    geocode: function (term) {
        if (this.currentViewport) {
            this.onGeocodeSelect(this.currentViewport);
        } else {
            this.doGeocode(term, function (results) {
                if (results.length == 0) {
                    this.noResult();
                    return;
                }
                var result = results[0];
                this.findInput[0].value = result.value;
                this.onGeocodeSelect(this.currentViewport = result.viewport);
            });
        }
    },
    doGeocode: function (term, callback) {
        var that = this;
        this.geocoder.geocode({
            address: term,
            language: am.locale.language,
            bounds: this.geocodeBounds
        }, function (results, status) {
            if (status == google.maps.GeocoderStatus.OK) {
                that.lastAdress = results[0];
                callback.apply(that, [that.parseGeocodeResult(results)]);
            } else {
                callback.apply(that, [[]]);
            }
        });
    },
    resetMap: function(map) {
        if(map.geocodingPin){
            map.geocodingLayer.removeMarker(map.geocodingPin);
        }
    },
    createMarker: function (result, map) {
        var size = new OpenLayers.Size(28, 39);
        var offset = new OpenLayers.Pixel(-(size.w / 2), -size.h);
        var icon = new OpenLayers.Icon('../main/images/pin.png', size, offset);

        var mercator = new OpenLayers.Projection(this.MERCATOR);
        var wgs1984 = new OpenLayers.Projection(this.WGS84);

        var coord = new OpenLayers.LonLat(result.geometry.location.lng(), result.geometry.location.lat());
        coord.transform(wgs1984, mercator);

        if(map.geocodingPin){
            map.geocodingLayer.removeMarker(map.geocodingPin);
        }
        map.geocodingPin = new OpenLayers.Marker(coord, icon);
        map.geocodingLayer.addMarker(map.geocodingPin);
    },
    parseGeocodeResult: function (results) {
        var autoCompleteResult = [];
        for (var i = 0; i < results.length; ++i) {
            var result = results[i];
            autoCompleteResult.push({
                value: result.formatted_address,
                viewport: result.geometry.viewport
            });
        }
        return autoCompleteResult;
    },
    isCanadian: function (result) {
        for (var i = 0; i < result.address_components.length; ++i) {
            var component = result.address_components[i];
            for (var j = 0; j < component.types.length; ++j) {
                if (component.types[j] == 'country' && component.short_name == 'CA') {
                    return true;
                }
            }
        }
        return false;
    },
    applyFinished: function () {
        this.changed = false;
        this.layerControl.setGeocodingLayerElVisibility(true);
    },
    createMarkerFromLastAdress: function () {
        if (this.changed) {
            this.createMarker(this.lastAdress, this.map);
        }
    },
    
    initializeLocations: function (){         
        $.ajax({
            url: '../GetCustomerCodes.safe',
            context: this,
            type: 'post',
            data: {
                
            },
            success: function (data) {
                this.customerLocationCodes = {};
                this.sponsorLocationCodes = {};
                for(var i = 0; i < data.length; i++){
                    this.customerLocationCodes[$.trim(data[i].CUSTOMER_LOCATION_CODE.toLowerCase())] = {y : data[i].LATITUDE, x: data[i].LONGITUDE};
                    this.sponsorLocationCodes[$.trim(data[i].SPONSOR_LOCATION_CODE.toLowerCase())] = {y : data[i].LATITUDE, x: data[i].LONGITUDE};
                }
            }
        });
    }
};