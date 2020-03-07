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


}
