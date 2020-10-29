package com.example.abcapp;

import com.google.android.gms.maps.model.LatLng;

public interface ABCLocation {
    public LatLng getCoordinates();
    public ABCMarker getAbcMarker();
}
