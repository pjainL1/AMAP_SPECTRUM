/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


Ext.define('AMAP.model.locationcolor', {
    extend: 'Ext.data.Model',
    fields: [
        {name: 'sponsorLocationKey', mapping: 'sponsorLocationKey'},
        {name: 'sponsorLocationCode', mapping: 'sponsorLocationCode'},
        {name: 'sponsorLocationName', mapping: 'sponsorLocationName'},
        {name: 'city', mapping: 'city'},
        {name: 'postalCode', mapping: 'postalCode'},
        {name: 'nwatchColor', mapping: 'nwatchColor'},
        {name: 'taColor', mapping: 'taColor'}
    ]
});