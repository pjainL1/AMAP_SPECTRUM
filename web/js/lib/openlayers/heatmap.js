/**
 * @requires OpenLayers/Layer/WMS.js
 */

/**
 * Class: OpenLayers.Layer.KMS
 * Instances of OpenLayers.Layer.KMS are used to display data from KMS.
 * Create a new KMS layer with the <OpenLayers.Layer.KMS> constructor.
 *
 * Inherits from:
 *  - <OpenLayers.Layer.WMS>
 */
OpenLayers.Layer.HeatMap = OpenLayers.Class(OpenLayers.Layer.KMS, {
    initialize: function(name, url, params, options) {
        OpenLayers.Layer.KMS.prototype.initialize.apply(this, [name, url, params, options]);
    },
    /**
     * Method: getURL
     * Return a GetMap query string for this layer
     *
     * Parameters:
     * bounds - {<OpenLayers.Bounds>} A bounds representing the bbox for the
     *                                request.
     *
     * Returns:
     * {String} A string with the layer's url and parameters and also the
     *          passed-in bounds and appropriate tile size specified as
     *          parameters.
     */
    getURL: function(bounds) {
        bounds = this.adjustBounds(bounds);
        bounds = this.map.getExtent().transform(this.map.projection, this.map.displayProjection);
        var imageSize = this.getImageSize();
        var newParams = {};
        // WMS 1.3 introduced axis order
        var reverseAxisOrder = this.reverseAxisOrder();
        newParams.BBOX = this.encodeBBOX ?
                bounds.toBBOX(null, reverseAxisOrder) :
                bounds.toArray(reverseAxisOrder);
        newParams.WIDTH = imageSize.w;
        newParams.HEIGHT = imageSize.h;
        newParams.zoom = this.map.getZoom();


        var sponsorKeyValMapping = am.instance.sponsorFilteringManager.sponsorKeyValMapping;
        var checkedVals = [];

        if (!this.selectedSponsors) {
            this.selectedSponsors = $("#sponsorFilterCmb").val() || am.instance.sponsorFilteringManager.selectedSponsorsCodes;
        }
        for (var i = 0; i < this.selectedSponsors.length; i++) {
            checkedVals.push(sponsorKeyValMapping[this.selectedSponsors[i]]);
        }
        newParams.filters = JSON.stringify(checkedVals);
        return this.getFullRequestString(newParams);

    },
    /**
     * APIMethod: getFullRequestString
     * Combine the layer's url with its params and these newParams.
     *
     *     Add the SRS parameter from projection -- this is probably
     *     more eloquently done via a setProjection() method, but this
     *     works for now and always.
     *
     * Parameters:
     * newParams - {Object}
     * altUrl - {String} Use this as the url instead of the layer's url
     *
     * Returns:
     * {String}
     */
    getFullRequestString: function(newParams, altUrl) {
        var projectionCode = this.map.displayProjection;
        var value = (projectionCode == 'none') ? null : projectionCode
        if (parseFloat(this.params.VERSION) >= 1.3) {
            this.params.CRS = value;
        } else {
            this.params.SRS = value;
        }

        return OpenLayers.Layer.Grid.prototype.getFullRequestString.apply(
                this, arguments);
    },
    applyBackBuffer: function() {
        this.removeBackBuffer();
    }
});