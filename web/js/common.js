var SC = new Object();

SC.asyncSubmit = function(form, options) {
    var f = $(form);
    new Ajax.Request(f.action, $H(options).merge({
        asynchronous: true,
        parameters: Form.serialize(f)
    }))
}

SC.asyncLink = function(anchor, options, indicator) {
    var a = $(anchor)
    Event.observe(a, "click", function(e) {
        if (indicator) { $(indicator).reveal() }
        Event.stop(e);
        new Ajax.Request(a.href, $H(options).merge({
            asynchronous: true,
            onComplete: function() {
                if (indicator) { $(indicator).conceal() }
            }
        }));
    })
}

////// COOKIES

/** Main fns based on http://www.quirksmode.org/js/cookies.html */
var Cookies = {
    add: function(name, value, days) {
        if (days) {
            var date = new Date();
            date.setTime(date.getTime() + (days*24*60*60*1000));
            var expires = "; expires=" + date.toGMTString();
        }
        else var expires = "";
        document.cookie = name + "=" + value + expires + "; path=/";
    },

    get: function(name) {
        var nameEQ = name + "=";
        var ca = document.cookie.split(';');
        for(var i=0;i < ca.length;i++) {
            var c = ca[i];
            while (c.charAt(0) == ' ') c = c.substring(1, c.length);
            if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length, c.length);
        }
        return null;
    },

    clear: function(name) {
        Cookies.add(name, "", -1);
    },

    set: function(name, value, days) {
        Cookies.clear(name)
        Cookies.add(name, value, days)
    }
}

////// PROTOTYPE EXTENSIONS

Element.addMethods( {
    // Like prototype's hide(), but uses the visibility CSS prop instead of display
    conceal: function() {
        for (var i = 0; i < arguments.length; i++) {
          var element = $(arguments[i]);
          element.style.visibility = 'hidden';
        }
    },

    // Like prototype's show(), but uses the visibility CSS prop instead of display
    reveal: function() {
        for (var i = 0; i < arguments.length; i++) {
          var element = $(arguments[i]);
          element.style.visibility = 'visible';
        }
    }
} );

////// DOM EXTENSIONS

// Adds an IE-like click() fn for other browsers
if (!document.all && HTMLElement && !HTMLElement.prototype.click) {
    HTMLElement.prototype.click = function() {
        var evt = this.ownerDocument.createEvent('MouseEvents');
        // evt.initMouseEvent('click', true, true, this.ownerDocument.defaultView, 1, 0, 0, 0, 0, false, false, false, false, 0, null);
        evt.initEvent("click", true, true);
        this.dispatchEvent(evt);
    }
}
