Ext.define('AMAP.controller.locationcolor', {
    SEARCH_KEYUP_DELAY: 2000,
    NW_COLUMN_INDEX: 4,
    TA_COLUMN_INDEX: 5,
    
    extend: 'Ext.app.Controller',
    //define the stores
    stores: ['locationcolor'],
    //define the models
    models: ['locationcolor'],
    //define the views
    views: ['locationcolorlist'],
    refs: [
        {
            ref: 'colorGrid',
            selector: 'locationcolorlist'
        },
        {
            ref: 'sponsorSelector',
            selector: "locationcolorlist component[name='sponsorSelector']"
        }
    ],
    init: function() {
        this.control({
            'locationcolorlist': {
                render: this.onRender,
                cellclick: this.onCellClick,
                tabHidden: this.onTabHidden
            },
            'locationcolorlist #gridTrigger': {
                keyup: this.onTriggerKeyUp,
                triggerClear: this.onTriggerClear
            },
            "locationcolorlist component[name='sponsorSelector']": {
                select: this.onSponsorSelected
            }
        });
        this.getLocationcolorStore().on('beforeload', function() {
            this.closePicker();
        }, this);

    },
    onTabHidden: function() {
        this.closePicker();
    },
    closePicker: function() {
        //IE =  close the colorpicker when the user change the page
        if (this.openedColorPicker != undefined) {
            this.openedColorPicker.hidePicker();
        }
    },
    onRender: function() {
        var store = this.getSponsorSelector().getStore();
        store.load({
            scope: this,
            callback: function() {
                var recordSelected = store.getAt(0);
                var selector = this.getSponsorSelector();
                selector.setValue(recordSelected.get('id'));
                selector.fireEvent('select', selector, [recordSelected]);
            }
        });
    },
    onSponsorSelected: function(combo, record, index, scope) {
        var locationColorStore = this.getColorGrid().getStore();
        locationColorStore.getProxy().setExtraParam('sponsor', record[0].get('id'));
        locationColorStore.load();
    },
    onTriggerKeyUp: function(t) {
        var store = this.getLocationcolorStore();
        if (event.keyCode === 13) {
            store.getProxy().setExtraParam('search', t.getValue());
            store.loadPage(1);
        }
        var me = this;
        if (event.keyCode !== 13) {
            clearTimeout(me.keyUpTimeout);
            this.keyUpTimeout = setTimeout(function() {
                store.getProxy().setExtraParam('search', t.getValue());
                store.loadPage(1);
            }, this.SEARCH_KEYUP_DELAY);
        }
    },
    onTriggerClear: function() {
        var store = this.getLocationcolorStore();
        store.clearFilter();
        store.getProxy().setExtraParam('search', "");
        store.loadPage(1);
    },
    onCellClick: function(gridView, htmlElement, columnIndex, dataRecord) {
        console.log('cell click');
        
        if(document.getElementById(dataRecord.id + columnIndex) == null) {
            console.log(dataRecord.id + columnIndex + ' is null, returning.');
            this.closePicker();
            return;
        }
        
        if (document.getElementById(dataRecord.id + columnIndex).color == undefined) {
            if ((columnIndex === this.NW_COLUMN_INDEX)) {
                var myPicker = new jscolor.color(document.getElementById(dataRecord.id + columnIndex), {});
                this.overRideHidePicker(myPicker);
                document.getElementById(dataRecord.id + columnIndex).color = myPicker;
                this.openedColorPicker = myPicker;
                myPicker.pickerClosable = true;
                myPicker.fromString(dataRecord.data.nwatchColor);
                myPicker.showPicker();
            } else if ((columnIndex === this.TA_COLUMN_INDEX)) {
                var myPicker = new jscolor.color(document.getElementById(dataRecord.id + columnIndex), {});
                this.overRideHidePicker(myPicker);
                document.getElementById(dataRecord.id + columnIndex).color = myPicker;
                this.openedColorPicker = myPicker;
                myPicker.pickerClosable = true;
                myPicker.fromString(dataRecord.data.taColor);
                myPicker.showPicker();
            } else {
                this.closePicker();
            }
        } else {
            var picker = document.getElementById(dataRecord.id + columnIndex).color;
            if (columnIndex == this.NW_COLUMN_INDEX) {
                picker.showPicker();
            } else if (columnIndex == this.TA_COLUMN_INDEX) {
                picker.showPicker();
            } else {
                this.closePicker();
            }
        }
    }, overRideHidePicker: function(picker) {
        var refPicker = picker.hidePicker;
        picker.hidePicker = function() {
            refPicker();
            document.getElementsByTagName('body')[0].focus();
        }
    }
});

