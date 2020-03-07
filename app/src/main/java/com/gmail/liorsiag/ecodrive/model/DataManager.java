package com.gmail.liorsiag.ecodrive.model;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.gmail.liorsiag.ecodrive.controller.DrivingController;
import com.gmail.liorsiag.ecodrive.controller.MainController;
import com.gmail.liorsiag.ecodrive.controller.PrefsController;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

public class DataManager {
    private final static String TAG = "DataManager";
    private final static boolean TESTMODE = true;

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

    private volatile boolean mIsInDrive = false;

    private ArrayList<String[]> mGpsData;
    private ArrayList<String[]> mObdData;

    File mDir;

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
            mGpsHelper = new GpsHelper(mContext);
            mDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "EcoDrive");
            if (TESTMODE)
                mObdHelper = new TObdHelper(c);
            else
                mObdHelper = new IObdHelper();
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

    public void startDrive() {
        mIsInDrive = true;
    }

    public void stopDrive() {
        mIsInDrive = false;
        saveDrive();
        mObdData=null;
        mGpsData=null;
    }

    public void setMainGpsListening(boolean status) {
        mIsMainGpsListening = status;
        if (!mIsDrivingGpsListening)
            if (status)
                mGpsHelper.registerGpsListener();
            else
                mGpsHelper.unregisterGpsListener();
    }

    public void setDrivingGpsListening(boolean status) {
        mIsDrivingGpsListening = status;
        if (!mIsMainGpsListening)
            if (status)
                mGpsHelper.registerGpsListener();
            else
                mGpsHelper.unregisterGpsListener();
    }

    void updateGps(String[] gpsCall) {
        if (mIsInDrive) {
            mGpsData.add(gpsCall);
        } else if (mMainC != null && mIsMainGpsListening)
            mMainC.updateGps(gpsCall);
    }

    public String getGpsStatus() {
        return mGpsHelper.getGpsStatus();
    }

    public void updateObd(String[] obdCall) {
        if (mIsInDrive) {
            mObdData.add(obdCall);
        }
    }

    public boolean connectToObd() {
        return mObdHelper.connect();
    }

    public void disconnectFromObd() {
        mObdHelper.disconnect();
    }

    public boolean isObdConnected() {
        return mObdHelper.isConnected();
    }

    public void createFolder() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            if (!mDir.exists())
                mDir.mkdirs();
        }
    }

    public void saveDrive(){
        createFolder();
        saveFile(mGpsData, "GPS");
        saveFile(mObdData, "OBD");
    }

    public void saveFile(ArrayList<String[]> data, String fileName){

    }
}
