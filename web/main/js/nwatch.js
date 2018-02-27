am.NWatch = function(options) {
    korem.apply(this, options);
    this.init();
}

am.NWatch.prototype = {

    updateLayerWhenApplied: true,

    parent: null,
    applyBtn: null,
    map: null,
    mapsManager: null,
    dates: null,
    layerControl: null,

    window: null,
    selectedCheckbox: null,
    selectedRadioBtn: null,
    hasChanged: false,

    init: function() {
        this.createDescWindow();
        this.handleDescBtn();
        this.handleCheckboxes();
        this.handleNWTypesRadioBtn();
        $(document).on("updateDataFiltering", korem.callback(this, this.needChange));
    },

    setMap: function(mapObj) {
        this.map = mapObj;
    },

    handleNWTypesRadioBtn: function() {
        var that = this; 
        var nwatchRadioBtns = [
        korem.get('nwatchSpend'),
        korem.get('nwatchUnit')
        ];
        for (var i = 0; i < nwatchRadioBtns.length; ++i) {
            nwatchRadioBtns[i].onclick = function() {
                if(that.selectedCheckbox != null){
                    that.changed();
                }
            };
        }        
    },
    
    handleCheckboxes: function() {
        var that = this;
        var checkboxes = [
        korem.get('nwatchChkPrimary'),
        korem.get('nwatchChkMajority')
        ];
        for (var i = 0; i < checkboxes.length; ++i) {
            checkboxes[i].onclick = function() {
                if (that.selectedCheckbox == this) {
                    that.selectedCheckbox = null;
                } else {
                    if (that.selectedCheckbox != null) {
                        that.selectedCheckbox.checked = false;
                    }
                    that.selectedCheckbox = this;
                }
                that.changed();
            };
            checkboxes[i].checked = false;
        }
    },

    handleDescBtn: function() {
        var that = this;
        $('#nwatchDescBtn').click(function() {
            that.window.dialog('open');
        });
    },
    
    createDescWindow: function() {
        this.window = $('#nWatchDescWindow').dialog({
            zIndex: 7000,
            autoOpen: false,
            resizable: false,
            width: 450,
            close: function() {},
            open: function() {}
        });
    },

    changed: function() {
        this.hasChanged = true;
        this.applyBtn.needed();
    },

    populateData: function(data, callback) {
        if (this.hasChanged) {
            if (this.selectedCheckbox) {
                if ((!data.locations || data.locations.length == 0)) {
                    am.AlertDialog.show(am.locale.errors.locations);
                    callback.error();
                    return;
                }

                data.nwatch = this.selectedCheckbox.value;
                data.from = this.formatDate(this.dates[0]);
                data.to = this.formatDate(this.dates[1]);
                data.dateType = $(".datesFilterSelect").val();
                data.nwatchtype =  $("input:radio[name ='nwtype']:checked").val();
            }
            data.methods += 'nwatch,';
            callback.add();
        }
    },

    applyFinished: function() {
        this.hasChanged = false;
        this.mapsManager.layerVisibilityUpdated(this.map.kmsLayer, this.map.container.id);
    },

    formatDate: function(el){
        return $.datepicker.formatDate('yymmdd', $(el).datepicker('getDate'));
    },

    selectedLocationChanged: function() {
        this.hasChanged = true;
    },
    
    needChange:function(){
        if(this.isActive()){
            this.changed();
        }
    },    
    
    isActive: function(){
        return $("#nwatchDiv input[type=checkbox]:checked").length > 0;
    }
};