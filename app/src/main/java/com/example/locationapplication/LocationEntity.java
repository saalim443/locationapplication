package com.example.locationapplication;

public class LocationEntity {

    private double latitude;
    private double longitude;
    private long timestamp;

    public LocationEntity(double latitude, double longitude, long timestamp) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "LocationEntity{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                ", timestamp=" + timestamp +
                '}';
    }
}
