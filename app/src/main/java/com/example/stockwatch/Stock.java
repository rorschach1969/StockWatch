package com.example.stockwatch;

import java.io.Serializable;

public class Stock implements Serializable, Comparable<Stock> {
    private String symbol;
    private String name;
    private double price;
    private double change;
    private double changePct;

    public Stock(String symbol, String name, double price, double change, double changePct) {
        this.symbol = symbol;
        this.name = name;
        this.price = price;
        this.change = change;
        this.changePct = changePct;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public double getChange() {
        return change;
    }

    public double getChangePct() {
        return changePct;
    }

    @Override
    public int compareTo(Stock o) {
        return this.getSymbol().compareTo(o.getSymbol());
    }
}
