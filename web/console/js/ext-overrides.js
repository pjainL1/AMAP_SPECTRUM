Ext.Element.prototype.scroll = function(direction, distance, animate) {
    if (!this.isScrollable()) {
        return false;
    }
    var me = this,
            dom = me.dom,
            side = direction === 'r' || direction === 'l' ? 'left' : 'top',
            scrolled = false,
            currentScroll, constrainedScroll;

    if (direction === 'r') {
        distance = -distance;
    }

    if (side === 'left') {
        currentScroll = dom.scrollLeft;
        constrainedScroll = me.constrainScrollLeft(currentScroll + distance);
    } else {
        currentScroll = dom.scrollTop;
        //currentScroll is the distance u can scroll 
        constrainedScroll = me.constrainScrollTop(currentScroll + (direction == 'up' ? -1 : 1) * distance);
    }

    if (constrainedScroll !== currentScroll) {
        console.log('scrolling = ' + side, constrainedScroll, animate);
        this.scrollTo(side, constrainedScroll, animate);
        scrolled = true;
    }

    return scrolled;
}