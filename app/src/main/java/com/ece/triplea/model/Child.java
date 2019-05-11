package com.ece.triplea.model;

public class Child {
    private long childId;
    private String childName, childPhone, childImage;

    public Child(long childId, String childName, String childPhone, String childImage) {
        this.childId = childId;
        this.childName = childName;
        this.childPhone = childPhone;
        this.childImage = childImage;
    }

    public long getChildId() {
        return childId;
    }

    public void setChildId(long childId) {
        this.childId = childId;
    }

    public String getChildName() {
        return childName;
    }

    public void setChildName(String childName) {
        this.childName = childName;
    }

    public String getChildPhone() {
        return childPhone;
    }

    public void setChildPhone(String childPhone) {
        this.childPhone = childPhone;
    }

    public String getChildImage() {
        return childImage;
    }

    public void setChildImage(String childImage) {
        this.childImage = childImage;
    }
}
