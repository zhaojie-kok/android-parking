package com.example.abcapp;

public class Carpark {
    private int id;
    private String name;
    private double rate;
    private double[] coordinates;

    public Carpark(int id, String name, double rate, double[] coordinates){
        this.id = id;
        this.name = name;
        this.rate = rate;
        this.coordinates = coordinates;
    }

    public double getRate() {
        return rate;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double[] getCoordinates() {
        return coordinates;
    }
}
