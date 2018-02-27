/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.lo.db.om;

/**
 *
 * @author slajoie
 */
public class Transaction {

    private Integer collectors;
    private Integer transactions;
    private Double spend;
    private Double units;

    public Integer getCollectors() {
        return collectors;
    }

    public void setCollectors(Integer collectors) {
        this.collectors = collectors;
    }

    public Integer getTransactions() {
        return transactions;
    }

    public void setTransactions(Integer transactions) {
        this.transactions = transactions;
    }

    public Double getSpend() {
        return spend;
    }

    public void setSpend(Double spend) {
        this.spend = spend;
    }

    public Double getUnits() {
        return units;
    }

    public void setUnits(Double units) {
        this.units = units;
    }
}
