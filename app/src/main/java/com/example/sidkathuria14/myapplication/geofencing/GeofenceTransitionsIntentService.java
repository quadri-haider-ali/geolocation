/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.geolocator.myapplication.geofencing;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.concurrent.TimeUnit;


import static com.example.geolocator.myapplication.helpers.Constants.CONNECTION_TIME_OUT_MS;
import static com.example.geolocator.myapplication.helpers.Constants.GEOFENCE_DATA_ITEM_PATH;
import static com.example.geolocator.myapplication.helpers.Constants.GEOFENCE_DATA_ITEM_URI;
import static com.example.geolocator.myapplication.helpers.Constants.KEY_GEOFENCE_ID;
import static com.example.geolocator.myapplication.helpers.Constants.TAG;

/**
 * Listens for geofence transition changes.
 */
public class GeofenceTransitionsIntentService extends IntentService
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;

    public GeofenceTransitionsIntentService() {
        super(GeofenceTransitionsIntentService.class.getSimpleName());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    /**
     * Handles incoming intents.
     * @param intent The Intent sent by Location Services. This Intent is provided to Location
     * Services (inside a PendingIntent) when addGeofences() is called.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geoFenceEvent = GeofencingEvent.fromIntent(intent);
        if (geoFenceEvent.hasError()) {
            int errorCode = geoFenceEvent.getErrorCode();
            Log.e(TAG, "Location Services error: " + errorCode);
        } else {

            int transitionType = geoFenceEvent.getGeofenceTransition();
            if (Geofence.GEOFENCE_TRANSITION_ENTER == transitionType) {
                // Connect to the Google Api service in preparation for sending a DataItem.
                mGoogleApiClient.blockingConnect(CONNECTION_TIME_OUT_MS, TimeUnit.MILLISECONDS);
                // Get the geofence id triggered. Note that only one geofence can be triggered at a
                // time in this example, but in some cases you might want to consider the full list
                // of geofences triggered.
                String triggeredGeoFenceId = geoFenceEvent.getTriggeringGeofences().get(0)
                        .getRequestId();
                // Create a DataItem with this geofence's id. The wearable can use this to create
                // a notification.
                final PutDataMapRequest putDataMapRequest =
                        PutDataMapRequest.create(GEOFENCE_DATA_ITEM_PATH);
                putDataMapRequest.getDataMap().putString(KEY_GEOFENCE_ID, triggeredGeoFenceId);
                putDataMapRequest.setUrgent();
                if (mGoogleApiClient.isConnected()) {
                    Wearable.DataApi.putDataItem(
                            mGoogleApiClient, putDataMapRequest.asPutDataRequest()).await();
                } else {
                    Log.e(TAG, "Failed to send data item: " + putDataMapRequest
                            + " - Client disconnected from Google Play Services");
                }
                Toast.makeText(this, "entering geofence",
                        Toast.LENGTH_SHORT).show();
                mGoogleApiClient.disconnect();
            } else if (Geofence.GEOFENCE_TRANSITION_EXIT == transitionType) {
                // Delete the data item when leaving a geofence region.
                mGoogleApiClient.blockingConnect(CONNECTION_TIME_OUT_MS, TimeUnit.MILLISECONDS);
                Wearable.DataApi.deleteDataItems(mGoogleApiClient, GEOFENCE_DATA_ITEM_URI).await();
                showToast(this, "exiting geofence");
                mGoogleApiClient.disconnect();
            }
        }
    }

    /**
     * Showing a toast message, using the Main thread
     */
    private void showToast(final Context context, final String str) {
        Handler mainThread = new Handler(Looper.getMainLooper());
        mainThread.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onConnected(Bundle connectionHint) {
    }

    @Override
    public void onConnectionSuspended(int cause) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
    }

}
