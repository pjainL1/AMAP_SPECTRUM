Ext.define('AMAP.view.sponsor', {
    extend: 'Ext.form.ComboBox',
    alias: 'widget.sponsorSelector',
    name: 'sponsorSelector',
    store: 'sponsor',
    fieldLabel: am.locale.console.colorManagementConsole.sponsorSelect,
    displayField: 'displayName',
    labelWidth: 125,
    valueField: 'id',
    multiSelect: false,
    editable: false,
    labelStyle:'font-weight:bold;',
    width: 400,
    initComponent: function() {
        this.callParent(arguments);
    }
});