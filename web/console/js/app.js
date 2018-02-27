Ext.override(Ext.data.proxy.Ajax, { timeout:300000 });
Ext.override(Ext.data.Connection, {
        timeout:300000
});

Ext.Loader.setConfig({
    enabled: true
});
Ext.tip.QuickTipManager.init();

Ext.Loader.setPath('Ext.ux', '../js/lib/ext4/ux');
Ext.require([
    'Ext.ux.grid.FiltersFeature',
    'Ext.toolbar.Paging'
]);

Ext.application({
    name: 'AMAP',
    appFolder: 'js/app',
    controllers: ['locationcolor', 'sponsor','layergroup','tradearea'],
    launch: function() {
        console.log("launch...");
    }
});