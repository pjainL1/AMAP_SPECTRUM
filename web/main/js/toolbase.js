am.ToolBase = function(options) {
    korem.apply(this, options);
    this.init();
}

am.ToolBase.prototype = {

    toolGroups: {},

    btnId: null,
    groupId: null,

    activated: false,
    button: null,

    init: function() {
        this.newButton();
        this.initButton();
        this.initialize();
    },
    
    getGroup: function() {
        var group = this.toolGroups[this.groupId];
        if (!group) {
            this.toolGroups[this.groupId] = group = [];
        }
        return group;
    },

    newButton: function() {
        this.getGroup().push(this);
    },

    makeExclusive: function() {
        var group = this.getGroup();
        for (var i = 0; i < group.length; ++i) {
            var tool = group[i];
            if (tool.activated) {
                tool.deactivate();
            }
        }
    },
    
    initButton: function() {
        var that = this;
        this.button = $('#' + this.btnId);
        this.button.click(function() {
            if (that.activated) {
                that.deactivate();
            } else {
                that.activate();
            }
            return false;
        });
    },

    activate: function() {
        this.makeExclusive();
        this.button.addClass('buttonActivated');
        this.doActivate();
        this.activated = true;
    },

    deactivate: function() {
        this.button.removeClass('buttonActivated');
        this.doDeactivate();
        this.activated = false;
    }
}