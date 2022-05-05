package com.example.geolocator.myapplication.activities;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import static com.example.geolocator.myapplication.R.id.map;
import static com.example.geolocator.myapplication.helpers.Constants.TAG;

import com.example.geolocator.myapplication.GeoFenceTransitionService;
import com.example.geolocator.myapplication.R;
import com.example.geolocator.myapplication.models.User;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;

import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
//import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        PlaceSelectionListener,
        ResultCallback<Status>
    ,GoogleMap.OnMarkerClickListener
//        , LocationListener

{
    List<Address> addressList;
    String mapAddress;
    Geocoder geocoder;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public   String latlng;
    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;

    GoogleApiClient googleApiClient;
    public static final int MY_LOCATION_REQUEST_CODE = 111;
    boolean bPermissionGranted;EditText etMessage,etDefault;String message,defaultMessage = "Hey there! I am currently at - ";Spinner spinner;
    Location currentLocation;
    GoogleMap googleMap;
    LocationManager locMan;LocationListener locLis;


    private final int UPDATE_INTERVAL =  1000;
    private final int FASTEST_INTERVAL = 900;
    private final int REQ_PERM_CODE=111;
    private static final long GEO_DURATION = 60 * 60 * 1000;
    private static final String GEOFENCE_REQ_ID = "My Geofence";
    private static final float GEOFENCE_RADIUS = 50.0f; // in meters
    private final int GEOFENCE_REQ_CODE = 0;
    private final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    private final int PLACE_PICKER_REQUEST = 2;
    private Circle geoFenceLimits;
    private PendingIntent geoFencePendingIntent;
    private Marker geoFencemarker;
    DatabaseReference mDatabase;
    String uid;
    String name,email;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);



        mDatabase = FirebaseDatabase.getInstance().getReference("users");

        FirebaseAuth auth= FirebaseAuth.getInstance();
        FirebaseUser mUser= auth.getCurrentUser();
        uid =  mUser.getUid();
        Log.d("friendmap", "onCreate: " + uid);
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("firebase_uid",uid);
        editor.commit();
       name =  getIntent().getStringExtra("name");
        email = getIntent().getStringExtra("email");
        User user = new User(name,email);
        mDatabase.child(uid).setValue(user);

//        FirebaseMessaging.getInstance().subscribeToTopic("pushNotifications");

        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
autocompleteFragment.setOnPlaceSelectedListener(this);
            final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(map);
            mapFragment.getMapAsync(this);
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                checkLocationPermission();
            }

        createGoogleAPIClient();
        mGoogleApiClient.connect();

geocoder= new Geocoder(this, Locale.getDefault());

            etMessage = (EditText) findViewById(R.id.etMessage);
//etMessage.setText(getLocation());

            ((Button) findViewById(R.id.quicksend)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                    builder.setTitle("choose a friend");
                    builder.setPositiveButton("select", null);
                    builder.setNegativeButton("cancel", null);
                    AlertDialog dialog = builder.create();
                    dialog.getWindow().getAttributes().windowAnimations = R.style.DialogTheme; //style id
                    dialog.show();
                }
            });

            ((Button) findViewById(R.id.btn)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    message = etMessage.getText().toString();
                    Intent share = new Intent(Intent.ACTION_SEND);
                    share.setType("text/plain");
                    share.putExtra(Intent.EXTRA_TEXT, message);
                    startActivity(Intent.createChooser(share, "Share using"));
                }

            });

        locMan = (LocationManager) getSystemService(LOCATION_SERVICE);
        locLis = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                String lat,lng;
                Log.d(TAG, "lat: " + location.getLatitude());
                Log.d(TAG, "lng: " + location.getLongitude());
                Log.d(TAG, "prov: " + location.getProvider());
                Log.d(TAG, "accuracy: " + location.getAccuracy());
                Log.d(TAG, "alt: " + location.getAltitude());
                Log.d(TAG, "speed: " + location.getSpeed());
                lat = String.valueOf(location.getLatitude());
                lng = String.valueOf(location.getLongitude());
                latlng = "("+lat + "," + lng +")";
                mapAddress = " http://maps.google.com/maps?q=loc:" + lat + "," + lng;
                try {
                    addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(),1);
                    if(addressList.size() >0){
                        Log.d(TAG, "onLocationChanged: " + addressList.get(0));
                    }

                } catch(IOException ioe){

                }
                etMessage.setText(defaultMessage +
//                        " ( " + lat + " , " + lng + " )" +
                        "  " + addressList.get(0).getAddressLine(0) + mapAddress);
//                        latlng );
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(new LatLng(
//            mMap.getMyLocation().getLatitude(),mMap.getMyLocation().getLongitude())
                                location.getLatitude(),location.getLongitude())
                        ,7.0f)));
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };
        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) &&

                (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED)) {

            ActivityCompat.requestPermissions(this,
                    new String[] {
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    }, 234);
        } else {
            startLocationTracking();
        }
        ((Button)findViewById(R.id.startgeofencing)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startGeofence();
            }
        });

    }

public void getAddress(double lat,double lng){

}
    @SuppressWarnings("MissingPermission")
    void startLocationTracking () {

        locMan.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                10000,
                10,
                locLis
        );
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (permissions[0].equals(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationTracking();
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Boolean isNight;
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        if(hour < 6 || hour > 18){
            googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this,R.raw.night));
        } else if(hour >12 && hour <18){
            googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this,R.raw.noon));
        } else{
            googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this,R.raw.retro));
        }


        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //User has previously accepted this permission
            if (ActivityCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
            }
        } else {
            //Not in api-23, no need to prompt
            mMap.setMyLocationEnabled(true);
        }
//        mMap.setMapType(R.string.style_map);
//        mMap.setMapStyle(
//                MapStyleOptions.loadRawResourceStyle(
//                        this, R.string.style_map));

//        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
//        CameraPosition.builder(new LatLng(mMap.getMyLocation().getLatitude(),
//                mMap.getMyLocation().getLongitude()),0.4f,20°,);
//        mMap.animateCamera(CameraUpdateFactory.newLatLng(
////                new LatLng(mMap.getMyLocation().getLatitude(), mMap.getMyLocation().getLongitude())));
//                new LatLng(latlng[])  ));
//        currentLocation = new Location(new LatLng(mMap.getMyLocation().getLatitude(),mMap.getMyLocation().getLongitude()),"");
//        currentLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
//        Log.d(TAG, "onMapReady: " + String.valueOf(currentLocation.getLatitude()));
//mMap.moveCamera(CameraUpdateFactory.zoomBy(1.8f));
//        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(new LatLng(mMap.getMyLocation().getLatitude(),mMap.getMyLocation().getLongitude()),2.0f)));
        Log.d(TAG, "onMapReady: " + latlng);

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                float zoomlevel = mMap.getCameraPosition().zoom;
                mMap.clear();
                markerForGeofence(latLng);
//                mMap.addMarker(new MarkerOptions().position(latLng));
                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(latLng,zoomlevel)));

            }
        });

    }
    private void markerForGeofence(LatLng latLng){
        Log.i(TAG, "markerForGeofence: ");
        String title ="lat:"+latLng.latitude+"long :"+latLng.longitude;
        MarkerOptions markerOptions= new MarkerOptions()
                .position(latLng)
                .title(title);
        if(mMap!=null) {
            if (geoFencemarker!= null) {
                geoFencemarker.remove();
            }
            geoFencemarker = mMap.addMarker(markerOptions);
        }
    }
    private void startGeofence() {
        Log.i(TAG, "startGeofence()");
        if( geoFencemarker != null ) {
            Geofence geofence = createGeofence( geoFencemarker.getPosition(), GEOFENCE_RADIUS );
            GeofencingRequest geofenceRequest = createGeofenceRequest( geofence );
            addGeofence( geofenceRequest );
        } else {
            Log.e(TAG, "Geofence marker is null");
        }
    }
    private Geofence createGeofence(LatLng latLng, float radius ) {
        Log.d(TAG, "createGeofence");
        Log.d(TAG, "createGeofence: "+latLng.latitude+":"+latLng.longitude);
        return new Geofence.Builder()
                .setRequestId(GEOFENCE_REQ_ID)
                .setCircularRegion( latLng.latitude, latLng.longitude, radius)
                .setExpirationDuration( GEO_DURATION )
                .setTransitionTypes( Geofence.GEOFENCE_TRANSITION_ENTER
                        | Geofence.GEOFENCE_TRANSITION_EXIT )
                .build();
    }

    private GeofencingRequest createGeofenceRequest(Geofence geofence ) {
        Log.d(TAG, "createGeofenceRequest");
        return new GeofencingRequest.Builder()
                .setInitialTrigger( GeofencingRequest.INITIAL_TRIGGER_ENTER )
                .addGeofence( geofence )
                .build();
    }
    private void addGeofence(GeofencingRequest request) {
        Log.d(TAG, "addGeofence");
        if (checkLocationPermission()) {
            PendingResult<Status> result = LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    request,
                    createGeofencePendingIntent()
            );
            result.setResultCallback(this);
        }
    }

    private void createGoogleAPIClient(){
        Log.d(TAG, "createGoogleAPIClient: ");
        if(mGoogleApiClient==null){
            mGoogleApiClient=new GoogleApiClient.Builder(this)
                    .enableAutoManage(this,this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        Log.d(TAG, "createGoogleAPIClient: "+mGoogleApiClient);
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    private PendingIntent createGeofencePendingIntent() {
        Log.d(TAG, "createGeofencePendingIntent");
        if ( geoFencePendingIntent != null )
            return geoFencePendingIntent;
        Log.d(TAG, "createGeofencePendingIntent: " + "not null");
        Intent intent = new Intent( MapsActivity.this, GeoFenceTransitionService.class);
        return PendingIntent.getService(
                this, GEOFENCE_REQ_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT );
    }

    private void drawGeofence() {
        Log.d(TAG, "drawGeofence()");

        if ( geoFenceLimits != null )
            geoFenceLimits.remove();

        CircleOptions circleOptions = new CircleOptions()
                .center( geoFencemarker.getPosition())
                .strokeColor(Color.argb(50, 70,70,70))
                .fillColor( Color.argb(100, 150,150,150) )
                .radius( GEOFENCE_RADIUS );
        geoFenceLimits = mMap.addCircle( circleOptions );
    }




    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                //  TODO: Prompt with explanation!

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode,
//                                           String permissions[], int[] grantResults) {
//        switch (requestCode) {
//            case MY_PERMISSIONS_REQUEST_LOCATION: {
//                // If request is cancelled, the result arrays are empty.
//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//
//                    // permission was granted, yay!
////                    function(googleMap);
//                    if (ActivityCompat.checkSelfPermission(this,
//                            android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//                        mMap.setMyLocationEnabled(true);
//                    }
//                } else {
//                    // permission denied, boo! Disable the
//                    // functionality that depends on this permission.
//                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
//                }
//                return;
//            }
//


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu,menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.addFriend :
startActivity(new Intent(MapsActivity.this,AddFriend.class));
                return true;
            case R.id.geofencing:
//startActivity(new Intent(MapsActivity.this, GeoFencingActivity.class));
//                geoFence();
                return true;
            case R.id.myqr:
                startActivity(new Intent(MapsActivity.this,MyQR.class));
                return true;
            case R.id.settings:
//                startActivity(new Intent(MapsActivity.this,S));
            case R.id.signOut:
//                signOut();
            default: return super.onOptionsItemSelected(item);
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                Log.i(TAG, "Place: " + place.getName());
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                // TODO: Handle the error.
                Log.i(TAG, status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
//        Log.d(TAG, "onConnected: "  + String.valueOf(currentLocation.getLatitude()));
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended: ");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed: ");
    }


    @Override
    public void onPlaceSelected(Place place) {
        Log.d(TAG, "onPlaceSelected: " + place.getName());
        if (mMap != null) {
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(place.getLatLng()));
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(place.getLatLng(), 12.4f)));
        }
    }

    @Override
    public void onError(Status status) {
        Log.d(TAG, "onError: ");
    }

    @Override
    public void onResult(@NonNull Status status) {
        if ( status.isSuccess() ) {
            drawGeofence();
        } else {
            // inform about fail
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.setVisible(true);
        return false;
    }
}
