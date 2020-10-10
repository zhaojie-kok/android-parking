package com.example.abcapp;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

public class Carpark implements Serializable {
    private String address;
    private String name;
    private double rate;
    private String carparkType;
    private LatLng coordinates;

    public Carpark(JSONObject carparkJSON) throws JSONException {
        this.carparkType = carparkJSON.getString("car_park_type");
        this.coordinates = new LatLng(carparkJSON.getDouble("x_coord"), carparkJSON.getDouble("y_coord"));
        this.address = carparkJSON.getString("address");
        this.name = carparkJSON.getString("car_park_no");
    }

    public String getName() {
        return name;
    }

    public LatLng getCoordinates() {
        return coordinates;
    }
}
