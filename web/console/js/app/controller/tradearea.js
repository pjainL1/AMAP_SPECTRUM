Ext.define('AMAP.controller.tradearea', {
    extend: 'Ext.app.Controller',
    //define the stores
    stores: ['tradearea'],
    //define the models
    models: ['tradearea'],
    //define the views
    views: ['tamanagementlist'],
    start: 0,
    SEARCH_KEYUP_DELAY: 2000,
    refs: [{
            ref: 'taGrid',
            selector: 'tagrid'
        }],
    init: function() {
        this.control({
            'viewport > panel': {
                render: this.onPanelRendered
            },
            'tamanagementlist #gridTrigger': {
                keyup: this.onTriggerKeyUp,
                triggerClear: this.onTriggerClear
            },
            'tamanagementlist #gridButtonDownload': {
                click: this.onDownloadBtnClick
            },
            'tamanagementlist #gridButtonChangeDate': {
                click: this.onChangeDateBtnClick
            }
        });
        console.log("init controller");
    },
    onTriggerKeyUp: function(t) {
        var store = this.getTradeareaStore();
        if (event.keyCode === 13) {
            store = this.getTradeareaStore();
            store.getProxy().setExtraParam('search', t.getValue());
            store.loadPage(1);
        }
        var me = this;

        if (event.keyCode !== 13) {
            clearTimeout(me.keyUpTimeout);
            this.keyUpTimeout = setTimeout(function()
            {
                store.getProxy().setExtraParam('search', t.getValue());
                store.loadPage(1);
            }
            , this.SEARCH_KEYUP_DELAY);
        }
    },
    onTriggerClear: function() {
        var store = this.getTradeareaStore();
        store.clearFilter();
        store.getProxy().setExtraParam('search', "");
        store.loadPage(1);
    },
    onDownloadBtnClick: function() {
        var property, direction, filters, sorter;
        if (this.getTradeareaStore().filterParams.params) {
            filters = this.getTradeareaStore().filterParams.params.filter;
        }
        if (this.getTradeareaStore().filterParams.sorters[0]) {
            property = this.getTradeareaStore().filterParams.sorters[0].property;
            direction = this.getTradeareaStore().filterParams.sorters[0].direction;
        }
        var search = this.getTradeareaStore().proxy.extraParams.search;
        Ext.Ajax.request({
            url: 'TaHistoryExport.safe',
            params: {
                search: search,
                filters: filters,
                sorter: sorter,
                direction: direction,
                property: property,
                dateFilters: this.dateFilters
            },
            success: function(response, opts, str) {
                var json = Ext.JSON.decode(response.responseText);
                if (!json.success) {
                    alert("Error while generating trade area MID/MIF file.");
                    return;
                }
                var store = this.store;
                $("#TradeAreaFileDownloadFrame").attr("src", "../GetTaHistoryFile.safe?" + Math.random());
            },
            failure: function(response, opts) {
                console.log('server-side failure with status code ' + response.status);
            }
        });
    },
    onChangeDateBtnClick: function() {
        var newFilter = new Array();

        var fromFilter = new Object();
        fromFilter.type = "date";
        fromFilter.comparison = "gt"
        fromFilter.value = Ext.getCmp('fromDate').getSubmitValue()+ ' 00:00';
        fromFilter.field = "creaDate1";

        var toFilter = new Object();
        toFilter.type = "date";
        toFilter.comparison = "lt"
        toFilter.value = Ext.getCmp('toDate').getSubmitValue() + ' 23:59';
        toFilter.field = "creaDate2";
        
        newFilter[0] = fromFilter;
        newFilter[1] = toFilter;

        var dateFilters = Ext.JSON.encode(newFilter);

        this.dateFilters = dateFilters;
        this.getTradeareaStore().getProxy().setExtraParam("dateFilters", dateFilters);
        this.getTradeareaStore().load();

    }
});



