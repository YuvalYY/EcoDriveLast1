package com.gmail.liorsiag.ecodrive.model;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.gmail.liorsiag.ecodrive.controller.DrivingController;
import com.gmail.liorsiag.ecodrive.controller.MainController;
import com.gmail.liorsiag.ecodrive.controller.PrefsController;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

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
    private PrefsHelper mPrefsHelper;

    private volatile boolean mIsInDrive = false;

    private ArrayList<String[]> mGpsData;
    private ArrayList<String[]> mObdData;

    private String mFileName;

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
            mPrefsHelper=new PrefsHelper(mContext);
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
        mObdData = new ArrayList<>();
        mGpsData = new ArrayList<>();
        mFileName = "ff " + new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date()); //get route name and car model from pref manager, and get date
        mIsInDrive = true;

    }

    public void stopDrive() {
        mIsInDrive = false;
        saveDrive();
        mObdData = null;
        mGpsData = null;
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
        if (mIsInDrive)
            mObdData.add(obdCall);
        Log.d(TAG, "updateObd: "+obdCall[1]);
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

    public void testAndSetObdType(){
        String type=mObdHelper.testObdType();
        mPrefsHelper.setObdType(type);
        Toast.makeText(mContext, "Test success: "+type, Toast.LENGTH_SHORT).show();
    }

    public void createFolder() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            if (!mDir.exists())
                mDir.mkdirs();
        }
    }

    public void saveDrive() {
        createFolder();
        saveFile(mGpsData, mFileName + " GPS.csv");
        saveFile(mObdData, mFileName + " OBD.csv");
        Toast.makeText(mContext, "Finished saving", Toast.LENGTH_SHORT).show();
    }

    public void saveFile(ArrayList<String[]> data, String fileName) {
        try {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                File file = new File(mDir, fileName + " " + mFileName);
                PrintWriter writer = new PrintWriter(new FileOutputStream(file));
                for (String[] row : data) {
                    for (int i = 0; i < row.length - 1; i++)
                        writer.print(row[i] + ",");
                    writer.println(row[row.length - 1]);
                }
                writer.flush();
                writer.close();
            } else {
                Toast.makeText(mContext, "Saving failed. External memory unavailable", Toast.LENGTH_SHORT).show();
            }
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Error occurred while saving file");
            e.printStackTrace();
        }
    }

    public void savePrefs(String carModel,String voiceFreq,String engineDisp,int fuelType){
        if(carModel!=null&&!carModel.isEmpty())
            mPrefsHelper.setCarModel(carModel);
        if(voiceFreq!=null&&!voiceFreq.isEmpty())
            mPrefsHelper.setVoiceFreq(Integer.parseInt(voiceFreq));
        if(engineDisp!=null&&!engineDisp.isEmpty())
            mPrefsHelper.setEngineDisp(Integer.parseInt(engineDisp));
        mPrefsHelper.setFuelTypePosition(fuelType);
    }

    public String getObdType(){
        return mPrefsHelper.getObdType();
    }

    public String getCarModel(){
        return mPrefsHelper.getCarModel();
    }

    public int getVoiceFreq(){
        return mPrefsHelper.getVoiceFreq();
    }

    public int getEngineDisp(){
        return mPrefsHelper.getEngineDisp();
    }

    public int getFuelTypePos(){
        return mPrefsHelper.getFuelTypePos();
    }

    public String getRouteName(){
        return mPrefsHelper.getRouteName();
    }

    public void setEliavPrefs(){
        mPrefsHelper.setObdType("MAF");
        mPrefsHelper.setCarModel("Some car");
        mPrefsHelper.setVoiceFreq(5);
        mPrefsHelper.setEngineDisp(1999);
        mPrefsHelper.setFuelTypePosition(0);
    }

    public boolean arePrefsSet(){
        return mPrefsHelper.arePrefsSet();
    }

    public boolean isGpsActive(){
        return mGpsHelper.isActive();
    }
}
