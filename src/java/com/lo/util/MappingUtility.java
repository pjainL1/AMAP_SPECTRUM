package com.lo.util;

import java.util.ArrayList;
import java.util.List;

import com.mapinfo.midev.service.featurecollection.v1.AttributeDataType;
import com.mapinfo.midev.service.featurecollection.v1.AttributeDefinition;
import com.mapinfo.midev.service.featurecollection.v1.AttributeDefinitionList;
import com.mapinfo.midev.service.featurecollection.v1.AttributeValue;
import com.mapinfo.midev.service.featurecollection.v1.FeatureCollection;
import com.mapinfo.midev.service.featurecollection.v1.FeatureCollectionMetadata;
import com.mapinfo.midev.service.featurecollection.v1.GeometryAttributeDefinition;
import com.mapinfo.midev.service.featurecollection.v1.GeometryValue;
import com.mapinfo.midev.service.featurecollection.v1.KeyDefinition;
import com.mapinfo.midev.service.featurecollection.v1.ScalarAttributeDefinition;
import com.mapinfo.midev.service.featurecollection.v1.StringValue;
import com.mapinfo.midev.service.geometries.v1.Envelope;
import com.mapinfo.midev.service.geometries.v1.Geometry;
import com.mapinfo.midev.service.geometries.v1.GeometryList;
import com.mapinfo.midev.service.geometries.v1.Pos;
import com.mapinfo.midev.service.mapping.v1.BarLayer;
import com.mapinfo.midev.service.mapping.v1.Category;
import com.mapinfo.midev.service.mapping.v1.CategoryList;
import com.mapinfo.midev.service.mapping.v1.ChartEffectType;
import com.mapinfo.midev.service.mapping.v1.FeatureLayer;
import com.mapinfo.midev.service.mapping.v1.GeometryLayer;
import com.mapinfo.midev.service.mapping.v1.GraduatedSymbolLayer;
import com.mapinfo.midev.service.mapping.v1.Graduation;
import com.mapinfo.midev.service.mapping.v1.GridLayer;
import com.mapinfo.midev.service.mapping.v1.LabelLayer;
import com.mapinfo.midev.service.mapping.v1.LabelSource;
import com.mapinfo.midev.service.mapping.v1.Layer;
import com.mapinfo.midev.service.mapping.v1.LineChartLayer;
import com.mapinfo.midev.service.mapping.v1.LineLinkageLayer;
import com.mapinfo.midev.service.mapping.v1.NamedLayer;
import com.mapinfo.midev.service.mapping.v1.PieLayer;
import com.mapinfo.midev.service.mappingcommon.v1.HorizontalAlignmentType;
import com.mapinfo.midev.service.mappingcommon.v1.InflectionCollection;
import com.mapinfo.midev.service.mappingcommon.v1.Interpolator;
import com.mapinfo.midev.service.mappingcommon.v1.VerticalAlignmentType;
import com.mapinfo.midev.service.mappingcommon.v1.VisibilityConstraintList;
import com.mapinfo.midev.service.style.v1.MapBasic30Symbol;
import com.mapinfo.midev.service.style.v1.MapBasicFontSymbol;
import com.mapinfo.midev.service.style.v1.MapBasicLineStyle;
import com.mapinfo.midev.service.style.v1.MapBasicPen;
import com.mapinfo.midev.service.style.v1.MapBasicPointStyle;
import com.mapinfo.midev.service.style.v1.MapBasicSymbol;
import com.mapinfo.midev.service.style.v1.NamedStyle;
import com.mapinfo.midev.service.style.v1.Style;
import com.mapinfo.midev.service.table.v1.MemoryTable;
import com.mapinfo.midev.service.table.v1.Table;
import com.mapinfo.midev.service.theme.v1.RangeThemeProperties;
import com.mapinfo.midev.service.theme.v1.RangeThemeType;
import com.mapinfo.midev.service.theme.v1.Theme;
import com.mapinfo.midev.service.theme.v1.ThemeList;
import com.mapinfo.midev.service.units.v1.Distance;
import com.mapinfo.midev.service.units.v1.DistanceUnit;

public final class MappingUtility {

    public static Layer buildGeometryLayer(List<Geometry> geoms) {
        GeometryLayer geometryLayer = new GeometryLayer();
        GeometryList geometryList = new GeometryList();
        try {
            geometryList.getGeometry().addAll(geoms);
        } catch (Exception e) {
            e.printStackTrace();
        }
        geometryLayer.setGeometryList(geometryList);
        return geometryLayer;
    }

    public static Pos buildPos(double x, double y) {
        Pos pos = new Pos();
        pos.setX(x);
        pos.setY(y);
        return pos;
    }

    public static Layer buildBarLayer(ChartEffectType chartType, HorizontalAlignmentType hType, VerticalAlignmentType vType,
            Table table, Graduation graduation, Style style, CategoryList categoryList) {
        BarLayer layer = new BarLayer();
        layer.setChartEffectType(chartType);
        layer.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
        layer.setGraduation(graduation);
        layer.setVerticalAlignment(VerticalAlignmentType.BOTTOM);
        layer.setTable(table);
        layer.setBorder(style);
        layer.setCategoryList(categoryList);
        return layer;
    }

    public static Layer buildLineChartLayer(Table table, Style style, CategoryList categoryList) {
        LineChartLayer layer = new LineChartLayer();
        layer.setTable(table);
        layer.setLine(style);
        layer.setCategoryList(categoryList);
        return layer;
    }

    /**
     * Line linkage layer
     *
     * @param geometryTable
     * @param linkageTable
     * @param sourcePointStyle
     * @param destinationPointStyle
     * @param relationshipExpression
     * @param sourceExpression
     * @param destinationExpression
     * @param metricExpression
     * @param isDirectionArrowEnabled
     *
     * @return	Layer
     */
    public static Layer buildLineLinkageLayer(Table geometryTable,
            Table linkageTable,
            Style sourcePointStyle,
            Style destinationPointStyle,
            String relationshipExpression,
            String sourceExpression,
            String destinationExpression,
            String metricExpression,
            boolean isDirectionArrowEnabled) {

        LineLinkageLayer layer = new LineLinkageLayer();
        layer.setGeometryTable(geometryTable);
        layer.setLinkageTable(linkageTable);

        layer.setSourcePointStyle(sourcePointStyle);
        layer.setDestinationPointStyle(destinationPointStyle);

        layer.setDescription("Line Linkage layer - to display link between source & destination");

        layer.setRelationshipExpression(relationshipExpression);
        layer.setSourceExpression(sourceExpression);
        layer.setDestinationExpression(destinationExpression);
        layer.setMetricExpression(metricExpression);
        layer.setDirectionArrowEnabled(isDirectionArrowEnabled);

        return layer;
    }

    public static Layer buildPieLayer(ChartEffectType chartType, HorizontalAlignmentType hType, VerticalAlignmentType vType,
            Table table, Graduation graduation, Style style, CategoryList categoryList) {
        PieLayer layer = new PieLayer();
        layer.setChartEffectType(chartType);
        layer.setHorizontalAlignment(HorizontalAlignmentType.CENTER);
        layer.setGraduation(graduation);
        layer.setVerticalAlignment(VerticalAlignmentType.BOTTOM);
        layer.setTable(table);
        layer.setBorder(style);
        layer.setCategoryList(categoryList);
        return layer;
    }

    public static Layer buildGraduatedSymbolLayer(Table table, Graduation graduation, Style positiveStyle, String valueExpression) {
        GraduatedSymbolLayer layer = new GraduatedSymbolLayer();
        layer.setGraduationMethod(graduation.getGraduationMethod());
        layer.setValueAtSize(graduation.getValueAtSize());
        layer.setPositiveSymbol(positiveStyle);
        layer.setTable(table);
        layer.setValueExpression(valueExpression);
        return layer;
    }

    public static Layer buildFeatureLayer(Table table) {
        FeatureLayer layer = new FeatureLayer();
        layer.setTable(table);
        return layer;
    }

    public static Layer buildGridLayer(Table table, Style style, Distance cellWidth, String valueExpression, String spatialExpression,
            Interpolator interpolator, InflectionCollection inflectionCollection, Envelope gridEnvelope) {
        GridLayer layer = new GridLayer();
        layer.setCellWidth(cellWidth);
        layer.setStyle(style);
        layer.setGridEnvelope(gridEnvelope);
        layer.setInflectionCollection(inflectionCollection);
        layer.setInterpolator(interpolator);
        layer.setSpatialExpression(spatialExpression);
        layer.setValueExpression(valueExpression);
        layer.setTable(table);
        return layer;
    }

    public static Layer buildLabelLayer(LabelSource labelSource) {

        LabelLayer labelLayer = new LabelLayer();
        labelLayer.getLabelSource().add(labelSource);

        return labelLayer;
    }

    public static VisibilityConstraintList getVisibilityConstraintList() {
        return null;
    }

    public static Distance buildDistance(double zoomLevel, DistanceUnit unit) {
        Distance distance = new Distance();
        distance.setValue(zoomLevel);
        distance.setUom(unit);
        return distance;
    }

    public static List<Category> createCategories() {
        List<Category> categories = new ArrayList<>();

        Category category = new Category();
        category.setExpression("Pop_2000");
        category.setDescription("Pop_2000");
        NamedStyle bluestyle = new NamedStyle();
        bluestyle.setName("/Samples/NamedStyles/AreaStyleBlue");
        category.setStyle(bluestyle);
        categories.add(category);

        Category category1 = new Category();
        category1.setExpression("Pop_Male");
        category1.setDescription("Pop_Male");
        NamedStyle greenstyle = new NamedStyle();
        greenstyle.setName("/Samples/NamedStyles/AreaStyleGreen");
        category1.setStyle(greenstyle);
        categories.add(category1);

        Category category2 = new Category();
        category2.setExpression("Pop_Female");
        category2.setDescription("Pop_Female");
        NamedStyle redstyle = new NamedStyle();
        redstyle.setName("/Samples/NamedStyles/AreaStyleRed");
        category2.setStyle(redstyle);
        categories.add(category2);

        return categories;
    }

    public static List<Category> createCategoriesForLineChart() {
        List<Category> categories = new ArrayList<>();

        Category category = new Category();
        category.setExpression("Pop_2000");
        category.setDescription("Pop_2000");
        NamedStyle bluestyle = new NamedStyle();
        bluestyle.setName("/Samples/NamedStyles/PointStylePin");
        category.setStyle(bluestyle);
        categories.add(category);

        Category category1 = new Category();
        category1.setExpression("Pop_Male");
        category1.setDescription("Pop_Male");
        NamedStyle greenstyle = new NamedStyle();
        greenstyle.setName("/Samples/NamedStyles/PointStylePin");
        category1.setStyle(greenstyle);
        categories.add(category1);

        Category category2 = new Category();
        category2.setExpression("Pop_Female");
        category2.setDescription("Pop_Female");
        NamedStyle redstyle = new NamedStyle();
        redstyle.setName("/Samples/NamedStyles/PointStyleStar");
        category2.setStyle(redstyle);
        categories.add(category2);

        return categories;
    }

    public static MapBasicFontSymbol buildMapBasicFontSymbol(int shape, String color, short size, String fontName,
            Boolean bold, String border, Boolean dropShadow, Float rotation) {

        MapBasicFontSymbol mapBasicFontSymbol = new MapBasicFontSymbol();

        mapBasicFontSymbol.setSize(size);
        mapBasicFontSymbol.setBold(bold);
        mapBasicFontSymbol.setBorder(border);
        mapBasicFontSymbol.setColor(color);
        mapBasicFontSymbol.setDropShadow(dropShadow);
        mapBasicFontSymbol.setFontName(fontName);
        mapBasicFontSymbol.setRotation(rotation);
        mapBasicFontSymbol.setShape(shape);

        return mapBasicFontSymbol;
    }

    public static MapBasic30Symbol buildMapBasic30Symbol(short shape, String color, short size) {

        MapBasic30Symbol mapBasic30Symbol = new MapBasic30Symbol();

        mapBasic30Symbol.setColor(color);
        mapBasic30Symbol.setShape(shape);
        mapBasic30Symbol.setSize(size);

        return mapBasic30Symbol;
    }

    public static MapBasicLineStyle buildMapBasicLineStyle(MapBasicPen mapBasicPen) {
        MapBasicLineStyle style = new MapBasicLineStyle();
        style.setMapBasicPen(mapBasicPen);
        return style;
    }

    public static MapBasicPointStyle buildMapBasicPointStyle(MapBasicSymbol mapBasicSymbol) {
        MapBasicPointStyle style = new MapBasicPointStyle();
        style.setMapBasicSymbol(mapBasicSymbol);
        return style;
    }

    public static Layer buildNamedLayer(String layerName, String layerDesc, boolean renderable,
            VisibilityConstraintList vList) {
        NamedLayer namedLayer = new NamedLayer();
        namedLayer.setName(layerName);
        namedLayer.setDescription(layerDesc);
        namedLayer.setRenderable(renderable);
        namedLayer.setVisibilityConstraintList(vList);
        return namedLayer;
    }

    public static ThemeList buildThemeList(List<Theme> themes) {
        ThemeList list = new ThemeList();
        list.getTheme().addAll(themes);
        return list;
    }

    public static RangeThemeProperties buildRangeThemeProperties(String expression, int numRanges, RangeThemeType rangeType) {
        RangeThemeProperties rangeThemeProperties = new RangeThemeProperties();

        rangeThemeProperties.setExpression(expression);
        rangeThemeProperties.setRangeType(rangeType);
        rangeThemeProperties.setNumRanges(numRanges);
        return rangeThemeProperties;
    }

    public static MemoryTable buildMemoryTable(String name, FeatureCollection fc) {
        MemoryTable memoryTable = new MemoryTable();
        memoryTable.setName(name);
        memoryTable.setFeatureCollection(fc);
        return memoryTable;
    }

    public static AttributeDefinition buildAttributeDefinition(String type, String name, AttributeDataType dataType) {
        AttributeDefinition def = null;

        if (type.equals("GeometryAttributeDefinition")) {
            def = new GeometryAttributeDefinition();
        } else if (type.equals("ScalarAttributeDefinition")) {
            def = new ScalarAttributeDefinition();
        }
        def.setDataType(dataType);
        def.setName(name);
        return def;
    }

    public static FeatureCollectionMetadata buildFeatureCollectionMetadata(AttributeDefinitionList attrDefList, Long count,
            Envelope envelope, KeyDefinition KeyDefinition) {
        FeatureCollectionMetadata fcMetaData = new FeatureCollectionMetadata();

        fcMetaData.setAttributeDefinitionList(attrDefList);
        fcMetaData.setCount(count);
        fcMetaData.setEnvelope(envelope);
        fcMetaData.setKeyDefinition(KeyDefinition);

        return fcMetaData;
    }

    public static AttributeValue buildAttributeValue(String type) {
        AttributeValue value = null;
        if (type.equals("GeometryValue")) {
            value = new GeometryValue();
        } else if (type.equals("StringValue")) {
            value = new StringValue();
        }
        return value;
    }

}
