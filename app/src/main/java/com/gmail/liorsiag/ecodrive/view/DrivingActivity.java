package com.gmail.liorsiag.ecodrive.view;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.gmail.liorsiag.ecodrive.R;
import com.gmail.liorsiag.ecodrive.controller.DrivingController;

public final class DrivingActivity extends AppCompatActivity {
    private final static String TAG="DrivingActivity";
    DrivingController mController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mController = new DrivingController(this);
        setContentView(R.layout.activity_driving);
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mController.endDrive();
            }
        });
        //register the gps listener
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        mController.onDestroy();
        super.onDestroy();
    }

    protected void registerListeners() {
        //register stop button
    }

    public void setActualSpeed(int value) {

    }

    public void setFuelConsumption(double value) {

    }

    public void setDesiredSpeed(int value) {

    }

    public void setSpeedIndicator(int value) {

    }
}
