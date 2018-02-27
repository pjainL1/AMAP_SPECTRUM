Ext.define('AMAP.store.locationcolor', {
    extend: 'Ext.data.Store',
    alias: 'widget.locationcolorstore',
    name: 'widget.locationcolorstore',
    model: 'AMAP.model.locationcolor',
    autoLoad: false,
    id: 'locationColorStore',
    pageSize: am.consoleConfig.datagrids.pageSize,
    remoteFilter: true,
    remoteSort: true, // to enable sorting
    proxy: {
        type: 'ajax',
        filterParam: 'query',
        encodeFilters: function(filters) {
            return filters[0].value;
        },
        url: 'ColorManagement.safe',
        reader: {
            type: 'json',
            totalProperty: 'totalCount',
            root: 'locationcolors'
        }
    },
    listeners: {
        load: function(store, records) {
            if (records.length == 0 && store.currentPage > 1) {
                store.loadPage(1);
            }
        }
    }
});
