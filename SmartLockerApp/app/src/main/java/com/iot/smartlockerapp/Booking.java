package com.iot.smartlockerapp;

import com.google.firebase.firestore.IgnoreExtraProperties;

import java.io.Serializable;

@IgnoreExtraProperties
public class Booking implements Serializable {

    private String user;
    private String city;
    private String park;
    private String date;
    private String km;
    private boolean active;
    private String lockHash;
    private String leave;

    public Booking(){ }

    public Booking(String user, String park, String date, boolean active, String lockHash, String leave) {
        this.user = user;
        this.park = park;
        this.date = date;
        this.active = active;
        this.lockHash = lockHash;
        this.leave = leave;
    }


    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPark() {
        return park;
    }

    public void setPark(String park) {
        this.park = park;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getKm() {
        return km;
    }

    public void setKm(String km) {
        this.km = km;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getLockHash() {
        return lockHash;
    }

    public void setLockHash(String lockHash) {
        this.lockHash = lockHash;
    }

    public String getLeave(){
        return leave;
    }

    public void setLeave(String leave){
        this.leave = leave;
    }
}
