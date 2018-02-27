Ext.define('AMAP.model.sponsor', {
    extend: 'Ext.data.Model',
    fields: [
        { name: 'id', type: 'string', mapping: 'id' },
        { name: 'name', type: 'string', mapping: 'name' },
        { name: 'displayName', type: 'string', mapping: 'name', convert: function(value, r) {
                return korem.format("{0}: {1}", r.get('id'), value);
        }}
    ]
});
