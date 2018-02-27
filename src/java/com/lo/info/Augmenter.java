/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lo.info;

import com.korem.openlayers.parameters.IInfoParameters;
import com.lo.ContextParams;
import java.sql.Connection;
import java.util.Map;

/**
 *
 * @author YDumais
 */
public abstract class Augmenter {

    abstract void prepare(Connection connection, ContextParams cp, IInfoParameters params) throws Exception;

    abstract void augment(Map<String, Object> info, IInfoParameters params, ContextParams cp) throws Exception;

    abstract void terminate();
}
