html = {};

html.element = function(type, options) {
    var newElement = document.createElement(type);
    if (options != null) {
        if (options.props) {
            for (var prop in options.props) {
                newElement[prop] = options.props[prop];
            }
        }
        if (options.style) {
            for (var key in options.style) {
                newElement.style[key] = options.style[key];
            }
        }
        if (options.children) {
            for (var i = 0; i < options.children.length; ++i) {
                newElement.appendChild(options.children[i]);
            }
        }
    }
    return newElement;
}

html.text = function(text) {
    return document.createTextNode(text);
}

html.img = function(options) {
    return html.element('img', options);
}

html.a = function(options) {
    return html.element('a', options);
}

html.td = function(options) {
    return html.element('td', options);
}

html.tr = function(options) {
    return html.element('tr', options);
}

html.tbody = function(options) {
    return html.element('tbody', options);
}

html.table = function(options) {
    if (!options) options = {}
    if (options.noTBody) {
        return html.element('table', options);
    }
    var tbody = html.tbody({
        children: options.children
    });
    delete options.children;
    var table = html.element('table', options);
    table.appendChild(tbody);
    return table;
}

html.leanTable = function(options) {
    if (!options) options = {}
    if (!options.props) options.props = {}
    options.props.cellPadding = 0;
    options.props.cellSpacing = 0;
    options.props.border = 0;
    return html.table(options);
}

html.span = function(options) {
    return html.element('span', options);
}

html.div = function(options) {
    return html.element('div', options);
}

html.select = function(options) {
    return html.element('select', options);
}

html.option = function(options) {
    return html.element('option', options);
}

html.button = function(options) {
    return html.element('button', options);
}

html.input = function(options) {
    return html.element('input', options);
}