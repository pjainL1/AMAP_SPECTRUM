am.Report = function(options) {
    korem.apply(this, options);
    this.init();
}

am.Report.prototype = {

    parent: null,
    applyBtn: null,
    map: null,
    mapsManager: null,
    dates: null,

    window: null,
    selectedCheckbox: null,
    hasChanged: false,

    init: function() {
        this.createDescWindow();
        this.handleDescBtn();
        this.handleCheckboxes();
    },

    setMap: function(mapObj) {
        this.map = mapObj
    },

    handleCheckboxes: function() {
        var that = this;
        var checkboxes = [
        korem.get('reportChkDistanceDecay'),
        korem.get('reportChkLocationSummary')
        ];
        for (var i = 0; i < checkboxes.length; ++i) {
            checkboxes[i].onclick = function() {
                //if (that.selectedCheckbox == this) {
                //    that.selectedCheckbox = null;
                //} else {
                //    if (that.selectedCheckbox != null) {
                //        that.selectedCheckbox.checked = false;
                //    }
                //    that.selectedCheckbox = this;
                //}
                that.changed();
            }
            checkboxes[i].checked = false;
        }
    },

    handleDescBtn: function() {
        var that = this;
        $('#reportDescBtn').click(function() {
            that.window.dialog('open');
        });
    },
    
    createDescWindow: function() {
        this.window = $('#reportDescWindow').dialog({
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
        //this.applyBtn.needed();
        EnableDisableBtns();
    },

    populateData: function(data, callback) {
        if (this.hasChanged && this.selectedCheckbox) {
            if (this.selectedCheckbox.value == 'locations' && (!data.locations || data.locations.length == 0)) {
                am.AlertDialog.show(am.locale.errors.locations);
                callback.error();
                return;
            }
            data.methods += 'report,';
            data.report = this.selectedCheckbox.value;
            data.from = this.formatDate(this.dates[0]);
            data.to = this.formatDate(this.dates[1]);
            callback.add();
        }
    },

    applyFinished: function() {
        this.hasChanged = false;
        this.mapsManager.layerVisibilityUpdated(this.map.kmsLayer);
    },

    formatDate: function(el){
        return $.datepicker.formatDate('yymmdd', $(el).datepicker('getDate'));
    }
}