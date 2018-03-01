korem = {

    WEBKIT_ANIMATION: '-webkit-animation',
    WEBKIT_TRANSFORM: '-webkit-transform',
    WEBKIT_ANIMATION_END: 'webkitAnimationEnd',
    FADE_ITE: 10,
    EMPTY_FNC: function() {},
    postInitListeners: [],

    registerForPostInit: function(listener) {
        korem.postInitListeners.push(listener);
    },

    doPostInit: function() {
        for (var i = 0; i < korem.postInitListeners.length; ++i) {
            korem.postInitListeners[i]();
        }
    },

    apply: function(dst, src) {
        if (dst && src) {
            for (var key in src) {
                dst[key] = src[key];
            }
        }
        return dst;
    },

    applyIf: function(dst, src) {
        if (dst && src) {
            for (var key in src) {
                if (!dst[key]) {
                    dst[key] = src[key];
                }
            }
        }
        return dst;
    },

    /**
     * Useful to select text with the user clicks on it.
     */
    selectOnClick: function(el) {
        if (document.selection) {
            korem.ieSelectOnClick(el);
        } else if (window.getSelection) {
            korem.mozillaSelectOnClick(el);
        }
    },

    ieSelectOnClick: function(el) {
        document.selection.empty();
        var range = document.body.createTextRange();
        range.moveToElementText(el);
        range.select();
    },

    mozillaSelectOnClick: function(el) {
        window.getSelection().removeAllRanges();
        var range = document.createRange();
        range.selectNode(el);
        window.getSelection().addRange(range);
    },

    get: function(id) {
        return document.getElementById(id);
    },

    fadeBackground: function(el, time, callback) {
        var waitTime;
        var waitTimeStep = waitTime = time / korem.FADE_ITE;
        var color = 255;
        var colorStep = color / korem.FADE_ITE;
        while (waitTime < time) {
            korem.willFadeBackground(el, waitTime += waitTimeStep, color -= colorStep);
        }
        window.setTimeout(function() {
            el.style.backgroundColor = '';
            if (callback != null) {
                callback();
            }
        }, waitTime += waitTimeStep);
    },

    willFadeBackground: function(el, waitTime, color) {
        window.setTimeout(function() {
            el.style.backgroundColor = color.toString() + color.toString() + color.toString();
        }, waitTime);
    },

    getTimeAsString: function() {
        return korem.getTimeAsStringFromObj(new Date());
    },

    getTimeAsStringFromObj: function(date) {
        return date.getFullYear() + '/' +
            ((date.getMonth().toString().length == 1) ? '0' : '') + date.getMonth() + '/' +
            ((date.getDate().toString().length == 1) ? '0' : '') + date.getDate() + ' - ' + date.toLocaleTimeString();
    },

    createSlower: function(waitTime, callback) {
        var lastTime = 0;
        var timerIds = [];
        return function(obj) {
            var executedTime = new Date().getTime();
            var elapsedTime = executedTime - lastTime;
            if (elapsedTime >= waitTime) {
                lastTime = executedTime;
                callback(obj);
            } else {
                while (timerIds.length > 1) {
                    clearTimeout(timerIds.pop());
                }
                timerIds.push(setTimeout(function() {
                    timerIds.shift();
                    lastTime = new Date().getTime();
                    callback(obj);
                }, waitTime - elapsedTime));
            }
        }
    },

    onEnterKey: function(evt, callback) {
        var isNotIE = window.event == undefined;
        var keyId = (isNotIE) ? evt.which : window.event.keyCode;
        var target = (isNotIE) ? evt.target : window.event.srcElement;
        if (keyId == 13 || keyId == 10) {
            callback.apply(target);
        }
    },
    
    callback: function(scope, fn, myArguments) {
       return function() {
           return fn.apply(scope, myArguments || arguments);
       };
    },
    
    format: function(formatted) {
        for (var i = 1; i < arguments.length; i++) {
            var regexp = new RegExp('\\{' + (i - 1) + '\\}', 'gi');
            formatted = formatted.replace(regexp, arguments[i]);
        }
        return formatted;
    },
    
    timeout: function(func, delay, scope) {
       var args = [];
       for (var i = 3; i < arguments.length; i++) {
           args.push(arguments[i]);
       }
       var callback = function() {
           func.apply(scope, args);
       }
       return window.setTimeout(callback, delay || 0);
   },
   
   clone: function(src, deep, keepFnc) {
        if ((typeof src) != 'object' || src == null) {
            return src;
        }
        var dest = (src.push) ? [] : {};
        for (var key in src) {
            if (!keepFnc || keepFnc(key)) {
                dest[key] = deep === false ? src[key] : this.clone(src[key], deep, keepFnc);
            }
        }
        return dest;
    },
    
    relax: function(waitTime, callback, scope) {
         var lastTime = null;
         var lastTimeout = null;
         var fnc = function() {
                var now = new Date().getTime();
                var difference;
                if (lastTime == null
                              || (difference = (now - lastTime)) >= waitTime) {
                       lastTime = now;
                       callback.apply(scope, arguments);
                } else {
                       var timeout = lastTimeout = now;
                       var rememberedArguments = arguments;
                       setTimeout(function() {
                              if (timeout == lastTimeout) {
                                    fnc.apply(null, rememberedArguments);
                             }
                       }, waitTime - difference);
                }
         };
         return fnc;
   }
};

window.console = window.console || {
    log: korem.EMPTY_FNC,
    debug: korem.EMPTY_FNC
};
