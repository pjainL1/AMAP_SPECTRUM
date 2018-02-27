Ext.define('AMAP.model.layergroup', {
    extend: 'Ext.data.Model',
    fields: [
        { name: 'id', type: 'int', mapping: 'id' },
        { name: 'leaf', type: 'boolean', mapping: 'leaf'},
        { name: 'expanded', type: 'boolean', mapping: 'expanded' },
        { name: 'groupId', type: 'int', mapping: 'groupId' },
        { name: 'sponsor', type: 'string', mapping: 'sponsor' },
        { name: 'text', type: 'string', mapping: 'text' },
        { name: 'realText', type: 'string', mapping: 'realText' },
        { name: 'index', type: 'int', mapping: 'index'},
        { name: 'isOther', type: 'boolean', mapping: 'isOther'}
    ]
});
