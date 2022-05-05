package com.example.geolocator.myapplication.models;

/**
 * Created by geolocator on 12/1/18.
 */

public class User {
    public String name;
    public String email;
public Friend[] friend;
    // Default constructor required for calls to
    // DataSnapshot.getValue(User.class)
    public User() {
    }

    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }
    public User(String name,String email,Friend[] friend){
        this.name = name;
        this.friend = friend;
        this.email = email;
    }

}
