package com.example.abcapp;

import com.google.android.gms.maps.model.LatLng;

public class BasicLocation implements ABCLocation{

    private float markerColor;
    private LatLng coordinates;
    private String address;

    public BasicLocation(LatLng coordinates, String address, float markerColor){
        this.coordinates = coordinates;
        this.address = address;
        this.markerColor = markerColor;
    }

    public float getMarkerColor(){
        return markerColor;
    }

    @Override
    public LatLng getCoordinates() {
        return coordinates;
    }

    @Override
    public String getAddress() {
        return address;
    }
    public String getSnippet(){
        return "";
    }
}
