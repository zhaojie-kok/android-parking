package com.example.abcapp;

import com.google.android.gms.maps.model.LatLng;

public class Carpark {
    private String address;
    private String name;
    private double rate;
    private String carparkType;
    private LatLng coordinates;

    public Carpark(String carparkType, LatLng coordinates, String address, String name){
        this.carparkType = carparkType;
        this.coordinates = coordinates;
        this.address = address;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public LatLng getCoordinates() {
        return coordinates;
    }
}
