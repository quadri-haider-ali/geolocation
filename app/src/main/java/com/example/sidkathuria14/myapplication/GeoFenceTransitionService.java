package com.example.geolocator.myapplication;

import android.app.Activity;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.geolocator.myapplication.activities.MainActivity;
import com.example.geolocator.myapplication.activities.MapsActivity;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;

import java.util.ArrayList;
import java.util.List;
import static com.example.geolocator.myapplication.helpers.Constants.TAG;
/**
 * Created by geolocator on 13/1/18.
 */

public class GeoFenceTransitionService extends IntentService {

    public static final int GEOFENCE_NOTIFICATION_ID = 0;
    public static final String ACTION_SMS_SENT = "com.techblogon.android.apis.os.SMS_SENT_ACTION";
    public GeoFenceTransitionService() {
        super(GeoFenceTransitionService.class.getSimpleName());
        Log.d(TAG, "GeoFenceTransitionService: ");
    }
//    public void onClickSend(View v)
//    {
        //Get recipient from user and check for null
//        if (TextUtils.isEmpty(recipientTextEdit.getText())) {
//            titleTextView.setText("Enter Receipent");
//            titleTextView.setTextColor(Color.RED);
//            return;
//        }

        //Get content and check for null
//        if (TextUtils.isEmpty(contentTextEdit.getText())) {
//            titleTextView.setText("Empty Content");
//            titleTextView.setTextColor(Color.RED);
//            return;
//        }

//    }


    @Override
    protected void onHandleIntent(Intent intent) {
        // Retrieve the Geofencing intent
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        Log.d(TAG, "onHandleIntent: ");
        AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);

//        //sms body coming from user input
//        String strSMSBody = "hey I am at location- " + " . It seems its not safe over here.Please remain in touch .";
//        //sms recipient added by user from the activity screen
//        String strReceipentsList = "8076997187";
//        //recipientTextEdit.getText().toString();
//        SmsManager sms = SmsManager.getDefault();
//        List<String> messages = sms.divideMessage(strSMSBody);
//        for (String message : messages) {
//            sms.sendTextMessage(strReceipentsList, null, message, PendingIntent.getBroadcast(
//                    this, 0, new Intent(ACTION_SMS_SENT), 0), null);
//        }




        // Handling errors
        if ( geofencingEvent.hasError() ) {
            String errorMsg = getErrorString(geofencingEvent.getErrorCode() );
            Log.e( TAG, errorMsg );
            return;
        }

        // Retrieve GeofenceTrasition
        int geoFenceTransition = geofencingEvent.getGeofenceTransition();
        // Check if the transition type
        if ( geoFenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geoFenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT ) {
            // Get the geofence that were triggered
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
            Log.d(TAG, "onHandleIntent: "+triggeringGeofences);
            // Create a detail message with Geofences received
            String geofenceTransitionDetails = getGeofenceTrasitionDetails(geoFenceTransition, triggeringGeofences );
            // Send notification details as a String
            sendNotification( geofenceTransitionDetails );
//            createNotification("blah",intent);
        }
    }
//    private void sendSMS(String phoneNumber, String message) {
//        ArrayList<PendingIntent> sentPendingIntents = new ArrayList<PendingIntent>();
//        ArrayList<PendingIntent> deliveredPendingIntents = new ArrayList<PendingIntent>();
//        PendingIntent sentPI = PendingIntent.getBroadcast(getApplicationContext(), 0,
//                new Intent(getApplicationContext(), SmsSentReceiver.class), 0);
//        PendingIntent deliveredPI = PendingIntent.getBroadcast(getApplicationContext(), 0,
//                new Intent(getApplicationContext(), SmsDeliveredReceiver.class), 0);
//        try {
//            SmsManager sms = SmsManager.getDefault();
//            ArrayList<String> mSMSMessage = sms.divideMessage(message);
//            for (int i = 0; i < mSMSMessage.size(); i++) {
//                sentPendingIntents.add(i, sentPI);
//                deliveredPendingIntents.add(i, deliveredPI);
//            }
//            sms.sendMultipartTextMessage(phoneNumber, null, mSMSMessage,
//                    sentPendingIntents, deliveredPendingIntents);
//
//        } catch (Exception e) {
//
//            e.printStackTrace();
//            Toast.makeText(getBaseContext(), "SMS sending failed...",Toast.LENGTH_SHORT).show();
//        }
//
//    }
    // Create a detail message with Geofences received
    private String getGeofenceTrasitionDetails(int geoFenceTransition, List<Geofence> triggeringGeofences) {
        // get the ID of each geofence triggered
        ArrayList<String> triggeringGeofencesList = new ArrayList<>();
        for ( Geofence geofence : triggeringGeofences ) {
            Log.d(TAG, "getGeofenceTrasitionDetails: geogfence Request Id "+geofence.getRequestId());
            triggeringGeofencesList.add( geofence.getRequestId() );
        }

        String status = null;
        if ( geoFenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER )
            status = "Entering ";
        else if ( geoFenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT )
            status = "Exiting ";
        Log.d(TAG, "getGeofenceTrasitionDetails: "+triggeringGeofencesList);
        return status + TextUtils.join( ", ", triggeringGeofencesList);
    }

    // Send a notification
    private void sendNotification( String msg ) {
        Log.i(TAG, "sendNotification: " + msg );

        // Intent to start the main Activity
        Intent notificationIntent = new Intent(this, MapsActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(notificationIntent);
        PendingIntent notificationPendingIntent = stackBuilder.getPendingIntent(0,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // Creating and sending Notification
        NotificationManager notificatioMng =
                (NotificationManager) getSystemService( Context.NOTIFICATION_SERVICE );
        notificatioMng.notify(
                GEOFENCE_NOTIFICATION_ID,
                createNotification(msg, notificationPendingIntent));
    }

    // Create a notification
    private Notification createNotification(String msg, PendingIntent notificationPendingIntent) {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
        notificationBuilder
                .setSmallIcon(R.drawable.ic_location_pin)
//                .setColor(Color.RED)
                .setContentTitle(msg)
                .setContentText("Geofence Notification!")
                .setContentIntent(notificationPendingIntent)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND)
                .setAutoCancel(true);
        return notificationBuilder.build();
    }

    // Handle errors
    private static String getErrorString(int errorCode) {
        switch (errorCode) {
            case GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE:
                return "GeoFence not available";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES:
                return "Too many GeoFences";
            case GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS:
                return "Too many pending intents";
            default:
                return "Unknown error.";
        }
    }
}
