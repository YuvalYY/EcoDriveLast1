package com.gmail.liorsiag.ecodrive.controller;

import android.content.Intent;
import android.util.Log;

import com.gmail.liorsiag.ecodrive.R;
import com.gmail.liorsiag.ecodrive.model.DataManager;
import com.gmail.liorsiag.ecodrive.view.DrivingActivity;
import com.gmail.liorsiag.ecodrive.view.MainActivity;
import com.gmail.liorsiag.ecodrive.view.PrefsActivity;

public class MainController {

    private final static String TAG="MainController";

    MainActivity mView;
    DataManager mDataManager;

    public MainController(MainActivity view) {
        mView = view;
        Log.d(TAG, "MainController: "+mView);
        mDataManager = DataManager.instance().setContext(mView.getApplicationContext());
        mDataManager.setMainController(this);

        //set this in the datamanager
    }

    public void onDestroy() {
        Log.d(TAG, "onDestroy: "+mView);
        mView=null;
        mDataManager.onMainDestroy();
    }

    //this controller needs an update view function

    public void updateGps(double lat, double lon) {
        mView.setGpsStatus(lat + "\n" + lon);
    }

    public void getGpsUpdates() {
        mDataManager.setMainGpsListening(true);
    }

    public void stopGpsUpdates() {
        mDataManager.setMainGpsListening(false);
    }

    public void updateGpsStatus() {
        mView.setGpsStatus(mDataManager.getGpsStatus());
    }

    public void startDrive() {
        //test parameters
        Intent intent = new Intent(mView, DrivingActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        mView.startActivity(intent);
        mView.finish();
    }

    public void startPrefs(){
        Intent intent = new Intent(mView, PrefsActivity.class);
        mView.startActivity(intent);
    }

    public void connectToObd() {
        if (mDataManager.connectToObd()) {
            setObdStatus(mView.getResources().getString(R.string.disconnect),
                    mView.getResources().getString(R.string.connected));
        }
    }

    public void disconnectFromObd() {
        mDataManager.disconnectFromObd();
        setObdStatus(mView.getResources().getString(R.string.connect),
                mView.getResources().getString(R.string.not_connected));
    }

    public boolean isObdConnected() {
        return mDataManager.isObdConnected();
    }

    public void updateObdStatus() {
        if (isObdConnected()) {
            setObdStatus(mView.getResources().getString(R.string.disconnect),
                    mView.getResources().getString(R.string.connected));
        } else {
            setObdStatus(mView.getResources().getString(R.string.connect),
                    mView.getResources().getString(R.string.not_connected));
        }
    }

    private void setObdStatus(String button, String status) {
        if(mView!=null){
            mView.setObdButtonText(button);
            mView.setObdStatus(status);
        }
    }

}
