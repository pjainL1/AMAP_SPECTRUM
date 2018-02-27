Ext.define('AMAP.view.layergroup', {
    extend: 'Ext.tree.Panel',
    alias: 'widget.layergroup',
    name: 'layergroup',
    store: 'layergroup',
    useArrows: true,
    rootVisible: false,
    root: {text: 'Root'},
    multiSelect: false,
    singleExpand: false,
    autoScroll: true,
    enableDD: true,
    id:'scrollableContainer',
    height: 520,
    viewConfig: {
        plugins: {
            ptype: 'treeviewdragdrop',
            containerScroll: {
                vthresh: 20, htresh:1 // this will use threshold of 100 pixels, 
            }            // so it will start scrolling at 100 pixels from the border
        }
    },
    columns: [{
            xtype: 'treecolumn',
            text: am.locale.console.layerGroup.columnTitleOne,
            flex: 2,
            dataIndex: 'text',
            sortable: false,
            menuDisabled: true
        }, {
            text: am.locale.console.layerGroup.columnTitleTwo,
            width: 50,
            menuDisabled: true,
            xtype: 'actioncolumn',
            items: [{
                    iconCls: 'icon-layergroup-edit',
                    handler: function(view, rowIndex, colIndex, actionItem, event, record, row) {
                        view.up('layergroup').fireEvent('nodeedit', rowIndex, colIndex, actionItem, event, record, row);
                    },
                    isDisabled: function(view, rowIdx, colIdx, item, record) {
                        return record.data.leaf || record.data.isOther === true;
                    }
                }],
            tooltip: am.locale.console.layerGroup.renameTooltip,
            align: 'center'
        }, {
            text: am.locale.console.layerGroup.columnTitleThree,
            width: 40,
            menuDisabled: true,
            xtype: 'actioncolumn',
            items: [{
                    iconCls: 'icon-layergroup-delete',
                    handler: function(view, rowIndex, colIndex, actionItem, event, record, row) {
                        view.up('layergroup').fireEvent('nodedelete', rowIndex, colIndex, actionItem, event, record, row);
                    },
                    isDisabled: function(view, rowIdx, colIdx, item, record) {
                        return record.data.leaf === true || record.data.isOther === true;
                    }
                }],
            tooltip: am.locale.console.layerGroup.deleteTooltip,
            align: 'center'
        }
    ],
    dockedItems: [{
            xtype: 'toolbar',
            dock: 'top',
            displayInfo: true,
            items: [{
                    xtype: 'sponsorSelector'
                }, '->', {
                    xtype: 'button',
                    iconCls: 'x-add-btn',
                    name: 'addGroupBtn',
                    text: am.locale.console.layerGroup.addBtnLabel,
                    enableToggle: false
                }]
        }]
});

