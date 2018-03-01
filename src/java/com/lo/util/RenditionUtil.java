/*
 * RenditionUtil.java
 *
 * Created on 26 septembre 2003, 14:17
 */

package com.lo.util;

import java.awt.Color;
import java.awt.HeadlessException;
import java.awt.Toolkit;


import com.korem.map.loader.BrushRenditionBuilder;
import com.korem.map.loader.PenRenditionBuilder;
import com.korem.map.loader.SymbolRenditionBuilder;
import com.korem.map.om.Brush;
import com.korem.map.om.Font;
import com.korem.map.om.Line;
import com.korem.map.om.MapUtils;
import com.korem.map.om.Pen;
import com.korem.map.om.Symbol;
import com.mapinfo.graphics.Rendition;
import com.mapinfo.graphics.RenditionImpl;
/**
 *
 * @author  pvachon
 */
public class RenditionUtil {
    static int screenResolution = 96;
    static {
        try {
            screenResolution = Toolkit.getDefaultToolkit().getScreenResolution();
        }catch(HeadlessException e){
            //Logger.log(e.getClass().getName());
        }
    }
    /** Creates a new instance of RenditionUtil */
    public RenditionUtil() {
    }

    public static String toXml(Rendition rendition) {
        return com.mapinfo.xmlprot.mxtj.RenditionHandler.createXmlString(rendition);
    }
    public static String mapBasicToXml(String mapBasicStyle) throws Exception {
        return toXml(createRendition(mapBasicStyle));
    }
    
    public static Rendition create(String renditionXmlString) {
        return com.mapinfo.xmlprot.mxtj.RenditionHandler.createRendition(renditionXmlString);
    }

    public static Rendition createRendition(Symbol symbol){
        return createRendition(new RenditionImpl(),symbol,screenResolution);
    }
    public static Rendition createRendition(Rendition rendition, Symbol symbol){
        return createRendition(rendition,symbol,screenResolution);
    }
    public static Rendition createRendition(Symbol symbol, int resolution){
        return createRendition(new RenditionImpl(),symbol,resolution);
    }
    public static Rendition createRendition(Rendition rendition, Symbol symbol, int resolution){
        SymbolRenditionBuilder builder = new SymbolRenditionBuilder(resolution);
        builder.setSymbol(symbol);
        builder.setRendition(rendition);
        builder.build();
        return rendition;
    }
    public static Rendition createRendition(Pen pen){
        return createRendition(RenditionImpl.getDefaultRendition().getCopy(),pen,screenResolution);
    }
    public static Rendition createRendition(Rendition rendition,Pen pen){
        return createRendition(rendition,pen,screenResolution);
    }
    public static Rendition createRendition(Pen pen, int resolution){
        return createRendition(new RenditionImpl(),pen,resolution);
    }
    public static Rendition createRendition(Rendition rendition,Pen pen, int resolution){
        PenRenditionBuilder builder = new PenRenditionBuilder(resolution);
        builder.setPen(pen);
        builder.setRendition(rendition);
        builder.build();
        return rendition;
    }
    public static Rendition createRendition(Brush brush){
        return createRendition(new RenditionImpl(),brush);
    }
    public static Rendition createRendition(Rendition rendition, Brush brush){
        BrushRenditionBuilder builder = new BrushRenditionBuilder();
        builder.setBrush(brush);
        builder.setRendition(rendition);
        builder.build();
        return rendition;
    }
    public static Rendition createRendition(Font font){
        return createRendition(font,screenResolution);
    }
    public static Rendition createRendition(Font font, int resolution){
        Rendition rendition = new RenditionImpl();
        rendition.setValue(Rendition.FONT_FAMILY,font.getDescription());
        if (font.isHalo())
            rendition.setValue(Rendition.FILTER_EFFECTS,Rendition.FilterEffects.HALO);
        if (font.isBold())
            rendition.setValue(Rendition.FONT_WEIGHT, new Float(2));
        if(font.isBox())
            rendition.setValue(Rendition.FILTER_EFFECTS,Rendition.FilterEffects.BOX);
        if(font.isOutline())
            rendition.setValue(Rendition.FILTER_EFFECTS,Rendition.FilterEffects.OUTLINE);
        if(font.isUnderline())
            rendition.setValue(Rendition.TEXT_DECORATIONS,Rendition.TextDecorations.UNDERLINE);
        if(font.isItalic())
            rendition.setValue(Rendition.FONT_STYLE,Rendition.FontStyle.ITALIC);
        rendition.setValue(Rendition.FONT_SIZE,new Float(MapUtils.transformFontSizeToPixels(resolution, font.getSize())));
        if(font.getOpaque()){
            rendition.setValue(Rendition.SYMBOL_BACKGROUND_OPACITY,1f);
            rendition.setValue(Rendition.SYMBOL_FOREGROUND_OPACITY,1f);
        }else{
            rendition.setValue(Rendition.SYMBOL_BACKGROUND_OPACITY,0f);
            rendition.setValue(Rendition.SYMBOL_FOREGROUND_OPACITY,0f);
        }
        rendition.setValue(Rendition.SYMBOL_FOREGROUND,new Color(font.getForecolor()));
        if(font.getBackcolor()!=-1)
            rendition.setValue(Rendition.SYMBOL_BACKGROUND,new Color(font.getBackcolor()));
        return rendition;
    }
    public static Rendition createRendition(String mapbasicString) throws Exception{
        try {
            return createRendition(mapbasicString,screenResolution);
        } catch(Exception e){
            throw e;  
        }
    }
    public static Rendition createRendition(String mapbasicString, int resolution) throws Exception{
        if (mapbasicString.contains("<Style>")) {
            return create(mapbasicString);
        } else {
            //pas de regex car peut etre applet un jour
            Font font = MapBasicStyleUtils.parseFont(mapbasicString);
            if(font!=null)
                return createRendition(font,resolution);
            Rendition rendition = new RenditionImpl();
            Symbol symbol = MapBasicStyleUtils.parseSymbol(mapbasicString);
            Brush brush = MapBasicStyleUtils.parseBrush(mapbasicString);
            Pen pen = MapBasicStyleUtils.parsePen(mapbasicString);
            Line line = MapBasicStyleUtils.parseLine(mapbasicString);
            if(symbol!=null)
                rendition = createRendition(rendition,symbol);
            if(pen!=null)
                rendition = createRendition(rendition,pen);
            if(line!=null)
                rendition = createRendition(rendition,line);
            if(brush!=null)
                rendition = createRendition(rendition,brush);
            return rendition;
        }
    }
}

