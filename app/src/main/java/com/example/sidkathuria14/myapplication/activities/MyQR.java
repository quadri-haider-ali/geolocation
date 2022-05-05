package com.example.geolocator.myapplication.activities;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.example.geolocator.myapplication.R;
import com.squareup.picasso.Picasso;

import retrofit2.Retrofit;

public class MyQR extends AppCompatActivity {
Retrofit retrofit;ImageView imgView;
    String uid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_qr);

        imgView = (ImageView)findViewById(R.id.imageqr);

        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
uid = pref.getString("firebase_uid",null);
        Log.d("friendmap", "onCreate: myqr activity  " + uid);
        Picasso.with(this).load("https://chart.googleapis.com//chart?cht=qr&chl=" +
//                "AvONmQvySBZsE4nmiBNFn2CR5Uz1" +
                 uid +  "&chs=160x160&chld=L|0").into(imgView);


    }
}
