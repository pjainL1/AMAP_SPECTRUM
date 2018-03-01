/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


Ext.define('AMAP.model.tradearea', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'creationDate', type: 'date', dateFormat: 'Y-mm-d'},
        {name: 'to'},
        {name: 'from'},
        {name: 'creaDate'},
        {name: 'toDate'},
        {name: 'fromDate'},
        {name: 'id'},
        {name: 'sponsorKey'},
        {name: 'sponsorLocation'},
        {name: 'sponsorLocationCode'},
        {name: 'style'},
        {name: 'type'},
        {name: 'typeDetail'},
        {name: 'userID'},
        {name: 'userLogin'},
        {name: 'rollupName'}
    ]
});