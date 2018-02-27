Ext.define('AMAP.store.sponsor', {
    extend: 'Ext.data.Store',
    alias: 'widget.sponsorstore',
    name: 'widget.sponsorstore',
    model: 'AMAP.model.sponsor',
    autoLoad: false,
    requires: 'AMAP.model.sponsor',
    proxy:{
        type: 'ajax',
        url: '../GetSponsors.safe',
        actionMethods: 'POST',
        reader: { 
            type: 'json'
        }
    }
});
