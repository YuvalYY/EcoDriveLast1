package com.gmail.liorsiag.ecodrive.model;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

public class PrefsHelper {
    SharedPreferences mSharedPrefs;
    String mFileName="EcoDrive";

    public String fuelNameFromPosition(int pos){
        if(pos==0)
            return "Gasoline";
        else if(pos==1)
            return "Diesel";
        return null;
    }

    public PrefsHelper(Context c){
        mSharedPrefs=c.getSharedPreferences(mFileName,Context.MODE_PRIVATE);
    }

    public void setObdType(String value){
        mSharedPrefs.edit().putString("obdType",value).commit();
    }

    public void setCarModel(String value){
        mSharedPrefs.edit().putString("carModel",value).commit();
    }

    public void setVoiceFreq(int value){
        mSharedPrefs.edit().putInt("voiceFreq",value).commit();
    }

    public void setEngineDisp(int value){
        mSharedPrefs.edit().putInt("engineDisp",value).commit();
    }

    public void setFuelTypePosition(int value){
        mSharedPrefs.edit().putInt("fuelTypePos",value).commit();
    }

    public void setRouteName(String value){
        mSharedPrefs.edit().putString("routeName",value).commit();
    }

    public String getObdType(){
        return mSharedPrefs.getString("obdType",null);
    }

    public String getCarModel(){
        return mSharedPrefs.getString("carModel",null);
    }

    public int getVoiceFreq(){
        return mSharedPrefs.getInt("voiceFreq",5); //If there is no voice freq, 5 will be the voice freq
    }

    public int getEngineDisp(){
        return mSharedPrefs.getInt("engineDisp",-1);
    }

    public int getFuelTypePos(){
        return mSharedPrefs.getInt("fuelTypePos",0); //0 would be the default
    }

    public String getRouteName(){
        return mSharedPrefs.getString("routeName",null);
    }
}
