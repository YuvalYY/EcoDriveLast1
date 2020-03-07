package com.gmail.liorsiag.ecodrive.model;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.gmail.liorsiag.ecodrive.controller.DrivingController;
import com.gmail.liorsiag.ecodrive.controller.MainController;
import com.gmail.liorsiag.ecodrive.controller.PrefsController;

import java.util.Arrays;

public class DataManager {
    private final static String TAG = "DataManager";
    private final static boolean TESTMODE=true;

    @SuppressLint("StaticFieldLeak")
    private static DataManager instance;
    private Context mContext;
    private boolean mIsInitialized = false;

    private MainController mMainC;
    private DrivingController mDrivingC;
//    private PrefsController mPrefsC;

    private boolean mIsMainGpsListening = false;
    private boolean mIsDrivingGpsListening = false;

    private GpsHelper mGpsHelper;
    private ObdHelper mObdHelper;

    public static DataManager instance() {
        if (instance == null)
            synchronized (DataManager.class) {
                if (instance == null)
                    instance = new DataManager();
            }
        return instance;
    }

    public DataManager setContext(Context c) {
        mContext = c.getApplicationContext();
        if (!mIsInitialized) {
            mGpsHelper=new GpsHelper(mContext);
            if(TESTMODE)
                mObdHelper= new TObdHelper(c);
            else
                mObdHelper=new IObdHelper();
        }
        mIsInitialized = true;
        return this;
    }

    public void setMainController(MainController mc) {
        Log.d(TAG, "setMainController: ");
        mMainC = mc;
    }

    public void setDrivingController(DrivingController dc) {
        mDrivingC = dc;
    }

//    public void setPrefsController(PrefsController pc) {
//        mPrefsC=pc;
//    }

    public void onMainDestroy() {
        Log.d(TAG, "onMainDestroy: ");
        mMainC = null;
        //register/unregister things
    }

    public void onDrivingDestroy() {
        mDrivingC = null;
        //register/unregister things
    }

//    public void onPrefsDestroy() {
//        mPrefsC = null;
//    }

    public void setMainGpsListening(boolean status) {
        mIsMainGpsListening = status;
        if(!mIsDrivingGpsListening)
            if(status)
                mGpsHelper.registerGpsListener();
            else
                mGpsHelper.unregisterGpsListener();
    }

    public void setDrivingGpsListening(boolean status) {
        mIsDrivingGpsListening = status;
        if(!mIsMainGpsListening)
            if(status)
                mGpsHelper.registerGpsListener();
            else
                mGpsHelper.unregisterGpsListener();
    }

    void updateGps(double lat, double lon) {
        if (mMainC!=null&&mIsMainGpsListening)
            mMainC.updateGps(lat, lon);
        if (mDrivingC!=null&&mIsDrivingGpsListening) {
            //do stuff
        }
    }

    public String getGpsStatus(){
        return mGpsHelper.getGpsStatus();
    }

    public void updateObd(String[] s){
        Log.d(TAG, "updateObd: "+ Arrays.toString(s));
    }

    public boolean connectToObd(){
        return mObdHelper.connect();
    }

    public void disconnectFromObd(){
        mObdHelper.disconnect();
    }

    public boolean isObdConnected(){
        return mObdHelper.isConnected();
    }
}
