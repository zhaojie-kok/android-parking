package com.example.abcapp.Carparks;

import com.example.abcapp.ABCMarker;
import com.example.abcapp.MapController;
import com.example.abcapp.MapsActivity;
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
    public transient int capacity = -1;
    public transient int availability = -1;
    private ABCMarker abcMarker;


    // Constructor for creating a carpark from a JSONObject
    public Carpark(JSONObject carparkJSON) throws JSONException {
        this.carparkType = carparkJSON.getString("car_park_type");
        this.address = carparkJSON.getString("address");
        this.carparkNo = carparkJSON.getString("car_park_no");

        // the provided coordinates for the carparks are in svy21 format
        // Hence we use the parser from https://github.com/cgcai/SVY21 to convert to latitude and longitude
        float x_coord = Float.parseFloat(carparkJSON.getString("x_coord"));
        float y_coord = Float.parseFloat(carparkJSON.getString("y_coord"));
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

    /* accessors */
    public String getCarparkNo() {
        return this.carparkNo;
    }

    public LatLng getCoordinates() {
        return this.coordinates;
    }

    public String getAddress() {
        return this.address;
    }

    // method to get the number of lots available
    public int getAvailability() {
        return this.availability;
    }

    // method to get the total capacity of the carpark
    public int getCapacity() {
        return this.capacity;
    }
    /* accessors */

    // method to update the number of lots available and total number of lots
    public void updateAvailability(int lotsAvail, int totalCapacity, GoogleMap mMap) {
        this.capacity = totalCapacity;
        this.availability = lotsAvail;

        // update the ABCMarker to reflect the carpark availability
        String newInfo = "lots available: " + lotsAvail + ", total capacity: " + totalCapacity;
        this.abcMarker.updateMarker(
                this.abcMarker.getMarkerOptions().snippet(newInfo),
                mMap,
                this.abcMarker.isShown());
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

    // method to access the carpark's ABCMarker
    public ABCMarker getAbcMarker() {
        return this.abcMarker;
    }

    // method to access the carpark type
    public String getCarparkType() {
        return this.carparkType;
    }
}
