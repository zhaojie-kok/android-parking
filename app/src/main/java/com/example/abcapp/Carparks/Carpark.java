package com.example.abcapp;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import libraries.qxcg.LatLonCoordinate;
import libraries.qxcg.SVY21Coordinate;

public class Carpark implements Serializable {
    private String address;
    private String carparkNo;
    private double rate;
    private String carparkType;
    private transient LatLng coordinates;
    public transient int capacity;
    public transient int availability;
    private ABCMarker abcMarker;


    // Constructor for creating a carpark from a JSONObject
    public Carpark(JSONObject carparkJSON) throws JSONException {
        this.carparkType = carparkJSON.getString("car_park_type");
        this.address = carparkJSON.getString("address");
        this.carparkNo = carparkJSON.getString("car_park_no");

        // the provided coordinates for the carparks are in svy21 format
        // Hence we use the parser from https://github.com/cgcai/SVY21 to convert to latitude and longitude
        double x_coord = Double.parseDouble(carparkJSON.getString("x_coord"));
        double y_coord = Double.parseDouble(carparkJSON.getString("y_coord"));
        LatLonCoordinate latlon_coord = new SVY21Coordinate(y_coord, x_coord).asLatLon();
        this.coordinates = new LatLng(latlon_coord.getLatitude(), latlon_coord.getLongitude());

        // instantiate the marker and create the abcMarker for the carker
        // the ABC marker will be used to interface with the map
        MarkerOptions markerOptions = new MarkerOptions()
                .position(this.coordinates)
                .title(this.address)
                .snippet("Carpark Information Unavailable")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
        this.abcMarker = new ABCMarker(markerOptions, this.address, null);
    }

    public String getCarparkNo() {
        return carparkNo;
    }

    public LatLng getCoordinates() {
        return coordinates;
    }

    // method to get the number of lots available
    public int getAvailability() {
        return this.availability;
    }

    // method to get the total capacity of the carpark
    public int getCapacity() {
        return this.capacity;
    }

    // method to update the number of lots available and total number of lots
    public void updateAvailability(int lotsAvail, int totalCapacity) {
        this.capacity = totalCapacity;
        this.availability = lotsAvail;

        // update the ABCMarker to reflect the carpark availability
        String newInfo = "lots available: " + lotsAvail + ", total capacity: " + totalCapacity;
        this.abcMarker.setMarker(
                this.abcMarker.getMarkerOptions().snippet(newInfo),
                null,
                false);
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

    // method to show a marker of the carpark on the map
    public void showMarker(GoogleMap mMap) {
        this.abcMarker.showMarker(mMap);
    }

    // method to remove the marker from the map
    public void removeMarker() {
        this.abcMarker.removeMarker();
    }
}
