Ext.define('AMAP.controller.layergroup', {
    extend: 'Ext.app.Controller',
    stores: ['layergroup'],
    models: ['layergroup'],
    views: ['layergroup'],
    
    refs: [
        {
            ref: 'treePanel',
            selector: 'layergroup'
        },
        {
            ref: 'sponsorSelector',
            selector: "layergroup component[name='sponsorSelector']"
        }
    ],
    
    init: function() {
        this.control({
            'layergroup > treeview': {
                beforedrop: this.beforeNodeDropped,
                drop: this.nodeDropped
            },
            'layergroup': {
                render: this.onRender,
                nodeedit: this.renameLayerGroup,
                nodedelete: this.deleteLayerGroup
            },
            "layergroup component[name='addGroupBtn']": {
                click: this.addGroup
            },
            "layergroup component[name='sponsorSelector']": {
                select: this.sponsorSelected
            }
        }); 
    },
    
    onRender: function(cmp) {
        var store = this.getSponsorSelector().getStore();
        
        store.load({
            scope: this,
            callback: function() {
                var recordSelected = store.getAt(0);
                var selector = this.getSponsorSelector();
                selector.setValue(recordSelected.get('id'));
                selector.fireEvent('select', selector, [recordSelected]);
                
                korem.timeout(function() {
                    this.overrideNodeOverBehavior(cmp);
                }, 0, this);
                
            }
        });
    },
    
    overrideNodeOverBehavior: function(cmp) {
        var dz = cmp.getView().getPlugin().dropZone;
        var unpatched = dz.isValidDropPoint;
        dz.isValidDropPoint = function(node, position, dragZone, e, data) {
            var isValid = unpatched.apply(this, arguments);
            var view = this.view;
            var targetNode = view.getRecord(node);
            var draggedRecord = data.records[0];
            
            if (isValid) {
                var draggingGroup = !draggedRecord.isLeaf();
                var toFirstLevel = targetNode.parentNode && targetNode.parentNode.isRoot();
                
                if (draggingGroup && !(position != 'append' && toFirstLevel)) {
                    //console.log('refused: group inside group');
                    return false;
                }
                if (draggedRecord.data.isOther) {
                    //console.log('refused: isOther');
                    return false;
                }
                if (draggedRecord.isLeaf() && (toFirstLevel && position != 'append')) {
                    //console.log('refused: leaf to root');
                    return false;
                }
                
                return true;
            }
            
            return isValid;
        };
    },
    
    sponsorSelected: function(combo, record, index) {
        var treeStore = this.getTreePanel().getStore();
        treeStore.getProxy().setExtraParam('sponsor', record[0].get('id'));
        treeStore.load();
    },
    
    beforeNodeDropped: function(node, data, overModel, dropPosition, dropFunction, eOpts) {
        var isOverRoot = overModel.parentNode.data.root;
        var isOverLeaf = overModel.data.leaf;
        
        if (data.records[0].data.isOther) {
            return false;
        }
        
        if (data.records[0].data.leaf) {
            if (dropPosition === "append") {
                return !isOverLeaf && 
                        data.records[0].parentNode.data.groupId != overModel.data.groupId && 
                        isOverRoot;
            }
            return overModel.data.leaf;
        }
        
        // it's a group. only allow reorder.
        return dropPosition !== "append" && 
                !isOverLeaf && 
                isOverRoot;
    },
    
    nodeDropped: function(node, data, overModel, dropPosition, eOpts) {
        var hierarchy = [];
        var store = this.getTreePanel().getStore();
        var groupCount = 0, layerCount = 0;
        var groups = store.getRootNode().childNodes;
        for (var gIdx in groups) {
            hierarchy.push({
                isLayer: false,
                groupId: groups[gIdx].data.groupId,
                oldGroupId: groups[gIdx].data.groupId,
                index: groupCount++,
                name: groups[gIdx].data.text,
                sponsor: groups[gIdx].data.sponsor
            });
            layerCount = 0;
            var layers = groups[gIdx].childNodes;
            for (var lIdx in layers) {
                hierarchy.push({
                    isLayer: true,
                    groupId: groups[gIdx].data.groupId,
                    oldGroupId: layers[lIdx].data.groupId,
                    index: layerCount++,
                    name: layers[lIdx].data.realText,
                    sponsor: layers[lIdx].data.sponsor
                });
            }
        }
        
        this.moveLayerGroup(hierarchy);
    },
    moveLayerGroup: function(hierarchy) {
        Ext.Ajax.request({
            url: '../console/MoveLayerGroup.safe',
            scope: this,
            params: {
                hierarchy: Ext.JSON.encode(hierarchy)
            },
            success: function(response, opts) {
                var json = response.responseText ? Ext.JSON.decode(response.responseText) : {};
                if (!json.success) {
                    alert("MoveLayerGroup failed.");
                }
            },
            failure: function(response, opts) {
                alert("Move failure, status: "+ response.status);
            }
        });
    },
    renameLayerGroup: function(rowIndex, colIndex, actionItem, event, record, row) {
        Ext.MessageBox.prompt(am.locale.console.layerGroup.editTitle, am.locale.console.layerGroup.editMsg, function(btn, text) {
            if (btn === 'ok') {
                if ($.trim(text).length > 0) {
                    this.doRenameLayerGroup(record, text);
                } else {
                    Ext.MessageBox.show({
                        title: am.locale.console.layerGroup.invalidTitle,
                        msg: am.locale.console.layerGroup.invalidMsg,
                        buttons: Ext.MessageBox.OK,
                        icon: Ext.MessageBox.INFO
                    });
                }
            }
        }, this, false, record.get('text'));
    },
    doRenameLayerGroup: function(record, text) {
        Ext.Ajax.request({
            url: 'RenameLayerGroup.safe',
            params: {
                "groupId": record.get('groupId'),
                "groupName": text
            },
            scope: this,
            success: function(response, opts) {
                var json = response.responseText ? Ext.JSON.decode(response.responseText) : {};
                if (json.success) {
                    record.set('text', text);
                    record.set('realText', text);
                    this.getTreePanel().getStore().sync();
                }else{
                    alert("RenameLayerGroup failed.");
                }
            },
            failure: function(response, opts) {
                alert("Rename failure, status: "+ response.status);
            }
        });
    },
    deleteLayerGroup: function(rowIndex, colIndex, actionItem, event, record, row) {
        var msg = korem.format(am.locale.console.layerGroup.confirmDelete, record.get('text'));
        Ext.MessageBox.confirm(am.locale.console.layerGroup.confirm, msg, function(btn) {
            if (btn === 'yes') {
                this.doDeleteLayerGroup(record);
                this.getTreePanel().getStore().load();
            }
        }, this);
    },
    doDeleteLayerGroup: function(record) {
        Ext.Ajax.request({
            url: 'DeleteLayerGroup.safe',
            params: {
                "groupId": record.get('groupId')
            },
            scope: this,
            success: function(response, opts) {
                var json = response.responseText ? Ext.JSON.decode(response.responseText) : {};
                if (json.success) {
                    record.remove();
                }else{
                    alert("DeleteLayerGroup failed.");
                }
            },
            failure: function(response, opts) {
                alert("Delete failure, status: "+ response.status);
            }
        });
    },
    
    addGroup: function() {
        Ext.MessageBox.prompt(am.locale.console.layerGroup.addPromptTitle, am.locale.console.layerGroup.addPromptLabel, function(btn, text) {
            if (btn === 'ok') {
                if ($.trim(text).length > 0) {
                    this.doAddGroup(text);
                } else {
                    Ext.MessageBox.show({
                        title: am.locale.console.layerGroup.invalidTitle,
                        msg: am.locale.console.layerGroup.invalidMsg,
                        buttons: Ext.MessageBox.OK,
                        icon: Ext.MessageBox.INFO
                    });
                }
            }
        }, this, false, '');
    },
    
    doAddGroup: function(groupName) {
        Ext.Ajax.request({
            url: 'AddLayerGroup.safe',
            params: {
                "groupName": groupName,
                "sponsor": this.getSponsorSelector().getValue()
            },
            scope: this,
            success: function(response, opts) {
                this.getTreePanel().getStore().load();
            },
            failure: function(response, opts) {
                alert("Add failure, status: "+ response.status);
            }
        });
    }
});
