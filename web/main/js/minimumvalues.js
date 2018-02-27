am.MinimumValues = function (options) {
    korem.apply(this, options);
    this.init();
};

am.MinimumValues.prototype = {
    minTransactions: null,
    minSpend: null,
    minUnit: null,
    hasChanged: false,
    applyBtn: null,
    hotspot: null,
    init: function () {
        $('#minTrans').keyup($.proxy(this.validateMinTrans, this));
        $('#minSpend').keyup($.proxy(this.validateMinSpend,this));
        $('#minUnit').keyup($.proxy(this.validateMinUnit, this));
    },
    populateData: function (data, callback) {       
        data.methods += 'setMinimumValues,';
        data.minTransactions = this.minTransactions;
        data.minSpend = this.minSpend;
        data.minUnit = this.minUnit;
        callback.add();

    },
    validateMinTrans: function (eventDat) {
        this.validateNumber(eventDat);
        if (eventDat.currentTarget.value != "") {
            this.minTransactions = parseInt(eventDat.currentTarget.value);
        } else {
            this.minTransactions = null;
        }
        this.toggleActiveDotButton(true);
        this.changed();
    },
    validateMinSpend: function (eventDat) {
        this.validateNumber(eventDat);
        if (eventDat.currentTarget.value != "") {
            this.minSpend = parseInt(eventDat.currentTarget.value);
        } else {
            this.minSpend = null;
        }
        this.toggleActiveDotButton(true);
        this.changed();
    },
    validateMinUnit: function (eventDat) {
        this.validateNumber(eventDat);
        if (eventDat.currentTarget.value != "") {
           this.minUnit = parseInt(eventDat.currentTarget.value);
        } else {
            this.minUnit = null;
        }
        this.toggleActiveDotButton(true);
        this.changed();
    },
    validateNumber: function (element) {
        element.currentTarget.value = element.currentTarget.value.replace(/[^0-9\.]/g, '');
        if (parseInt(element.currentTarget.value) === 0) {
            element.currentTarget.value = "";
        }
    },
    applyFinished: function () {
        this.toggleActiveDotButton(false);
        this.hasChanged = false;
    },
    toggleActiveDotButton: function (isChanged) {
        var dotButton = $('#filterH3 > div');
        (isChanged) ? dotButton.removeClass("buttonDotOff").addClass("buttonDotOn") : dotButton.removeClass("buttonDotOn").addClass("buttonDotOff");
    },
    onValuesChanged: function(){},
    changed: function () {
        this.hasChanged = true;
        this.onValuesChanged();
        this.applyBtn.needed();
        $(document).trigger("updateDataFiltering");
    },
    setHotspot: function(hotspot) {
        this.hotspot = hotspot;
    },
    hasMinCollector: function(){
        return $('#minTrans').val() != "" || $('#minSpend').val() != "" || $('#minUnit').val() != "";
    }
};