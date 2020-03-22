package com.gmail.liorsiag.ecodrive.model;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.os.Handler;
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
    private final static boolean TESTMODE = false;

    //General Data
    @SuppressLint("StaticFieldLeak")
    private static DataManager instance;
    private Context mContext;
    private boolean mIsInitialized = false;

    //Controllers
    private MainController mMainC;
    private DrivingController mDrivingC;

    //Who is listening to GPS updates
    private boolean mIsMainGpsListening = false;
    private boolean mIsDrivingGpsListening = false;

    //Helpers
    private GpsHelper mGpsHelper;
    private ObdHelper mObdHelper;
    private PrefsHelper mPrefsHelper;

    //Drive related variables
    private volatile boolean mIsInDrive = false;
    private ArrayList<String[]> mGpsData;
    private ArrayList<String[]> mObdData;
    private String mFileName;
    private Runnable mScreenUpdate;
    private Handler mHandler;

    //Most recent drive data
    private double mSpeed = 0;
    private double mFuel = 0;
    private double mMaf = 0;
    private double mRpm = 0;
    private double mMap = 0;
    private double mIat = 0;

    private double[] mFuelInfo;
    private double mEngineDisp=0;

    //EcoDrive directory
    private File mDir;

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
            mPrefsHelper = new PrefsHelper(mContext);
            mGpsHelper = new GpsHelper(mContext);
            mDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "EcoDrive");
            if (TESTMODE)
                mObdHelper = new TObdHelper(c);
            else
                mObdHelper = new IObdHelper(mContext, getObdType());
        }
        mIsInitialized = true;
        return this;
    }

    public void setMainController(MainController mc) {
        Log.d(TAG, "setMainController: ");
        mMainC = mc;
        createFolder();
    }

    public void setDrivingController(DrivingController dc) {
        mDrivingC = dc;
    }

    public void onMainDestroy() {
        Log.d(TAG, "onMainDestroy: ");
        mMainC = null;
        //register/unregister things
    }

    public void onDrivingDestroy() {
        mDrivingC = null;
        //register/unregister things
    }

    public void startDrive() {
        mObdData = new ArrayList<>(); //consider putting initial capacity
        mGpsData = new ArrayList<>();
        mFileName = mPrefsHelper.getCarModel() + " " + mPrefsHelper.getRouteName() + " " + new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date()); //get route name and car model from pref manager, and get date
        createAndRegisterUpdateScreen();
        mIsInDrive = true;
        //here comes the update screen callback from handler
    }

    public void stopDrive() {
        //remove update screen callback from handler
        mIsInDrive = false;
        mHandler.removeCallbacks(mScreenUpdate);
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
        if(mIsInDrive)
            mObdData.add(obdCall);
        switch (obdCall[1].charAt(0)) {
            case 'V': //Vehicle Speed
                mSpeed = Double.parseDouble(obdCall[2]);
                break;
            case 'F': //Fuel consumption rate
                mFuel = Double.parseDouble(obdCall[2]);
                break;
            case 'M': //Mass Air Flow
                mMaf = Double.parseDouble(obdCall[2]);
                break;
            case 'E': //Engine RPM
                mRpm = Double.parseDouble(obdCall[2]);
                break;
            case 'A': //Air Intake Temperature
                mIat = Double.parseDouble(obdCall[2]);
                break;
            case 'I': //Intake Manifold Pressure
                mMap = Double.parseDouble(obdCall[2]);
                break;
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

    public void testAndSetObdType() {
        if (mObdHelper.isConnected()) {
            mObdHelper.stopRecording();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }
            String type = mObdHelper.testObdType();
            if (type != null) {
                mPrefsHelper.setObdType(type);
                Toast.makeText(mContext, "Test success: " + type, Toast.LENGTH_SHORT).show();
            }
            mObdHelper.startRecording();
        } else {
            Toast.makeText(mContext, "Connect to the OBD first", Toast.LENGTH_SHORT).show();
        }

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
                File file = new File(mDir, fileName);
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

    public void savePrefs(String carModel, String voiceFreq, String engineDisp, int fuelType) {
        if (carModel != null && !carModel.isEmpty())
            mPrefsHelper.setCarModel(carModel);
        if (voiceFreq != null && !voiceFreq.isEmpty())
            mPrefsHelper.setVoiceFreq(Integer.parseInt(voiceFreq));
        if (engineDisp != null && !engineDisp.isEmpty())
            mPrefsHelper.setEngineDisp(Integer.parseInt(engineDisp));
        mPrefsHelper.setFuelTypePosition(fuelType);
    }

    public void saveRouteName(String value) {
        if (value != null && !value.isEmpty())
            mPrefsHelper.setRouteName(value);
    }

    public void saveObdType(String value) {
        mPrefsHelper.setObdType(value);
    }

    public String getObdType() {
        return mPrefsHelper.getObdType();
    }

    public String getCarModel() {
        return mPrefsHelper.getCarModel();
    }

    public int getVoiceFreq() {
        return mPrefsHelper.getVoiceFreq();
    }

    public int getEngineDisp() {
        return mPrefsHelper.getEngineDisp();
    }

    public int getFuelTypePos() {
        return mPrefsHelper.getFuelTypePos();
    }

    public String getRouteName() {
        return mPrefsHelper.getRouteName();
    }

    public void setEliavPrefs() {
        mPrefsHelper.setObdType("MAF");
        mPrefsHelper.setCarModel("Mitsubishi");
        mPrefsHelper.setVoiceFreq(5);
        mPrefsHelper.setEngineDisp(1999);
        mPrefsHelper.setFuelTypePosition(0);
    }

    public boolean arePrefsSet() {
        return mPrefsHelper.arePrefsSet();
    }

    public boolean isGpsActive() {
        return mGpsHelper.isActive();
    }

    private void createAndRegisterUpdateScreen() {
        mHandler = new Handler();
        final int delay = getVoiceFreq()*1000;
        mEngineDisp=getEngineDisp();
        String obdType = getObdType();
        mFuelInfo=mPrefsHelper.getFuelInfo();
        if (obdType.equals("FUEL")) {
            mScreenUpdate = new Runnable() {
                @Override
                public void run() {
                    mDrivingC.updateFuelConsumption(mFuel);
                    mDrivingC.updateActualSpeed(String.valueOf(mSpeed));
                    mHandler.postDelayed(this, delay);
                }
            };
        } else if (obdType.equals("MAF")) {
            mScreenUpdate = new Runnable() {
                @Override
                public void run() {
                    calcFuel();
                    mDrivingC.updateFuelConsumption(mFuel);
                    mDrivingC.updateActualSpeed(String.valueOf(mSpeed));
                    mHandler.postDelayed(this, delay);
                }
            };
        } else {
            mScreenUpdate = new Runnable() {
                @Override
                public void run() {
                    calcMaf();
                    calcFuel();
                    mDrivingC.updateFuelConsumption(mFuel);
                    mDrivingC.updateActualSpeed(String.valueOf(mSpeed));
                    mHandler.postDelayed(this, delay);
                }
            };
        }
        mHandler.post(mScreenUpdate);
    }

    public void calcMaf() {
        double iat = mIat + 273.15;
        double imap = mRpm * mMap / iat / 2;
        mMaf= ((imap / 60) * (0.8) * mEngineDisp * (28.97 / 8.314)) / 1000;
    }

    public void calcFuel() {
        mFuel = (mMaf * 3600) / (mFuelInfo[0] * mFuelInfo[1]);
    }
}
