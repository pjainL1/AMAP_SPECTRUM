Ext.define('AMAP.view.tamanagementlist', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.tamanagementlist',
    name: 'widget.tamanagementlist',
    viewConfig: {
        emptyText: 'No records'
    },
    features: [{
            ftype: 'filters', 
            encode: true, 
            local: false,
            filters: [
                {type: 'numeric', dataIndex: 'id'}, 
                {type: 'string', dataIndex: 'rollupName'}, 
                {type: 'string', dataIndex: 'userLogin'}, 
                {type: 'string', dataIndex: 'type'}, 
                {type: 'string', dataIndex: 'sponsorLocationCode'}
            ]
    }],
    store: 'tradearea',
    id: 'taGrid',
    loadMask: true,
    height: 520,
    dockedItems: [
        {
            xtype: 'toolbar',
            dock: 'top',
            displayInfo: true,
            items: [
                {
                    xtype: 'label',
                    text: am.locale.console.taHistoryConsole.from,
                    margins: {top: 0, right: 10, bottom: 0, left: 0},
                    style: 'margin-left:5px;margin-right05px'
                },
                {
                    xtype: 'datefield',
                    itemId: 'fromDate',
                    id: 'fromDate',
                    anchor: '100%',
                    textAlign: 'right',
                    name: 'from_date',
                    value: new Date()
                }, {
                    xtype: 'label',
                    text: am.locale.console.taHistoryConsole.to,
                    margins: {top: 0, right: 10, bottom: 0, left: 25},
                    style: 'margin-left:5px;'
                }, {
                    xtype: 'datefield',
                    itemId: 'toDate',
                    id: 'toDate',
                    anchor: '100%',
                    textAlign: 'right',
                    name: 'to_date',
                    value: new Date()
                },
                {
                    xtype: 'button',
                    id: 'changeDateButton',
                    iconCls: 'x-refresh-btn',
                    itemId: 'gridButtonChangeDate',
                    margins: {top: 0, right: 0, bottom: 0, left: 15},
                    text: am.locale.console.taHistoryConsole.change,
                    enableToggle: false
                }
            ]
        },
        {
            xtype: 'toolbar',
            dock: 'bottom',
            displayInfo: true,
            items: [
                '->', {
                    xtype: 'button',
                    iconCls: 'icon-download-big',
                    itemId: 'gridButtonDownload',
                    text: am.locale.console.taHistoryConsole.download,
                    enableToggle: false,
                    scale: 'large',
                    width: 100
                }
            ]
        },
        {
            xtype: 'toolbar',
            dock: 'bottom',
            displayInfo: true,
            items: [{
                    xtype: 'pagingtoolbar',
                    store: 'tradearea',
                    dock: 'bottom',
                    items: [{
                            xtype: 'trigger',
                            itemId: 'gridTrigger',
                            fieldLabel: 'Filter Grid Data',
                            triggerCls: 'x-form-clear-trigger',
                            emptyText: am.locale.console.filterText,
                            size: 30,
                            minChars: 1,
                            enableKeyEvents: true,
                            onTriggerClick: function() {
                                this.reset();
                                this.fireEvent('triggerClear');
                            }
                        }]
                }
            ]
        }
    ],
    initComponent: function() {
        this.columns = [
            {header: am.locale.console.taHistoryConsole.id, dataIndex: 'id', flex: 1},
            {header: am.locale.console.taHistoryConsole.date, dataIndex: 'creaDate', flex: 1},
            {header: am.locale.console.taHistoryConsole.rollupName, dataIndex: 'rollupName', flex: 2},
            {header: am.locale.console.taHistoryConsole.user, dataIndex: 'userLogin', flex: 1},
            {header: am.locale.console.taHistoryConsole.type, dataIndex: 'type', flex: 1},
            {header: am.locale.console.taHistoryConsole.locationCode, dataIndex: 'sponsorLocationCode', flex: 1}
        ];
        this.callParent(arguments);
    }
});
