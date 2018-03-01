package com.lo.config;

import java.util.Set;

import java.util.Map;

/**
 * StaticConfig interface.
 * @author Auto generator
 */
public interface StaticConfig {

    /**
     * Return the property SRID of the staticConfig.properties file
     * @return Integer
     */
    Integer SRID();

    /**
     * Return the property charset of the staticConfig.properties file
     * @return String
     */
    String charset();

    /**
     * Return the property pdfGenerator.subTitle.legend.font.style of the staticConfig.properties file
     * @return Integer
     */
    Integer pdfGeneratorSubTitleLegendFontStyle();

    /**
     * Return the property output.gzip.excludedPaths.set of the staticConfig.properties file
     * @return Set<String>
     */
    Set<String> outputGzipExcludedPathsSet();

    /**
     * Return the property tradearea.midmif.download.session.attr.path of the staticConfig.properties file
     * @return String
     */
    String tradeareaMidmifDownloadSessionAttrPath();

    /**
     * Return the property pdfGenerator.subTitle.legend.font.size of the staticConfig.properties file
     * @return Integer
     */
    Integer pdfGeneratorSubTitleLegendFontSize();

    /**
     * Return the property kmsLegend.font.size of the staticConfig.properties file
     * @return Integer
     */
    Integer kmsLegendFontSize();

    /**
     * Return the property tradearea.csv.download.session.attr.path of the staticConfig.properties file
     * @return String
     */
    String tradeareaCsvDownloadSessionAttrPath();

    /**
     * Return the property kmsLegend.font.style of the staticConfig.properties file
     * @return Integer
     */
    Integer kmsLegendFontStyle();

    /**
     * Return the property kms.coordsys of the staticConfig.properties file
     * @return String
     */
    String kmsCoordsys();

    /**
     * Return the property web.coordsys of the staticConfig.properties file
     * @return String
     */
    String webCoordsys();

    /**
     * Return the property pdfGenerator.title.legend.font.size of the staticConfig.properties file
     * @return Integer
     */
    Integer pdfGeneratorTitleLegendFontSize();

    /**
     * Return the property pdfGenerator.title.legend.font.style of the staticConfig.properties file
     * @return Integer
     */
    Integer pdfGeneratorTitleLegendFontStyle();

    /**
     * Return the property pdfGenerator.color of the staticConfig.properties file
     * @return Integer
     */
    Integer pdfGeneratorColor();

    /**
     * Return the property postalcode.xml.properties of the staticConfig.properties file
     * @return String
     */
    String postalcodeXmlProperties();

    /**
     * Return the property pdfGenerator.subTitle.legend.font.name of the staticConfig.properties file
     * @return String
     */
    String pdfGeneratorSubTitleLegendFontName();

    /**
     * Return the property kmsLegend.font.name of the staticConfig.properties file
     * @return String
     */
    String kmsLegendFontName();

    /**
     * Return the property web.coordsys.epsg of the staticConfig.properties file
     * @return String
     */
    String webCoordsysEpsg();

    /**
     * Return the property tradearea.csv.download.file.prefix of the staticConfig.properties file
     * @return String
     */
    String tradeareaCsvDownloadFilePrefix();

    /**
     * Return the property tradearea.midmif.excludedColumns.set of the staticConfig.properties file
     * @return Set<String>
     */
    Set<String> tradeareaMidmifExcludedColumnsSet();

    /**
     * Return the property pdfGenerator.title.legend.font.name of the staticConfig.properties file
     * @return String
     */
    String pdfGeneratorTitleLegendFontName();

    /**
     * Return the property postalcode.rendition of the staticConfig.properties file
     * @return String
     */
    String postalcodeRendition();

}