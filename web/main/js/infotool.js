am.InfoTool = function(options) {
    korem.apply(this, options);
    korem.apply(this, {
        btnId: 'infoToolButton',
        groupId: 'tools'
    });
    this.init();
}

am.InfoTool.prototype = korem.apply({
    mapInstanceKey: null,
    map: null,
    dates: null,
    clickHandlers: null,
    window: null,
    content: null,
    button: null,
    activated: false,
    loading: null,
    minimumValues: null,
    exclusions: ['mipk', 'location_key', 'id'],
    initialize: function() {
        this.loading = $('#infoToolWindowLoading');
        this.clickHandlers = {};
        this.getClickHandler();
        this.createWindow();
        this.content = korem.get('infoToolWindowContent');
    },
    getClickHandler: function() {
        var handler = this.clickHandlers[this.mapInstanceKey];
        if (!handler) {
            this.map.addControl(this.clickHandlers[this.mapInstanceKey] = handler = new OpenLayers.Control.Click({
                listener: this
            }));
        }
        return handler;
    },
    setMap: function(map, mapInstanceKey) {
        this.map = map;
        this.mapInstanceKey = mapInstanceKey;
        this.getClickHandler();
    },
    createWindow: function() {
        var that = this;
        var fixSize = function() {
            if (navigator.appVersion.indexOf("MSIE") > 0) {
                var titleWidth = $(that.window).parent().width();
                var titleHeight = $(that.window).parent().height();
                $(that.window).width(titleWidth).height(titleHeight - 38);
                $(that.window).css('margin-bottom', '15px');
            }
        };
        
        this.window = $('#infoToolWindow').dialog({
            zIndex: 7000,
            autoOpen: false,
            resizable: true,
            minWidth: 400,
            height: 240,
            close: function() {
                that.map.events.unregister('movestart', that, that.movestart);
            },
            open: function() {
                that.map.events.register('movestart', that, that.movestart);
                setTimeout(function() {
                    fixSize();
                }, 0);
            },
            dragStop: function() {
                fixSize();
            },
            resizeStop: function(e, ui) {
                fixSize();
            }
        });
    },
    doDeactivate: function() {
        if (!this.isOn()) {
            this.getClickHandler().deactivate();

        }
    },
    doActivate: function() {
        if (this.isOn()) {
            this.hideWindow();
        }
        this.getClickHandler().activate();
    },
    movestart: function() {
        this.hideWindow();
    },
    trigger: function(e) {
        var that = this;
        if (this.activated) {

            if (this.isOn()) {
                this.hideWindow();
            }

            this.showWindow(e.xy);
            $.ajax({
                url: '../getInfo.safe',
                context: this,
                type: 'post',
                data: {
                    mapInstanceKey: this.mapInstanceKey,
                    x: e.xy.x,
                    y: e.xy.y,
                    from: that.formatDate(that.dates[0]),
                    to: that.formatDate(that.dates[1]),
                    minTransactions: that.minimumValues.minTransactions,
                    minSpend: that.minimumValues.minSpend,
                    minUnit: that.minimumValues.minUnit
                    
                },
                success: function(data) {
                    this.populateWindow(data);
                },
                error: function() {
                    korem.get('infoToolWindowError').className = 'infoToolWindowError';
                }
            });
        } else {
            this.hideWindow();
        }
    },
    formatDate: function(el) {
        return $.datepicker.formatDate('yymmdd', $(el).datepicker('getDate'));
    },
    populateWindow: function(data) {
        var noResult = true;

        var resultTable = html.leanTable({
            props: {
                className: 'infotoolWindowResult'
            }
        });

        var tbody = resultTable.childNodes[0];

        for (var layerName in data) {
            var layer = data[layerName];

            var div1 = html.div({
                props: {
                    className: 'infotoolWindowLayer',
                    innerHTML: layerName
                }
            });

            var layerNameTD = html.td({props: {colSpan: 3}, children: [div1]});

            tbody.appendChild(html.tr({
                children: [
                    layerNameTD
                ]
            }));


            for (var i = 0; i < layer.length; ++i) {
                noResult = false;
                var result = layer[i];
                var cpt = 0;
                for (var key in result) {
                    var keyDisplayValue = am.locale.infoTool.keyDisplayValues[key.toUpperCase()] || this.removeUnderscore(key);

                    if ($.inArray(key.toLowerCase(), this.exclusions) == -1) {
                        if (result[key] != '_SPACER_') {
                            tbody.appendChild(html.tr({
                                props: {
                                    className: 'infotoolWindowRow_' + (cpt++ % 2)
                                },
                                children: [
                                    html.td({
                                        props: {
                                            width: 15,
                                            className: 'whiteTD',
                                            innerHTML: ''
                                        }
                                    }),
                                    html.td({
                                        props: {
                                            className: 'infotoolWindowKey',
                                            innerHTML: keyDisplayValue + ':'
                                        }
                                    }),
                                    html.td({
                                        props: {
                                            className: 'infotoolWindowValue',
                                            innerHTML: result[key]
                                        }
                                    })
                                ]
                            }));
                        } else {
                            /*cpt = 0;*/
                            tbody.appendChild(html.tr({
                                props: {
                                    className: 'infotoolWindowRow_1'
                                },
                                children: [
                                    html.td({
                                        props: {
                                            width: 15,
                                            className: 'spacer',
                                            innerHTML: ''
                                        }
                                    }),
                                    html.td({
                                        props: {
                                            className: 'spacer',
                                            innerHTML: ''
                                        }
                                    }),
                                    html.td({
                                        props: {
                                            className: 'spacer',
                                            innerHTML: ''
                                        }
                                    })
                                ]
                            }));
                        }

                    }

                }
                var td = html.td({props: {
                        colspan: 3,
                        height : '20px'
                    }});
                var tr = html.tr({
                    props: {
                    }
                });
                tr.appendChild(td);
                tbody.appendChild(tr);
            }
        }

        this.content.appendChild(resultTable);

        this.loading.hide();
        if (noResult) {
            korem.get('infoToolWindowNoResult').className = 'infoToolWindowNoResult';
        }
    },
    isOn: function() {
        return this.window.dialog('isOpen');
    },
    showWindow: function() {
        this.content.innerHTML = '';
        korem.get('infoToolWindowNoResult').className = "noshow";
        korem.get('infoToolWindowError').className = "noshow";
        this.loading.show();
        this.window.dialog('open');
    },
    hideWindow: function() {
        this.window.dialog('close');
    },
    removeUnderscore: function(value) {
        return value.replace(/_/g, ' ');
    }
}, am.ToolBase.prototype);



OpenLayers.Control.Click = OpenLayers.Class(OpenLayers.Control, {
    defaultHandlerOptions: {
        'single': true,
        'double': true,
        'pixelTolerance': 0,
        'stopSingle': false,
        'stopDouble': false
    },
    initialize: function(options) {
        this.listener = options.listener;
        this.listener.trigger = this.listener.trigger || korem.EMPTY_FNC;
        this.listener.triggerDblClick = this.listener.triggerDblClick || korem.EMPTY_FNC;

        this.handlerOptions = OpenLayers.Util.extend(
                {}, this.defaultHandlerOptions
                );
        OpenLayers.Control.prototype.initialize.apply(
                this, arguments
                );
        this.handler = new OpenLayers.Handler.Click(
                this, {
                    'click': this.trigger,
                    'double': this.triggerDblClick
                }, this.handlerOptions
                );
    },
    trigger: function(e) {
        this.listener.trigger(e);
    },
    triggerDblClick: function(e) {
        this.listener.triggerDblClick(e);
    }
});