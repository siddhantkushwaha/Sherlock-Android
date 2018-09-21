package com.sherlock.androidapp;

import com.google.android.gms.maps.model.LatLng;

public class Location {

    private double latitude;
    private double longitude;

    public Location() {

    }

    public Location(LatLng latLng) {

        setLatitude(latLng.latitude);
        setLongitude(latLng.longitude);
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
