package com.gmail.liorsiag.ecodrive.view;

import android.Manifest;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.view.View.OnClickListener;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.gmail.liorsiag.ecodrive.R;
import com.gmail.liorsiag.ecodrive.controller.MainController;

public final class MainActivity extends AppCompatActivity {

    private final static String TAG="MainActivity";

    MainController mController;

    TextView mGpsStatus,mObdStatus;
    Button mStartDrive,mObdConnect,mPrefs;
    EditText mRouteName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        mController = new MainController(this);
        setContentView(R.layout.activity_main);
        initializeVariables();
        requestPermissions();
        registerListeners();
    }

    protected void initializeVariables(){
        mGpsStatus=findViewById(R.id.text_gps);
        mStartDrive=findViewById(R.id.btn_start);
        mObdConnect=findViewById(R.id.btn_obd);
        mObdStatus=findViewById(R.id.text_obd);
        mPrefs=findViewById(R.id.btn_settings);
        mRouteName=findViewById(R.id.editt_route_name);
        mController.updateRouteName();
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart: called");
        super.onStart();
        mController.updateGpsStatus();
        mController.getGpsUpdates();
        mController.updateObdStatus();

    }

    @Override
    protected void onStop() {
        super.onStop();
        mController.stopGpsUpdates();
    }

    protected void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_ADMIN},
                101);
    }

    @Override
    protected void onDestroy() {
        mController.onDestroy();
        super.onDestroy();
    }

    protected void registerListeners() {
        mStartDrive.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mController.startDrive();
            }
        });
        //start drive button - save the route name at this point
        //change route file
        //connect button
        mObdConnect.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mController.isObdConnected())
                    mController.disconnectFromObd();
                else
                    mController.connectToObd();
            }
        });
        //settings button
        mPrefs.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mController.startPrefs();
            }
        });

        findViewById(R.id.btn_delete).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                getApplicationContext().getSharedPreferences("EcoDrive",MODE_PRIVATE).edit().clear().commit();
            }
        });
    }

    public void setRouteName(String value) {
        mRouteName.setText(value);
    }

    public String getRouteName(){
        return mRouteName.getText().toString();
    }

    public void setGpsStatus(String value) {
        mGpsStatus.setText(value);
    }

    public void setObdStatus(String value) {
        mObdStatus.setText(value);
    }

    public void setObdButtonText(String value) {
        mObdConnect.setText(value);
    }


    public void setSpinnerEntries(String[] entries) {

    }

    public void setUseVoiceStatus(boolean status) {

    }
}
