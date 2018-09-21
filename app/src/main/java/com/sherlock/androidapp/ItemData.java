package com.sherlock.androidapp;

import com.google.android.gms.maps.model.LatLng;

public class ItemData {

    private String itemId;
    private String itemName;
    private String itemPhoto;
    private String timestamp;
    private Location location;

    public String getItemName() {
        return itemName;
    }

    public String getItemPhoto() {
        return itemPhoto;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public void setItemPhoto(String itemPhoto) {
        this.itemPhoto = itemPhoto;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public Location getLocation() {
        return location;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public void clone(ItemData itemData) {

        setItemPhoto(itemData.getItemPhoto());
        setTimestamp(itemData.getTimestamp());
        setItemName(itemData.getItemName());
        setItemId(itemData.getItemId());
        setLocation(itemData.getLocation());
    }
}
