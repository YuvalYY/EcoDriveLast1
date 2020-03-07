package com.gmail.liorsiag.ecodrive.model;

import android.content.Context;
import android.util.Log;

import com.gmail.liorsiag.ecodrive.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class TObdHelper implements ObdHelper {

    private static final String TAG = "TObdHelper";
    private static final String FILENAME = "obd";

    private ArrayList<String[]> mData;
    private Thread mThread;
    private DataManager mDataManager;
    private volatile boolean mStop;


    private Context mContext;
    private boolean mIsConnected;

    TObdHelper(Context c) {
        mContext = c;
        mData = loadFile();
        mDataManager = DataManager.instance();
    }

    @Override
    public boolean isConnected() {
        return mIsConnected;
    }

    @Override
    public boolean connect() {
        mIsConnected = true;
        startRecording();
        return mIsConnected;
    }

    @Override
    public void disconnect() {
        mIsConnected = false;
        stopRecording();
    }

    @Override
    public void startRecording() {
        mStop = false;
        mThread = new Thread() {
            @Override
            public void run() {
                while (!mStop)
                    try {
                        for (String[] s : mData) {
                            mDataManager.updateObd(s);
                            Thread.sleep(Long.parseLong(s[0]));
                        }
                    } catch (InterruptedException ignored) {
                    }
            }
        };
        mThread.start();
    }

    @Override
    public void stopRecording() {
        mStop = true;
        mThread.interrupt();
        mThread = null;
    }

    private ArrayList<String[]> loadFile() {
        ArrayList<String[]> temp = new ArrayList<>();
        BufferedReader br = null;
        try {
            String sCurrentLine;
            mContext.getResources().openRawResource(R.raw.obd);
            InputStream fileIS = mContext.getResources().openRawResource(mContext.getResources().getIdentifier(FILENAME, "raw", mContext.getPackageName()));
            br = new BufferedReader(new InputStreamReader(fileIS));
            while ((sCurrentLine = br.readLine()) != null) {
                temp.add(sCurrentLine.split(","));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) br.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        for (int i = temp.size() - 1; i > 0; i--) {
            temp.get(i)[0] = String.valueOf(Long.parseLong(temp.get(i)[0]) - Long.parseLong(temp.get(i - 1)[0]));
        }
        temp.get(0)[0] = "140";
        return temp;
    }
}