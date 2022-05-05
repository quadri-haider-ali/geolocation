package com.example.geolocator.myapplication.models;

/**
 * Created by geolocator on 15/1/18.
 */

public class Friend {
   private String name;
    private String email;

    public Friend(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }
}
