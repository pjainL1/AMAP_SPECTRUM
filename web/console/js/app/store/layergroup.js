Ext.define('AMAP.store.layergroup', {
    extend: 'Ext.data.TreeStore',
    alias: 'layergroupstore',
    name: 'layergroupstore',
    model: 'AMAP.model.layergroup',
    requires: 'AMAP.model.layergroup',
    autoLoad: false,
    proxy:{
        type: 'ajax',
        url: 'GetLayerGroup.safe',
        actionMethods: 'POST',
        reader: { 
            type: 'json'
        }
    },
    data: {
        text: 'Layer Groups',
        expanded: true
    }
});
