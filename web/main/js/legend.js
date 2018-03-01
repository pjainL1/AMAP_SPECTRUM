am.Legend = function (options) {
    korem.apply(this, options);
    this.init();
}

am.Legend.prototype = {
    NW_LAYER_NAME: "Neighbourhood Watch",
    TA_LAYER_NAME: "Trade Area",
    DEFAULT_WIDTH: 180,
    id: null,
    map: null,
    mapInstanceKey: null,
    baseUrl: null,
    legendContainer: null,
    legendList: null,
    legendCanvas: null,
    legendEmpty: null,
    legendTitle: null,
    toggler: null,
    legends: null,
    hotspotCpt: 0,
    hotspotEl: null,
    init: function () {
        this.legends = {};
        this.baseUrl = am.constants.legendUrl.replace('%s0', this.mapInstanceKey);
        this.initUI();

        //instantiate legendmanager
        this.legendManager = new am.NwTaLegendManager({mapInstanceKey: this.mapInstanceKey, legend: this});
    },
    moveTo: function (container) {
        container.appendChild(this.legendContainer);
    },
    initUI: function () {
        this.legendContainer = korem.get(this.id + 'Legend');
        this.legendList = korem.get(this.id + 'LegendList');
        this.legendCanvas = korem.get(this.id + 'LegendCanvas');
        this.legendEmpty = korem.get(this.id + 'LegendEmpty');
        this.legendTitle = korem.get(this.id + 'LegendTitle');
        this.createToggler();
    },
    createToggler: function () {
        var that = this;
        this.toggler = korem.apply(korem.apply({}, am.ToolBase.prototype), {
            btnId: this.id + 'LegendToggler',
            groupId: this.id + 'Legend',
            initialize: function () {
            },
            doActivate: function () {
                that.legendList.style.display = 'block';
                that.adjustSize(that.isEmpty());
            },
            doDeactivate: function () {
                that.legendList.style.display = 'none';
            }
        });
        this.toggler.init();
    },
    updateNwTaLegend: function () {
        // keeps track of the current legend once added to the legend list. '-1' is just a random id.
        var layer = {id: -1};

        // remove previously created legend
        this.removeLegend(layer);

        var html = this.legendManager.createLegend();

        if (!html) {
            return;
        }
        // create an element from the string
        var div = $(html);
        $(".legendEmpty", this.legendCanvas).after(div);

        // add the html elemend to the legend list
        this.legends[layer.id] = div[0];

    },
    update: function (layer, forcedVisibility) {

        var addingHotspot = false;
        if (layer.isTheme || layer.isHotspot) {
            this.removeLegend(layer);
            if (layer.visibility || forcedVisibility) {
                if (layer.isTheme) {

                    var legend = this.legends[layer.id] = html.img({
                        props: {
                            src: this.baseUrl.replace('%s1', layer.id) + '&r=' + new Date().getTime(),
                            className: 'legendImg'
                        }
                    });

                    if (this.hotspotEl) {
                        this.legendCanvas.insertBefore(legend, this.hotspotEl);
                    } else {
                        this.legendCanvas.appendChild(legend);
                    }
                    this.addLoadListener(legend);
                } else if (layer.isHotspot) {
                    var that = this;
                    addingHotspot = true;
                    var hotspotCpt = ++this.hotspotCpt;
                    this.createHotSpotLegend(function (el) {
                        if (hotspotCpt == that.hotspotCpt) {
                            that.legendCanvas.appendChild(that.legends[layer.id] = that.hotspotEl = el);
                            that.postUpdate();
                        }
                    });
                }
            }

        }

        if (layer.name == this.TA_LAYER_NAME || layer.name == this.NW_LAYER_NAME) {
            this.updateNwTaLegend();
        }

        if (!addingHotspot) {
            this.postUpdate();
        }
    },
    addLoadListener: function (img) {
        $(img).load(korem.callback(this, function () {
            var empty = this.isEmpty();
            this.adjustSize(empty);
        }));
    },
    postUpdate: function () {
        var empty = this.isEmpty();
        this.legendEmpty.style.display = (empty) ? '' : 'none';
        this.legendTitle.style.display = (empty) ? 'none' : '';
        var that = this;
        setTimeout(function () {
            that.adjustSize(empty);
        }, 500);
    },
    adjustSize: function (empty) {
        var mapHeight = $('#firstRelativeMapContainer').height() - 50; // subtract 50px to keep space for the map buttons.
        if (!empty) {
            $(this.legendList).css('padding-right', '0');
            this.adjustWidth();
            if ($(this.legendCanvas).height() > mapHeight) {
                // deal with overflow
                $(this.legendList).height(mapHeight);
                $(this.legendList).css('overflow-y', 'auto');
                $(this.legendList).css('padding-right', '20px');
                $(this.legendList).css('overflow-x', 'hidden');
            } else {
                $(this.legendList).height('auto');
                $(this.legendList).css('overflow-y', 'visible');
            }
        } else {
            $(this.legendList).width(this.DEFAULT_WIDTH);
        }
    },
    adjustWidth: function () {
        var width = 0;
        $(".legendImg").each(function () {
            var aWidth = $(this).width();
            if (aWidth > width) {
                width = aWidth;
            }
        });
        if (width < this.DEFAULT_WIDTH) {
            width = this.DEFAULT_WIDTH;
        }
        $(this.legendList).width(width);
    },
    isEmpty: function () {
        for (var key in this.legends) {
            return false;
        }
        return true;
    }, removeLegend: function (layer) {
        var legend = this.legends[layer.id];
        if (legend) {
            delete this.legends[layer.id];
            legend.parentNode.removeChild(legend);
            if (layer.isHotspot) {
                this.hotspotEl = null;
            }
        }
    },
    createHotSpotLegend: function (callback) {
        $.ajax({
            url: '../getHotSpotLegend.safe',
            context: this,
            type: 'post',
            data: {
                mapInstanceKey: this.mapInstanceKey
            },
            success: function (data) {
                if (!data) {
                    return;
                }
                var legend = html.leanTable({
                    props: {
                        className: 'fullTable hotspotLegendTable legendImg'
                    }
                });
                var tbody = legend.childNodes[0];
                tbody.appendChild(html.tr({
                    children: [
                        html.td({
                            props: {
                                className: 'hotspotLegendTitle',
                                colSpan: 2,
                                innerHTML: data.title
                            }
                        })
                    ]
                }));
                tbody.appendChild(html.tr({children: [
                        html.td({
                            props: {
                                className: 'hotspotLegendSubTitle',
                                colSpan: 2,
                                innerHTML: data.subtitle
                            }
                        })
                    ]
                }));
                tbody.appendChild(html.tr({
                    children: [
                        html.td({
                            props: {
                                className: 'hotspotPrecision',
                                colSpan: 2,
                                innerHTML: am.locale.hotspot.legend.per + ' ' + data.precision + am.locale.hotspot.legend.km
                            }
                        })
                    ]
                }));
                if (data.items) {
                    for (var i = data.items.length - 1; i >= 0; --i) {
                        var legendItem = data.items[i];
                        tbody.appendChild(html.tr({
                            children: [
                                html.td({
                                    props: {
                                        className: 'hotspotLegendColorTd'
                                    },
                                    children: [
                                        html.div({
                                            props: {
                                                className: 'hotspotLegendColorDiv ' + ((i == 0) ? 'hotspotLegendColorLastDiv' : '')
                                            },
                                            style: {
                                                backgroundColor: '#' + legendItem.color
                                            }
                                        })
                                    ]
                                }),
                                html.td({
                                    props: {
                                        className: 'hotspotLegendValueTd',
                                        innerHTML: this.generateRanges(legendItem)
                                    }
                                })
                            ]
                        }));
                    }
                    callback(legend);
                }
            }
        });
    },
    generateRanges: function (legendItem) {
        if (am.instance.datePickers.selectedDateType == "single") {
            return ((legendItem.min == '0' || legendItem.min == '$0') ? 'under' : (legendItem.min + ' ' + am.locale.hotspot.legend.to)) + ' ' + legendItem.max
        } else {
            return ((legendItem.min == '0' || legendItem.min == '$0') && (legendItem.max == '0' || legendItem.max == '$0')) ? legendItem.max : (legendItem.min + ' ' + am.locale.hotspot.legend.to + ' ' + legendItem.max);
        }

    },
    reset: function () {
        for (var id in this.legends) {
            var legend = this.legends[id];
            legend.parentNode.removeChild(legend);
        }
        this.legends = [];
        this.legendEmpty.style.display = '';
        this.legendTitle.style.display = 'none';
    }
}