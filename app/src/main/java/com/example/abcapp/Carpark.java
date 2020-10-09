package com.example.abcapp;

import com.google.android.gms.maps.model.LatLng;

public class Carpark {
    private int id;
    private String name;
    private double rate;
    private LatLng coordinates;

    public Carpark(int id, String name, double rate, LatLng coordinates){
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

    public LatLng getCoordinates() {
        return coordinates;
    }
}
