package com.example.geolocator.myapplication.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.geolocator.myapplication.R;
import com.example.geolocator.myapplication.models.User;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthProvider;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.Result;

import static android.os.Build.VERSION_CODES.N;
import static com.example.geolocator.myapplication.helpers.Constants.TAG;

import java.util.List;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class AddFriend extends AppCompatActivity implements ZXingScannerView.ResultHandler {
    EditText etInput;
    DatabaseReference mDatabase;
    String friend_name,friend_email, userId;
    public List<User> friendList;
    private ZXingScannerView mScannerView;
    String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);
//        mScannerView = new ZXingScannerView(this);
                mScannerView= (ZXingScannerView) findViewById(R.id.scanner);
//        setContentView(R.layout.activity_add_friend);
//        mDatabase = FirebaseDatabase.getInstance().getReference();
//        mDatabase = FirebaseDatabase.getInstance().getReference("allusers");
//        etInput = (EditText)findViewById(R.id.etUserId);
//        userId = mDatabase.push().getKey();
//final User user = new User("sid","blahblah@yahoo.com");
//
//        mDatabase.child(userId).setValue(user);
//        Log.d(TAG, "onCreate: " + mDatabase.getKey());
//        ((Button)findViewById(R.id.btnAdd)).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//            }
//        });
//        mScannerView = new ZXingScannerView(this);
//        setContentView(mScannerView);
    }

    @Override
    public void handleResult(final Result result) {
     final  AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Scan Result");
        builder.setMessage(result.getText());

        uid = result.getText();
//        uid = "L2tw9fhZ-dT7Lt8Z2KQ";
        Log.d(TAG, "handleResult: addfriend zxing " + uid);
final String currUID= FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase.getInstance().getReference("users").child(currUID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onDataChange: " + dataSnapshot.exists());
                Log.d(TAG, "onDataChange: "  + dataSnapshot.child("name").getValue());
                friend_name = String.valueOf(dataSnapshot.child("name").getValue());
                friend_email = String.valueOf(dataSnapshot.child("email").getValue());
                String id =String.valueOf(dataSnapshot.child("friend").getValue());
                Log.d(TAG, "onDataChange: id = " + id);
                Log.d(TAG, "onDataChange: " + friend_name + " " + friend_email);
                User user =new User(friend_name,friend_email);
                FirebaseDatabase.getInstance().getReference("users").child(currUID).child("friend").child(result.getText()).setValue(user);

                if(dataSnapshot.exists() == true){
                    FirebaseDatabase.getInstance().getReference("users").child(currUID).child("friend").setValue(result.getText());
                    builder.setMessage("Friend successfully added!");
//                    FirebaseDatabase.getInstance().getReference(currUID).child("friends").setValue(uid);
                }
                else {
                    builder.setMessage("Friend could not be added. Please check the QR Code or try again.");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(TAG, "onCancelled: ");
            }
        });
        Log.d(TAG, "handleResult: " );
        builder.setNeutralButton("Exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                startActivity(new Intent(AddFriend.this,MapsActivity.class));
            }
        });
        FirebaseDatabase.getInstance().getReference(uid).orderByChild("name")
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        User user = dataSnapshot.getValue(User.class);
                        Log.d(TAG, "onChildAdded: final vala" );
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                        Log.d(TAG, "onChildChanged: ");
                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                        Log.d(TAG, "onChildRemoved: ");
                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                        Log.d(TAG, "onChildMoved: ");
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d(TAG, "onCancelled: " + databaseError);
                    }
                });
        AlertDialog alert1 = builder.create();
        alert1.show();

        // If you would like to resume scanning, call this method below:
        mScannerView.resumeCameraPreview(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
    }
}
