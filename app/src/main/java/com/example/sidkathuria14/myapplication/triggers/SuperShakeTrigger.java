package com.example.geolocator.myapplication.triggers;

import android.app.Activity;
import android.content.Context;
import android.hardware.SensorManager;

import com.squareup.seismic.ShakeDetector;

/**
 * Created by geolocator on 2/1/18.
 */

public class SuperShakeTrigger extends BaseTrigger {
    public SuperShakeTrigger(Activity context)
    {
        super (context);
    }

    @Override
    public void activateTrigger() {

        //setup shake detection using ShakeDetector library
        SensorManager sensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);

        ShakeDetector sd = new ShakeDetector(new ShakeDetector.Listener()
        {
            public void hearShake() {

                //you shook me!
                launchPanicIntent(getContext());

            }
        });

        sd.start(sensorManager);

    }
}
