package com.gmail.liorsiag.ecodrive.controller;

import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.gmail.liorsiag.ecodrive.R;
import com.gmail.liorsiag.ecodrive.model.DataManager;
import com.gmail.liorsiag.ecodrive.view.DrivingActivity;
import com.gmail.liorsiag.ecodrive.view.MainActivity;
import com.gmail.liorsiag.ecodrive.view.PrefsActivity;

public class MainController {

    private final static String TAG = "MainController";

    MainActivity mView;
    DataManager mDataManager;

    public MainController(MainActivity view) {
        mView = view;
        Log.d(TAG, "MainController: " + mView);
        mDataManager = DataManager.instance().setContext(mView.getApplicationContext());
        mDataManager.setMainController(this);

        //set this in the datamanager
    }

    public void onDestroy() {
        Log.d(TAG, "onDestroy: " + mView);
        mView = null;
        mDataManager.onMainDestroy();
    }

    //this controller needs an update view function

    //This method is called by the DataManager to update the current latlong of the phone
    public void updateGps(String latlon) {
        if (mView != null)
            mView.setGpsStatus(latlon);
    }

    //This method is used to notify the DataManager that the main activity is now listening to GPS updates
    public void getGpsUpdates() {
        mDataManager.setMainGpsListening(true);
    }

    //This method is used to notify the DataManager that the main activity is no longer listening to GPS updates
    public void stopGpsUpdates() {
        mDataManager.setMainGpsListening(false);
    }

    //This method is used to set the status of the GPS (Searching or Off)
    public void updateGpsStatus() {
        mView.setGpsStatus(mDataManager.getGpsStatus());
    }

    //Create a driving activity and end the main one
    public void startDrive(boolean useVoice,String fileName) {
        //put this inside a dialog or something
        if(mDataManager.arePrefsSet()) {
            mView.getRouteName();
            if(!mView.getRouteName().isEmpty()){
                mDataManager.saveRouteName(mView.getRouteName());
                if(!mDataManager.getGpsStatus().equals("Off")&&mDataManager.isGpsActive())
                    if(mDataManager.isObdConnected()){
                        mDataManager.prepareDrive(useVoice,fileName);
                        Intent intent = new Intent(mView, DrivingActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        mView.startActivity(intent);
                        mView.finish();
                    }
                    else
                        Toast.makeText(mView, "Connect to OBD", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(mView, "Turn GPS on", Toast.LENGTH_SHORT).show();
            }

            else
                Toast.makeText(mView, "Set route name", Toast.LENGTH_SHORT).show();
        }

        else
            Toast.makeText(mView, "Set your preferences", Toast.LENGTH_SHORT).show();
        //dialog box as well
    }

    //
    public void startPrefs() {
        Intent intent = new Intent(mView, PrefsActivity.class);
        mView.startActivity(intent);
    }

    //Calls for connection to the OBD, if successful it will update the UI.
    public void connectToObd() {
        if (mDataManager.connectToObd()) {
            setObdStatus(mView.getResources().getString(R.string.disconnect),
                    mView.getResources().getString(R.string.connected));
        }
    }

    //Calls for disconnection from the OBD
    public void disconnectFromObd() {
        mDataManager.disconnectFromObd();
        setObdStatus(mView.getResources().getString(R.string.connect),
                mView.getResources().getString(R.string.not_connected));
    }

    public boolean isObdConnected() {
        return mDataManager.isObdConnected();
    }

    //Called in the onStart of the main activity
    public void updateObdStatus() {
        if (isObdConnected()) {
            setObdStatus(mView.getResources().getString(R.string.disconnect),
                    mView.getResources().getString(R.string.connected));
        } else {
            setObdStatus(mView.getResources().getString(R.string.connect),
                    mView.getResources().getString(R.string.not_connected));
        }
    }

    //Sets the text in the UI
    private void setObdStatus(String button, String status) {
        if (mView != null) {
            mView.setObdButtonText(button);
            mView.setObdStatus(status);
        }
    }

    public void updateRouteName() {
        String v=mDataManager.getRouteName();
        if(v!=null)
            mView.setRouteName(v);
    }

    public void updateUseVoiceAndModels(){
        String[] names=mDataManager.loadModelsNames();
        if(names!=null&&names.length!=0){
            mView.setSpinnerEntries(names);
            mView.setUseVoiceStatus(true);
        }
    }
}
