package com.ece.triplea;

public class MyLocation {
    private int locationId, childId;
    private double latitude, longitude;
    private String time, childName;

    public String getChildName() {
        return childName;
    }

    public void setChildName(String childName) {
        this.childName = childName;
    }

    public MyLocation(int locationId, int childId, String childName, double latitude, double longitude, String time) {
        this.locationId = locationId;
        this.childId = childId;
        this.childName = childName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.time = time;
    }

    public int getLocationId() {
        return locationId;
    }

    public void setLocationId(int locationId) {
        this.locationId = locationId;
    }

    public int getChildId() {
        return childId;
    }

    public void setChildId(int childId) {
        this.childId = childId;
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

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
