package com.iot.smartlockerapp;

import com.google.firebase.firestore.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Park {

    private String name;

    public Park(){
    }

    public Park(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
