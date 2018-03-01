OpenLayers.Layer.Google.v3.setGMapVisibility = function(visible) {
    var cache = OpenLayers.Layer.Google.cache[this.map.id];
    if (cache) {
        var type = this.type;
        var layers = this.map.layers;
        var layer;
        for (var i=layers.length-1; i>=0; --i) {
            layer = layers[i];
            if (layer instanceof OpenLayers.Layer.Google &&
                layer.visibility === true && layer.inRange === true) {
                type = layer.type;
                visible = true;
                break;
            }
        }
        var container = this.mapObject.getDiv();
        if (visible === true) {
            this.mapObject.setMapTypeId(type);
            container.style.left = "";
            if (cache.termsOfUse && cache.termsOfUse.style) {
                cache.termsOfUse.style.left = "";
                cache.termsOfUse.style.display = "";
                cache.poweredBy.style.display = "";
            }
            cache.displayed = this.id;
        } else {
            delete cache.displayed;
            if (cache.termsOfUse && cache.termsOfUse.style) {
                cache.termsOfUse.style.display = "none";
                // move ToU far to the left in addition to setting
                // display to "none", because at the end of the GMap
                // load sequence, display: none will be unset and ToU
                // would be visible after loading a map with a google
                // layer that is initially hidden.
                cache.termsOfUse.style.left = "-9999px";
                cache.poweredBy.style.display = "none";
            }
        }
    }
}

OpenLayers.Layer.Google.v3.repositionMapElements = function() {
    // This is the first time any Google layer in this mapObject has been
    // made visible.  The mapObject needs to know the container size.
    google.maps.event.trigger(this.mapObject, "resize");

    var div = this.mapObject.getDiv().firstChild;
    if (!div || div.childNodes.length < 3) {
        this.repositionTimer = window.setTimeout(
            OpenLayers.Function.bind(this.repositionMapElements, this),
            250
            );
        return false;
    }

    var cache = OpenLayers.Layer.Google.cache[this.map.id];
    var container = this.map.viewPortDiv;

    // move the ToS and branding stuff up to the container div
    var termsOfUse = div.lastChild;
    container.appendChild(termsOfUse);

    var poweredBy = div.lastChild;
    container.appendChild(poweredBy);
    cache.poweredBy = poweredBy;

    setTimeout(function() {
        var el = container.lastChild;
        while (el.className != '') {
            el = el.previousSibling;
        }
        el.className = "olLayerGooglePoweredBy olLayerGoogleV3 gmnoprint";
    }, 1);

    var googleLogo = div.lastChild;
    container.appendChild(googleLogo);
    googleLogo.style.zIndex = "1100";
    googleLogo.style.bottom = "";
    googleLogo.style.display = "";
    cache.googleLogo = googleLogo;

    this.setGMapVisibility(this.visibility);

}