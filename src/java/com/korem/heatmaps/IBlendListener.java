package com.korem.heatmaps;

/**
 *
 * @author jduchesne
 */
interface IBlendListener {

    void blended(int x, int y, int pixel, int resultPixel);

    void value(int x, int y);
}
