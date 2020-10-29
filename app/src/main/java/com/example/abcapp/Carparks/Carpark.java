package com.example.abcapp.Carparks;

import com.example.abcapp.ABCLocation;
import com.example.abcapp.ABCMarker;
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
import java.util.ArrayList;
import java.util.Arrays;

import libraries.qxcg.LatLonCoordinate;
import libraries.qxcg.SVY21Coordinate;

public class Carpark implements Serializable, ABCLocation{
    private String address;
    private String carparkNo;
    private double rate;
    private String carparkType;
    private transient LatLng coordinates;
    public transient int capacity = -1;
    public transient int availability = -1;
    private transient String snippet = "Carpark information unavailable";

    public Carpark(JSONObject carparkJSON) throws JSONException{
        this.carparkType = carparkJSON.getString("car_park_type");
        this.address = carparkJSON.getString("address");
        this.carparkNo = carparkJSON.getString("car_park_no");

        //List of Car Parks Within Central Area
        //Car Park Number	Location of Car Park
        //ACB	Block 270, 271 Albert Centre
        //BBB	Block 232 Bras Basah Complex
        //BRB1	Block 665 Tekka Centre
        //CY	Block 269, 269A, 269B Cheng Yan Court
        //DUXM	Block 1 The Pinnacle @ Duxton
        //HLM	Block 531A Upper Cross Street
        //KAB	Block 334 Kreta Ayer Road
        //KAM	Block 335 Kreta Ayer Road
        //KAS	Block 333 Kreta Ayer Road
        //PRM	Block 33 Park Crescent
        //SLS	Block 4 Sago Lane
        //SR1	Block 10 Selegie Road
        //SR2	Block 8, 9 Selegie Road
        //TPM	Tanjong Pagar Plaza
        //UCS	Block 34 Upper Cross Street
        //WCB	Block 261, 262, 264 Waterloo Centre

        if ((new ArrayList(Arrays.asList(
                "ACB", "BBB", "BRB1", "CY", "DUXM", "HLM", "KAB",
                "KAS", "PRM", "SLS", "SR1", "SR2", "TPM", "UCS", "WCB"))).contains(this.carparkNo)){
            rate = 1.2;
        }
        else{
            rate = 0.6;
        }

        // the provided coordinates for the carparks are in svy21 format
        // Hence we use the parser from https://github.com/cgcai/SVY21 to convert to latitude and longitude
        float x_coord = Float.parseFloat(carparkJSON.getString("x_coord"));
        float y_coord = Float.parseFloat(carparkJSON.getString("y_coord"));
        LatLonCoordinate latlon_coord = new SVY21Coordinate(y_coord, x_coord).asLatLon();
        this.coordinates = new LatLng(latlon_coord.getLatitude(), latlon_coord.getLongitude());
    }
    /* accessors */
    // method to access the carpark number eg "ACB"
    public String getCarparkNo() {
        return this.carparkNo;
    }

    // method to access the coordinates
    public LatLng getCoordinates() {
        return this.coordinates;
    }

    // method to access the carpark type
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
    public void updateAvailability(int lotsAvail, int totalCapacity) {
        this.capacity = totalCapacity;
        this.availability = lotsAvail;

        // update the ABCMarker to reflect the carpark availability
        snippet = String.format("rate:$%.2f ", this.rate) + " lots available: " + lotsAvail + ", total capacity: " + totalCapacity;
    }

    // method to access the carpark type
    public String getCarparkType() {
        return this.carparkType;
    }

    // method to access the parking rate
    public double getRate(){
        return this.rate;
    }

    // implement getMarkerColor, default all carparks to violet
    public float getMarkerColor(){
        return BitmapDescriptorFactory.HUE_VIOLET;
    }

    // implement getSnippet method to access snippet (formatted string including lots available, etc)
    public String getSnippet(){
        return this.snippet;
    }

    // toString method for searching
    public String toString(){
        return this.getAddress();
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
