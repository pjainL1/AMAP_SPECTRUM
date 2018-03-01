Ext.define('AMAP.store.tradearea', {
    extend: 'Ext.data.Store',
    alias: 'widget.tradeareastore',
    name: 'widget.tradeareastore',
    model: 'AMAP.model.tradearea',
    autoLoad: false,
    id: 'tradeAreaStore',
    pageSize: am.consoleConfig.datagrids.tradeAreas.pageSize,
    remoteFilter: true,
    remoteSort: true, // to enable sorting
    proxy: {
        type: 'ajax',
        filterParam: 'query',
        encodeFilters: function(filters) {
            return filters[0].value;
        },
        url: 'TradeAreaHistoryManagement.safe',
        reader: {
            type: 'json',
            totalProperty: 'totalCount',
            root: 'tahistory'
        }
    },
    sorters: [{property: 'creaDate',direction: 'DESC'}],
    listeners: {
        load: function(store, records, opts) {
            var options = Ext.apply({
                action: 'read',
                filters: this.filters.items,
                sorters: this.getSorters()
            }, options);
            this.lastOptions = options;

            var operation = new Ext.data.Operation(options);
            this.fireEvent('beforeload', this, operation);
            this.filterParams = operation;

            if (records !== null && records.length === 0 && store.currentPage > 1) {
                store.loadPage(1);
            }
        }
    }
});
