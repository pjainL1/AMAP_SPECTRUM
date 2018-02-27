package com.lo.config;

import java.util.Set;

import java.util.Map;

/**
 * Build interface.
 * @author Auto generator
 */
public interface Build {

    /**
     * Return the property version of the build.properties file
     * @return Integer
     */
    Integer version();

    /**
     * Return the property svn.revision of the build.properties file
     * @return String
     */
    String svnRevision();

    /**
     * Return the property minor of the build.properties file
     * @return Integer
     */
    Integer minor();

    /**
     * Return the property build of the build.properties file
     * @return Integer
     */
    Integer build();

}