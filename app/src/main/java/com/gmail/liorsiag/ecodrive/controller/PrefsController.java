package com.gmail.liorsiag.ecodrive.controller;

import com.gmail.liorsiag.ecodrive.model.DataManager;
import com.gmail.liorsiag.ecodrive.view.DrivingActivity;
import com.gmail.liorsiag.ecodrive.view.PrefsActivity;

public class PrefsController {

    PrefsActivity mView;
    DataManager mDataManager;

    public PrefsController(PrefsActivity view){
        mView=view;
        mDataManager= DataManager.instance().setContext(mView.getApplicationContext());
        //set this in the datamanager
    }

    public void savePrefs(String carModel,String voiceFreq,String engineDisp,int fuelType){
        mDataManager.savePrefs(carModel,voiceFreq,engineDisp,fuelType);

    }

    public void loadPrefs(){
        if(mDataManager.getObdType()!=null)
            mView.setObdType(mDataManager.getObdType());
        if(mDataManager.getCarModel()!=null)
            mView.setCarModel(mDataManager.getCarModel());
        mView.setVoiceFreq(mDataManager.getVoiceFreq());
        if(mDataManager.getEngineDisp()!=-1)
            mView.setEngineDisp(mDataManager.getEngineDisp());
        mView.setSelectedFuelTypePos(mDataManager.getFuelTypePos());

    }

    public void testAndSetObd(){

    }

    public void setEliavPrefs(){
        mDataManager.setEliavPrefs();
        loadPrefs();
    }
}
