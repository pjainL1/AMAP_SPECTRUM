/*
 * MapBasicStyleUtils.java
 *
 * Created on November 30, 2004, 2:12 PM
 */

package com.lo.util;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import com.korem.map.om.Brush;
import com.korem.map.om.Font;
import com.korem.map.om.Line;
import com.korem.map.om.Pen;
import com.korem.map.om.Symbol;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author  agilbert
 */
public class MapBasicStyleUtils {
    private final static Log log = LogFactory.getLog(MapBasicStyleUtils.class);

    public static int parseAlpha(String mapbasicString) throws Exception{
        int i1 = mapbasicString.toLowerCase().indexOf("alpha");
        if(i1==-1){
            return -1;
        }
        i1 = i1 + "alpha".length();
        String alphaValue = mapbasicString.substring(i1).trim();
        return new Integer(alphaValue).intValue();
    }
    
    public static Brush parseBrush(String mapbasicString) throws Exception{
        int i1 = mapbasicString.toLowerCase().indexOf("brush");
        if(i1==-1)
            return null;
        int i2 = mapbasicString.indexOf("(",i1);
        int i3 = mapbasicString.indexOf(")",i1);
        String params = mapbasicString.substring(i2+1,i3);
        Brush brush = new Brush();
        try{
            StringTokenizer st = new StringTokenizer(params, ",");
            String stringToFormat = st.nextToken();
            int pattern = 0;
            if ( (stringToFormat.substring(0,1)).equals("(") )
                pattern = Integer.parseInt(stringToFormat.substring(1, stringToFormat.length()));
            else
                pattern = Integer.parseInt(stringToFormat);
            brush.setPattern(pattern);
            String color_s = st.nextToken();
            int color = java.lang.Integer.parseInt(color_s.trim());
            brush.setColor(color);
            stringToFormat = st.nextToken();
            int backgroundColor = 0;
            if ( (stringToFormat.substring(stringToFormat.length() -1, stringToFormat.length())).equals(")") ){
                backgroundColor = Integer.parseInt(stringToFormat.substring(0, stringToFormat.length() -1));
            }else{
                backgroundColor = Integer.parseInt(stringToFormat.trim());
            }
            brush.setBackgroundColor(backgroundColor);
        }catch(NoSuchElementException e){
            brush.setBackgroundColor(-1);
        }
        return brush;
    }
    public static Pen parsePen(String mapbasicString) throws Exception{
        int i1 = mapbasicString.toLowerCase().indexOf("pen");
        if(i1==-1)
            return null;
        int i2 = mapbasicString.indexOf("(",i1);
        int i3 = mapbasicString.indexOf(")",i1);
        if(i2==-1 || i3==-1){
            return null;
        }
        String params = mapbasicString.substring(i2+1,i3);
        Pen pen = new Pen();
        int thickness;
        int pattern;
        int color;
        StringTokenizer st = new StringTokenizer(params, ",");
        String toCheck = st.nextToken();
        if ( toCheck.substring(0,1).equals("(") ){
            thickness = Integer.parseInt(toCheck.substring(1, toCheck.length()));
            pen.setThickness(thickness);
        }else{
            thickness = Integer.parseInt(toCheck);
            pen.setThickness(thickness);
        }
        pattern = Integer.parseInt(st.nextToken().trim());
        pen.setPattern(pattern);
        color = Integer.parseInt(st.nextToken().trim());
        pen.setColor(color);
        return pen;
    }
    public static Symbol parseSymbol(String mapbasicString) throws Exception{
        int i1 = mapbasicString.toLowerCase().indexOf("symbol");
        if(i1==-1)
            return null;
        int i2 = mapbasicString.indexOf("(",i1);
        int i3 = mapbasicString.indexOf(")",i1);
        String params = mapbasicString.substring(i2+1,i3);
        Symbol symbol = new Symbol();
        StringTokenizer st = new StringTokenizer(params, ",");
        int count = st.countTokens();
        if ( count == 6 ){
            String stringToFormat = st.nextToken();
            if ( (stringToFormat.substring(0,1)).equals("(") )
                symbol.setShape(Integer.parseInt(stringToFormat.substring(1, stringToFormat.length())));
            else
                symbol.setShape(Integer.parseInt(stringToFormat));
            symbol.setColor(Integer.parseInt(st.nextToken().trim()));
            symbol.setSize(Integer.parseInt(st.nextToken().trim()));
            String description = st.nextToken();
            int fontStyle = Integer.parseInt(st.nextToken().trim());
            symbol.setFontStyle(fontStyle);
            int rotation = Integer.parseInt(st.nextToken().trim());
            symbol.setFont(new Font(description, fontStyle,rotation));
        } else if ( count == 4 ){
            String s = st.nextToken().trim();
            symbol.setBitmapName(s.substring(1,s.length()-1));
            symbol.setColor(0xFF000000|Integer.parseInt(st.nextToken().trim()));
            symbol.setSize(Integer.parseInt(st.nextToken().trim()));
            symbol.setCustomStyle(Integer.parseInt(st.nextToken().trim()));
            log.debug("Symbol clause with 4 parameter are ignored.");
        } else if ( count == 3 ){
            String stringToFormat = st.nextToken();
            if ( (stringToFormat.substring(0,1)).equals("(") )
                symbol.setShape(Integer.parseInt(stringToFormat.substring(1, stringToFormat.length())));
            else
                symbol.setShape(Integer.parseInt(stringToFormat));
            symbol.setColor(Integer.parseInt(st.nextToken().trim()));
            symbol.setSize(Integer.parseInt(st.nextToken().trim()));
            symbol.setCompatible(true);
        } else{
           log.debug("Symbol clause with 6 parameter are ignored.");
        }
        return symbol;
    }
    public static Line parseLine(String mapbasicString) throws Exception{
        int i1 = mapbasicString.toLowerCase().indexOf("line");
        if(i1==-1)
            return null;
        int i2 = mapbasicString.indexOf("(",i1);
        int i3 = mapbasicString.indexOf(")",i1);
        String params = mapbasicString.substring(i2+1,i3);
        Line line = new Line();
        int thickness;
        int pattern;
        int color;
        StringTokenizer st = new StringTokenizer(params, ",");
        String stringToConvert = st.nextToken();
        if ( stringToConvert.substring(0,1).equals("(") ){
            stringToConvert = stringToConvert.substring(1,stringToConvert.length());
            thickness = Integer.parseInt(stringToConvert);
            line.setThickness(thickness);
        }
        else{
            thickness = Integer.parseInt(stringToConvert);
            line.setThickness(thickness);
        }
        pattern = Integer.parseInt(st.nextToken().trim());
        line.setPattern(pattern);
        color = Integer.parseInt(st.nextToken().trim());
        line.setColor(color);
        if ( pattern == 1 && thickness == 1 )
            line.setThickness(0);
        return line;
    }
    public static Font parseFont(String mapbasicString) throws Exception{
        int i1 = mapbasicString.toLowerCase().indexOf("font");
        if(i1==-1)
            return null;
        int i2 = mapbasicString.indexOf("(",i1);
        int i3 = mapbasicString.indexOf(")",i1);
        String params = mapbasicString.substring(i2+1,i3);
        StringTokenizer st = new StringTokenizer(params, ",");
        int nbTokens = st.countTokens();
        if ( nbTokens == 4 ){
            String description = st.nextToken(",");
            int style = Integer.parseInt(st.nextToken(","));
            int size = Integer.parseInt(st.nextToken(","));
            int forecolor = Integer.parseInt(st.nextToken(","));
            return new Font(description, style, size, forecolor);
        }
        else if ( nbTokens == 5 ){
            String description = st.nextToken(",");
            int style = Integer.parseInt(st.nextToken(","));
            int size = Integer.parseInt(st.nextToken(","));
            int forecolor = Integer.parseInt(st.nextToken(","));
            int backcolor = Integer.parseInt(st.nextToken(","));
            return new Font(description, style, size, forecolor, backcolor);
        }
        return null;
    }
}
