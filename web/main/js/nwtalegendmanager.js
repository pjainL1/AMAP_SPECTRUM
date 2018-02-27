am.NwTaLegendManager = function(options) {
    korem.apply(this, options);
    this.init();
};


am.NwTaLegendManager.prototype = {
    locationColors: null,
    mapInstanceKey: null,
    taLayer: null,
    nwLayer: null,
    html: null,
    init: function() {
    },
    setMapInstanceKey: function(mapInstanceKey) {
        this.mapInstanceKey = mapInstanceKey;
    },
    
    getLegendColors: function() {
        $.ajax({
            url: '../getNwTaLegend',
            context: this,
            type: 'post',
            data: {
                mapInstanceKey: this.mapInstanceKey
            },
            success: function(jsonArray) {
                this.locationColors = jsonArray;
                this.legend.updateNwTaLegend();
                this.legend.postUpdate();
            }
        });
    },
    reset: function() {
        this.nwLayer = null;
        this.taLayer = null;
    },
    createLegendTitle: function(isTA, isNW) {
        var title = '';
        if (isNW) {
            title += am.locale.legends.nwTa.title.nw;
        }
        if (isNW && isTA) {
            title += '<br />';
        }
        if (isTA) {
            title += am.locale.legends.nwTa.title.ta;
        }
        
        return title;
    },
    createLegend: function() {
        /*
         * Generates the html code to generate the legend, in case of NWatch or Trade Area
         */

        var isTA = this.taLayer && this.taLayer.visibility;
        var isNW = this.nwLayer && this.nwLayer.visibility;
        
        if (!korem.get('tradeAreaChkIssuance').checked && !korem.get('tradeAreaChkDistance').checked) {
            // no insuance or drive distance trade area: no TA legend needed.
            isTA = false;
        }

        if (!(isTA || isNW) || !this.locationColors || !this.locationColors.length) {
            // no need to proceed if either layers doesn't exist or if it's invisible
            return null;
        }

        var html = [];
        
        html.push("<div class='nwTaLegendContainer legendImg'>");
        
        html.push(korem.format("<div class='nwTaLegendTitle'>{0}</div>", this.createLegendTitle(isTA, isNW)));

        html.push("<table class='baseTable nwTaLegendTable' cellpadding='0' cellspacing='0' border='0'>");

        html.push("<tr>");
        if (isNW) {
            html.push(korem.format("<td class='legendHeader nwHeader'>{0}</td>", am.locale.legends.nwTa.subtitle.nw));
        }
        if (isTA) {
            html.push(korem.format("<td class='legendHeader taHeader'>{0}</td>", am.locale.legends.nwTa.subtitle.ta));
        }
        html.push("<td></td>");
        html.push("</tr>");

        for (var i = 0; i < this.locationColors.length; i++) {

            var locationCode = this.locationColors[i].locationCode;
            var nwColor = this.locationColors[i].nwColor;
            var taColor = this.locationColors[i].taColor;

            html.push("<tr>");

            var last = "";
            if (i == this.locationColors.length - 1) {
                // if we're on the bottom last element, draw bottom border on the colored div
                last = "nwtaLegendColorLastDiv";
            }
            if (isNW) {
                html.push("<td class='nwtaLegendColorTd'>", "<div class='nwtaLegendColorDiv ", last, "' style='background-color:#", nwColor, "'></div>", "</td>");
            }
            if (isTA) {
                html.push("<td class='nwtaLegendColorTd'>", "<div class='nwtaLegendColorDiv ", last, "' style='background-color:#", taColor, "'></div>", "</td>");
            }

            html.push("<td class='nwtaLegendValueTd'>", korem.format(am.locale.legends.nwTa.locTemplate, locationCode), "</td>");
            html.push("</tr>");
        }
        html.push("</table>");
        html.push("</div>");

        var htmlString = html.join("");

        return htmlString;

    }

};