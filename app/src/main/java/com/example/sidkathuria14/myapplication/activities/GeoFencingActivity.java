package com.example.geolocator.myapplication.activities;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.geolocator.myapplication.R;
import com.example.geolocator.myapplication.geofencing.GeofenceTransitionsIntentService;
import com.example.geolocator.myapplication.geofencing.SimpleGeofence;
import com.example.geolocator.myapplication.geofencing.SimpleGeofenceStore;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;
import static com.example.geolocator.myapplication.helpers.Constants.TAG;
import static com.example.geolocator.myapplication.helpers.Constants.ANDROID_BUILDING_ID;
import static com.example.geolocator.myapplication.helpers.Constants.ANDROID_BUILDING_LATITUDE;
import static com.example.geolocator.myapplication.helpers.Constants.ANDROID_BUILDING_LONGITUDE;
import static com.example.geolocator.myapplication.helpers.Constants.ANDROID_BUILDING_RADIUS_METERS;
import static com.example.geolocator.myapplication.helpers.Constants.CONNECTION_FAILURE_RESOLUTION_REQUEST;
import static com.example.geolocator.myapplication.helpers.Constants.GEOFENCE_EXPIRATION_TIME;
import static com.example.geolocator.myapplication.helpers.Constants.YERBA_BUENA_ID;
import static com.example.geolocator.myapplication.helpers.Constants.YERBA_BUENA_LATITUDE;
import static com.example.geolocator.myapplication.helpers.Constants.YERBA_BUENA_LONGITUDE;
import static com.example.geolocator.myapplication.helpers.Constants.YERBA_BUENA_RADIUS_METERS;

public class GeoFencingActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks{

    List<Geofence> mGeofenceList;

    // These will store hard-coded geofences in this sample app.
    private SimpleGeofence mAndroidBuildingGeofence;
    private SimpleGeofence mYerbaBuenaGeofence;

    // Persistent storage for geofences.
    private SimpleGeofenceStore mGeofenceStorage;

    private LocationServices mLocationService;
    // Stores the PendingIntent used to request geofence monitoring.
    private PendingIntent mGeofenceRequestIntent;
    private GoogleApiClient mApiClient;

    // Defines the allowable request types (in this example, we only add geofences).
    private enum REQUEST_TYPE {ADD}
    private REQUEST_TYPE mRequestType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Rather than displayng this activity, simply display a toast indicating that the geofence
        // service is being created. This should happen in less than a second.
        if (!isGooglePlayServicesAvailable()) {
            Log.e(TAG, "Google Play services unavailable.");
            finish();
            return;
        }

        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mApiClient.connect();

        // Instantiate a new geofence storage area.
        mGeofenceStorage = new SimpleGeofenceStore(this);
        // Instantiate the current List of geofences.
        mGeofenceList = new ArrayList<Geofence>();
        createGeofences();
    }

    /**
     * In this sample, the geofences are predetermined and are hard-coded here. A real app might
     * dynamically create geofences based on the user's location.
     */
    public void createGeofences() {
        // Create internal "flattened" objects containing the geofence data.
        mAndroidBuildingGeofence = new SimpleGeofence(
                ANDROID_BUILDING_ID,                // geofenceId.
                ANDROID_BUILDING_LATITUDE,
                ANDROID_BUILDING_LONGITUDE,
                ANDROID_BUILDING_RADIUS_METERS,
                GEOFENCE_EXPIRATION_TIME,
                Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT
        );
        mYerbaBuenaGeofence = new SimpleGeofence(
                YERBA_BUENA_ID,                // geofenceId.
                YERBA_BUENA_LATITUDE,
                YERBA_BUENA_LONGITUDE,
                YERBA_BUENA_RADIUS_METERS,
                GEOFENCE_EXPIRATION_TIME,
                Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT
        );

        // Store these flat versions in SharedPreferences and add them to the geofence list.
        mGeofenceStorage.setGeofence(ANDROID_BUILDING_ID, mAndroidBuildingGeofence);
        mGeofenceStorage.setGeofence(YERBA_BUENA_ID, mYerbaBuenaGeofence);
        mGeofenceList.add(mAndroidBuildingGeofence.toGeofence());
        mGeofenceList.add(mYerbaBuenaGeofence.toGeofence());
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // If the error has a resolution, start a Google Play services activity to resolve it.
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                Log.e(TAG, "Exception while resolving connection error.", e);
            }
        } else {
            int errorCode = connectionResult.getErrorCode();
            Log.e(TAG, "Connection to Google Play services failed with error code " + errorCode);
        }
    }

    /**
     * Once the connection is available, send a request to add the Geofences.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        // Get the PendingIntent for the geofence monitoring request.
        // Send a request to add the current geofences.
        mGeofenceRequestIntent = getGeofenceTransitionPendingIntent();
        LocationServices.GeofencingApi.addGeofences(mApiClient, mGeofenceList,
                mGeofenceRequestIntent);
        Toast.makeText(this, "geofence service started", Toast.LENGTH_SHORT).show();
//        finish();
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (null != mGeofenceRequestIntent) {
            LocationServices.GeofencingApi.removeGeofences(mApiClient, mGeofenceRequestIntent);
        }
    }


    /**
     * Checks if Google Play services is available.
     * @return true if it is.
     */
    private boolean isGooglePlayServicesAvailable() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == resultCode) {
            if (Log.isLoggable(TAG, Log.DEBUG)) {
                Log.d(TAG, "Google Play services is available.");
            }
            return true;
        } else {
            Log.e(TAG, "Google Play services is unavailable.");
            return false;
        }
    }

    /**
     * Create a PendingIntent that triggers GeofenceTransitionIntentService when a geofence
     * transition occurs.
     */
    private PendingIntent getGeofenceTransitionPendingIntent() {
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

}
