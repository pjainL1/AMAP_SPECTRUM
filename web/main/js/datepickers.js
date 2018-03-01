am.DataPickers = function (options) {
    korem.apply(this, options);
    this.init();
}

am.DataPickers.masterToSlaveMap = {
    fromPicker: 'fromSlavePicker',
    toPicker: 'toSlavePicker',
    comparisonFromPicker: 'comparisonFromSlavePicker',
    comparisonToPicker: 'comparisonToSlavePicker'
},
am.DataPickers.prototype.defaultDateType = 'single',
        am.DataPickers.prototype = {
            defaultFromDate: '',
            defaultToDate: '',
            dates: null,
            slaveDates: null,
            applyBtn: null,
            selectionTool: null,
            layerControl: null,
            storeLevelAnalysis: null,
            hasChanged: true,
            slaveDatepickers: null,
            comparisonSlaveDatepickers: null,
            comparisonDatepickers: null,
            datesDropdown: null,
            map: null,
            mapsManager: null,
            selectedDateType: 'single',
            hotspot: null,
            init: function () {
                this.slaveDatepickers = korem.get('slaveDatepickers');
                this.comparisonSlaveDatepickers = korem.get('comparisonSlaveDatepickers');
                this.hideSlaves();
                this.createDatePickers();
                this.createSlaveDatePickers();
                this.restrict();
                this.setDefaultButton();
                this.initDatesDropDown();
                this.initDataFilteringBtn();
            },
            createDescWindow: function () {
                this.window = $('#dataFilteringDescWindow').dialog({
                    zIndex: 7000,
                    autoOpen: false,
                    resizable: false,
                    width: 500,
                    height: 500,
                    close: function () {
                    },
                    open: function () {
                    }
                });
            },
            initDataFilteringBtn: function () {
                this.createDescWindow();
                $('#dataFilteringBtn').click(korem.callback(this, function () {
                    this.window.dialog('open');
                    this.window.height(300).dialog('option', 'position', 'center');
                }));
            },
            initDatesDropDown: function () {
                this.datesDropdown = $('#datesFilterTypesSelect');
                var that = this;
                that.hideComparisonDates();

                var changeHandler = function (event) {
                    if (event.target.value === 'single') {
                        this.hideComparisonDates();
                    } else {
                        this.showComparisonDates();
                    }
                    this.hasChanged = true;
                    this.selectedDateType = event.target.value;
                    this.toggleActiveDotButton(true);
                    this.applyBtn.forceNeeded();
                    this.storeLevelAnalysis.onDateTypeChange(event.target.value);

                    $('#datesFilterTypesSelect').trigger("datepickerchanges");
                };
                this.datesDropdown.on("change", $.proxy(changeHandler, this));
            },
            hideComparisonDates: function () {
                $('.comparisonDates').hide();
                $('.periodCaption')[0].innerHTML = am.locale.datePickers.period;
            },
            showComparisonDates: function () {
                $('.comparisonDates').show();
                $('.periodCaption')[0].innerHTML = am.locale.datePickers.periodOne;
            },
            setDefaultButton: function () {
                $('#defaultBtnCenter').click($.proxy(function () {
                    $('#fromPicker').datepicker('setDate', this.defaultFromDate);
                    $('#toPicker').datepicker('setDate', this.defaultToDate);
                    $('#comparisonFromPicker').datepicker('setDate', this.defaultComparisonFromDate);
                    $('#comparisonToPicker').datepicker('setDate', this.defaultComparisonToDate);                
                    this.applyBtn.needed();
                    this.onDateSelected();
                }, this));

            },
            saveDefaultDates: function () {
                var defaultFrom = document.getElementById('fromPicker').value;
                var defaultTo = document.getElementById('toPicker').value;
                var defaultComparisonFrom = document.getElementById('comparisonFromPicker').value;
                var defaultComparisonTo = document.getElementById('comparisonToPicker').value;

                am.DataPickers.prototype.defaultFromDate = defaultFrom;
                am.DataPickers.prototype.defaultToDate = defaultTo;
                am.DataPickers.prototype.defaultComparisonFromDate = defaultComparisonFrom;
                am.DataPickers.prototype.defaultComparisonToDate = defaultComparisonTo;
                am.DataPickers.prototype.defaultDateType = this.selectedDateType

            },
            setMap: function (map, init) {
                this.map = map;
                if (!init) {
                    this.changed();
                }
            },
            setMapManager: function (mapManager) {
                this.mapsManager = mapManager;
            },
            setHotspot: function (hotspot) {
                this.hotspot = hotspot;
            },
            onDateSelected: function (selectedDate) {
                var that = this;

                var data = {
                    methods: ''
                };
                that.hasChanged = true;
                that.populateData(data);
                that.hasChanged = false;
                that.toggleActiveDotButton(true);

                that.applyBtn.doApply(data, function () {
                    $.ajax({
                        url: '../removeAnalysis.safe',
                        context: that,
                        type: 'post',
                        data: {
                            mapInstanceKey: that.map.mapInstanceKey
                        },
                        success: function () {
                            $.ajax({
                                url: '../updateLocations.safe',
                                context: that,
                                type: 'post',
                                data: {
                                    mapInstanceKey: that.map.mapInstanceKey,
                                    from: that.formatDate($(that.dates[0]).datepicker('getDate')),
                                    to: that.formatDate($(that.dates[1]).datepicker('getDate')),
                                    r: Math.random()
                                },
                                success: function () {
                                    that.applyBtn.forceNeeded();
                                    that.applyBtn.setIsApplying(false);
                                    that.layerControl.updateLayers();
                                    that.mapsManager.layerVisibilityUpdated(that.map.kmsLayer);
                                    that.selectionTool.redoSelection();
                                    that.hotspot.valuesChanged();
                                }
                            });
                        }
                    });
                });
            },
            toggleActiveDotButton: function (isDifferent) {
                var dotButton = $('#filterH3 > div');
                (isDifferent) ? dotButton.removeClass("buttonDotOff").addClass("buttonDotOn") : dotButton.removeClass("buttonDotOn").addClass("buttonDotOff")
            },
            createDatePickers: function () {
                var that = this;

                this.dates = $('#fromPicker, #toPicker, #comparisonFromPicker, #comparisonToPicker').datepicker({
                    changeMonth: true,
                    changeYear: true,
                    showOn: 'both',
                    buttonImage: '../main/images/calendar.gif',
                    buttonImageOnly: true,
                    showAnim: '',
                    onChangeMonthYear: function () {

                    },
                    onSelect: function (selectedDate) {
                        that.applyBtn.setIsApplying(true);
                        var option = (this.id.indexOf('fromPicker') > -1 || this.id.indexOf('FromPicker') > -1) ? 'minDate' : 'maxDate';
                        var basicDatePicker, comparisonDatePicker;
                        if (option == 'minDate') {
                            basicDatePicker = document.getElementById('fromPicker');
                            comparisonDatePicker = document.getElementById('comparisonFromPicker');
                        } else {
                            basicDatePicker = document.getElementById('toPicker');
                            comparisonDatePicker = document.getElementById('comparisonToPicker');
                        }


                        var instance = $(this).data('datepicker');
                        var date = $.datepicker.parseDate(instance.settings.dateFormat || $.datepicker._defaults.dateFormat, selectedDate, instance.settings);

                        if (this.id == 'fromPicker') {
                            that.dates.not(this).not(document.getElementById('comparisonFromPicker')).not(document.getElementById('comparisonToPicker')).datepicker('option', option, date);
                        }
                        if (this.id == 'comparisonFromPicker') {
                            that.dates.not(this).not(document.getElementById('fromPicker')).not(document.getElementById('toPicker')).datepicker('option', option, date);
                        }
                        if (this.id == 'toPicker') {
                            that.dates.not(this).not(document.getElementById('comparisonFromPicker')).not(document.getElementById('comparisonToPicker')).datepicker('option', option, date);
                        }
                        if (this.id == 'comparisonToPicker') {
                            that.dates.not(this).not(document.getElementById('fromPicker')).not(document.getElementById('toPicker')).datepicker('option', option, date);
                        }

                        that.onDateSelected(selectedDate);
                    },
                    beforeShow: function (input, inst) {
                        if (input.id.indexOf('ToPicker') > -1 || input.id.indexOf('toPicker') > -1) {
                            $('#streetViewControlContainer').hide();
                            $('#compareBtn').hide();
                            $('#dashboardToggler').hide();
                        }

                    },
                    onClose: function () {
                        $('#streetViewControlContainer').show();
                        $('#compareBtn').show();
                        $('#dashboardToggler').show();
                    }
                });
                this.dates[0].readOnly = true;
                this.dates[1].readOnly = true;
                this.dates[2].readOnly = true;
                this.dates[3].readOnly = true;
            },
            formatDate: function (date, formatDate) {
                return $.datepicker.formatDate(formatDate ? formatDate : $.datepicker._defaults.dateFormat, date);
            },
            updateSlaves: function () {
                var that = this;
                this.dates.each(function (i, date) {
                    var current = am.DataPickers.masterToSlaveMap[date.id]
                    for (var ii = 0; ii < that.slaveDates.length; ii++) {
                        if (that.slaveDates[ii].id == current) {
                            $(that.slaveDates[ii]).datepicker('setDate', $(date).datepicker('getDate'));
                            break;
                        }
                    }
                });
                this.slaveDatepickers.style.display = '';
                if (this.selectedDateType !== 'single') {
                    this.slaveDatepickers.style.borderBottomWidth = '0';
                    this.comparisonSlaveDatepickers.style.display = '';
                    this.slaveDatepickers.style.bottom = '';
                } else {
                    this.slaveDatepickers.style.borderBottomWidth = '1px';
                    this.comparisonSlaveDatepickers.style.display = 'none';
                    this.slaveDatepickers.style.bottom = $(this.comparisonSlaveDatepickers).css("bottom");
                }
            },
            hideSlaves: function () {
                this.slaveDatepickers.style.display = 'none';
                this.comparisonSlaveDatepickers.style.display = 'none';
            },
            createSlaveDatePickers: function () {
                this.slaveDates = $('#fromSlavePicker, #toSlavePicker, #comparisonFromSlavePicker, #comparisonToSlavePicker').datepicker({
                    showOn: 'both',
                    buttonImage: '../main/images/calendar.gif',
                    buttonImageOnly: true
                });
                this.slaveDates.each(function (i, date) {
                    $(date).datepicker('disable');
                });
            },
            restrict: function () {
                var that = this;
                $.ajax({
                    url: '../getUsefulDates.safe',
                    context: this,
                    type: 'post',
                    success: function (data) {
                        if (data.minDateLimit) {
                            $(this.dates[0]).datepicker('option', 'minDate', data.minDateLimit);
                            $(this.dates[1]).datepicker('option', 'minDate', data.minDateLimit);
                            $(this.dates[2]).datepicker('option', 'minDate', data.minDateLimit);
                            $(this.dates[3]).datepicker('option', 'minDate', data.minDateLimit);
                        }
                        if (data.maxDateLimit) {
                            $(this.dates[0]).datepicker('option', 'maxDate', data.maxDateLimit);
                            $(this.dates[1]).datepicker('option', 'maxDate', data.maxDateLimit);
                            $(this.dates[2]).datepicker('option', 'maxDate', data.maxDateLimit);
                            $(this.dates[3]).datepicker('option', 'maxDate', data.maxDateLimit);
                        }

                        $(this.dates[0]).datepicker('setDate', data.minDate);
                        $(this.dates[1]).datepicker('setDate', data.maxDate);


                        var comparisonMinDate = new Date(data.minDate);
                        var comparisonMaxDate = new Date(data.maxDate);

                        //comparison dates are one year less than the first ones
                        $(this.dates[2]).datepicker('setDate', (comparisonMinDate.getMonth() + 1) + '/' + comparisonMinDate.getDate() + '/' + (comparisonMinDate.getFullYear() - 1));
                        $(this.dates[3]).datepicker('setDate', (comparisonMaxDate.getMonth() + 1) + '/' + comparisonMaxDate.getDate() + '/' + (comparisonMaxDate.getFullYear() - 1));

                        that.saveDefaultDates();
                    }
                });
            },
            changed: function () {
                this.hasChanged = true;
                this.applyBtn.needed();
                $(document).trigger("updateDataFiltering");
            },
            populateData: function (data, callback) {
                data.startDate = this.formatDate($(this.dates[0]).datepicker('getDate'));
                data.endDate = this.formatDate($(this.dates[1]).datepicker('getDate'));
                data.comparisonStartDate = this.formatDate($(this.dates[2]).datepicker('getDate'));
                data.comparisonEndDate = this.formatDate($(this.dates[3]).datepicker('getDate'));
                data.timeType = this.selectedDateType;
                data.mapInstanceKey = this.map.mapInstanceKey;
                data.from = this.formatDate($(this.dates[0]).datepicker('getDate'), "yymmdd");
                data.to = this.formatDate($(this.dates[1]).datepicker('getDate'), "yymmdd");
                data.compareFrom = this.formatDate($(this.dates[2]).datepicker('getDate'), "yymmdd");
                data.compareTo = this.formatDate($(this.dates[3]).datepicker('getDate'), "yymmdd");
                if (this.hasChanged) {
                    data.methods += 'setDates,';
                    if (callback) {
                        callback.add();
                    }
                }
            },
            applyFinished: function () {
                this.hasChanged = false;
            },
            updateLocations: function (masterObj, callback) {
                var that = this;
                $.ajax({
                    url: '../updateLocations.safe',
                    context: that,
                    type: 'post',
                    data: {
                        mapInstanceKey: masterObj.mapInstanceKey,
                        from: that.formatDate($(that.dates[0]).datepicker('getDate')),
                        to: that.formatDate($(that.dates[1]).datepicker('getDate')),
                        r: Math.random()
                    },
                    success: function () {
                        if (callback) {
                            callback();
                        }
                    }
                });
            }
        };