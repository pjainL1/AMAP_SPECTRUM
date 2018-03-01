function saveColor(param) {
    var colorVal = document.getElementById(param).value;
    var sponsorKey = document.getElementById(param).getAttribute("sponsor_key");
    var colorFlag = document.getElementById(param).getAttribute("color-flag");
    $.ajax({url: '../SetColor.safe',
        data: {colorVal: colorVal, sponsor_location_key: sponsorKey, colorFlag: colorFlag},
        dataType: 'text'
    });
}

Ext.define('AMAP.view.locationcolorlist', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.locationcolorlist',
    name: 'locationcolorlist',
    enableColumnMove : false,
    viewConfig: {
        emptyText: 'No records'
    },
    features: [{
            ftype: 'filters', encode: true, local: false,
            filters: [
                {type: 'numeric', dataIndex: 'sponsorLocationKey'},
                {type: 'string', dataIndex: 'sponsorLocationCode'},
                {type: 'string', dataIndex: 'sponsorLocationName'},
                {type: 'string', dataIndex: 'city'},
                {type: 'string', dataIndex: 'postalCode'}
            ]
        }],
    store: 'locationcolor',
    loadMask: true,
    height: 520,
    dockedItems: [{
            xtype: 'toolbar',
            dock: 'top',
            displayInfo: true,
            items: [
                {
                    xtype: 'sponsorSelector'
                }
            ]
        },
        {
            xtype: 'toolbar',
            dock: 'bottom',
            displayInfo: true,
            items: [{
                    xtype: 'pagingtoolbar',
                    store: 'locationcolor',
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
        }],
    initComponent: function() {
        this.columns = [
            {header: am.locale.console.colorManagementConsole.locationCode, dataIndex: 'sponsorLocationCode', flex: 2},
            {header: am.locale.console.colorManagementConsole.city, dataIndex: 'city', flex: 2, filter: {type: 'string'}},
            {header: am.locale.console.colorManagementConsole.locationName, dataIndex: 'sponsorLocationName', flex: 2, filter: {type: 'string'}},
            {header: am.locale.console.colorManagementConsole.postalCode, dataIndex: 'postalCode', flex: 2},
            {header: am.locale.console.colorManagementConsole.nwatchColor, menuDisabled: true, dataIndex: 'nwatchColor', width: 65, resizable: false, sortable: false, renderer: this.renderColorColumn},
            {header: am.locale.console.colorManagementConsole.taColor, menuDisabled: true, dataIndex: 'taColor', width: 65, resizable: false, sortable: false, renderer: this.renderColorColumn}
        ];
        this.callParent(arguments);
    },
    renderColorColumn: function(val, id, r) {
        var color = "";

        if (id.columnIndex === 4) {
            color = "NWATCH";
        }
        else {
            color = "TA";
        }
        return '<div align="left" style="margin-right:5px;"><input class="color" sponsor_key="' + id.record.data.sponsorLocationKey +
                '" color-flag="' + color + '" onblur="saveColor(\'' + id.record.id + id.columnIndex +
                '\');" style="background-color:#' + val +
                ';width: 120%; -webkit-box-sizing: border-box; -moz-box-sizing: border-box; box-sizing: border-box; border:1px #C9C9C9;color:#' +
                this.getBackgroundColor(val) + '" id="' + id.record.id + id.columnIndex + '" value="' + val + '"></div>';
    },
    getBackgroundColor: function(colorValue) {
        var rgb = parseInt(colorValue, 16);   // convert rrggbb to decimal
        var r = (rgb >> 16) & 0xff;  // extract red
        var g = (rgb >> 8) & 0xff;  // extract green
        var b = (rgb >> 0) & 0xff;  // extract blue

        var luma = 0.2126 * r + 0.7152 * g + 0.0722 * b; // per ITU-R BT.709 - calculate color luminance

        if (luma < 150) {
            return "FFFFFF";
        } else {
            return "000000";
        }
    }

});
