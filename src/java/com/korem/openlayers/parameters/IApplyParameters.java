/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.korem.openlayers.parameters;

/**
 *
 * @author ydumais
 */
public interface IApplyParameters extends IBaseParameters {
    String from();

    String to();
    
    String compareFrom();
    
    String compareTo();

    String locations();

    Integer minTransactions();

    Integer minSpend();

    Integer minUnit();

    String dateType();

}
