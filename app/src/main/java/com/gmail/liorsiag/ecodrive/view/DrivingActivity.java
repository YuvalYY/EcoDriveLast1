package com.gmail.liorsiag.ecodrive.view;

import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.gmail.liorsiag.ecodrive.R;
import com.gmail.liorsiag.ecodrive.controller.DrivingController;

import java.util.Locale;

public final class DrivingActivity extends AppCompatActivity {
    private final static String TAG="DrivingActivity";
    DrivingController mController;
    TextView mActualSpeed,mFuelConsumption,mDesiredSpeed;
    TextToSpeech mTextToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mController = new DrivingController(this);
        setContentView(R.layout.activity_driving);
        initVars();
        mTextToSpeech= new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = mTextToSpeech.setLanguage(Locale.US);
                    if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED){
                        Toast.makeText(DrivingActivity.this, "US language is not supported, how?!", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(DrivingActivity.this, "TTS failed, reset driving", Toast.LENGTH_SHORT).show();
                }
            }
        });
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mController.endDrive();
            }
        });
        //register the gps listener
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        if (mTextToSpeech != null) {
            mTextToSpeech.stop();
            mTextToSpeech.shutdown();
        }
        mController.onDestroy();
        super.onDestroy();
    }

    private void initVars(){
        mActualSpeed=findViewById(R.id.text_actual_speed);
        mFuelConsumption=findViewById(R.id.text_fuel_consumption);
        mDesiredSpeed=findViewById(R.id.text_desired_speed);
    }

    protected void registerListeners() {
        //register stop button
    }

    public void setActualSpeed(final String value) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mActualSpeed.setText(value+"km/h");
            }
        });
    }

    public void setFuelConsumption(final String value) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mFuelConsumption.setText(value+"l/h");
            }
        });
    }

    public void setDesiredSpeed(String text) {//consider adding the color to change to
        mDesiredSpeed.setText(text);
        sayText(text);
    }

    public void sayText(String text){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mTextToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
        else {
            mTextToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    public void setDesiredSpeedColor(int color) {
        mDesiredSpeed.setTextColor(color);
    }

    public void emptyDesiredSpeed(){
        mDesiredSpeed.setText("");
    }
}
