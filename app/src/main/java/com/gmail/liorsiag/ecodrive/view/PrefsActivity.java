package com.gmail.liorsiag.ecodrive.view;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.gmail.liorsiag.ecodrive.R;
import com.gmail.liorsiag.ecodrive.controller.PrefsController;

public final class PrefsActivity extends AppCompatActivity {

    PrefsController mController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mController=new PrefsController(this);
        setContentView(R.layout.activity_prefs);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    protected void registerListeners(){
        //register buttons
        //click the area of the spinner opens the spinner
    }

    public void setObdType(String value){

    }

    public void setCarModel(String value){

    }

    public void setVoiceFreq(String value){

    }

    public void setEngineDisp(int value){

    }

    public void setFuelType(int entryPosition){

    }
}
