package com.lo.config;

import java.util.Set;

import java.util.Map;

/**
 * Config interface.
 * @author Auto generator
 */
public interface Config {

    /**
     * Return the property ws.spectrum.timeout of the config.properties file
     * @return Integer
     */
    Integer wsSpectrumTimeout();

    /**
     * Return the property https.enabled of the config.properties file
     * @return Boolean
     */
    Boolean httpsEnabled();

    /**
     * Return the property koremInternalAccess.enabled of the config.properties file
     * @return Boolean
     */
    Boolean koremInternalAccessEnabled();

    /**
     * Return the property db.timezone of the config.properties file
     * @return String
     */
    String dbTimezone();

    /**
     * Return the property db.sponsors.schema.array of the config.properties file
     * @return String[]
     */
    String[] dbSponsorsSchemaArray();

    /**
     * Return the property loading.dir.home of the config.properties file
     * @return String
     */
    String loadingDirHome();

    /**
     * Return the property loading.alert.toEmail of the config.properties file
     * @return String
     */
    String loadingAlertToEmail();

    /**
     * Return the property loading.alert.fromEmail of the config.properties file
     * @return String
     */
    String loadingAlertFromEmail();

    /**
     * Return the property amap.korem.param.key of the config.properties file
     * @return String
     */
    String amapKoremParamKey();

    /**
     * Return the property console.download.path.midmif of the config.properties file
     * @return String
     */
    String consoleDownloadPathMidmif();

    /**
     * Return the property amap.external.param.key of the config.properties file
     * @return String
     */
    String amapExternalParamKey();

    /**
     * Return the property tradearea.csv.download.folder.path of the config.properties file
     * @return String
     */
    String tradeareaCsvDownloadFolderPath();

    /**
     * Return the property debug of the config.properties file
     * @return Boolean
     */
    Boolean debug();

    /**
     * Return the property loading.parallel.threadPool of the config.properties file
     * @return Integer
     */
    Integer loadingParallelThreadPool();

    /**
     * Return the property projectedMarker.path of the config.properties file
     * @return String
     */
    String projectedMarkerPath();

    /**
     * Return the property amap.sponsor.groups.all of the config.properties file
     * @return String
     */
    String amapSponsorGroupsAll();

    /**
     * Return the property amap.internal.param.key of the config.properties file
     * @return String
     */
    String amapInternalParamKey();

    /**
     * Return the property db.main.schema of the config.properties file
     * @return String
     */
    String dbMainSchema();

    /**
     * Return the property loading.run.denormalizer of the config.properties file
     * @return Boolean
     */
    Boolean loadingRunDenormalizer();
    
    String spectrumHost();
    
    String spectrumUrl();
    
    String spectrumPort();
    
    String namedTableLIM_TA_PLOYGON();
    

}