package com.gmail.liorsiag.ecodrive.controller;

import android.content.Intent;

import com.gmail.liorsiag.ecodrive.model.DataManager;
import com.gmail.liorsiag.ecodrive.view.DrivingActivity;
import com.gmail.liorsiag.ecodrive.view.MainActivity;

public class DrivingController {

    DrivingActivity mView;
    DataManager mDataManager;

    public DrivingController(DrivingActivity view) {
        mView = view;
        mDataManager = DataManager.instance().setContext(mView.getApplicationContext());
        mDataManager.setDrivingController(this);
        mDataManager.setDrivingGpsListening(true);
        mDataManager.startDrive();
    }

    public void onDestroy() {
        mDataManager.setDrivingGpsListening(false);
        mDataManager.onDrivingDestroy();
    }

    public void endDrive() {
        mDataManager.stopDrive();
        Intent intent = new Intent(mView, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        mView.startActivity(intent);
        mView.finish();
    }

    public void updateActualSpeed(String value) {
        mView.setActualSpeed(value);
    }

    public void updateFuelConsumption(double value) { //or double?
        mView.setFuelConsumption(String.format("%.3f", value));
    }

    public void updateDesiredSpeed(String text,int color){
        mView.setDesiredSpeed(text);
        mView.setDesiredSpeedColor(color);
    }

    public void sayText(String text){
        mView.sayText(text);
    }

    public void emptyDesiredSpeed(){
        mView.emptyDesiredSpeed();
    }
}
