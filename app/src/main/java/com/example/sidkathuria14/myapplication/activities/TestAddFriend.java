package com.example.geolocator.myapplication.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.example.geolocator.myapplication.models.User;
import com.example.geolocator.myapplication.services.FirebaseIDService;
import com.example.geolocator.myapplication.services.MyFirebaseMessagingService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import static android.R.attr.data;
import static com.example.geolocator.myapplication.helpers.Constants.TAG;

/**
 * Created by geolocator on 15/1/18.
 */

public class TestAddFriend extends AppCompatActivity {
    String uid;
    String msgId;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        FirebaseMessaging.getInstance()
//                .send(new RemoteMessage.Builder("SENDER_ID" + "BLAHBLAH")
////                .setMessageId(Integer.toString(msgId.incrementAndGet()))
//        .addData("title","body")
//        .build());
      String token = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "onCreate: " + token);

    }
}
