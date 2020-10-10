package com.example.abcapp;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Carpark implements Serializable {
    private String address;
    private String name;
    private double rate;
    private String carparkType;
    private transient LatLng coordinates;

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

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeDouble(coordinates.latitude);
        out.writeDouble(coordinates.longitude);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        coordinates = new LatLng(in.readDouble(), in.readDouble());
    }
}
