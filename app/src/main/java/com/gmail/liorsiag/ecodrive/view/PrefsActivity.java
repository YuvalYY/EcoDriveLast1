package com.gmail.liorsiag.ecodrive.view;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View.OnClickListener;
import android.widget.Toast;

import com.gmail.liorsiag.ecodrive.R;
import com.gmail.liorsiag.ecodrive.controller.PrefsController;

public final class PrefsActivity extends AppCompatActivity {

    private final static String TAG="PrefsActivity";

    PrefsController mController;

    TextView mObdType;
    EditText mCarModel,mVoiceFreq,mEngineDisp;
    Spinner mFuelType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mController=new PrefsController(this);
        setContentView(R.layout.activity_prefs);
        initVars();
        registerListeners();
        mController.loadPrefs();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    protected void initVars(){
        mObdType=findViewById(R.id.text_obd_type);
        mCarModel=findViewById(R.id.editt_car_model);
        mVoiceFreq=findViewById(R.id.editt_voice_freq);
        mEngineDisp=findViewById(R.id.editt_engine_disp);
        mFuelType=findViewById(R.id.spinner_fuel_type);
    }

    protected void registerListeners(){
        findViewById(R.id.btn_save).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                savePrefs();
                Toast.makeText(PrefsActivity.this, "Preferences saved", Toast.LENGTH_SHORT).show();
            }
        });
        findViewById(R.id.btn_test_obd).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mController.testAndSetObdType();
            }
        });
        findViewById(R.id.btn_eliav).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mController.setEliavPrefs();
            }
        });
        findViewById(R.id.ll_fuel_type).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mFuelType.performClick();
            }
        });
    }

    public void savePrefs(){
        mController.savePrefs(mCarModel.getText().toString(),
                mVoiceFreq.getText().toString(),
                mEngineDisp.getText().toString(),
                mFuelType.getSelectedItemPosition());
    }

    public void setObdType(String value){
        mObdType.setText(value);
        if(value.equals("FUEL")){
            findViewById(R.id.ll_engine_disp).setVisibility(View.GONE);
            findViewById(R.id.ll_fuel_type).setVisibility(View.GONE);
        } else if(value.equals("MAF")){
            findViewById(R.id.ll_engine_disp).setVisibility(View.GONE);
            findViewById(R.id.ll_fuel_type).setVisibility(View.VISIBLE);
        } else { //RPM
            findViewById(R.id.ll_engine_disp).setVisibility(View.VISIBLE);
            findViewById(R.id.ll_fuel_type).setVisibility(View.VISIBLE);
        }
    }

    public void setCarModel(String value){
        mCarModel.setText(value);
    }

    public void setVoiceFreq(int value){
        mVoiceFreq.setText(value+"");
    }

    public void setEngineDisp(int value){
        mEngineDisp.setText(value+"");
    }

    public void setSelectedFuelTypePos(int entryPosition){
        mFuelType.setSelection(entryPosition);
    }
}
