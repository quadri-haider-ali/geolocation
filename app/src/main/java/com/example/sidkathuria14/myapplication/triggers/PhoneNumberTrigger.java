package com.example.geolocator.myapplication.triggers;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;

/**
 * Created by geolocator on 2/1/18.
 */

public class PhoneNumberTrigger extends BaseTrigger {
    public final static String PHONE_NUMBER_TRIGGER = "1234";

    public PhoneNumberTrigger(Activity context)
    {
        super (context);
    }

    @Override
    public void activateTrigger() {

        OutgoingCallReceiver ocr = new OutgoingCallReceiver();
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_NEW_OUTGOING_CALL);
        getContext().registerReceiver(ocr,intentFilter);

    }
}
