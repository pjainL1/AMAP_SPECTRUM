/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lo.web;

import com.mapinfo.midev.service.namedresource.v1.ListNamedResourceResponse;
import java.awt.image.RenderedImage;

/**
 *
 * @author pjain
 */
public class Tile {
        private RenderedImage tile;
 
        public Tile(RenderedImage tile) {
            this.tile = tile;
        }
 
        public RenderedImage getTile() {
            return tile;
        }
}
