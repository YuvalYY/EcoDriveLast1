package com.gmail.liorsiag.ecodrive.view;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.gmail.liorsiag.ecodrive.R;
import com.gmail.liorsiag.ecodrive.controller.DrivingController;

public final class DrivingActivity extends AppCompatActivity {
    private final static String TAG="DrivingActivity";
    DrivingController mController;
    TextView mActualSpeed,mFuelConsumption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mController = new DrivingController(this);
        setContentView(R.layout.activity_driving);
        initVars();
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

    private void initVars(){
        mActualSpeed=findViewById(R.id.text_actual_speed);
        mFuelConsumption=findViewById(R.id.text_fuel_consumption);
    }

    protected void registerListeners() {
        //register stop button
    }

    public void setActualSpeed(final String value) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mActualSpeed.setText(value+"km/h");
            }
        });
    }

    public void setFuelConsumption(final String value) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mFuelConsumption.setText(value+"l/h");
            }
        });
    }

    public void setDesiredSpeed(int value) {

    }

    public void setSpeedIndicator(int value) {

    }
}
