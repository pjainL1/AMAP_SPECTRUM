am.StoreLevelAnalysis = function (options) {
    korem.apply(this, options);
    this.init();
};
am.StoreLevelAnalysis.prototype = {
    applyBtn: null,
    map: null,
    abbr: 'SLA.',
    mapsManager: null,
    dates: null,
    storeLevelAnalysisMapping: {
        slaStfTotalCollectors : "collectors",
        slaStfTotalSales : "spend",
        slaStfTotalTransactions : "transactions",
        slaStfTotalUnit : "units",
        slaCaTotalCollectors : "collectorsComparison",
        slaCaTotalSales : "spendComparison",
        slaCaTotalTransactions : "transactionsComparison",
        slaCaTotalUnit : "unitsComparison"
    },    
    layerControl: null,
    selectedCheckbox: null,
    selectedMode: null,
    updateLayerWhenApplied: true,
    init: function () {
        this.createDescWindow();
        this.handleDescBtn();
        this.handleCheckboxes();
        this.createAttributesWindow();
        this.rangeInputsValidation();
        $(document).on("updateDataFiltering", korem.callback(this, this.needChange));
    },
    createDescWindow: function () {
        this.window = $('#slaDescWindow').dialog({
            zIndex: 7000,
            autoOpen: false,
            resizable: false,
            width: 450,
            close: function () {
            },
            open: function () {
            }
        });
    },
    createAttributesWindow: function () {
        for (var i = 0; i < this.checkboxes.length; i++) {
            var customRangeBtn = $('#'+this.checkboxes[i].id).parent().next();
            var that = this;
            $(customRangeBtn).click(function () {
                if (!$(this.parentElement).find('input').prop('checked')){
                    return;
                }
                if (that.openDialogOpen) {
                    $("#rangeAttributesWindow").dialog('close');
                }

                $('#rangeAttributesWindow').dialog(korem.apply({
                    autoOpen: false,
                    position: [$(this).offset().left+55, $(this).offset().top-153],
                    width: 253,
                    title:am.locale.storeLevelAnalysis.thematicConfiguration,
                    height: 320,                    
                    resizable: false,
                    hide: { effect: "fade", duration: 200 },
                    draggable: false,
                    dialogClass: "arrowLeft-dialog",
                    buttons: [
                        {
                            text: "Save",
                            id: "modalSaveBtn",
                            click: function(){            
                                that.saveAttributes();
                                
                            }
                        }, 
                        {
                            text: "Cancel",
                            id: "modalCancelBtn",
                            click: function(){            
                                $(this).dialog('close');
                            }
                        }                        

                    ],
                    close : function(){
                      return function(){
                          that.closeDialog();
                      };
                    }(),
                    open : function(me){
                      return function(){
                        $('#selectedAnalysis').val(that.selectedAnalysis);
                        $('#rangeAttributesTitle').text(am.locale.storeLevelAnalysis.attributeTitles[that.storeLevelAnalysisMapping[that.selectedAnalysis]]); 

                        $('#rangeAttributesWindowHook').empty();  
                        that.createContent(that);
 
                      };
                    }(this)
                })); 
                if (that.selectedAnalysis){
                    that.opendialog = $("#rangeAttributesWindow").dialog('open');
                    that.openDialogOpen = true;
                }
            });
        }
        
        $(document).bind("closeModal datepickerchanges", function(){
            that.closeDialog();
        });
        $(document).bind("openModal", function(evt, modalId){
            
        });
        
        
        
    },

    rangeInputsValidation: function(rangeInput){
        var that = this;
        $('body').on('keyup', '.rangeInput', function(evt) {
            that.changed();
            that.greenDotBtn.removeClass("buttonDotOff").addClass("buttonDotOn");
            if (this.value.match(/^(0|[-][0-9]*|[1-9][0-9]*)$/) || this.value === ""){
                this.defaultValue =this.value;
            } else {
                this.value = this.defaultValue;
            }            
            //check if all inputs contain values
            var empty = that.isOneRangeInputEmpty();
            if(empty.length) {
                that.enableModalSaveButton(false);
            } else {
                that.enableModalSaveButton(true);
            }            
            
        });

        
    },
    isOneRangeInputEmpty: function(){
        //check if all inputs contain values
        var empty = $('#rangeAttributesWindowHook .rangeInput').filter(function() {
                return this.value === "";
        }); 
        return empty;
    },
    createContent: function(ctx){
        var valuesSla = am.user.attributes[0][ctx.abbr+ctx.storeLevelAnalysisMapping[ctx.selectedAnalysis]] || new Array(8).join();
        var isActiveSla = false;
        try{
            isActiveSla = JSON.parse(am.user.attributes[0][ctx.abbr+ctx.storeLevelAnalysisMapping[ctx.selectedAnalysis]+'Active']);
        }catch(e){}
        
        var res = valuesSla.split(",");
        var techmaticConfigTable = document.getElementById('techmaticConfigTable');
        res.unshift(techmaticConfigTable.innerHTML);
        var trHTML =korem.format.apply(this, res);

        $('#rangeAttributesWindowHook').append('<p class="themeActiveState"><label><input class="themeActiveCheckBox" type="checkbox" id="'+this.storeLevelAnalysisMapping[this.selectedAnalysis]+'Active" name="isThemeActive"></input>Active</label></p>'+trHTML);   
        this.initThemeCheckBox(isActiveSla);
        
        var empty = this.isOneRangeInputEmpty();
        if(empty.length){
            this.enableModalSaveButton(false);
        }         
    },
    initThemeCheckBox: function(state){
        var activeCheckBox = $('#'+this.storeLevelAnalysisMapping[this.selectedAnalysis]+'Active');
        var that = this;        
        activeCheckBox.prop('checked', state);
        if (state){
            this.disableRangeInputs(false);
            this.enableModalSaveButton(true);
        } else {
            this.disableRangeInputs(true);
        }
        activeCheckBox.click(function () {
            if (this.checked){
                that.disableRangeInputs(false);
            } else {
                that.disableRangeInputs(true);
            }
            var empty = that.isOneRangeInputEmpty();
                if(!empty.length){
                    that.enableModalSaveButton(true);
                } 
            that.changed();
            that.greenDotBtn.removeClass("buttonDotOff").addClass("buttonDotOn");
        });
    },
    disableRangeInputs: function(check){
        $('#rangeAttributesWindowHook .rangeInput').each(function(idx,elt){
            $(elt).prop("disabled", check);
        });
    },
    enableModalSaveButton: function(enable){
        if (enable){
            $('#modalSaveBtn').removeClass('ui-state-disabled');
            $('#modalSaveBtn').prop("disabled", false);
        } else {
            $('#modalSaveBtn').addClass('ui-state-disabled');
            $('#modalSaveBtn').prop("disabled", true);
        }
    },
    saveAttributes: function(){
        var ranges = [];
        var valid = true;
        $('#rangeAttributesWindow .rangeInput').each(function(idx, val, itSelf){
            var value = parseInt(val.value);
            if (!isNaN(value)) {
                this.style.backgroundColor = '#fff';
                ranges.push(value);
            } else {
                this.style.backgroundColor = '#fcc';
                valid = false;
            }
        });
        
        if (!valid) {
            return;
        }
        
        var sortingRule = function(a,b) {
            return b - a;
        };
        ranges = ranges.sort(sortingRule);
        
        am.user.attributes[0][this.abbr+this.storeLevelAnalysisMapping[this.selectedAnalysis]] = ranges.toString();
        am.user.attributes[0][this.abbr+this.storeLevelAnalysisMapping[this.selectedAnalysis]+'Active'] = ($('#'+this.storeLevelAnalysisMapping[this.selectedAnalysis]+'Active').prop('checked')).toString();
        
        var that = this;
        
        var sendToServer = function() {
            $.ajax({
                url: '../UpdateUserAttributes.safe',
                context: this,
                type: 'post',
                data: {
                    userAttributes: JSON.stringify(am.user.attributes[0])
                },
                beforeSend: function() {
                    that.spinner = that.mapsManager.mainInstance.loadingStateSpinner.spin();
                    $('#rangeAttributesWindow').append(that.spinner.el);
                },                
                success: function(json) {
                    that.spinner.stop();
                },
                complete: function() {
                    that.spinner.stop();
                     
                    $('#rangeAttributesWindowHook').empty();  
                    that.createContent(that); 
                    setTimeout(function(){ that.closeDialog(); }, 600);
                }                
            });
        };
        sendToServer();
    },
    closeDialog: function(){
        try{
            $("#rangeAttributesWindow").dialog('close');
            this.openDialogOpen = false;
        } catch(ex){ }
    },
    selectedLocationChanged: function () {
        this.hasChanged = true;
    },
    handleDescBtn: function () {
        var that = this;
        $('#slaDescBtn').click(function () {
            that.window.dialog('open');
        });
    },
    handleCheckboxes: function () {
        var that = this;
        var checkboxes = [
            korem.get('slaStfTotalCollectors'),
            korem.get('slaStfTotalSales'),
            korem.get('slaStfTotalTransactions'),
            korem.get('slaStfTotalUnit'),
            korem.get('slaCaTotalCollectors'),
            korem.get('slaCaTotalSales'),
            korem.get('slaCaTotalTransactions'),
            korem.get('slaCaTotalUnit')
        ];
        this.checkboxes = checkboxes;
        for (var i = 0; i < checkboxes.length; ++i) {
            checkboxes[i].onclick = function () {
                if (that.openDialogOpen) {
                    that.closeDialog();
                }
                if (that.selectedCheckbox === this) {
                    that.selectedCheckbox = null;
                } else {
                    if (that.selectedCheckbox !== null) {
                        that.selectedCheckbox.checked = false;
                    }
                    that.selectedCheckbox = this;
                }
                that.selectedAnalysis = event.target.id;
                that.changed();
                that.toggleThematicConfigIcon();
                if (!this.checked) {
                    that.selectedAnalysis = null;
                }
            };
            if(checkboxes[i].checked){
                this.greenDotBtn.removeClass("buttonDotOff").addClass("buttonDotOn");
            }
            checkboxes[i].checked = false;
        }
    },
    toggleThematicConfigIcon: function(){
        var thematicIcon = $('#'+event.target.id).parent().next().find('.customizeRangeBtn');
        var rangeBtnList = $('.customizeRangeBtnVisible');
        rangeBtnList.each(function(idx, elt){
            $(elt).switchClass( 'customizeRangeBtnVisible', 'customizeRangeBtn' , 10  , 'swing');
        });
        thematicIcon.toggleClass( "customizeRangeBtnVisible", event.target.checked);
    },    
    changed: function () {
        this.hasChanged = true;
        this.applyBtn.needed();
    },
    populateData: function (data, callback) {
        if (this.hasChanged) {
            if (this.selectedCheckbox) {
                data.dateType = this.getSelectedDateType();
                data.slaTransactionValue = this.selectedCheckbox.value;
            }
            data.methods += 'storeLevelAnalysis,';
            callback.add();
            
        }
    },
    applyFinished: function () {
        this.hasChanged = this.isActive();
        this.mapsManager.layerVisibilityUpdated(this.map.kmsLayer, this.map.container.id);
    },
    getSelectedDateType: function () {
        return am.instance.datePickers.selectedDateType;
    },
    onDateTypeChange: function (dateType) {
        this.selectedCheckbox = null;
        this.refreshDialog(dateType);
        this.handleCheckboxes();
        $('.customizeRangeBtnVisible').each(function(idx, elt){
            $(elt).switchClass( 'customizeRangeBtnVisible', 'customizeRangeBtn' , 50  , 'swing');
        });
    },
    refreshDialog: function (dateType) {
        var singleDiv = $("#slaStf");
        var comparisonDiv = $("#slaCa");
        if (dateType === "single") {
            singleDiv.show();
            comparisonDiv.hide();
        } else {
            singleDiv.hide();
            comparisonDiv.show();
        }
    },
    
    needChange:function(){
        if(this.isActive()){
            this.changed();
        }
    },    
    isActive: function(){
        return $("#storeLevelAnalysisDiv input[type=checkbox]:checked").length > 0;
    },
    hasInfoTextActive: function(){
        return this.mapsManager.mainInstance.minumumValues.hasMinCollector() && korem.get('slaCaTotalCollectors').checked;
    }
};

