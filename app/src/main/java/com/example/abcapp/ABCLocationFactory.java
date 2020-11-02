package com.example.abcapp;

import com.example.abcapp.Carparks.Carpark;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;

public class ABCLocationFactory {
    public static final int START = 0;
    public static final int END = 1;
    public static final int CARPARK = 2;
    public static ABCLocation getABCLocation(Object[] o, int type){
        ABCLocation abcLocation = null;
        if (type == START){
            // If start, provide pos: LatLng and name: String
            abcLocation =  new BasicLocation((LatLng)o[0], (String)o[1], BitmapDescriptorFactory.HUE_RED);
        }
        else if (type == END){
            // If end, provide pos: LatLng and name: String
            abcLocation =  new BasicLocation((LatLng)o[0], (String)o[1], BitmapDescriptorFactory.HUE_BLUE);
        }
        else if (type == CARPARK){
            // If carpark, provide  carpark: Carpark
            abcLocation = (Carpark) o[0];
        }
        return abcLocation;
    }
}
