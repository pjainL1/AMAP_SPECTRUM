/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.korem.openlayers.parameters;

/**
 *
 * @author YDumais
 */
public interface IInfoParameters extends IMousePositionParameters {

    String from();

    String to();

    Integer minTransactions();

    Integer minSpend();

    Integer minUnit();
}
