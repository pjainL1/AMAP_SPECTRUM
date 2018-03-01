package com.lo.export.midmif;

import com.spinn3r.log5j.Logger;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author agilbert
 */
public abstract class AbstractMidMifExport {
    private static Logger logger = Logger.getLogger();
    
    protected static final int STRING_LIMIT = 127;
    private static final String VERSION = "Version 300";
    private static final String DELIMITER = ",";
    private static final String TEXT_DELIMITER = "\"";
    private static final String TEXT_DELIMITER_ESCAPE = "\"\"";
    
    protected GeometryFactory geometryFactory = new GeometryFactory();
    private boolean interrupted = false;
    
    public int generate(File mifFile, File midFile, File abortFile) throws Exception{
        PrintWriter mifWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(mifFile), "ISO8859-1"));
        PrintWriter midWriter = new PrintWriter(new OutputStreamWriter(new FileOutputStream(midFile), "ISO8859-1"));
        try{
            mifWriter.println(VERSION);
            mifWriter.println("Charset \"WindowsLatin1\""); //TODO try unicode
            mifWriter.println("Delimiter \""+DELIMITER+"\"");

            MidMifMetadata metadata = this.getMetadata();
            mifWriter.println("CoordSys "+metadata.getCoordsysClause().replaceAll("\"m\"", "7").replaceAll("'m'", "7"));
            mifWriter.println("Columns "+metadata.getColumnNames().size());
            for(int i=0;i<metadata.getColumnNames().size();i++){
                mifWriter.println("  "+metadata.getColumnNames().get(i)+" "+metadata.getColumnTypes().get(i));
            }

            mifWriter.println("Data");
            mifWriter.println();
            
            int count = 0;
            for(MidMifRecord record;(record=nextRecord())!=null&&!interrupted;){//interrupt from outside possible
//                if(count%100==0&&abortFile!=null&&abortFile.exists()){
//                    interrupt();
//                    break;
//                }
                writeRecord(record, mifWriter, midWriter);
                count++;
            }
            return count;
        }finally{
            mifWriter.close();
            midWriter.close();
        }
    }
    
    private void writeRecord(MidMifRecord record, PrintWriter mifWriter, PrintWriter midWriter) throws Exception{
        for(int i=0;i<record.getDescriptiveValues().size();i++){
            Object value = record.getDescriptiveValues().get(i);
            if(i>0){
                midWriter.print(DELIMITER);
            }
            if(value instanceof String){
                midWriter.print(TEXT_DELIMITER);
                midWriter.print(value.toString().replaceAll(TEXT_DELIMITER, TEXT_DELIMITER_ESCAPE));
                midWriter.print(TEXT_DELIMITER);
            }else if(value!=null){
                if(value.toString().length()>STRING_LIMIT){
                    midWriter.print(value.toString().substring(0, STRING_LIMIT));
                }else{
                    midWriter.print(value.toString());
                }
            }
        }
        writeGeometry(record.getGeometry(), mifWriter);
        if(record.getStyle()!=null){
            mifWriter.println("    "+record.getStyle());
        }
        midWriter.println();
    }
    
    private void writeGeometry(Geometry geometry, PrintWriter mifWriter){
        if(geometry instanceof MultiPolygon){
            writeGeometry((MultiPolygon)geometry, mifWriter);
        }else if(geometry instanceof GeometryCollection){//parait ça arrive desfois un multi polygon dans cette forme
            writeGeometry((GeometryCollection)geometry, mifWriter);    
        }else if(geometry instanceof Polygon){
            writeGeometry((Polygon)geometry, mifWriter);
        }else if(geometry instanceof Point){
            writeGeometry((Point)geometry, mifWriter);
        }else if(geometry instanceof MultiLineString){
            writeGeometry((MultiLineString)geometry, mifWriter);    
        }else if(geometry instanceof LineString){
            writeGeometry((LineString)geometry, mifWriter);      
        }else{
            throw new RuntimeException("jts geometry type not supported yet: "+geometry.getClass().getName());
        }
    }
    
    private void writeGeometry(LineString lineString, PrintWriter mifWriter){
        mifWriter.println("PLINE 1");
        writeLineStringsGeometry(Arrays.asList(new LineString[]{lineString}), mifWriter);
        //mifWriter.println("    Pen (1,2,0)");
    }
    
    private void writeGeometry(MultiLineString multiLineString, PrintWriter mifWriter){
        mifWriter.print("PLINE");
        if(multiLineString.getNumGeometries()>1){
            mifWriter.print(" MULTIPLE "+multiLineString.getNumGeometries());
        }
        mifWriter.println();
        List<LineString> lines = new ArrayList<>();
        for (int i=0;i<multiLineString.getNumGeometries();i++) {
            lines.add((LineString)multiLineString.getGeometryN(i));
        }
        writeLineStringsGeometry(lines, mifWriter);
        //mifWriter.println("    Pen (1,2,0)");
    }
    
    private void writeGeometry(GeometryCollection geometryCollection, PrintWriter mifWriter){
        if(geometryCollection.getGeometryN(0) instanceof Polygon){
            List<Polygon> polygons = new ArrayList<>();
            for (int i=0;i<geometryCollection.getNumGeometries();i++) {
                polygons.add((Polygon)geometryCollection.getGeometryN(i));
            }
            writeGeometry(polygons, mifWriter);
        }else{
            LineString[] lines = new LineString[geometryCollection.getNumGeometries()];
            for (int i=0;i<geometryCollection.getNumGeometries();i++) {
                lines[i] = (LineString)geometryCollection.getGeometryN(i);
            }
            MultiLineString multiLineString = geometryFactory.createMultiLineString(lines);
            writeGeometry(multiLineString, mifWriter);
        }
    }
    
    private void writeGeometry(MultiPolygon multiPolygon, PrintWriter mifWriter){
        List<Polygon> polygons = new ArrayList<>();
        for (int i=0;i<multiPolygon.getNumGeometries();i++) {
            polygons.add((Polygon)multiPolygon.getGeometryN(i));
        }
        writeGeometry(polygons, mifWriter);
    }
    
    private void writeGeometry(List<Polygon> polygons, PrintWriter mifWriter){
        int n = 0;
        for(int i=0;i<polygons.size();i++){
            n += polygons.get(i).getNumInteriorRing()+1;
        }
        mifWriter.println("REGION "+n);
        List<LineString> rings = new ArrayList<>();
        for(int i=0;i<polygons.size();i++){
            rings.addAll(extractRings(polygons.get(i)));
        }
        writeLineStringsGeometry(rings, mifWriter);
        //mifWriter.println("    Pen (1,2,0)");
        //mifWriter.println("    Brush (2,14680064,16777215)");
    }
    
    private void writeGeometry(Polygon polygon, PrintWriter mifWriter){
        writeGeometry(Arrays.asList(new Polygon[]{polygon}), mifWriter);
        /*mifWriter.println("REGION "+(polygon.getNumInteriorRing()+1));
        writeLineStringsGeometry(extractRings(polygon), mifWriter);
        mifWriter.println("    Pen (1,2,0)");
        mifWriter.println("    Brush (2,14680064,16777215)");*/
    }
    
    private void writeGeometry(Point point, PrintWriter mifWriter){
        mifWriter.println("POINT "+point.getCoordinate().x+" "+point.getCoordinate().y);
        //mifWriter.println("    SYMBOL (35,16711680,24)");
    }
    
    private void writeLineStringsGeometry(List<LineString> rings, PrintWriter mifWriter){
        for (LineString ring: rings) {
            writeLineStringGeometry(ring, mifWriter);
        }
    }
    
    private void writeLineStringGeometry(LineString ring, PrintWriter mifWriter){
        mifWriter.println("  "+ring.getNumPoints());
        for (int i=0;i<ring.getNumPoints();i++) {
            Coordinate coord = ring.getCoordinateN(i);
            mifWriter.println(coord.x+" "+coord.y);
        }
    }
    
    private List<LineString> extractRings(Polygon polygon){
        List<LineString> rings = new ArrayList<>();
        rings.add(polygon.getExteriorRing());
        for(int i=0;i<polygon.getNumInteriorRing();i++){
            rings.add(polygon.getInteriorRingN(i));
        }
        return rings;
    }

    public boolean isInterrupted() {
        return interrupted;
    }

    public void interrupt() {
        this.interrupted = true;
    }
    
    public abstract MidMifRecord nextRecord() throws Exception;
    
    public abstract MidMifMetadata getMetadata() throws Exception;
}
