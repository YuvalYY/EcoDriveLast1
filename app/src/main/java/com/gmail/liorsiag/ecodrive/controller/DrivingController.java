package com.gmail.liorsiag.ecodrive.controller;

import android.content.Intent;

import com.gmail.liorsiag.ecodrive.model.DataManager;
import com.gmail.liorsiag.ecodrive.view.DrivingActivity;
import com.gmail.liorsiag.ecodrive.view.MainActivity;

public class DrivingController {

    DrivingActivity mView;
    DataManager mDataManager;

    public DrivingController(DrivingActivity view){
        mView=view;
        mDataManager= DataManager.instance().setContext(mView.getApplicationContext());
        mDataManager.setDrivingGpsListening(true);
        //set this in the datamanager
    }

    public void onDestroy(){
        mDataManager.setDrivingGpsListening(false);
        mDataManager.onDrivingDestroy();
    }

    public void endDrive(){
        Intent intent = new Intent(mView, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        mView.startActivity(intent);
        mView.finish();
    }
}
