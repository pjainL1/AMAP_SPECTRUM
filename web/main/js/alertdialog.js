am.AlertDialog = {
    defaultTitle: am.locale.errors.title,
    dialog: null,
    show: function(msg, title, options, callback) {
        this.dialog = $(document.body.appendChild(html.div()));
        var overlay;
        am.AlertDialog.show = function(msg, title, options, callback) {
            am.instance.enterKeyEnabled = false;
            this.dialog[0].innerHTML = msg;
            this.dialog.dialog(korem.apply({
                modal: true,
                minHeight: 0,
                resizable: false,
                title: title || this.defaultTitle,
                buttons: {
                    Ok: function() {
                        $(this).dialog('close');
                        if (callback instanceof Function){
                            callback();
                        }
                    }
                },
                open: function() {
                    if (!overlay) {
                        overlay = $('.ui-widget-overlay');
                    }
                    overlay.css({
                        width: '',
                        height: ''
                    });
                },
                close: function() {
                    setTimeout(function(){
                        am.instance.enterKeyEnabled = true;
                    }, 1000);
                }
            },options));
        };
        am.AlertDialog.show.apply(this, [msg, title, options, callback]);
    }
};
