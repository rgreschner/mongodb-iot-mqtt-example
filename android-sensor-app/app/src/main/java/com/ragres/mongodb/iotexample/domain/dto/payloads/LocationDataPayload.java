package com.ragres.mongodb.iotexample.domain.dto.payloads;

import android.location.Location;

public class LocationDataPayload extends AbstractPayload {
    private double longitude;
    private double latitude;
    private double altitude;
    private float bearing;
    private String provider;

    public static LocationDataPayload fromLocation(Location location) {
        LocationDataPayload locationDataPayload = new LocationDataPayload();
        locationDataPayload.setAltitude(location.getAltitude());
        locationDataPayload.setBearing(location.getBearing());
        locationDataPayload.setProvider(location.getProvider());
        locationDataPayload.setLongitude(location.getLongitude());
        locationDataPayload.setLatitude(location.getLatitude());
        return locationDataPayload;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public float getBearing() {
        return bearing;
    }

    public void setBearing(float bearing) {
        this.bearing = bearing;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }
}
